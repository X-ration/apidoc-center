package com.adam.apidoc_center.common;

import java.util.Arrays;
import java.util.List;

/**
 * 存放各种非字符串信息的系统常量
 */
public class SystemConstants {

    public static final String DEFAULT_AVATAR_URL = "/resources/default_user.png";
    public static final long INVALID_USER_ID = -1;
    public static final List<String> FORM_DATA_PROHIBIT_FIELD_NAME_LIST = Arrays.asList("deploymentId", "headerList", "callStack");

}
