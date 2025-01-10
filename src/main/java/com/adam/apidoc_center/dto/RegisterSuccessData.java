package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class RegisterSuccessData {
    protected long userId;
    protected String username;
    protected String email;
}
