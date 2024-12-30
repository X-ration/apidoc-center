package com.adam.apidoc_center.domain;

import lombok.Data;

@Data
public class InterfaceFieldDTO {
    private String name;
    private InterfaceField.Type type;
    private String description;
}
