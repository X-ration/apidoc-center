package com.adam.apidoc_center.dto;

import lombok.Data;

@Data
public class CallInterfaceResponseDTO {

    private String time;
    private Integer status;
    private String contentType;
    private String body;
    private String exception;

}
