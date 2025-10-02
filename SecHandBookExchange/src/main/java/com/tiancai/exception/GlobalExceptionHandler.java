package com.tiancai.exception;

// 在 GlobalExceptionHandler.java 中添加
import com.tiancai.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // <--- 返回 404 Not Found
    public Result handleResourceNotFound(ResourceNotFoundException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // <--- 返回 403 Forbidden
    public Result handleForbidden(ForbiddenException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // <--- 返回 500 Internal Server Error
    public Result handleGenericException(Exception ex) {
        ex.printStackTrace(); // 生产环境应使用日志框架
        return Result.error("服务器内部错误，请稍后重试");
    }

    /**
     * 捕获业务异常 (BusinessException)
     * @param ex 我们自定义的业务异常
     * @return 统一的错误响应结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 返回 400 状态码
    public Result handleBusinessException(BusinessException ex) {
        log.error("业务异常: {}", ex.getMessage());
        // 直接使用异常中携带的信息来构建 Result 对象
        return Result.error(ex.getMessage());
    }
}