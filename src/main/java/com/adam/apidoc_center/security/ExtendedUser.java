package com.adam.apidoc_center.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class ExtendedUser extends User implements SecurityUser{
    private final com.adam.apidoc_center.domain.User user;
    public ExtendedUser(String username, String password, Collection<? extends GrantedAuthority> authorities, com.adam.apidoc_center.domain.User user) {
        super(username, password, authorities);
        this.user = user;
    }
}
