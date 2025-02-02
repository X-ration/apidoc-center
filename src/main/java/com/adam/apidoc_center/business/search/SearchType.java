package com.adam.apidoc_center.business.search;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SearchType {
    ALL("所有类型"),PROJECT("项目"),GROUP("分组"),INTERFACE("接口");
    private final String desc;
}
