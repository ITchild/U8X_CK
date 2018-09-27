package com.ck.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ck.utils.PreferenceHelper;
import com.ck.utils.StringUtil;

public class BaseActivity extends Activity {

	public App_DataPara AppDatPara;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		AppDatPara = (App_DataPara) getApplicationContext();

		if (savedInstanceState == null) {
			AppDatPara.nTheme = PreferenceHelper.getTheme(this);
		} else {
			AppDatPara.nTheme = savedInstanceState.getInt("theme");
		}
		Log.i("main", "nTheme = " + AppDatPara.nTheme);
		setTheme(AppDatPara.nTheme);

		super.onCreate(savedInstanceState);

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (AppDatPara.nTheme != PreferenceHelper.getTheme(this)) {
			reload();
		}
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
     * 判断字符串是否为null或空字符
     * @param str
     * @return
     */
    protected boolean isStrEmpty(String str){
        return StringUtil.isEmpty(str);
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
	 * Toast的Base方法
	 * @param str
	 */
	protected void showToast(String str){
		Toast.makeText(this,str,Toast.LENGTH_SHORT).show();
	}
    /**
     * 获取资源文件中的颜色值
     * @param res
     * @return
     */
    protected int getRColor(int res){
        return ContextCompat.getColor(this,res);
    }
}
