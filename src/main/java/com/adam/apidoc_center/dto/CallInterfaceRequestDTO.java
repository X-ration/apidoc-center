package com.adam.apidoc_center.dto;

import com.adam.apidoc_center.common.NameValuePair;
import lombok.Data;

import java.util.List;

@Data
public class CallInterfaceRequestDTO {

    private long interfaceId;
    private long deploymentId;
    private CallStack callStack;
    private List<NameValuePair<String,String>> headerList;
    private List<NameValuePair<String,Object>> fieldList;

    public enum CallStack {
        RestTemplate,OkHttp
        ;
    }

}
