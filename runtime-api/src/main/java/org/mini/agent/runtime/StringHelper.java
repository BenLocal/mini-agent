package org.mini.agent.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * 
 * @date Jul 22, 2023
 * @time 9:56:52 PM
 * @author tangchuanyu
 * @description
 * 
 */
public final class StringHelper {
    private StringHelper() {
    }

    public static List<String> toList(String str) {
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.asList(str.split(","));
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String confirmLeadingSlash(String path) {
        if (path != null && path.length() >= 1 && !path.substring(0, 1).equals("/")) {
            return "/" + path;
        }
        return path;
    }
}
