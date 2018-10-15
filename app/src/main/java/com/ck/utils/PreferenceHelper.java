package com.ck.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ck.main.App_DataPara;
import com.hc.u8x_ck.R;

public class PreferenceHelper {
	/**
	 * {功能}<获取主题>
	 * 
	 * @throw
	 * @return int
	 */
	public static int getTheme() {
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("theme",Context.MODE_PRIVATE);
		return mdPreferences.getInt("theme", R.style.AppTheme_Black);
	}

	/**
	 * {功能}<设置主题>
	 * 
	 * @throw
	 * @return int
	 */
	public static void setTheme(int theme) {
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("theme", Context.MODE_PRIVATE);
		Editor mEditor = mdPreferences.edit();
		mEditor.putInt("theme", theme).commit();
	}

	/**
	 * 获取盘屏幕的亮度值
	 * @return
	 */
	public static int getScreenLisght(){
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("theme", Context.MODE_PRIVATE);
		return mdPreferences.getInt("light", 0);
	}

	/**
	 * 存储盘屏幕的亮度值
	 * @param light
	 */
	public static void setScreenLight(int light){
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("theme", Context.MODE_PRIVATE);
		Editor editor = mdPreferences.edit();
		editor.putInt("light",light).commit();
	}


	/*********************************************/
	public static String getProName() {
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("Pro", Context.MODE_PRIVATE);
		return mdPreferences.getString("ProName", "默认工程");
	}
	public static void setProName(String pro) {
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("Pro", Context.MODE_PRIVATE);
		Editor mEditor = mdPreferences.edit();
		mEditor.putString("ProName", pro).commit();
	}
	
	/**********************************************/
	public static String getGJName() {
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("Pro", Context.MODE_PRIVATE);
		return mdPreferences.getString("GJName", "默认构件1");
	}
	public static void setGJName(String gj) {
		SharedPreferences mdPreferences = App_DataPara.getApp().getSharedPreferences("Pro", Context.MODE_PRIVATE);
		Editor mEditor = mdPreferences.edit();
		mEditor.putString("GJName", gj).commit();
	}
}
