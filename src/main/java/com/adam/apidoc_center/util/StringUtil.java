package com.adam.apidoc_center.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    public static final Pattern EMAIL_PATTERN = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    public static final Pattern PASSWORD_PATTERN = Pattern.compile("[\\w\\W]+");
    public static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    public static final Pattern HTTP_OR_HTTPS_URL_PATTERN = Pattern.compile(
            "((http|https)://)" + //协议
                    "([\\w\\-]+\\.)*[\\w\\-]+" + //域名
                    "(:[0-9]+)?" + //端口
                    "((/[\\w-]*)*" + //路径
                    "(\\?[\\w-%:;&@#+=]*)?)"); //参数
    public static final Pattern RELATIVE_PATH_PATTERN = Pattern.compile("(/[\\w-{}]*)+");

    public static boolean isEmail(String email) {
        Objects.requireNonNull(email);
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.matches();
    }

    public static boolean isPassword(String password) {
        Objects.requireNonNull(password);
        Matcher matcher = PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }

    public static boolean isNumber(String string) {
        Objects.requireNonNull(string);
        Matcher matcher = NUMBER_PATTERN.matcher(string);
        return matcher.matches();
    }

    public static boolean isHttpOrHttpsUrl(String string) {
        Objects.requireNonNull(string);
        Matcher matcher = HTTP_OR_HTTPS_URL_PATTERN.matcher(string);
        return matcher.matches();
    }

    public static boolean isRelativePath(String string) {
        Objects.requireNonNull(string);
        Matcher matcher = RELATIVE_PATH_PATTERN.matcher(string);
        return matcher.matches();
    }

    public static void main(String[] args) {
        System.out.println(isHttpOrHttpsUrl("http://localhost:8080?abcd=123&adfdsa=2212#abc"));
        System.out.println(isHttpOrHttpsUrl("http://localhost:8080?abcd=123"));
        System.out.println(isHttpOrHttpsUrl("http://localhost:8080"));
        System.out.println(isHttpOrHttpsUrl("http://localhost"));
        System.out.println(isHttpOrHttpsUrl("http://www.sina.com"));
        System.out.println(isHttpOrHttpsUrl("http://www.sina.com/abcd/adbc/?fileName=dasfj+@:;"));
    }

}