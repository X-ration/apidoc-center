package com.adam.apidoc_center.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String error403() {
        return "error/403";
    }

}