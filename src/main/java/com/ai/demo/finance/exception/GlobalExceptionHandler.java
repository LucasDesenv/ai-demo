package com.ai.demo.finance.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundResourceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO handleNotFoundResourceException(NotFoundResourceException ex) {
        log.warn(ex.getMessage());
        return new ErrorDTO(ex.getClass().getCanonicalName(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorDTO handleGlobalException(Exception ex) {
        log.error(ex);
        return new ErrorDTO(ex.getClass().getCanonicalName(), ex.getMessage());
    }
}