package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.common.StringConstants;
import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.dto.*;
import com.adam.apidoc_center.security.ImprovedSavedRequestAwareAuthenticationSuccessHandler;
import com.adam.apidoc_center.security.SecurityUtil;
import com.adam.apidoc_center.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ImprovedSavedRequestAwareAuthenticationSuccessHandler authenticationSuccessHandler;

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
    public String modifyProfilePage(Model model) {
        User user = SecurityUtil.getUser();
        //隐藏密码
        UserDTO userDTO = new UserDTO(user);
        model.addAttribute("user", userDTO);
        return "user/modifyProfile";
    }

    @PostMapping("/modifyProfile")
    public String modifyProfile(UserDTO userDTO, Model model) {
        Assert.notNull(userDTO, "modifyProfile userDTO null");
        Response<?> registerResponse = userService.checkAndModify(userDTO);
        model.addAttribute("user", userDTO);
        if(registerResponse.isSuccess()) {
//            return "redirect:/user/modifyProfile?success=true";
            model.addAttribute("successMessage", "修改资料成功");
        } else {
            try {
                String json = objectMapper.writeValueAsString(registerResponse);
                model.addAttribute("error", json);
            } catch (JsonProcessingException e) {
                log.error("register Jackson processing exception", e);
                model.addAttribute("error", "注册失败：服务器出现异常");
            }
        }
        return "user/modifyProfile";
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

    @GetMapping("/bindOAuth2Login")
    public String bindOAuth2Login(Model model) {
        model.addAttribute("oauth2User", SecurityUtil.getExtendedOAuth2User());
        model.addAttribute("registrationId", SecurityUtil.getOAuth2RegistrationId());
        return "user/oauth2BindLogin";
    }

    @PostMapping("/bindOAuth2Login")
    public String bindOAuth2LoginPost(@RequestParam String username, @RequestParam String password, Model model) {
        Response<OAuth2BindSuccessData> response = userService.oauth2BindLogin(username, password);
        if(response.isSuccess()) {
            model.addAttribute("data", response.getData());
            return "user/oauth2BindSuccess";
        } else {
            model.addAttribute("oauth2User", SecurityUtil.getExtendedOAuth2User());
            model.addAttribute("registrationId", SecurityUtil.getOAuth2RegistrationId());
            model.addAttribute("error", response.getMessage());
            return "user/oauth2BindLogin";
        }
    }

    @GetMapping("/bindOAuth2Register")
    public String bindOAuth2Register(Model model) {
        model.addAttribute("oauth2User", SecurityUtil.getExtendedOAuth2User());
        model.addAttribute("registrationId", SecurityUtil.getOAuth2RegistrationId());
        model.addAttribute("registerForm", new RegisterForm());
        return "user/oauth2BindRegister";
    }

    @PostMapping("/bindOAuth2Register")
    public String bindOAuth2RegisterPost(RegisterForm registerForm, Model model) {
        Assert.notNull(registerForm, "register registerForm null");
        Response<?> response = userService.oauth2BindRegister(registerForm);
        if(response.isSuccess()) {
            model.addAttribute("data", response.getData());
            return "user/oauth2BindSuccess";
        } else {
            try {
                String json = objectMapper.writeValueAsString(response);
                model.addAttribute("error", json);
            } catch (JsonProcessingException e) {
                log.error("register Jackson processing exception", e);
                model.addAttribute("error", "注册失败：服务器出现异常");
            }
            model.addAttribute("oauth2User", SecurityUtil.getExtendedOAuth2User());
            model.addAttribute("registrationId", SecurityUtil.getOAuth2RegistrationId());
            model.addAttribute("registerForm", registerForm);
            return "user/oauth2BindRegister";
        }
    }

    @GetMapping("/bindOAuth2Redirect")
    public void bindOAuth2Redirect(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();;
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
    }

    @PostMapping("/unbindOAuth2Huawei")
    public String unbindOAuth2Huawei(RedirectAttributes redirectAttributes) {
        Response<Void> response = userService.unbindOAuth2User(User.UserType.OAUTH2_HUAWEI);
        if(response.isSuccess()) {
            redirectAttributes.addAttribute("successMessage", StringConstants.OAUTH2_UNBIND_SUCCESS);
        } else {
            redirectAttributes.addAttribute("error", response.getMessage());
        }
        return "redirect:/user/modifyProfile";
    }

    @PostMapping("/unbindOAuth2Github")
    public String unbindOAuth2Github(Model model) {
        Response<Void> response = userService.unbindOAuth2User(User.UserType.OAUTH2_GITHUB);
        if(response.isSuccess()) {
            model.addAttribute("successMessage", StringConstants.OAUTH2_UNBIND_SUCCESS);
        } else {
            model.addAttribute("error", response.getMessage());
        }
        return "redirect:/user/modifyProfile";
    }

}