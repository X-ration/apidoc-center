package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.InterfaceField;
import lombok.Data;

@Data
public class InterfaceFieldDTO {
    private String name;
    private InterfaceField.Type type;
    private Boolean required;
    private String description;
}
