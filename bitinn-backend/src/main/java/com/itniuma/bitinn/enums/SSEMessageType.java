package com.itniuma.bitinn.enums;

/**
 * SSE（Server-Sent Events）消息类型枚举
 *
 * 定义服务端向前端推送事件时使用的消息类型。
 * 前端 EventSource 通过 event 字段区分不同消息并执行对应逻辑。
 *
 * 流式 AI 回复的典型消息序列：
 *   CONNECTED → ADD（重复多次） → FINISH
 *
 * @author aceFelix
 */
public enum SSEMessageType {
    /** 单次发送的普通类型消息（非流式） */
    MESSAGE("message", "单次发送的普通类型消息"),
    /** 消息追加，适用于流式 Stream 推送，前端累加到当前 AI 回复中 */
    ADD("add", "消息追加，适用于流式stream推送"),
    /** 单次消息推送完成，前端停止 loading 并记录时间 */
    FINISH("finish", "单次消息推送完成"),
    /** 消息推送错误，前端显示错误提示 */
    ERROR("error", "消息推送错误"),
    /** 会话标题更新，前端更新侧边栏标题 */
    TITLE_UPDATE("title_update", "会话标题更新"),
    /** 自定义事件，用于扩展其他业务场景 */
    CUSTOM_EVENT("custom-event", "自定义事件"),
    /** 会话结束 */
    DONE("done", "会话结束"),
    /** SSE 连接建立确认，前端收到后认为连接就绪 */
    CONNECTED("connected", "SSE连接建立确认");

    /** SSE event 字段值 */
    public final String type;
    /** 中文描述 */
    public final String value;

    SSEMessageType(String type, String value) {
        this.type = type;
        this.value = value;
    }
}
