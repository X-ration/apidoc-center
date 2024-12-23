package com.adam.apidoc_center.common;

/**
 * 存储各种字符串信息常量
 */
public class StringConstants {

    //文件上传/下载相关
    public static final String REQUEST_FILE_IS_NULL = "上传的文件为null";
    public static final String UPLOAD_FAIL = "上传失败";
    public static final String FILE_NOT_FOUND = "找不到对应的文件，文件可能已被删除或移动";

    //注册,修改资料相关
    public static final String REGISTER_FAIL_CHECK_INPUTS = "注册失败，请检查输入";
    public static final String MODIFY_FAIL_CHECK_INPUTS = "修改失败，请检查输入";
    public static final String REGISTER_FAIL_SERVER_ERROR = "注册服务器出现异常，请稍候再试";
    public static final String MODIFY_FAIL_SERVER_ERROR = "修改过程出现异常，请稍候再试";
    public static final String USERNAME_INPUT_BLANK = "用户名输入为空";
    public static final String USERNAME_LENGTH_EXCEEDED = "用户名不能超过32个字符";
    public static final String USERNAME_NUMBER_NOT_ALLOWED = "用户名不允许是纯数字";
    public static final String USERNAME_ALREADY_IN_USE = "用户名已被使用";
    public static final String EMAIL_INPUT_BLANK = "邮箱输入为空";
    public static final String EMAIL_LENGTH_EXCEEDED = "邮箱长度不能超过256个字符";
    public static final String EMAIL_INVALID = "邮箱格式不正确";
    public static final String EMAIL_ALREADY_IN_USE = "邮箱地址已被使用";
    public static final String PASSWORD_INPUT_BLANK = "密码输入为空";
    public static final String PASSWORD_LENGTH_EXCEEDED = "密码长度不能超过32个字符";
    public static final String PASSWORD_INVALID = "密码只能包含字母、数字、英文标点符号";
    public static final String VERIFY_PASSWORD_INPUT_BLANK = "确认密码输入为空";
    public static final String VERIFY_PASSWORD_NOT_EQUAL = "两次输入的密码不一致";
    public static final String DESCRIPTION_LENGTH_EXCEEDED = "个人介绍不能超过100个字符";

    //用户相关
    public static final String QUERY_USER_CORE_PARAM_BLANK = "参数为空";
    public static final String QUERY_USER_CORE_NOT_FOUND = "未找到用户";

}
