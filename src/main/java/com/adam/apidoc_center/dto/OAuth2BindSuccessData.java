package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.security.oauth2.ExtendedOAuth2User;
import com.adam.apidoc_center.security.oauth2.OAuth2Provider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OAuth2BindSuccessData extends RegisterSuccessData{

    protected OAuth2Provider provider;
    protected ExtendedOAuth2User oAuth2User;

    public OAuth2BindSuccessData(RegisterSuccessData registerSuccessData) {
        this.userId = registerSuccessData.userId;
        this.username = registerSuccessData.username;
        this.email = registerSuccessData.email;
    }

}
