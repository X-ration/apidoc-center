package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class RegisterSuccessData {
    private long userId;
    private String username;
    private String email;
}
