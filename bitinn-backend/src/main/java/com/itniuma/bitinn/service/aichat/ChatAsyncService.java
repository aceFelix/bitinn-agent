package com.itniuma.bitinn.service.aichat;

import com.itniuma.bitinn.config.SkillPromptConfig;
import com.itniuma.bitinn.service.ai.ImageGenService;
import com.itniuma.bitinn.service.ai.ModelRouter;
import com.itniuma.bitinn.enums.SSEMessageType;
import com.itniuma.bitinn.pojo.mongo.Conversation;
import com.itniuma.bitinn.pojo.mongo.Message;
import com.itniuma.bitinn.repository.ConversationRepository;
import com.itniuma.bitinn.repository.MessageRepository;
import com.itniuma.bitinn.service.ai.SummaryService;
import com.itniuma.bitinn.service.ai.TitleService;
import com.itniuma.bitinn.utils.SSEServer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 聊天异步处理服务
 *
 * 核心职责：接收用户消息后，在异步线程中调用 AI 模型（DashScope / DeepSeek 等），
 * 将 AI 返回的流式结果逐块通过 SSE（Server-Sent Events）实时推送给前端。
 *
 * 支持的四种对话模式：
 *   1. normal（普通模式）：纯文本对话，支持切换模型
 *   2. professional（专业模式）：使用 ReAct Agent 进行推理，适合复杂任务
 *   3. vision（识图模式）：让 AI 识别图片内容并回答
 *   4. image-gen（生图模式）：根据文字描述生成图片
 *
 * 为什么用 @Async 异步？
 *   AI 模型的响应时间可能很长（几秒到几十秒），如果同步阻塞会占满 Tomcat 线程池，
 *   导致其他用户请求无法处理。@Async 将 AI 调用放到独立线程池中执行。
 *
 * 流式推送的完整链路：
 *   前端发消息 → ChatController → ChatServiceImpl（保存用户消息到 MongoDB）
 *     → ChatAsyncService.pushMessage()（异步）
 *       → ModelRouter（调用 AI API，获取 Flux 流）
 *         → doOnNext：每收到一个 token 就立即通过 SSE 推送给前端（SSEMessageType.ADD）
 *         → doOnComplete：所有 token 收完后发 FINISH 事件，保存完整回复到 MongoDB
 *         → doOnError：出错时发 ERROR 事件告知前端
 *
 * @author aceFelix
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAsyncService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final ModelRouter modelRouter;
    private final SummaryService summaryService;
    private final TitleService titleService;
    private final ImageGenService imageGenService;

    /** AI 上下文字数上限，超过则压缩或丢弃老消息（默认 60000 字符） */
    @Value("${bitinn.ai.max-context-chars:60000}")
    private int maxContextChars;

    /** 最近 N 条消息保持完整不压缩（默认 5 条，保证最近对话体验） */
    @Value("${bitinn.ai.recent-full-count:5}")
    private int recentFullCount;

    /** 单条消息超过这个字符数会被压缩（默认 3000 字符，适用于非最近的旧消息） */
    @Value("${bitinn.ai.compress-threshold-chars:3000}")
    private int compressThresholdChars;

    /** 用户当前发送的消息最大字符数，超过则压缩（默认 8000 字符） */
    @Value("${bitinn.ai.max-current-message-chars:8000}")
    private int maxCurrentMessageChars;

    /** 累计消息达到这个数量时触发自动摘要（默认 30 条） */
    @Value("${bitinn.ai.summary-trigger-count:30}")
    private int summaryTriggerCount;

    /**
     * AI 聊天的异步入口
     *
     * 调用链路：
     *   ChatServiceImpl.chat() 保存用户消息后调用此方法。
     *   此方法 @Async 标注，在独立线程池中执行，不阻塞 HTTP 请求线程。
     *
     * 流程：
     *   1. 从 MongoDB 查会话信息，获取模式（normal/vision/professional/image-gen）和模型
     *   2. 给新会话设置初始占位标题
     *   3. 根据模式分发到不同的处理方法
     *
     * @param userId         用户 ID（字符串，对应 SSE 连接的 key）
     * @param prompt         用户输入的文本内容
     * @param conversationId 会话 ID，新对话时为 null
     * @param attachments    附件列表（图片、文件等），可能为 null
     * @param skill          AI 技能类型（article / code / data / translate / summary / resume），非技能请求为 null
     */
    @Async("aiChatExecutor")
    public void pushMessage(String userId, String prompt, String conversationId, List<Message.Attachment> attachments, String skill) {
        log.info("[Async] 开始处理AI对话, userId: {}, conversationId: {}, 附件数: {}, skill: {}", userId, conversationId, attachments != null ? attachments.size() : 0, skill);

        // 从 MongoDB 加载会话信息，读取模式（普通/识图/专业/生图）和指定模型
        String mode = "normal";
        String model = null;
        if (conversationId != null) {
            Conversation conv = conversationRepository.findById(conversationId).orElse(null);
            if (conv != null) {
                if (conv.getMode() != null) {
                    mode = conv.getMode();
                }
                model = conv.getModel();
            }
        }

        // 给新会话（conversationId 为 null）设置占位标题 "新对话"
        titleService.setInitialTitle(conversationId, userId, prompt);

        // 根据模式分发：生图模式优先，避免被识图逻辑拦截
        if ("image-gen".equals(mode)) {
            pushImageGenMessage(userId, prompt, conversationId, attachments);
        } else if ("vision".equals(mode) || hasImageAttachments(attachments)) {
            pushVisionMessage(userId, prompt, conversationId, attachments);
        } else if ("professional".equals(mode)) {
            pushProfessionalMessage(userId, prompt, conversationId, skill);
        } else {
            pushNormalMessage(userId, prompt, conversationId, model, skill);
        }
    }

    /**
     * 【普通模式】纯文本对话
     *
     * 流程：
     *   1. buildMessagesWithBudget() 构建带上下文预算的消息列表（含历史消息 + 压缩逻辑）
     *   2. ModelRouter.streamNormalChat() 返回 Flux<ChatResponse> 流
     *   3. streamResponse() 统一处理流式推送 + 完成回调
     *
     * 支持模型切换：DeepSeek-V4-Pro / Qwen3.7-Max / Qwen3.6-Plus
     */
    private void pushNormalMessage(String userId, String prompt, String conversationId, String model, String skill) {
        try {
            List<org.springframework.ai.chat.messages.Message> messages = buildMessagesWithBudget(conversationId, prompt, null, skill);
            Flux<ChatResponse> responseFlux = modelRouter.streamNormalChat(messages, model);
            streamResponse(responseFlux, userId, conversationId, "普通模式", prompt);
        } catch (Exception e) {
            log.error("[Async] 普通模式对话失败, userId: {}, error: {}", userId, e.getMessage(), e);
            handleAsyncError(userId, conversationId, e);
        }
    }

    /**
     * 【专业模式】使用 ReAct Agent 进行推理
     *
     * 特点：
     *   使用的是 Spring AI Alibaba Graph 框架的 ReAct Agent，而非普通 ChatClient。
     *   ReAct Agent 会进行"思考 → 行动 → 观察"循环，适合需要多步推理的复杂任务。
     *
     * 与普通模式的关键区别：
     *   - 不走 Flux<ChatResponse> + streamResponse() 通用方法
     *   - 直接调用 ModelRouter.streamProfessionalChat()，传入回调函数逐块处理
     *   - Agent 内部可能调用工具、搜索等，所以耗时比普通模式更长
     *
     * 监控指标：TTFB（首字节时间）、chunk 间隔、总耗时
     */
    private void pushProfessionalMessage(String userId, String prompt, String conversationId, String skill) {
        try {
            List<org.springframework.ai.chat.messages.Message> messages = buildMessagesWithBudget(conversationId, prompt, null, skill);

            // fullContent 收集完整回复用于最终保存
            StringBuilder fullContent = new StringBuilder();
            // chunkCount 用于监控日志
            AtomicInteger chunkCount = new AtomicInteger(0);
            // 性能监控：记录流开始时间
            long streamStart = System.currentTimeMillis();
            long[] firstChunkTime = {0};
            long[] lastChunkTime = {0};
            log.info("[Perf|专业模式] 开始流式调用, userId: {}", userId);

            // 调用 ReAct Agent 的流式接口，每个 chunk 到达时回调
            modelRouter.streamProfessionalChat(messages, fullContent, chunkCount, chunk -> {
                long now = System.currentTimeMillis();
                // 记录首个 chunk 到达时间（TTFB，首字节延迟）
                if (firstChunkTime[0] == 0) {
                    firstChunkTime[0] = now;
                    long ttfb = now - streamStart;
                    log.info("[Perf|专业模式] 首个chunk到达, userId: {}, TTFB={}ms", userId, ttfb);
                } else {
                    // 检测 chunk 之间间隔是否异常的
                    long sinceLast = now - lastChunkTime[0];
                    if (sinceLast > 2000) {
                        log.warn("[Perf|专业模式] chunk间隔过长, userId: {}, 间隔={}ms, chunk#={}", userId, sinceLast, chunkCount.get());
                    }
                }
                lastChunkTime[0] = now;
                int count = chunkCount.incrementAndGet();
                // 每 50 个 chunk 打印一次进度
                if (count % 50 == 0) {
                    log.info("[Perf|专业模式] chunk进度, userId: {}, chunk#={}, 累计字符={}", userId, count, fullContent.length());
                }
                // 实时推送每个 chunk 到前端
                SSEServer.sendMessageWithResult(userId, chunk, SSEMessageType.ADD);
            });

            // Agent 所有输出完毕，计算总耗时
            long totalMs = System.currentTimeMillis() - streamStart;
            String finalContent = fullContent.toString();
            log.info("[Perf|专业模式] 对话完成, userId: {}, 流式chunk数={}, 响应长度={}, 总耗时={}ms, TTFB={}ms", userId, chunkCount.get(), finalContent.length(), totalMs, firstChunkTime[0] > 0 ? firstChunkTime[0] - streamStart : -1);

            if (finalContent.isEmpty()) {
                log.warn("[Async] 专业模式AI响应内容为空!");
            }

            // 保存完整回复到 MongoDB，并发送 FINISH 事件给前端
            saveAssistantMessage(conversationId, userId, finalContent);
            SSEServer.sendMessageWithResult(userId, finalContent, SSEMessageType.FINISH);

            // 检查是否需要生成对话摘要
            triggerSummaryIfNeeded(conversationId);
            titleService.refreshTitle(conversationId, userId);

        } catch (Exception e) {
            log.error("[Async] 专业模式对话失败, userId: {}, error: {}", userId, e.getMessage(), e);
            handleAsyncError(userId, conversationId, e);
        }
    }

    /**
     * 【识图模式】让 AI 识别图片并回答
     *
     * 流程：
     *   1. 把附件中的图片转成 base64 data URI（通过 buildMediaList()）
     *   2. 拼装历史消息（纯文本角色，不含历史图片）
     *   3. 调用 ModelRouter.streamVisionChat()，传入当前图片 + 带图片的 UserMessage
     *   4. 走通用的 streamResponse() 流式推送
     *
     * 注意：
     *   - 历史消息中的图片会被丢弃，只有当前发送的图片会传给 AI（节省 token）
     *   - 识图模式固定使用 Qwen3.6-Plus（支持视觉）
     */
    private void pushVisionMessage(String userId, String prompt, String conversationId, List<Message.Attachment> attachments) {
        try {
            // 把前端传来的图片附件转成 Spring AI 的 Media 对象
            List<Media> currentMediaList = buildMediaList(attachments);

            // 构建消息列表：系统提示 + 历史消息（纯文本）+ 当前消息（含图片）
            List<org.springframework.ai.chat.messages.Message> historyMsgs = new ArrayList<>();
            historyMsgs.add(new SystemMessage("你是 BitInn 开发者社区的 AI 助手，名叫小B。用户可能会发送图片，请你仔细观察图片内容，并用专业、热心的态度回答用户的问题。"));

            // 加载历史消息作为上下文（只保留文本角色，不带图片）
            if (conversationId != null) {
                List<Message> recentMessages = loadRecentMessages(conversationId);
                for (Message msg : recentMessages) {
                    if (msg.getContent() != null && !msg.getContent().isEmpty()) {
                        if ("user".equals(msg.getRole())) {
                            historyMsgs.add(new UserMessage(msg.getContent()));
                        } else {
                            historyMsgs.add(new AssistantMessage(msg.getContent()));
                        }
                    }
                }
            }

            log.info("[Async] 识图模式: 历史消息数={}, 当前图片数={}",
                    historyMsgs.size() - 1, currentMediaList.size());

            Flux<ChatResponse> responseFlux = modelRouter.streamVisionChat(historyMsgs, prompt, currentMediaList);
            streamResponse(responseFlux, userId, conversationId, "识图模式", prompt);

        } catch (Exception e) {
            log.error("[Async] 识图模式对话失败, userId: {}, error: {}", userId, e.getMessage(), e);
            handleAsyncError(userId, conversationId, e);
        }
    }

    /**
     * 【生图模式】根据用户描述生成图片
     *
     * 流程：
     *   1. 调用 ImageGenService.generateImage() 生成图片，返回图片 URL
     *   2. 前端通过 Markdown 图片语法 `![生成图片](url)` 渲染
     *   3. 直接发送 ADD + FINISH（非流式，因为生图是同步等待结果）
     */
    private void pushImageGenMessage(String userId, String prompt, String conversationId, List<Message.Attachment> attachments) {
        try {
            List<String> imageBase64List = null;
            if (attachments != null && !attachments.isEmpty()) {
                imageBase64List = new ArrayList<>();
                for (Message.Attachment att : attachments) {
                    if ("image".equals(att.getType()) && att.getData() != null) {
                        imageBase64List.add(att.getData());
                    }
                }
            }

            String imageUrl = imageGenService.generateImage(prompt, imageBase64List);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                // 用 Markdown 图片语法包装 URL，前端渲染时自动显示图片
                String content = "![生成图片](" + imageUrl + ")";
                SSEServer.sendMessageWithResult(userId, content, SSEMessageType.ADD);
                saveAssistantMessage(conversationId, userId, content);
                log.info("[Async] 生图完成: userId={}, url={}", userId, imageUrl);
            } else {
                String errMsg = "图片生成失败，请检查提示词后重试";
                SSEServer.sendMessageWithResult(userId, errMsg, SSEMessageType.ADD);
                saveAssistantMessage(conversationId, userId, errMsg);
            }
            SSEServer.sendMessageWithResult(userId, "", SSEMessageType.FINISH);
        } catch (Exception e) {
            log.error("[Async] 生图失败: userId={}, error={}", userId, e.getMessage(), e);
            handleAsyncError(userId, conversationId, e);
        }
    }

    /**
     * 将前端传来的 Attachment 列表转换成 Spring AI 的 Media 对象
     *
     * 只处理 MIME 类型为 "image/*" 且有 base64 数据的附件：
     *   1. 去掉 data URI 协议头（`data:image/png;base64,xxx` → `xxx`）
     *   2. 重新拼回完整的 data URI 格式
     *   3. 创建 Media 对象传给 Spring AI
     */
    private List<Media> buildMediaList(List<Message.Attachment> attachments) {
        List<Media> mediaList = new ArrayList<>();
        if (attachments != null) {
            for (Message.Attachment att : attachments) {
                if ("image".equals(att.getType()) && att.getData() != null) {
                    try {
                        String mimeTypeStr = att.getMimeType() != null ? att.getMimeType() : "image/png";
                        var mimeType = MimeTypeUtils.parseMimeType(mimeTypeStr);
                        // 去掉 data URI 协议头，只保留纯 base64
                        String base64Data = att.getData();
                        if (base64Data.contains(",")) {
                            base64Data = base64Data.substring(base64Data.indexOf(",") + 1);
                        }
                        // 重新拼回 data URI，Spring AI 需要这个格式
                        String dataUri = "data:" + mimeTypeStr + ";base64," + base64Data;
                        mediaList.add(new Media(mimeType, URI.create(dataUri)));
                    } catch (Exception e) {
                        log.warn("[Async] 图片附件处理失败: name={}, error={}", att.getName(), e.getMessage());
                    }
                }
            }
        }
        return mediaList;
    }

    /**
     * 【核心方法】统一处理 Reactor Flux 流式响应，逐块推送 SSE 给前端
     *
     * Reactor Flux 是 Spring 响应式编程的核心概念，这里 Flux<ChatResponse> 代表 AI 模型
     * 返回的一个"数据流"，每个 ChatResponse 就是一个 token（文本片段）。
     *
     * 三个关键回调（类似 Promise 的 then/catch/finally）：
     *   .doOnNext()   → 每来一个 token 就触发一次，立即 SSE 推送到前端（ADD 事件）
     *   .doOnComplete() → 流正常结束时触发，保存完整回复到 MongoDB，发 FINISH 事件
     *   .doOnError()  → 流出错时触发，发 ERROR 事件
     *   .blockLast()  → 阻塞当前线程直到流结束（因为是 @Async 线程，阻塞不影响主线程）
     *
     * 监控指标：
     *   TTFB（Time To First Byte）：从请求发送到收到第一个 token 的耗时，反映 AI 模型首次响应速度
     *   chunk 间隔：两个 token 之间的等待时间，> 2s 会告警
     *   总耗时：整个对话完成的时间
     *
     * @param responseFlux   AI 返回的流式响应
     * @param userId         用户 ID（SSE 连接标识）
     * @param conversationId 会话 ID
     * @param modeName       模式名称（用于日志前缀，如 "普通模式"、"识图模式"）
     * @param prompt         用户的原始问题
     */
    private void streamResponse(Flux<ChatResponse> responseFlux, String userId, String conversationId, String modeName, String prompt) {
        try {
            StringBuilder fullContent = new StringBuilder();
            AtomicInteger chunkCount = new AtomicInteger(0);
            // 性能监控时间戳
            long streamStart = System.currentTimeMillis();
            long[] firstChunkTime = {0};   // 首个 token 到达时间
            long[] lastChunkTime = {0};    // 上一个 token 到达时间
            log.info("[Perf|{}] 开始流式调用, userId: {}", modeName, userId);

            responseFlux
                    // ========== doOnNext：每当 AI 吐出一个 token 就触发 ==========
                    .doOnNext(chatResponse -> {
                        if (chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                            String chunk = chatResponse.getResult().getOutput().getText();
                            if (chunk != null && !chunk.isEmpty()) {
                                long now = System.currentTimeMillis();
                                // 记录 TTFB（首字节到达时间）
                                if (firstChunkTime[0] == 0) {
                                    firstChunkTime[0] = now;
                                    long ttfb = now - streamStart;
                                    log.info("[Perf|{}] 首个chunk到达, userId: {}, TTFB={}ms", modeName, userId, ttfb);
                                } else {
                                    // 检测 chunk 间隔是否异常（> 2 秒说明 AI 卡了）
                                    long sinceLast = now - lastChunkTime[0];
                                    if (sinceLast > 2000) {
                                        log.warn("[Perf|{}] chunk间隔过长, userId: {}, 间隔={}ms, chunk#={}", modeName, userId, sinceLast, chunkCount.get());
                                    }
                                }
                                lastChunkTime[0] = now;
                                fullContent.append(chunk);
                                int count = chunkCount.incrementAndGet();
                                // 每 50 个 chunk 汇报一次进度，方便追踪大段回复
                                if (count % 50 == 0) {
                                    log.info("[Perf|{}] chunk进度, userId: {}, chunk#={}, 累计字符={}", modeName, userId, count, fullContent.length());
                                }
                                // 立即通过 SSE 推送给前端
                                SSEServer.sendMessageWithResult(userId, chunk, SSEMessageType.ADD);
                            }
                        }
                    })
                    // ========== doOnComplete：AI 流正常结束时触发 ==========
                    .doOnComplete(() -> {
                        long totalMs = System.currentTimeMillis() - streamStart;
                        String finalContent = fullContent.toString();
                        log.info("[Perf|{}] 对话完成, userId: {}, 流式chunk数={}, 响应长度={}, 总耗时={}ms, TTFB={}ms", modeName, userId, chunkCount.get(), finalContent.length(), totalMs, firstChunkTime[0] > 0 ? firstChunkTime[0] - streamStart : -1);

                        if (finalContent.isEmpty()) {
                            log.warn("[Async] {}AI响应内容为空!", modeName);
                        }

                        // 保存 AI 完整回复到 MongoDB（前端也缓存了，但数据库是持久化的）
                        saveAssistantMessage(conversationId, userId, finalContent);
                        // 发送 FINISH 事件告知前端 AI 回复完毕
                        SSEServer.sendMessageWithResult(userId, finalContent, SSEMessageType.FINISH);

                        // 检查是否需要自动生成对话摘要（> 30 条消息时触发）
                        triggerSummaryIfNeeded(conversationId);
                        // 刷新会话标题（AI 根据对话内容自动生成）
                        titleService.refreshTitle(conversationId, userId);
                    })
                    // ========== doOnError：流出错时触发 ==========
                    .doOnError(e -> {
                        long totalMs = System.currentTimeMillis() - streamStart;
                        log.error("[Perf|{}] 对话失败, userId: {}, 总耗时={}ms, error: {}", modeName, userId, totalMs, e.getMessage(), e);
                        handleAsyncError(userId, conversationId, e);
                    })
                    // 阻塞等待流结束（@Async 线程中阻塞是安全的，不影响主线程）
                    .blockLast();

        } catch (Exception e) {
            log.error("[Async] {}对话失败, userId: {}, error: {}", modeName, userId, e.getMessage(), e);
            handleAsyncError(userId, conversationId, e);
        }
    }

    /**
     * 保存 AI 的完整回复到 MongoDB
     *
     * 为什么在 FINISH 时才保存而不是逐 chunk 保存？
     *   1. 性能：逐 chunk 保存会产生大量数据库写入
     *   2. 一致性：只有完整回复才值得持久化存储
     *   3. 前端通过 SSE 实时接收 chunk，显示不受影响
     */
    private void saveAssistantMessage(String conversationId, String userId, String content) {
        Message assistantMsg = Message.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role("assistant")
                .content(content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        messageRepository.save(assistantMsg);
    }

    /**
     * 统一错误处理
     *
     *   1. 通过 SSE 向前端推送错误消息（前端显示 "AI服务暂时不可用"）
     *   2. 将错误消息作为 AI 回复保存到 MongoDB（避免会话丢失上下文）
     */
    private void handleAsyncError(String userId, String conversationId, Throwable e) {
        String errorMessage = "AI服务暂时不可用，请稍后重试";
        SSEServer.sendMessage(userId, errorMessage, SSEMessageType.ERROR);

        Message errorMsg = Message.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role("assistant")
                .content(errorMessage)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        messageRepository.save(errorMsg);
    }

    /**
     * 【重要】构建发送给 AI 的消息列表，包含上下文预算控制
     *
     * AI 模型有上下文长度限制（Token 上限），无法无限塞历史消息。
     * 本方法实现了智能的上下文管理策略：
     *
     * 策略层次：
     *   1. 系统提示词（System Prompt）：定义 AI 的角色和行为
     *   2. 历史摘要：如果之前已经生成了摘要（>30 条消息触发），用摘要代替部分历史
     *   3. 历史消息（带预算控制）：
     *      - 从最新到最旧倒序遍历
     *      - 最近 recentFullCount 条消息保持完整不压缩（保证最近对话质量）
     *      - 较早的消息如果超过 compressThresholdChars 则智能压缩（保留头和尾）
     *      - 总字符数不能超过 maxContextChars，超出则丢弃最旧的消息
     *   4. 当前用户消息：放在最后，最长不超过 maxCurrentMessageChars
     *
     * 为什么要倒序遍历？
     *   因为要从最新往旧消息逐个加入，直到预算用完。倒序便于在预算不够时直接 break。
     *
     * @param conversationId 会话 ID，为 null 表示新对话（无历史）
     * @param currentPrompt  用户当前输入的文本
     * @param attachments    附件列表
     * @return 构建好的消息列表，可直接传给 AI ChatClient
     */
    private List<org.springframework.ai.chat.messages.Message> buildMessagesWithBudget(String conversationId, String currentPrompt, List<Message.Attachment> attachments, String skill) {
        List<org.springframework.ai.chat.messages.Message> messages = new ArrayList<>();

        // 第1层：系统提示词，定义 AI 角色（有技能参数则使用技能专属 Prompt）
        messages.add(new SystemMessage(SkillPromptConfig.getPrompt(skill)));

        // 如果有会话ID，加载历史上下文
        if (conversationId != null) {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);

            // 第2层：如果之前生成了摘要，把摘要作为额外的系统提示注入
            // 摘要相当于"压缩版"历史对话，大幅节省 token
            if (conversation != null && conversation.getSummary() != null && !conversation.getSummary().isEmpty()) {
                String summaryText = "【历史对话摘要】\n" + conversation.getSummary();
                messages.add(new SystemMessage(summaryText));
                log.info("[Async] 加载历史摘要: {}字符", summaryText.length());
            }

            // 第3层：加载历史消息，用预算控制数量
            List<Message> historyMessages = loadRecentMessages(conversationId);
            int totalHistory = historyMessages.size();
            int budget = maxContextChars;   // 剩余可用字符预算
            List<org.springframework.ai.chat.messages.Message> historyPart = new ArrayList<>();

            // 从最新到最旧倒序遍历（i 从末尾往前）
            for (int i = totalHistory - 1; i >= 0; i--) {
                Message msg = historyMessages.get(i);
                int distanceFromEnd = totalHistory - 1 - i; // 距离最新消息的位置
                boolean isRecent = distanceFromEnd < recentFullCount; // 是否在"不压缩"范围内

                // 根据消息是否在近期范围内决定是否压缩
                String content = processMessageContent(msg.getContent(), isRecent, msg.getAttachments());
                if (content == null || content.isEmpty()) continue;

                int msgCost = content.length();
                // 预算不够时：近期消息尝试压缩，旧消息直接丢弃
                if (msgCost > budget) {
                    if (isRecent) {
                        content = smartCompress(content, budget);
                        msgCost = content.length();
                        // 压缩后仍然超预算，极限裁剪
                        if (msgCost > budget && budget > 200) {
                            content = content.substring(0, budget - 100)
                                    + "\n\n[预算极限，内容被压缩]";
                            msgCost = content.length();
                        }
                    } else {
                        log.info("[Async] 上下文预算不足, 跳过较老消息. 剩余预算={}, 需要={}, 已加载{}条",
                                budget, msgCost, historyPart.size());
                        break;
                    }
                }

                // 将消息转成 Spring AI 的 Message 对象（区分 user 和 assistant 角色）
                org.springframework.ai.chat.messages.Message aiMsg;
                if ("user".equals(msg.getRole()) && hasImageAttachments(msg.getAttachments())) {
                    aiMsg = buildUserMessageWithImages(content, msg.getAttachments());
                } else {
                    aiMsg = "user".equals(msg.getRole())
                            ? new UserMessage(content)
                            : new AssistantMessage(content);
                }

                historyPart.add(aiMsg);
                budget -= msgCost; // 扣除已用预算
            }

            // 逆序回来（因为我们倒序遍历插入的，需要调整回正常的时间顺序）
            Collections.reverse(historyPart);
            messages.addAll(historyPart);
            log.info("[Async] 加载历史消息: {}条, 消耗预算: {}字符", historyPart.size(), maxContextChars - budget);
        }

        // 第4层：处理当前用户消息，超过上限则压缩
        String currentContent = processMessageContent(currentPrompt, true, attachments);
        if (currentContent.length() > maxCurrentMessageChars) {
            currentContent = smartCompress(currentContent, maxCurrentMessageChars);
        }

        // 构建当前消息（支持带图片的 UserMessage）
        org.springframework.ai.chat.messages.Message currentUserMsg;
        if (hasImageAttachments(attachments)) {
            currentUserMsg = buildUserMessageWithImages(currentContent, attachments);
        } else {
            currentUserMsg = new UserMessage(currentContent);
        }
        messages.add(currentUserMsg);

        log.info("[Async] 构建消息完成: 总消息数={}", messages.size());

        return messages;
    }

    /**
     * 判断附件列表中是否包含有效的图片附件
     *
     * 条件是：type 为 "image" 且 data（base64 数据）不为空
     */
    private boolean hasImageAttachments(List<Message.Attachment> attachments) {
        if (attachments == null || attachments.isEmpty()) return false;
        return attachments.stream().anyMatch(a -> "image".equals(a.getType()) && a.getData() != null);
    }

    /**
     * 构建带图片的用户消息
     *
     * 把文本内容和图片附件合并成一条 UserMessage，
     * Spring AI 会自动处理多模态内容（文本 + 图片）发送给视觉模型
     */
    private org.springframework.ai.chat.messages.Message buildUserMessageWithImages(String textContent, List<Message.Attachment> attachments) {
        List<Media> mediaList = buildMediaList(attachments);
        if (mediaList.isEmpty()) {
            return new UserMessage(textContent);
        }
        return UserMessage.builder()
                .text(textContent)
                .media(mediaList)
                .build();
    }

    /**
     * 处理消息内容：根据是否为近期消息决定是否压缩
     *
     * 压缩规则：
     *   - 近期消息（isRecent=true）：保持原文不动
     *   - 旧消息中有图片：内容替换为提示文字（图片无法压缩，直接丢弃内容）
     *   - 旧消息超出阈值：调用 smartCompress() 保留首尾
     *   - 旧消息未超阈值：保持原文
     *
     * @param content     原始消息内容
     * @param isRecent    是否为近期消息（最近 N 条不压缩）
     * @param attachments 消息附件
     * @return 处理后的内容
     */
    private String processMessageContent(String content, boolean isRecent, List<Message.Attachment> attachments) {
        if (content == null || content.isEmpty()) return content;

        // 近期消息保持原文不压缩
        if (isRecent) {
            return content;
        }

        // 历史消息中的图片无法有效压缩，直接替换为提示文字
        if (hasImageAttachments(attachments)) {
            return "[用户发送了图片，图片内容已省略]";
        }

        // 未超过阈值的不压缩
        if (content.length() <= compressThresholdChars) {
            return content;
        }

        return smartCompress(content, compressThresholdChars);
    }

    /**
     * 智能压缩：保留头部 60% + 尾部 30%，中间省略
     *
     * 为什么要保留尾部？
     *   对话的结尾往往包含关键信息（结论、问题、后续指令等），
     *   只保留头部会丢失重要上下文。
     *
     * 分配比例：头部 60% + 尾部 30% + 省略标记 ≈ 100%
     *
     * @param content     原始内容
     * @param targetChars 目标字符数
     * @return 压缩后的内容
     */
    private String smartCompress(String content, int targetChars) {
        if (content.length() <= targetChars) return content;

        int headLen = (int) (targetChars * 0.6);
        int tailLen = (int) (targetChars * 0.3);
        int omittedLen = content.length() - headLen - tailLen;

        String head = content.substring(0, headLen);
        String tail = content.substring(content.length() - tailLen);

        return head + "\n\n[...省略了" + omittedLen + "字符...]\n\n" + tail;
    }

    /**
     * 从 MongoDB 加载最近的对话历史消息
     *
     * 加载数量：min(实际消息总数, summaryTriggerCount * 2)
     * 即默认最多加载 60 条（30 * 2），防止一次加载过多数据
     *
     * @param conversationId 会话 ID
     * @return 按创建时间升序排列的消息列表
     */
    private List<Message> loadRecentMessages(String conversationId) {
        long totalMessages = messageRepository.countByConversationId(conversationId);
        int loadCount = (int) Math.min(totalMessages, summaryTriggerCount * 2);

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(
                conversationId, PageRequest.of(0, loadCount));
    }

    /**
     * 检查是否需要为会话生成摘要
     *
     * 触发条件：
     *   1. 消息总数 >= summaryTriggerCount（默认 30 条）
     *   2. 会话还没有生成过摘要（summary 字段为 null）
     *
     * 满足条件则异步调用 SummaryService 生成摘要，
     * 摘要会保存在 Conversation 的 summary 字段中，
     * 下次构建上下文时作为系统提示注入（见 buildMessagesWithBudget() 的第2层）
     */
    private void triggerSummaryIfNeeded(String conversationId) {
        long messageCount = messageRepository.countByConversationId(conversationId);
        if (messageCount >= summaryTriggerCount) {
            Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
            if (conversation != null && conversation.getSummary() == null) {
                log.info("[Async] 消息数({})达到摘要触发阈值({}), 异步生成摘要",
                        messageCount, summaryTriggerCount);
                summaryService.generateSummaryAsync(conversationId);
            }
        }
    }
}
