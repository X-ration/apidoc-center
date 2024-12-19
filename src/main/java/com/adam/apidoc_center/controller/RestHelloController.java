package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import org.springframework.security.access.AccessDeniedException;
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

    @RequestMapping("error500")
    public void error500() throws Exception {
        throw new Exception("error500");
    }

    @RequestMapping("error403")
    public void error403() {
        throw new AccessDeniedException("error403");
    }

}