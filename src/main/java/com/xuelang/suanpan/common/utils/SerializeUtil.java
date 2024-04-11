package com.xuelang.suanpan.common.utils;

public class SerializeUtil {

    public static String serialize(Object[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < data.length - 1; i += 2) {
            sb.append(data[i].toString()).append("=").append(data[i + 1].toString()).append(",");
        }

        sb.deleteCharAt(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}
