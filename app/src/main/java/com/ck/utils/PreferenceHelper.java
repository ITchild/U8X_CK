package com.ck.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.hc.u8x_ck.R;

public class PreferenceHelper {
	/**
	 * {功能}<获取主题>
	 * 
	 * @throw
	 * @return int
	 */
	public static int getTheme(Context context) {
		SharedPreferences mdPreferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
		return mdPreferences.getInt("theme", R.style.AppTheme_Black);
	}

	/**
	 * {功能}<设置主题>
	 * 
	 * @throw
	 * @return int
	 */
	public static void setTheme(Context context, int theme) {
		SharedPreferences mdPreferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
		Editor mEditor = mdPreferences.edit();
		mEditor.putInt("theme", theme).commit();
	}
	/*********************************************/
	public static String getProName(Context context) {
		SharedPreferences mdPreferences = context.getSharedPreferences("Pro", Context.MODE_PRIVATE);
		return mdPreferences.getString("ProName", "默认工程");
	}
	public static void setProName(Context context, String pro) {
		SharedPreferences mdPreferences = context.getSharedPreferences("Pro", Context.MODE_PRIVATE);
		Editor mEditor = mdPreferences.edit();
		mEditor.putString("ProName", pro).commit();
	}
	
	/**********************************************/
	public static String getGJName(Context context) {
		SharedPreferences mdPreferences = context.getSharedPreferences("Pro", Context.MODE_PRIVATE);
		return mdPreferences.getString("GJName", "默认构件1");
	}
	public static void setGJName(Context context, String gj) {
		SharedPreferences mdPreferences = context.getSharedPreferences("Pro", Context.MODE_PRIVATE);
		Editor mEditor = mdPreferences.edit();
		mEditor.putString("GJName", gj).commit();
	}
}
