package com.ck.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.ck.App_DataPara;
import com.ck.utils.PreferenceHelper;

public class BaseActivity extends Activity {

	public App_DataPara AppDatPara;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

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

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (AppDatPara.nTheme != PreferenceHelper.getTheme()) {
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
}
