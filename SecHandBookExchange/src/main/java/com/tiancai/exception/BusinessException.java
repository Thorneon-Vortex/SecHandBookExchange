package com.tiancai.exception;

/**
 * 自定义业务异常类
 * 用于封装业务逻辑层面的错误信息
 */
public class BusinessException extends RuntimeException {

    // 错误码，可以用于前端根据不同错误码做不同处理
    private Integer code;

    /**
     * 构造函数，只包含错误信息
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 构造函数，包含错误码和错误信息
     * @param code 错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}