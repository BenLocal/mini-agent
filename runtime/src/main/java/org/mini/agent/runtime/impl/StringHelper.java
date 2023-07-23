package org.mini.agent.runtime.impl;

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
}
