package com.adam.apidoc_center.common;

/**
 * 存储各种字符串信息常量
 */
public class StringConstants {

    //通用
    public static final String REQUEST_PARAM_IS_NULL = "请求参数为null";

    //文件上传/下载相关
    public static final String REQUEST_FILE_IS_NULL = "上传的文件为null";
    public static final String UPLOAD_FAIL = "上传失败";
    public static final String FILE_NOT_FOUND = "找不到对应的文件，文件可能已被删除或移动";

    //注册,修改资料相关
    public static final String REGISTER_FAIL_CHECK_INPUTS = "注册失败，请检查输入";
    public static final String MODIFY_FAIL_CHECK_INPUTS = "修改失败，请检查输入";
    public static final String REGISTER_FAIL_SERVER_ERROR = "注册服务器出现异常，请稍候再试";
    public static final String MODIFY_FAIL_SERVER_ERROR = "修改过程出现异常，请稍候再试";
    public static final String SEND_EMAIL_CODE_PARAM_INVALID = "发送验证码失败";
    public static final String SEND_EMAIL_CODE_ERROR = "验证码发送异常";
    public static final String SEND_EMAIL_CODE_SUBJECT = "注册为接口文档中心用户";
    public static final String SEND_EMAIL_CODE_TEXT = "【Apidoc-center接口文档中心】验证码：{}";
    public static final String SEND_EMAIL_CODE_RATE_LIMITED = "发送验证码接口限流";
    public static final String USERNAME_INPUT_BLANK = "用户名输入为空";
    public static final String USERNAME_LENGTH_EXCEEDED = "用户名不能超过32个字符";
    public static final String USERNAME_NUMBER_NOT_ALLOWED = "用户名不允许是纯数字";
    public static final String USERNAME_ALREADY_IN_USE = "用户名已被使用";
    public static final String EMAIL_INPUT_BLANK = "邮箱输入为空";
    public static final String EMAIL_LENGTH_EXCEEDED = "邮箱长度不能超过256个字符";
    public static final String EMAIL_INVALID = "邮箱格式不正确";
    public static final String EMAIL_ALREADY_IN_USE = "邮箱地址已被使用";
    public static final String EMAIL_CODE_INPUT_BLANK = "邮箱验证码输入为空";
    public static final String EMAIL_CODE_NOT_MATCH = "邮箱验证码不正确";
    public static final String PASSWORD_INPUT_BLANK = "密码输入为空";
    public static final String PASSWORD_LENGTH_EXCEEDED = "密码长度不能超过32个字符";
    public static final String PASSWORD_INVALID = "密码只能包含字母、数字、英文标点符号";
    public static final String VERIFY_PASSWORD_INPUT_BLANK = "确认密码输入为空";
    public static final String VERIFY_PASSWORD_NOT_EQUAL = "两次输入的密码不一致";
    public static final String DESCRIPTION_LENGTH_EXCEEDED = "个人介绍不能超过100个字符";

    //用户相关
    public static final String QUERY_USER_CORE_PARAM_BLANK = "参数为空";
    public static final String QUERY_USER_CORE_NOT_FOUND = "未找到用户";
    public static final String USERNAME_OR_PASSWORD_WRONG = "用户名或密码错误";
    public static final String OAUTH2_UNKNOWN_PROVIDER = "未知的OAuth2服务提供者";
    public static final String OAUTH2_USER_ALREADY_BIND = "该用户已经被绑定了";
    public static final String OAUTH2_UNBIND_SUCCESS = "解绑成功";
    public static final String OAUTH2_UNBIND_FAIL_NO_BINDING_HUAWEI = "解绑失败，未绑定华为账号";
    public static final String OAUTH2_UNBIND_FAIL_NO_BINDING_GITHUB = "解绑失败，未绑定GitHub账号";

    //项目相关
    public static final String PROJECT_CREATE_FAIL_CHECK_INPUT = "项目创建失败，请检查输入";
    public static final String PROJECT_MODIFY_FAIL_CHECK_INPUT = "项目修改失败，请检查输入";
    public static final String PROJECT_NAME_BLANK = "项目名为空";
    public static final String PROJECT_NAME_LENGTH_EXCEEDED = "项目名不超过32个字符";
    public static final String PROJECT_DESCRIPTION_LENGTH_EXCEEDED = "项目介绍不超过200个字符";
    public static final String PROJECT_ACCESS_MODE_NULL = "项目访问模式为空";
    public static final String PROJECT_ALLOW_USER_IDS_INVALID = "项目允许访问的用户参数非法";
    public static final String PROJECT_DEPLOYMENT_ID_INVALID = "项目部署环境id不合法";
    public static final String PROJECT_DEPLOYMENT_ENVIRONMENT_BLANK = "项目部署环境名为空";
    public static final String PROJECT_DEPLOYMENT_ENVIRONMENT_LENGTH_EXCEEDED = "项目部署环境名不超过32个字符";
    public static final String PROJECT_DEPLOYMENT_URL_BLANK = "项目部署地址为空";
    public static final String PROJECT_DEPLOYMENT_URL_LENGTH_EXCEEDED = "项目部署地址不超过256个字符";
    public static final String PROJECT_DEPLOYMENT_URL_INVALID = "项目部署地址不是有效的http(s)地址";
    public static final String PROJECT_NOT_EXISTS = "项目不存在";
    public static final String PROJECT_ONLY_OWNER_CAN_DELETE = "只有项目创建者或分享者才能删除项目";
    public static final String PROJECT_ONLY_OWNER_CAN_MODIFY = "只有项目创建者或分享者才能修改项目";
    public static final String PROJECT_DELETE_FAIL = "删除失败，请稍候再试";
    public static final String PROJECT_ID_INVALID = "项目id非法";
    public static final String PROJECT_FOLLOW_FAIL = "关注项目失败";
    public static final String PROJECT_UNFOLLOW_FAIL = "取消关注项目失败";

    //分组、接口、字段相关
    public static final String PROJECT_GROUP_CREATE_FAIL_CHECK_INPUT = "分组创建失败，请检查输入";
    public static final String PROJECT_GROUP_MODIFY_FAIL_CHECK_INPUT = "分组修改失败，请检查输入";
    public static final String PROJECT_GROUP_NAME_BLANK = "分组名称为空";
    public static final String PROJECT_GROUP_NAME_LENGTH_EXCEEDED = "分组长度不超过32个字符";
    public static final String PROJECT_GROUP_ID_INVALID = "分组id非法";
    public static final String GROUP_INTERFACE_ID_INVALID = "接口id非法";
    public static final String GROUP_INTERFACE_CREATE_FAIL_CHECK_INPUT = "接口创建失败，请检查输入";
    public static final String GROUP_INTERFACE_MODIFY_FAIL_CHECK_INPUT = "接口修改失败，请检查输入";
    public static final String GROUP_INTERFACE_DELETE_FAIL = "删除失败，请稍候再试";
    public static final String GROUP_INTERFACE_NAME_BLANK = "接口名称为空";
    public static final String GROUP_INTERFACE_NAME_LENGTH_EXCEEDED = "接口名称长度不超过32个字符";
    public static final String GROUP_INTERFACE_DESCRIPTION_LENGTH_EXCEEDED = "接口描述长度不超过100个字符";
    public static final String GROUP_INTERFACE_RELATIVE_PATH_BLANK = "接口相对路径为空";
    public static final String GROUP_INTERFACE_RELATIVE_PATH_LENGTH_EXCEEDED = "接口相对路径长度不超过256个字符";
    public static final String GROUP_INTERFACE_RELATIVE_PATH_INVALID = "无效的相对路径";
    public static final String GROUP_INTERFACE_METHOD_NULL = "接口请求方法必填";
    public static final String GROUP_INTERFACE_TYPE_NULL = "接口请求体类型必填";
    public static final String GROUP_INTERFACE_RESPONSE_TYPE_NULL = "接口响应体类型必填";
    public static final String GROUP_INTERFACE_CALL_PARAM_INVALID = "调用接口参数不合法：";
    public static final String GROUP_INTERFACE_CALL_HEADER_MISSING = "头部{}为空";
    public static final String GROUP_INTERFACE_CALL_FIELD_MISSING = "字段{}为空";
    public static final String GROUP_INTERFACE_CALL_TEXT_PARAM_INVALID = "文本字段{}的值不是文本类型";
    public static final String GROUP_INTERFACE_CALL_FILE_PARAM_INVALID = "文件字段{}的值不是文件类型";
    public static final String GROUP_INTERFACE_CALL_FILE_PARAM_NOT_ALLOWED = "请求体类型不允许文件字段";
    public static final String GROUP_INTERFACE_CALL_EXCEPTION = "调用接口出现异常";
    public static final String GROUP_INTERFACE_CALL_DEFAULT_CONTENT_TYPE = "未知类型";
    public static final String INTERFACE_HEADER_NAME_BLANK = "接口头部名称为空";
    public static final String INTERFACE_HEADER_NAME_LENGTH_EXCEEDED = "接口头部长度不超过32个字符";
    public static final String INTERFACE_HEADER_DESCRIPTION_LENGTH_EXCEEDED = "接口头部描述长度不超过100个字符";
    public static final String INTERFACE_HEADER_REQUIRED_NULL = "接口头部是否必填未选择";
    public static final String INTERFACE_FIELD_NAME_BLANK = "接口字段名称为空";
    public static final String INTERFACE_FIELD_NAME_LENGTH_EXCEEDED = "接口字段长度不超过32个字符";
    public static final String INTERFACE_FIELD_NAME_PROHIBITED = "请求体类型不允许该字段名的出现";
    public static final String INTERFACE_FIELD_TYPE_NULL = "接口字段类型必填";
    public static final String INTERFACE_FIELD_SHOULD_NOT_EXIST = "无请求体类型接口字段不应出现";
    public static final String INTERFACE_FIELD_TYPE_SHOULD_NOT_BE_FILE = "请求体类型不允许文件类型字段";
    public static final String INTERFACE_FIELD_DESCRIPTION_LENGTH_EXCEEDED = "接口字段描述长度不超过100个字符";
    public static final String INTERFACE_FIELD_REQUIRED_NULL = "接口字段是否必填未选择";

    //搜索相关
    public static final String SEARCH_FAIL_PREFIX = "搜索失败：";
    public static final String SEARCH_PARAM_BLANK = "搜索字符串为空";

}
