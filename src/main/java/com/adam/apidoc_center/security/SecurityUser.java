package com.adam.apidoc_center.security;

import com.adam.apidoc_center.domain.User;

public interface SecurityUser {

    User getUser();
    LoginType getLoginType();

}
