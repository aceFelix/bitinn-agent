package com.itniuma.bitinn.exception;

import com.itniuma.bitinn.pojo.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * 使用 @RestControllerAdvice 拦截所有 Controller 层抛出的异常，
 * 统一包装为 Result 对象返回给前端，避免异常信息直接暴露给客户端。
 *
 * 当前处理范围：
 *   - Exception.class：兜底捕获所有未处理的异常
 *   后续可按需添加更细粒度的处理（如 MethodArgumentNotValidException 校验异常）
 *
 * @author aceFelix
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 兜底异常处理
     *
     * 捕获所有未被细粒度 Handler 处理的异常，记录日志并返回友好提示。
     * 生产环境应避免将原始异常信息返回给客户端。
     *
     * @param e 异常对象
     * @return Result 错误响应，message 为异常信息或默认提示
     */
    @ExceptionHandler(Exception.class)
    public Result handlerException(Exception e) {
        log.error("全局异常处理", e);
        return Result.error(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败");
    }
}
