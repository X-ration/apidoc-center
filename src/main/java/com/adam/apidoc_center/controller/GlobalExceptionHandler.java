package com.adam.apidoc_center.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.Locale;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleException(Exception exception, Locale locale) {
        log.error("GlobalExceptionHandler handleException {}", exception.getClass().getName(), exception);
        ModelAndView modelAndView = new ModelAndView("error/500");
        modelAndView.addObject("err", exception.getClass().getName() + ":" + exception.getMessage());
        return modelAndView;
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException exception) {
        ModelAndView modelAndView = new ModelAndView("error/403");
        modelAndView.addObject("err", exception.getClass().getName() + ":" + exception.getMessage());
        return modelAndView;
    }

}