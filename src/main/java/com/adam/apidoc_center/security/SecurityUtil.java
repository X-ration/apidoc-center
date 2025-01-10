package com.adam.apidoc_center.security;

import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.security.oauth2.ExtendedOAuth2User;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.Objects;

public class SecurityUtil {
    public static ExtendedOAuth2User getExtendedOAuth2User() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof OAuth2AuthenticationToken) {
            return (ExtendedOAuth2User) authentication.getPrincipal();
        } else {
            return null;
        }
    }

    public static ExtendedUser getExtendedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof UsernamePasswordAuthenticationToken ||
                authentication instanceof RememberMeAuthenticationToken) {
            return (ExtendedUser) authentication.getPrincipal();
        } else {
            return null;
        }
    }

    public static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof UsernamePasswordAuthenticationToken ||
                authentication instanceof RememberMeAuthenticationToken) {
            ExtendedUser extendedUser = (ExtendedUser) authentication.getPrincipal();
            return extendedUser.getUser();
        } else if(authentication instanceof OAuth2AuthenticationToken) {
            ExtendedOAuth2User oAuth2User = (ExtendedOAuth2User) authentication.getPrincipal();
            return oAuth2User.getUser();
        }
        throw new NullPointerException();
    }

    public static String getOAuth2RegistrationId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof OAuth2AuthenticationToken) {
            return ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        } else {
            return null;
        }
    }

    public static boolean hasUserType(String registrationId) {
        User user = getUser();
        return user.getUserTypeList().contains(User.UserType.of(registrationId));
    }

}
