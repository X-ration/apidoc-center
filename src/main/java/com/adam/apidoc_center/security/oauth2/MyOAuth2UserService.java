package com.adam.apidoc_center.security.oauth2;

import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.service.UserService;
import com.adam.apidoc_center.util.ReflectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class MyOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) super.loadUser(userRequest);
        String nameAttributeKey;
        try {
            nameAttributeKey = ReflectUtils.getPrivateField(oAuth2User, "nameAttributeKey", String.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("获取name attribute key失败：", e);
            OAuth2Error oauth2Error = new OAuth2Error("missing_user_name_attribute",
                    "Missing required \"user name\" attribute name in UserInfoEndpoint for Client Registration: "
                            + userRequest.getClientRegistration().getRegistrationId(),
                    null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
        }
        Set<GrantedAuthority> authoritySet = new HashSet<>();
        authoritySet.add(new SimpleGrantedAuthority("ROLE_USER"));

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Provider oAuth2Provider = OAuth2Provider.of(registrationId);
        if (oAuth2Provider == null) {
            OAuth2Error oAuth2Error = new OAuth2Error("unknown_oauth2_provider", "Unknown OAuth2 provider \"" + registrationId + "\"", null);
            throw new OAuth2AuthenticationException(oAuth2Error, oAuth2Error.toString());
        }
        User user = null;
        switch (oAuth2Provider) {
            case GITHUB:
                Integer githubId = oAuth2User.getAttribute("id");
                user = userService.findGithubBindUser(githubId);
                break;
            case HUAWEI:
                String unionId = oAuth2User.getAttribute("unionID");
                user = userService.findHuaweiBindUser(unionId);
                break;
        }

        return new ExtendedOAuth2User(authoritySet, oAuth2User.getAttributes(), nameAttributeKey, OAuth2Provider.of(registrationId), user);
    }
}
