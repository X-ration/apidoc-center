package com.adam.apidoc_center.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class MyOAuth2LoginAuthenticationFilter extends OAuth2LoginAuthenticationFilter {
    public MyOAuth2LoginAuthenticationFilter(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService authorizedClientService,
                                             String pathMatcher) {
        super(clientRegistrationRepository, authorizedClientService);
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(pathMatcher));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("attemptAuthentication request={}", request.getRequestURI());
        String[] splits = request.getRequestURI().split("/");
        String oauth2Provider = splits[splits.length - 1];
        if(oauth2Provider.equalsIgnoreCase("huawei")) {
            HttpServletRequestWrapper requestWrapper = new HuaweiRewriteAuthorizationCodeRequestWrapper(request);
            return super.attemptAuthentication(requestWrapper, response);
        } else {
            return super.attemptAuthentication(request, response);
        }
    }

}
