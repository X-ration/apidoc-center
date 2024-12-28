package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class ProfileErrorMsg implements ErrorMsg{
    private String username;
    private String email;
    private String password;
    private String verifyPassword;
    private String description;

    @Override
    public boolean hasError() {
        return username != null || email != null || password != null || verifyPassword != null || description != null;
    }
}
