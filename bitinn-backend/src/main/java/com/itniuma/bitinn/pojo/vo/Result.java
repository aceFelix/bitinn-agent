package com.itniuma.bitinn.pojo.vo;

import com.itniuma.bitinn.enums.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果
 *
 * 所有 API 的返回格式统一包装为此对象。
 * 前端根据 code 判断请求结果（200 成功，4xx/5xx 失败），
 * message 提供中文提示，data 携带业务数据，timestamp 用于调试。
 *
 * @author aceFelix
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    /** 状态码（200 成功，其他为错误） */
    private Integer code;
    /** 提示信息 */
    private String message;
    /** 响应数据 */
    private T data;
    /** 响应时间戳（毫秒） */
    private Long timestamp;

    /** 成功响应（带数据） */
    public static <E> Result<E> success(E data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }

    /** 成功响应（带数据和自定义消息） */
    public static <E> Result<E> success(E data, String message) {
        return new Result<>(200, message, data, System.currentTimeMillis());
    }

    /** 成功响应（无数据） */
    public static <E> Result<E> success() {
        return new Result<>(200, "操作成功", null, System.currentTimeMillis());
    }

    /** 错误响应（自定义状态码和消息） */
    public static <E> Result<E> error(Integer code, String message) {
        return new Result<>(code, message, null, System.currentTimeMillis());
    }

    /** 错误响应（默认 500，自定义消息） */
    public static <E> Result<E> error(String message) {
        return new Result<>(500, message, null, System.currentTimeMillis());
    }

    /** 错误响应（使用 ResultCode 枚举） */
    public static <E> Result<E> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null, System.currentTimeMillis());
    }
}
