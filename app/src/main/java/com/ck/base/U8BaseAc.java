package com.ck.base;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ck.App_DataPara;
import com.ck.bean.RxBusMsgBean;
import com.ck.utils.PreferenceHelper;
import com.ck.utils.Stringutil;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * @author fei
 * @date on 2018/10/12 0012
 * @describe TODO :
 **/
public abstract class U8BaseAc extends AppCompatActivity {

    public App_DataPara AppDatPara;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBeforSetContentView(savedInstanceState);
        setContentView(initLayout());
        initView();
        initData();
        initListener();
    }

    protected void actionBeforSetContentView(Bundle savedInstanceState) {
        Window window = this.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        AppDatPara = (App_DataPara) getApplicationContext();
        if (savedInstanceState == null) {
            AppDatPara.nTheme = PreferenceHelper.getTheme();
        } else {
            AppDatPara.nTheme = savedInstanceState.getInt("theme");
        }
        Log.i("main", "nTheme = " + AppDatPara.nTheme);
        setTheme(AppDatPara.nTheme);
        hideBottomUIMenu();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (AppDatPara.nTheme != PreferenceHelper.getTheme()) {
            reload();
        }
        App_DataPara.getApp().addAcToList(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("theme", AppDatPara.nTheme);
    }

    protected void reload() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    /**
     * Toast的Base方法
     * @param str
     */
    protected void showToast(String str){
        Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
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
     * 抽象方法 （设置activity的Layout的方法）
     *
     * @return
     */
    protected abstract int initLayout();

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
     * 省去类型转换  将此方法写在基类Activity
     */
    protected <T extends View> T findView(int id) {
        return (T) super.findViewById(id);
    }

    /**
     * 跳转Activity
     *
     * @param cls
     */
    protected void JumpToAc(Class cls) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        startActivity(intent);
    }

    /**
     * antionBar中返回按钮的监听
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 判断字符串是否为null或空字符
     * @param str
     * @return
     */
    protected boolean isStrEmpty(String str){
        return Stringutil.isEmpty(str);
    }

    /**
     * 得到资源文件中的String字符串
     * @param res
     * @return
     */
    protected String getStr(int res){
        return getResources().getString(res);
    }

    /**
     * 获取资源文件中的颜色值
     * @param res
     * @return
     */
    protected int getRColor(int res){
        return ContextCompat.getColor(this,res);
    }


    /**
     * 注册RxBus
     */
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        App_DataPara.getApp().removeAcFromList(this);
    }
}
