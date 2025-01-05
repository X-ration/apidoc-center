package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.InterfaceField;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InterfaceFieldDisplayDTO {
    private String name;
    private InterfaceField.Type type;
    private String typeDesc;
    private Boolean required;
    private String description;

    public InterfaceFieldDisplayDTO(InterfaceField interfaceField) {
        this.name = interfaceField.getName();
        this.type = interfaceField.getType();
        this.typeDesc = interfaceField.getType().getDesc();
        this.required = interfaceField.isRequired();
        this.description = interfaceField.getDescription();
    }
}
