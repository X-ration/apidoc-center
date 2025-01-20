package com.adam.apidoc_center.controller;

import com.adam.apidoc_center.common.Response;
import com.adam.apidoc_center.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    @RequestMapping("error403")
    public void error403() {
        throw new AccessDeniedException("error403");
    }

    @RequestMapping("/paramTest")
    public Response<?> paramTest(@RequestParam Map<String,Object> paramMap, @RequestParam Map<String, MultipartFile> fileMap) {
        log.debug("paramTest paramMap={} fileMap={}", paramMap, fileMap);
        return Response.success(paramMap);
    }

    @RequestMapping("/oauth2UserAttributes")
    public Map<String,Object> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String,Object> resultMap = new HashMap<>();
        if(authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            Collection<? extends GrantedAuthority> authorities = oAuth2AuthenticationToken.getAuthorities();
            List<String> authorityStrings = new LinkedList<>();
            for (GrantedAuthority authority : authorities) {
                authorityStrings.add(authority.getAuthority());
            }
            resultMap.put("authority", authorityStrings);
            String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
            User.UserType userType = User.UserType.of(registrationId);

            OAuth2User oAuth2User = oAuth2AuthenticationToken.getPrincipal();
            resultMap.put("userType", userType);
            resultMap.put("name", oAuth2User.getName());
            switch (userType) {
                case OAUTH2_GITHUB:
                    resultMap.put("id", oAuth2User.getAttribute("id"));
                    resultMap.put("username", oAuth2User.getAttribute("login"));
                    resultMap.put("avatar_url", oAuth2User.getAttribute("avatar_url"));
                    resultMap.put("realName", oAuth2User.getAttribute("name"));
                    resultMap.put("email", oAuth2User.getAttribute("email"));
                    resultMap.put("bio", oAuth2User.getAttribute("bio"));
                    break;
                case OAUTH2_HUAWEI:
                    resultMap.put("displayName", oAuth2User.getAttribute("displayName"));
                    resultMap.put("headPictureURL", oAuth2User.getAttribute("headPictureURL"));
                    resultMap.put("unionID", oAuth2User.getAttribute("unionID"));
                    resultMap.put("openID", oAuth2User.getAttribute("openID"));
                    resultMap.put("displayNameFlag", oAuth2User.getAttribute("displayNameFlag"));
                    break;
                default:
            }
        }
        return resultMap;
    }
}