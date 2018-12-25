package com.easyway.rxjava;

import android.support.annotation.NonNull;
import android.util.Log;

/**
 * @author 侯建军
 * @data on 2018/1/4 10:40
 * @org www.hopshine.com
 * @function 请填写
 * @email 474664736@qq.com
 */
public class Ulog {
    private static final String TAG = "cc-";

    public static void i(Object content) {
        Log.i(TAG, content + "");
    }


    public static void i(Object tag, Object... content) {
        Log.i(TAG  + tag, getString(content));
    }

    @NonNull
    private static String getString(Object[] obArray) {
        String message="";
        for (int i = 0; i < obArray.length; i++) {
            message+=obArray[i]+(i==1||i==(obArray.length-1)?"":"===");
        }

        return message;
    }

}