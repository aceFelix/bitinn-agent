package com.itniuma.bitinn.service.ai;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.graph.NodeOutput;
import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.streaming.StreamingOutput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 模型路由服务
 * 根据模型名称将请求分发到对应的 AI 后端（DashScope/DeepSeek/ReActAgent），
 * 支持普通对话、视觉多模态和专业级 Agent 三种模式。
 *
 * @author aceFelix
 */
@Service
public class ModelRouter {

    private final ChatClient dashScopeChatClient;
    private final ChatClient deepSeekChatClient;
    private final ReactAgent reactAgent;

    private static final Map<String, String> MODEL_PROVIDER = Map.of(
            "deepseek-v4-pro", "deepseek",
            "qwen3.7-max", "dashscope",
            "qwen3.6-plus", "dashscope"
    );

    private static final Map<String, String> MODEL_ID_MAP = Map.of(
            "deepseek-v4-pro", "deepseek-v4-pro",
            "qwen3.7-max", "qwen3.7-max",
            "qwen3.6-plus", "qwen3.6-plus"
    );

    private static final Set<String> TEXT_ONLY_MODELS = Set.of("qwen3.7-max");

    public ModelRouter(ChatClient dashScopeChatClient,
                       @Qualifier("deepSeekChatModel") ChatModel deepSeekChatModel,
                       ReactAgent reactAgent) {
        this.dashScopeChatClient = dashScopeChatClient;
        this.deepSeekChatClient = ChatClient.builder(deepSeekChatModel).build();
        this.reactAgent = reactAgent;
    }

    /**
     * 普通文本对话流式请求，根据模型名分流到 DeepSeek 或 DashScope。
     */
    public Flux<ChatResponse> streamNormalChat(List<Message> messages, String model) {
        if (isDeepSeekModel(model)) {
            return deepSeekChatClient.prompt()
                    .messages(messages.toArray(new Message[0]))
                    .stream()
                    .chatResponse();
        }

        var chatRequest = dashScopeChatClient.prompt()
                .messages(messages.toArray(new Message[0]))
                .options(buildDashScopeOptions(model));

        return chatRequest.stream().chatResponse();
    }

    /**
     * 视觉多模态流式请求，支持图片+文本输入。
     */
    public Flux<ChatResponse> streamVisionChat(List<Message> historyMessages,
                                                 String prompt,
                                                 List<Media> mediaList) {
        var userSpec = dashScopeChatClient.prompt()
                .messages(historyMessages.toArray(new Message[0]))
                .user(u -> {
                    u.text(prompt != null ? prompt : "");
                    if (mediaList != null && !mediaList.isEmpty()) {
                        u.media(mediaList.toArray(new Media[0]));
                    }
                })
                .options(DashScopeChatOptions.builder()
                        .model("qwen3.6-plus")
                        .multiModel(true)
                        .build());

        return userSpec.stream().chatResponse();
    }

    /**
     * 专业 Agent 模式流式请求，通过 ReActAgent 进行多步推理。
     */
    public void streamProfessionalChat(List<Message> messages,
                                        StringBuilder fullContent,
                                        AtomicInteger chunkCount,
                                        Consumer<String> onChunk) throws Exception {
        Flux<NodeOutput> nodeFlux = reactAgent.stream(messages);

        nodeFlux.toStream().forEach(nodeOutput -> {
            if (nodeOutput instanceof StreamingOutput streamingOutput) {
                String chunk = streamingOutput.chunk();
                if (chunk != null && !chunk.isEmpty()) {
                    fullContent.append(chunk);
                    chunkCount.incrementAndGet();
                    if (onChunk != null) {
                        onChunk.accept(chunk);
                    }
                }
            }
        });
    }

    public boolean isDashScopeModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) return true;
        return "dashscope".equals(MODEL_PROVIDER.getOrDefault(modelName, "dashscope"));
    }

    public boolean isDeepSeekModel(String modelName) {
        if (modelName == null || modelName.isEmpty()) return false;
        return "deepseek".equals(MODEL_PROVIDER.getOrDefault(modelName, "dashscope"));
    }

    private DashScopeChatOptions buildDashScopeOptions(String modelName) {
        String modelId = MODEL_ID_MAP.getOrDefault(modelName, "qwen3.6-plus");
        var builder = DashScopeChatOptions.builder()
                .model(modelId)
                .temperature(0.8);
        if (TEXT_ONLY_MODELS.contains(modelName)) {
            builder.multiModel(false);
        }
        return builder.build();
    }
}
