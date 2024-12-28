package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class ProjectGroupErrorMsg implements ErrorMsg{

    private String name;

    @Override
    public boolean hasError() {
        return name != null;
    }
}
