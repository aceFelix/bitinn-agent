package com.itniuma.bitinn.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一响应状态码枚举
 *
 * 定义 API 返回的 HTTP 状态码和对应的中文提示信息。
 * 通过 Result 对象包装后返回给前端，前端根据 code 判断请求结果。
 *
 * 状态码范围：
 *   2xx：成功
 *   4xx：客户端错误（参数、权限、资源）
 *   5xx：服务端错误
 *
 * @author aceFelix
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    /** 操作成功 */
    SUCCESS(200, "操作成功"),
    /** 创建成功 */
    CREATED(201, "创建成功"),
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** 未登录或 Token 已过期 */
    UNAUTHORIZED(401, "未登录或token已过期"),
    /** 无权限访问 */
    FORBIDDEN(403, "无权限访问"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 请求方法不允许 */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    /** 资源冲突（如重复注册） */
    CONFLICT(409, "资源冲突"),
    /** 参数校验失败（JSR-303） */
    VALIDATE_FAILED(422, "参数校验失败"),
    /** 服务器内部错误 */
    INTERNAL_ERROR(500, "服务器内部错误"),
    /** 服务暂不可用 */
    SERVICE_UNAVAILABLE(503, "服务暂不可用");

    /** HTTP 状态码 */
    private final Integer code;
    /** 中文提示信息 */
    private final String message;
}
