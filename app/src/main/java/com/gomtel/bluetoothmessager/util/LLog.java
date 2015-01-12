package com.gomtel.bluetoothmessager.util;

import android.util.Log;

/**
 * Created by lixiang on 15-1-7.
 */
public class LLog {
    private static final boolean DEBUG = true;
    public static void e(String TAG,String content){
        if(DEBUG)
        Log.e(TAG, content);
    }

    public static void i(String TAG,String content){
        if(DEBUG)
        Log.i(TAG, content);
    }

    public static void w(String TAG,String content){
        if(DEBUG)
        Log.w(TAG, content);
    }

    public static void v(String TAG,String content){
        if(DEBUG)
        Log.v(TAG, content);
    }

    public static void d(String TAG,String content){
        if(DEBUG)
        Log.d(TAG, content);
    }
}
