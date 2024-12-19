package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.dto.RegisterForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    @GetMapping("/login")
    public ModelAndView login(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("login");
        Map<String,String[]> parameterMap = request.getParameterMap();
        if(parameterMap.containsKey("error")) {
            modelAndView.addObject("error", "");
        }
        if(parameterMap.containsKey("logout")) {
            modelAndView.addObject("logout", "");
        }
        return modelAndView;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

}