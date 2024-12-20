package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.dto.RegisterForm;
import com.adam.apidoc_center.dto.RegisterErrorMsg;
import com.adam.apidoc_center.dto.RegisterSuccessData;
import com.adam.apidoc_center.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;

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
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(RegisterForm registerForm, Model model) {
        Assert.notNull(registerForm, "register registerForm null");
        Response<?> registerResponse = userService.checkAndRegister(registerForm);
        if(registerResponse.isSuccess()) {
            RegisterSuccessData registerSuccessData = (RegisterSuccessData) registerResponse.getData();
            model.addAttribute("userId", registerSuccessData.getUserId());
            model.addAttribute("username", registerSuccessData.getUsername());
            model.addAttribute("email", registerSuccessData.getEmail());
            return "registerSuccess";
        } else {
            try {
                String json = objectMapper.writeValueAsString(registerResponse);
                model.addAttribute("error", json);
            } catch (JsonProcessingException e) {
                log.error("register Jackson processing exception", e);
                model.addAttribute("error", "注册失败：服务器出现异常");
            }
            return "register";
        }
    }

}