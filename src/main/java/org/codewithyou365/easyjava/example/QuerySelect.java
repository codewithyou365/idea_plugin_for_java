package org.codewithyou365.easyjava.example;

import java.lang.reflect.Field;

public class QuerySelect {

    public static String from(String... columns) {
        StringBuilder sb = new StringBuilder();
        for (String column : columns) {
            sb.append(column).append(",");
        }
        return sb.substring(0, sb.toString().length() - 1);
    }



}
