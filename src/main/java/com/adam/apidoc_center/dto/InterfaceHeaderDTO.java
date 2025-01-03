package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class InterfaceHeaderDTO {
    private String name;
    private String description;
    private Boolean required;
}
