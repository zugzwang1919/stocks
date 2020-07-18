package com.wolfesoftware.stocks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class UnexpectedException extends RuntimeException {
    public UnexpectedException(String reason) {
        super(reason);
    }
}
