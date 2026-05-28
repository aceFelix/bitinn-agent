package com.itniuma.bitinn.utils;

import com.itniuma.bitinn.enums.SSEMessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * SSE连接管理服务
 *
 * 使用 ConcurrentHashMap 管理所有用户的 SSE 长连接，支持连接数上限控制，
 * 防止恶意客户端创建大量连接耗尽服务器资源。
 *
 * @author aceFelix
 */
@Slf4j
public class SSEServer {
    /** 全局最大连接数，超出后拒绝新连接 */
    private static final int MAX_TOTAL_CONNECTIONS = 500;

    /** 单用户最大连接数，同一用户重复连接时自动关闭旧连接 */
    private static final int MAX_PER_USER_CONNECTIONS = 3;

    /** 存放所有用户的SSE连接，key=userId */
    private static final Map<String, SseEmitter> SSE_CLIENTS = new ConcurrentHashMap<>();

    /**
     * 创建SSE连接
     *
     * 连接前检查：
     *   1. 全局连接数是否超限（MAX_TOTAL_CONNECTIONS）
     *   2. 同一 userId 是否已有旧连接（自动关闭旧连接，避免僵尸连接累积）
     *
     * @param userId 用户ID
     * @return SseEmitter 实例
     */
    public static SseEmitter connect(String userId){
        // 关闭同一用户的旧连接，避免僵尸连接累积
        SseEmitter oldEmitter = SSE_CLIENTS.get(userId);
        if (oldEmitter != null) {
            log.warn("SSE 用户{}已有旧连接，正在关闭...", userId);
            remove(userId, oldEmitter);
        }

        // 检查全局连接数上限
        if (SSE_CLIENTS.size() >= MAX_TOTAL_CONNECTIONS) {
            log.error("SSE 全局连接数已达上限: {}, 拒绝新连接", MAX_TOTAL_CONNECTIONS);
            // 返回一个已完成的 SseEmitter，前端收到后会自动重试
            SseEmitter rejectedEmitter = new SseEmitter(0L);
            rejectedEmitter.completeWithError(new RuntimeException("服务器连接数已满，请稍后重试"));
            return rejectedEmitter;
        }

        // 设置超时时间为 10 分钟（之前是 0=永不过期，可能导致僵尸连接）
        SseEmitter sseEmitter = new SseEmitter(10 * 60 * 1000L);

        // 注册SSE三个生命周期回调
        sseEmitter.onTimeout(timeoutCallback(userId, sseEmitter));
        sseEmitter.onCompletion(completionCallback(userId, sseEmitter));
        sseEmitter.onError(errorCallback(userId, sseEmitter));

        // 放入连接池
        SSE_CLIENTS.put(userId, sseEmitter);

        log.info("SSE连接创建成功...连接的用户id为:{}", userId);

        try {
            SseEmitter.SseEventBuilder connectedEvent = SseEmitter.event()
                    .id(userId)
                    .name(SSEMessageType.CONNECTED.type)
                    .data("connected");
            // 发送CONNECT握手确认
            sseEmitter.send(connectedEvent);
            log.info("已发送connected确认事件给用户:{}", userId);
        } catch (IOException e) {
            log.error("发送connected确认事件失败: {}", e.getMessage());
            remove(userId, sseEmitter);
        }

        return sseEmitter;
    }

    /**
     * 发送单个消息
     * @param userId
     * @param message
     * @param messageType
     */
    public static void sendMessage(String userId, String message, SSEMessageType messageType){
        sendMessageWithResult(userId, message, messageType);
    }

    /**
     * 发送消息并返回是否成功
     * @param userId 用户ID
     * @param message 消息内容
     * @param messageType 消息类型
     * @return true=发送成功, false=发送失败或用户不在线
     */
    public static boolean sendMessageWithResult(String userId, String message, SSEMessageType messageType){
        if(CollectionUtils.isEmpty(SSE_CLIENTS)) {
            log.warn("[SSE] 发送失败: 连接池为空, userId={}", userId);
            return false;
        }
        if (SSE_CLIENTS.containsKey(userId)){
            SseEmitter sseEmitter = SSE_CLIENTS.get(userId);
            return sendEmitterMessageWithResult(sseEmitter, userId, message, messageType);
        } else {
            log.warn("[SSE] 发送失败: 用户不在连接池中, userId={}, 当前连接数={}", userId, SSE_CLIENTS.size());
            return false;
        }
    }

    private static boolean sendEmitterMessageWithResult(SseEmitter sseEmitter, String userId, String message, SSEMessageType messageType){
        SseEmitter.SseEventBuilder msgEvent = SseEmitter.event()
                .id(userId)
                .name(messageType.type)
                .data(message);
        try {
            sseEmitter.send(msgEvent);
            return true;
        } catch (IOException e) {
            log.error("[SSE] 发送异常: userId={}, error={}", userId, e.getMessage());
            remove(userId, sseEmitter);
            return false;
        }
    }

    /**
     * 发送消息方法
     * @param sseEmitter
     * @param userId
     * @param message
     * @param messageType
     */
    private static void sendEmitterMessage(SseEmitter sseEmitter,String userId, String  message, SSEMessageType messageType){
        SseEmitter.SseEventBuilder msgEvent = SseEmitter.event()
                .id(userId)
                .name(messageType.type)
                .data(message);
        try {
            sseEmitter.send(msgEvent);
        } catch (IOException e) {
            log.error("SSE连接异常...{}",e.getMessage());
            remove(userId, sseEmitter);
        }
    }

    public static Runnable timeoutCallback(String userId, SseEmitter sseEmitter){
        return () -> {
            log.info("SSE连接超时...");
            remove(userId, sseEmitter);
        };
    }

    public static Runnable completionCallback(String userId, SseEmitter sseEmitter){
        return () -> {
            log.info("SSE连接完成...");
            remove(userId, sseEmitter);
        };
    }

    public static Consumer<Throwable> errorCallback(String userId, SseEmitter sseEmitter){
        return throwable -> {
            log.info("SSE连接异常...");
            remove(userId, sseEmitter);
        };
    }

    public static void remove(String userId, SseEmitter sseEmitter){
        SSE_CLIENTS.remove(userId, sseEmitter);
        log.info("SSE连接被移除...移除的用户id为:{}", userId);
    }

    /**
     * 获取所有SSE客户端
     * @return SSE客户端Map
     */
    public static Map<String, SseEmitter> getSseClients() {
        return SSE_CLIENTS;
    }

    /**
     * 获取当前连接数
     * @return 连接数
     */
    public static int getConnectionCount() {
        return SSE_CLIENTS.size();
    }
}
