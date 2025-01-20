package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.domain.GroupInterface;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.List;

@Data
public class GroupInterfaceDTO {
    protected String name;
    protected String description;
    protected String relativePath;
    protected HttpMethod method;
    protected GroupInterface.Type type;
    protected GroupInterface.ResponseType responseType;
    protected List<InterfaceHeaderDTO> headerList;
    protected List<InterfaceFieldDTO> fieldList;
}
