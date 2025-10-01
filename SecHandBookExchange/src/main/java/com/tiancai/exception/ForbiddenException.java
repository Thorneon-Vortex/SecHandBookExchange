package com.tiancai.exception; // 建议创建一个 exception 包

public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}