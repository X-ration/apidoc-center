package com.adam.rest_demo_project;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/restHello")
@Slf4j
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

    @RequestMapping("/paramTest")
    public Response<?> paramTest(@RequestParam Map<String,Object> paramMap, @RequestParam Map<String, MultipartFile> fileMap) {
        log.debug("paramTest paramMap={} fileMap={}", paramMap, fileMap);
        return Response.success(paramMap);
    }

}