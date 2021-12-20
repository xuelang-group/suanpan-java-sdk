package com.xuelang.mqstream.common;

import com.sun.prism.shader.Solid_TextureYV12_AlphaTest_Loader;

import java.util.Locale;

public class CommonUtil {
    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean isLinux(){
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }
}
