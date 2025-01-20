package com.adam.apidoc_center.util;

import java.util.Objects;

public class AssertUtil {

    public static void requireNonNull(Object... objects) {
        for(Object object: objects) {
            Objects.requireNonNull(object);
        }
    }

}
