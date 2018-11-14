package com.ck.base;

import android.app.Application;

import com.ck.listener.MyExceptionHandler;


public abstract class BaseApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        //开启程序错误时的错误信息保存
        MyExceptionHandler.create(this,setErrorLogPath());
    }

    /**
     * 设置程序报错后的错误信息存储路径
     * @return
     */
    protected abstract String setErrorLogPath();

}
