package cn.an0nymous.chatroom.Util;

import android.content.Context;
import android.content.SharedPreferences;

import cn.an0nymous.chatroom.MyApplication;

/**
 * Created by 77009 on 2017-01-23.
 */

public class SPUtil {
    private static Context context = MyApplication.getInstance();
    private static SharedPreferences sp = context.getSharedPreferences("chatroom",context.MODE_PRIVATE);
    private static SharedPreferences.Editor editor = sp.edit();

    public static void putString(String key,String value){
        editor.putString(key,value);
        editor.apply();
    }
    public static void putInt(String key,int value){
        editor.putInt(key, value);
        editor.apply();
    }
    public static void putBoolean(String key,boolean value){
        editor.putBoolean(key,value);
        editor.apply();
    }
    public static String getString(String key,String defValue){
        return sp.getString(key, defValue);
    }
    public static int getInt(String key, int defValue){
        return sp.getInt(key, defValue);
    }
    public static boolean getBoolean(String key,boolean defValue){
        return sp.getBoolean(key, defValue);
    }
}
