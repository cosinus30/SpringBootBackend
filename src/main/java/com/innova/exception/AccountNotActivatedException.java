package com.innova.exception;

import javax.naming.AuthenticationException;

public class AccountNotActivatedException extends RuntimeException {
    public AccountNotActivatedException(String explanation) {
        super(explanation);
    }
}
