package com.tiancai.exception; // 确保包名与你的项目结构一致

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 自定义异常：资源未找到异常
 * <p>
 * 当业务逻辑中尝试访问一个不存在的资源时（例如，根据ID查询一个不存在的用户），
 * 应该抛出此异常。
 * <p>
 * 全局异常处理器会捕获此异常，并向客户端返回一个HTTP 404 Not Found状态码。
 * </p>
 *
 * @ResponseStatus(value = HttpStatus.NOT_FOUND) 注解是另一种实现方式，
 * 它能让Spring Boot在没有全局异常处理器的情况下，也能自动将此异常映射到404状态码。
 * 但为了统一处理和返回自定义的JSON格式，我们更推荐使用全局异常处理器（GlobalExceptionHandler）。
 */
// @ResponseStatus(value = HttpStatus.NOT_FOUND) // 通常我们会选择在GlobalExceptionHandler中统一处理，所以这里可以注释掉
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 默认构造函数
     */
    public ResourceNotFoundException() {
        super("Resource not found");
    }

    /**
     * 带有自定义消息的构造函数
     * @param message 异常的详细信息
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 带有自定义消息和根本原因的构造函数
     * @param message 异常的详细信息
     * @param cause 导致此异常的根本原因
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}