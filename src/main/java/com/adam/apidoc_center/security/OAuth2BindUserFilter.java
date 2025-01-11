package com.adam.apidoc_center.security;

import com.adam.apidoc_center.security.oauth2.ExtendedOAuth2User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class OAuth2BindUserFilter extends OncePerRequestFilter {

    private final AntPathRequestMatcher bindPathMatcher = new AntPathRequestMatcher("/user/bindOAuth2*");
    private final List<AntPathRequestMatcher> ignorePathMatcherList = new LinkedList<>();

    public OAuth2BindUserFilter(String... ignorePaths) {
        for(String path: ignorePaths) {
            ignorePathMatcherList.add(new AntPathRequestMatcher(path));
        }
    }

    private boolean ignoreMatch(HttpServletRequest request) {
        for(AntPathRequestMatcher requestMatcher: ignorePathMatcherList) {
            if(requestMatcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain chain) throws ServletException, IOException {
        log.debug("OAuth2BindUserFilter.doFilterInternal requestURI={}", servletRequest.getRequestURI());
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if(ignoreMatch(request)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof OAuth2AuthenticationToken) {
            ExtendedOAuth2User oAuth2User = (ExtendedOAuth2User) authentication.getPrincipal();
            if(oAuth2User.getUser() == null && !bindPathMatcher.matches(request)) {
                response.sendRedirect("/user/bindOAuth2Login");
                return;
            } else if(oAuth2User.getUser() != null && bindPathMatcher.matches(request)) {
                response.sendRedirect("/");
                return;
            }
        } else if(authentication instanceof UsernamePasswordAuthenticationToken
                || authentication instanceof RememberMeAuthenticationToken) {
            if(bindPathMatcher.matches(request)) {
                response.sendRedirect("/");
                return;
            }
        }
        chain.doFilter(servletRequest, servletResponse);
    }
}
