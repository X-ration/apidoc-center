package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.InterfaceField;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InterfaceFieldDTO {
    private String name;
    private InterfaceField.Type type;
    private Boolean required;
    private String description;

    public InterfaceFieldDTO(InterfaceField interfaceField) {
        this.name = interfaceField.getName();
        this.type = interfaceField.getType();
        this.required = interfaceField.isRequired();
        this.description = interfaceField.getDescription();
    }
}
