package com.adam.apidoc_center.common;

import okhttp3.MediaType;

import java.util.Arrays;
import java.util.List;

/**
 * 存放各种非字符串信息的系统常量
 */
public class SystemConstants {

    public static final String DEFAULT_AVATAR_URL = "/resources/default_user.png";
    public static final long INVALID_USER_ID = -1;
    public static final List<String> FORM_DATA_PROHIBIT_FIELD_NAME_LIST = Arrays.asList("deploymentId", "headerList", "callStack");
    public static final MediaType OKHTTP_MEDIA_TYPE_OCTET_STREAM = MediaType.parse("application/octet-stream");
    public static final MediaType OKHTTP_MEDIA_TYPE_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");
    public static final int EMAIL_CODE_NUM_OF_DIGITS = 6;

}
