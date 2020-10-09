package com.innova.exception;

public class CaptchaExpectedException extends RuntimeException {
    public CaptchaExpectedException(String message) {
        super(message);
    }
}
