package com.adam.apidoc_center.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@Controller
public class IndexController {

    @RequestMapping("")
    public String indexPage() {
        return "redirect:/project/viewAll";
    }

}
