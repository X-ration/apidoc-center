package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.InterfaceHeader;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InterfaceHeaderDTO {
    private String name;
    private String description;
    private Boolean required;

    public InterfaceHeaderDTO(InterfaceHeader interfaceHeader) {
        this.name = interfaceHeader.getName();
        this.description = interfaceHeader.getDescription();
        this.required = interfaceHeader.isRequired();
    }
}
