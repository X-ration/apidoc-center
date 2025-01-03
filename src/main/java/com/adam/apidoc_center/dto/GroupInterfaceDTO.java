package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.GroupInterface;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.List;

@Data
public class GroupInterfaceDTO {
    private String name;
    private String description;
    private String relativePath;
    private HttpMethod method;
    private GroupInterface.Type type;
    private List<InterfaceHeaderDTO> headerList;
    private List<InterfaceFieldDTO> fieldList;
}
