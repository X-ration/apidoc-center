package com.adam.apidoc_center.dto;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class GroupInterfaceErrorMsg implements ErrorMsg{

    private String name;
    private String description;
    private String relativePath;
    private String method;
    private String type;
    private String responseType;
    private List<InterfaceHeaderErrorMsg> headerList;
    private List<InterfaceFieldErrorMsg> fieldList;

    @Override
    public boolean hasError() {
        if(name != null || description != null || relativePath != null || method != null || type != null || responseType != null) {
            return true;
        }
        if(!CollectionUtils.isEmpty(headerList)) {
            for(InterfaceHeaderErrorMsg errorMsg: headerList) {
                if(errorMsg.hasError()) {
                    return true;
                }
            }
        }
        if(!CollectionUtils.isEmpty(fieldList)) {
            for(InterfaceFieldErrorMsg errorMsg: fieldList) {
                if(errorMsg.hasError()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Data
    public static class InterfaceHeaderErrorMsg implements ErrorMsg {
        private String name;
        private String description;
        private String required;

        @Override
        public boolean hasError() {
            return name != null || description != null || required != null;
        }
    }

    @Data
    public static class InterfaceFieldErrorMsg implements ErrorMsg {
        private String name;
        private String type;
        private String description;
        private String required;

        @Override
        public boolean hasError() {
            return name != null || type != null || description != null || required != null;
        }
    }
}
