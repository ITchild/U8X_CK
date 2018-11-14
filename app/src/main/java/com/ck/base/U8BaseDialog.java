package com.ck.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.ck.bean.RxBusMsgBean;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author fei
 * @date on 2018/11/12 0012
 * @describe TODO :
 **/
public abstract class U8BaseDialog extends Dialog {
    protected Context mContext;

    protected U8BaseDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    protected U8BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    protected U8BaseDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBeforSetContentView(savedInstanceState);
        setContentView(setLayout());
        initView();
        initData();
        initListener();
    }

    protected void actionBeforSetContentView(Bundle savedInstanceState) {
        hideBottomUIMenu();
    }

    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }
    /**
     * 设置布局资源文件
     * @return
     */
    protected abstract int setLayout();

    /**
     * 省去类型转换  将此方法写在基类Dialog
     */
    protected <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }
    /**
     * 抽象方法（初始化View的方法）
     */
    protected abstract void initView();

    /**
     * 初始化数据的方法（非必须实现的方法）
     */
    protected void initData() {

    }

    /**
     * 初始化监听事件的方法（非必须实现的方法）
     */
    protected void initListener() {

    }

    /**
     * 获取资源文件中的字符串
     *
     * @param res
     * @return
     */
    protected String getStr(int res) {
        return null != mContext ? mContext.getResources().getString(res) : "";
    }

    /**
     * 获取资源文件中的颜色值
     * @param res
     * @return
     */
    protected int getRColor(int res) {
        return null != mContext ? ContextCompat.getColor(mContext, res) : 0;
    }


    protected void subScribeRxbus(Context context){
        Disposable register = RxBus.getInstance().register(RxBusMsgBean.class, new Consumer<RxBusMsgBean>() {
            @Override
            public void accept(@NonNull RxBusMsgBean msgBean) {
                /**这个地方获取到数据。并执行相应的操作*/
                doRxBus(msgBean);
            }
        });
        RxBus.getInstance().addSubscription(context,register);
    }

    /**
     * 数据返回的处理
     * @param bean
     */
    protected void doRxBus(@NonNull RxBusMsgBean bean){

    }

    /**
     * 取消订阅RxBus
     */
    protected void unSubscribeRxBus(Context context){
        RxBus.getInstance().unSubscribe(context);
    }
}
