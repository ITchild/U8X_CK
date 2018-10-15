package com.ck.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ck.netcloud.ClasSysPara;
import com.ck.utils.DBService;
import com.fei.feilibs_1_0_0.BaseApplication;
import com.hc.u8x_ck.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class App_DataPara extends BaseApplication {
	/**
	 * 屏幕密度
	 */
	public float fDispDensity;
	
	public int nTheme = R.style.AppTheme_White;
	
	 public  ClasSysPara sysPara = new ClasSysPara();           //系统参数 
	/**
	 * 播放音乐标记位
	 */
	public boolean m_bPlayMusic;

	private static App_DataPara app;

	/**
	 * 选择的仪器索引
	 */
	public int m_nSelectUserNidx = 0;
	
	/**
	 * 数据库
	 */
	public DBService m_DbService;
	/**
	 * 工程ListView选择item
	 */
	public int m_nProjectSeleteNidx = 0;
	/**
	 * 构件ListView选择item
	 */
	public int m_nGJSeleteNidx = -1;
	private List<Activity> acList ;
	@Override
	public void onCreate() {
		super.onCreate();
		app = this;
		m_DbService = DBService.getInstence(this);
		RegistDiskReceiver();
		initPro();
		initRegisterTime();
	}

	public void addAcToList(Activity activity){
		if(null == acList){
			acList = new ArrayList<>();
		}
		acList.remove(activity);
		acList.add(activity);
	}
	public void removeAcFromList(Activity activity){
		if(null == acList || acList.size() <= 0){
			return;
		}
		acList.remove(activity);
	}

	public void finishAll(){
		if(null == acList){
			return;
		}
		Log.i("fei",acList.size()+"");
		for (Activity activity : acList){
			activity.finish();
		}
	}

	/**
	 * 崩溃文件的文件位置
	 * @return
	 */
	@Override
	protected String setErrorLogPath() {
		return Environment.getExternalStorageDirectory().getAbsolutePath()+
				"/"+getPackageName()+"/错误日志";
	}

	public static App_DataPara getApp(){
		return app;
	}
	public void initRegisterTime() {
		SharedPreferences sp = getSharedPreferences("RegisterTime", Context.MODE_PRIVATE);
		String str = sp.getString("time", "");
		if (str.equals("")) {
			str = new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
			str = str.substring(0, str.length() - 1);
			Editor edit = sp.edit();
			edit.putString("time", str);
			edit.commit();
		}
		sysPara.strDevRegistSN = "149" + str;
	}
	public void initPro() {
		
	}


	
	private IntentFilter mFilter;
	private String m_strESDir = null;

	// U盘路径
	public String GetExternalStorageDirectory() {
		return m_strESDir;
	}

	private void RegistDiskReceiver() {
		if (mFilter == null) {
			mFilter = new IntentFilter();
			mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
			mFilter.addAction(Intent.ACTION_MEDIA_EJECT);
			mFilter.addDataScheme("file");
			registerReceiver(mHandleMsg, mFilter);
		}
	}

	private BroadcastReceiver mHandleMsg = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String pathString = intent.getData().getPath();

			if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {// 插入设备
				m_strESDir = pathString;
				Log.i("fei",pathString + "        "+ intent.getDataString());
				Toast.makeText(context, "U盘已插入", Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
				m_strESDir = null;
			}
		}
	};

	public void UnRegistDiskReceiver() {
		if (mFilter != null) {
			unregisterReceiver(mHandleMsg);
		}
	}

	public String GetDigitalPile(String strData) {
		String strName = ""; // 汉字部分
		String strDigital = ""; // 数字部分
		int nDigital = 1; // 数字部分
		for (int i = 0; i < strData.length(); i++) {
			if (Character.isDigit(strData.charAt(i))) {
				strDigital += String.valueOf(strData.charAt(i));
			} else {
				strName += strData.charAt(i);
			}
		}

		if (!strDigital.equals("")) {
			nDigital = Integer.parseInt(strDigital) + 1;
		}
		return strName + nDigital;
	}

	/**
	 * {功能}<请描述这个方法是干什么的>
	 * 
	 * @throw nType:1-波形，2-字体,3-背景颜色
	 * @return void
	 */
	public int getZhuTiColor(int nType) {
		switch (nTheme) {
		case R.style.AppTheme_White: // 主题-白色
			switch (nType) {
			case 1: // 波形
				return Color.BLACK;
			case 2:// 字体颜色
				return Color.BLACK;
			case 3:
				return Color.WHITE;
			}
			break;
		case R.style.AppTheme_Black: // 主题-黑色
			switch (nType) {
			case 1: // 波形
				return Color.WHITE;
			case 2:// 字体颜色
				return Color.WHITE;
			case 3:
				return Color.BLACK;
			}
			break;
		}
		return 0;
	}
}
