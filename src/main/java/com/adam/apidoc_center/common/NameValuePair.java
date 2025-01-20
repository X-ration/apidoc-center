package com.adam.apidoc_center.common;

import lombok.Data;

@Data
public class NameValuePair<T1,T2> {
    private T1 name;
    private T2 value;
}
