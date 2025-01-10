package com.adam.apidoc_center.security.oauth2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public enum OAuth2Provider {

    HUAWEI("huawei", "华为"),
    GITHUB("github","GitHub"),
    ;

    private final String registrationId;
    private final String desc;

    public static OAuth2Provider of(String registrationId) {
        for(OAuth2Provider oAuth2Provider: values()) {
            if(oAuth2Provider.registrationId.equals(registrationId)) {
                return oAuth2Provider;
            }
        }
        return null;
    }

}
