package com.wolfesoftware.stocks.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class IllegalActionException extends RuntimeException {
    public IllegalActionException(String reason) {
        super(reason);
    }
}
