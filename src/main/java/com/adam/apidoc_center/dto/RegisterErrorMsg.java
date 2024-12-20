package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class RegisterErrorMsg {
    private String username;
    private String email;
    private String password;
    private String verifyPassword;
    private String description;

    public boolean hasError() {
        return username != null || email != null || password != null || verifyPassword != null || description != null;
    }
}
