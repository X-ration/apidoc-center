package com.adam.apidoc_center.security.oauth2;

import com.adam.apidoc_center.util.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class MyDefaultOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private DefaultOAuth2AuthorizationRequestResolver internalResolver;
    private AntPathRequestMatcher authorizationRequestMatcher;
    private static final String REGISTRATION_ID_URI_VARIABLE_NAME = "registrationId";

    public MyDefaultOAuth2AuthorizationRequestResolver(DefaultOAuth2AuthorizationRequestResolver resolver, String authorizationRequestBaseUri) {
        this.internalResolver = resolver;
        this.authorizationRequestMatcher = new AntPathRequestMatcher(
                authorizationRequestBaseUri + "/{" + REGISTRATION_ID_URI_VARIABLE_NAME + "}");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = internalResolver.resolve(request);
        String registrationId = resolveRegistrationId(request);
        processRequest(oAuth2AuthorizationRequest, registrationId);
        return oAuth2AuthorizationRequest;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = internalResolver.resolve(request, clientRegistrationId);
        processRequest(oAuth2AuthorizationRequest, clientRegistrationId);
        return oAuth2AuthorizationRequest;
    }

    private void processRequest(OAuth2AuthorizationRequest request, String registrationId) {
        if(request != null && registrationId != null) {
            log.info("processRequest, authorizationUri={}, registrationId={}", request.getAuthorizationRequestUri(), registrationId);
            //华为指定scope为多个http连接以空格为分隔符
            if(registrationId.equalsIgnoreCase("huawei")) {
                processRequestUriHuawei(request);
            }
        }
    }

    private void processRequestUriHuawei(OAuth2AuthorizationRequest request) {
        String requestUri = processRequestUriHuawei(request.getAuthorizationRequestUri());
        try {
            ReflectUtils.setPrivateField(request, "authorizationRequestUri", requestUri);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static String processRequestUriHuawei(String requestUri) {
        StringBuilder stringBuilder = new StringBuilder();
        String[] splits1 = requestUri.split("\\?");
        stringBuilder.append(splits1[0]).append("?");
        String[] splits2 = splits1[1].split("&");
        for(String split2:splits2) {
            String[] splits3 = split2.split("=");
            if(!splits3[0].equals("scope")) {
                stringBuilder.append(split2).append("&");
            } else {
                //去掉首尾的中括号
                String scopeValue = splits3[1].substring(3, splits3[1].length() - 3);
                stringBuilder.append("scope=").append(scopeValue).append("&");
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    private String resolveRegistrationId(HttpServletRequest request) {
        if (this.authorizationRequestMatcher.matches(request)) {
            return this.authorizationRequestMatcher.matcher(request).getVariables()
                    .get(REGISTRATION_ID_URI_VARIABLE_NAME);
        }
        return null;
    }

    public static void main(String[] args) {
        String requestUri = "https://oauth-login.cloud.huawei.com/oauth2/v2/authorize?response_type=code&client_id=112985827&scope=%5Bhttps://www.huawei.com/auth/account/base.profile%20https://ads.cloud.huawei.com/report%20https://ads.cloud.huawei.com/promotion%20https://ads.cloud.huawei.com/tools%20https://ads.cloud.huawei.com/account%20https://ads.cloud.huawei.com/finance%5D&state=wnd9a9G3ubmHWD-VUnqFy3zr-K_IlE4zdAWrbJ7PQrQ%3D&redirect_uri=http://localhost:8080/user/login/oauth2/callback/huawei";
        System.out.println(processRequestUriHuawei(requestUri));
    }
}