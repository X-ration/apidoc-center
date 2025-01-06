package com.adam.apidoc_center.util;

import java.lang.reflect.Field;

public class ReflectUtils {

    public static <T> T getPrivateField(Object object, String fieldName, Class<T> clazz) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    public static void setPrivateField(Object object, String fieldName, Object fieldValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, fieldValue);
    }
}
