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

}
