package com.adam.apidoc_center.security.oauth2;

import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 重写此类的原因是DefaultOAuth2UserService会调用convert方法将OAuth2UserRequest转换成供
 * RestTemplate调用的RequestEntity对象，即控制着如何调用用户信息接口的逻辑
 */
public class MyOAuth2UserRequestEntityConverter extends OAuth2UserRequestEntityConverter {
    @Override
    public RequestEntity<?> convert(OAuth2UserRequest userRequest) {
        RequestEntity<?> requestEntity = super.convert(userRequest);
        //华为指定通过POST调用
        if(userRequest.getClientRegistration().getRegistrationId().equalsIgnoreCase("huawei")) {
            MultiValueMap<String,String> formParameters = new LinkedMultiValueMap<>();
            formParameters.add("getNickName", "1");
            formParameters.add("access_token", requestEntity.getHeaders().get("Authorization").get(0).substring(7));
            requestEntity = new RequestEntity<>(formParameters, requestEntity.getHeaders(), HttpMethod.POST, requestEntity.getUrl());
        }
        return requestEntity;
    }
}
