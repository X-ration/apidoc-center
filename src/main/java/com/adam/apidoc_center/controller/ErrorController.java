package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/error")
public class ErrorController {

    @GetMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String error403() {
        return "error/403";
    }

    @PostMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Response<Void> error403Post() {
        return Response.fail("不允许的访问");
    }

}