package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.dto.RegisterForm;
import com.adam.apidoc_center.dto.RegisterSuccessData;
import com.adam.apidoc_center.dto.UserCoreDTO;
import com.adam.apidoc_center.dto.UserDTO;
import com.adam.apidoc_center.security.ExtendedUser;
import com.adam.apidoc_center.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
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
        ModelAndView modelAndView = new ModelAndView("user/login");
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
        return "user/register";
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
            return "user/registerSuccess";
        } else {
            try {
                String json = objectMapper.writeValueAsString(registerResponse);
                model.addAttribute("error", json);
            } catch (JsonProcessingException e) {
                log.error("register Jackson processing exception", e);
                model.addAttribute("error", "注册失败：服务器出现异常");
            }
            return "user/register";
        }
    }

    @GetMapping("/modifyProfile")
    public String modifyProfilePage(Model model, @RequestParam(required = false) boolean success) {
        ExtendedUser extendedUser = (ExtendedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //隐藏密码
        UserDTO userDTO = new UserDTO(extendedUser.getUser());
        model.addAttribute("user", userDTO);
        if(success) {
            model.addAttribute("success", true);
        }
        return "user/modifyProfile";
    }

    @PostMapping("/modifyProfile")
    public String modifyProfile(UserDTO userDTO, Model model) {
        Assert.notNull(userDTO, "modifyProfile userDTO null");
        Response<?> registerResponse = userService.checkAndModify(userDTO);
        if(registerResponse.isSuccess()) {
            return "redirect:/user/modifyProfile?success=true";
        } else {
            try {
                String json = objectMapper.writeValueAsString(registerResponse);
                model.addAttribute("error", json);
            } catch (JsonProcessingException e) {
                log.error("register Jackson processing exception", e);
                model.addAttribute("error", "注册失败：服务器出现异常");
            }
            model.addAttribute("user", userDTO);
            return "user/modifyProfile";
        }
    }

    @GetMapping("/queryUserCore")
    @ResponseBody
    public Response<UserCoreDTO> queryUserCore(@RequestParam String queryParam) {
        if(StringUtils.isBlank(queryParam)) {
            return Response.fail(StringConstants.QUERY_USER_CORE_PARAM_BLANK);
        }
        UserCoreDTO userCoreDTO = userService.queryUserCore(queryParam);
        if(userCoreDTO != null) {
            return Response.success(userCoreDTO);
        } else {
            return Response.fail(StringConstants.QUERY_USER_CORE_NOT_FOUND);
        }
    }

}