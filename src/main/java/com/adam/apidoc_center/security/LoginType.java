package com.adam.apidoc_center.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    USERNAME_PASSWORD("用户名密码"),
    OAUTH2_GITHUB("GitHub OAuth2"),
    OAUTH2_HUAWEI("华为OAuth2"),
    ;

    private String desc;
    public String getFullDesc() {
        return desc + "登录";
    }

}
