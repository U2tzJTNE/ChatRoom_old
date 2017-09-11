package cn.an0nymous.chatroom;

import android.app.Application;

import org.xutils.x;


/**
 * Created by 77009 on 2017-01-16.
 */

public class MyApplication extends Application {
    private static MyApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
        x.Ext.setDebug(false); // 是否输出debug日志, 开启debug会影响性能.
        instance = this;
    }
    public static MyApplication getInstance(){
        return instance;
    }
}
