package com.adam.apidoc_center.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SavedRequestAwareAuthenticationSuccessHandler:表单登录默认的success handler，重定向到之前访问的地址
 * 此Handler做出改进：当直接访问登录页时跳转到首页
 */
@Slf4j
public class ImprovedSavedRequestAwareAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //request.getRequestURI():相对路径
        //request.getRequestURL():绝对路径，带ip和端口号
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
        if(savedRequest == null) {
            response.sendRedirect("/");
        } else {
            String requestURI = ((DefaultSavedRequest)savedRequest).getRequestURI();
            if(requestURI.startsWith("/user/login")) {
                response.sendRedirect("/");
            } else {
                super.onAuthenticationSuccess(request, response, authentication);
            }
        }
        //获取认证用户信息
        UserDetails user = (UserDetails) authentication.getPrincipal();
        log.info("=====authentication success user={} authorities={} ip={}", user.getUsername(), user.getAuthorities(), request.getRemoteAddr());
    }
}
