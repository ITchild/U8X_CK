package com.ck.base;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ck.App_DataPara;
import com.ck.utils.PreferenceHelper;
import com.fei.feilibs_1_0_0.base.ac.BaseActivity;

/**
 * @author fei
 * @date on 2018/10/12 0012
 * @describe TODO :
 **/
public abstract class U8BaseAc extends BaseActivity {

    public App_DataPara AppDatPara;

    @Override
    protected void actionBeforSetContentView(Bundle savedInstanceState) {
        super.actionBeforSetContentView(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        AppDatPara = (App_DataPara) getApplicationContext();

        if (savedInstanceState == null) {
            AppDatPara.nTheme = PreferenceHelper.getTheme();
        } else {
            AppDatPara.nTheme = savedInstanceState.getInt("theme");
        }
        Log.i("main", "nTheme = " + AppDatPara.nTheme);
        setTheme(AppDatPara.nTheme);
    }



    @Override
    protected void initView() {
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        App_DataPara.getApp().removeAcFromList(this);
    }
}
