package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/restHello")
public class RestHelloController {

    @RequestMapping("hello")
    public Response<?> hello() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return Response.success(localDateTime);
    }

}