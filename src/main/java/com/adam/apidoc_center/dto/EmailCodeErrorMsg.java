package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class EmailCodeErrorMsg implements ErrorMsg{

    private String email;
    private String code;

    @Override
    public boolean hasError() {
        return email != null || code != null;
    }
}
