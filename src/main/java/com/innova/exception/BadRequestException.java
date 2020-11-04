package com.innova.exception;
public class BadRequestException extends RuntimeException {

    private int messageCode;

    public BadRequestException(String message, int messageCode) {
        super(message);
        this.messageCode = messageCode;
    }

    public int getmessageCode(){
        return this.messageCode;
    }
}