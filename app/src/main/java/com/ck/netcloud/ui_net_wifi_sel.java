package com.ck.netcloud;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ck.App_DataPara;
import com.hc.u8x_ck.R;

import java.util.ArrayList;
import java.util.List;


public class ui_net_wifi_sel extends Activity {

	App_DataPara AppDatPara;

	List<clasWifiInfo> m_listWifiInfo;
	ListView m_listView_Wifi;
	Adapter_wifi_sel m_WifiAdapter;

	int m_iSavedWifiNameIndex; // 已存储的 wifi name 序号
	int m_iHighLightIndex;

	public classWiFi mclsWifi;

	private WifiManager mWifiManager;
	private IntentFilter netFilter;
	public NetReceiver netReceiver;

	public class NetReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			// wifi已成功扫描到可用wifi。
			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				// mScanResults = mWifiManager.getScanResults();
				DispWifiValidList();
			}
			// 系统wifi的状态
			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
				switch (wifiState) {
				case WifiManager.WIFI_STATE_ENABLED:
					mclsWifi.StartScan();
					Log.i("main", "StartScan");
					break;
				case WifiManager.WIFI_STATE_DISABLED:

					break;
				}
			}
		}
	}

	// 获取 wifi信号有效的列表名称
	public List<String> GetWifiValidNameList() {
		String strbuf;
		List<String> strListValidWifiName = new ArrayList<String>();
		strListValidWifiName.clear();

		List<ScanResult> mScanResults = mWifiManager.getScanResults();
		if (mScanResults.size() > 0) {
			for (ScanResult scanResult : mScanResults) {
				strbuf = scanResult.SSID;
				int ilenth = strbuf.length();
				String strName = strbuf.substring(1, ilenth - 1);
				strListValidWifiName.add(strName);
			}
		}
		return strListValidWifiName;
	}

	public void DispWifiValidList() {
		m_listWifiInfo.clear();
		m_WifiAdapter.notifyDataSetChanged();

		m_iHighLightIndex = -1;
		m_iSavedWifiNameIndex = -2;
		String strName;

		List<String> strListName = mclsWifi.GetWifiValidNameList();
		for (int i = 0; i < strListName.size(); i++) {
			strName = strListName.get(i);
			clasWifiInfo clsWifiInfo = new clasWifiInfo();
			if (strName.equalsIgnoreCase(AppDatPara.sysPara.strNetCloudWifiName)) {
				if (m_iSavedWifiNameIndex < 0) {
					clsWifiInfo.strWifiName = strName;
					clsWifiInfo.strWifiContent = "Saved";
					m_iSavedWifiNameIndex = i;
				} else {
					continue;
				}
			} else {
				clsWifiInfo.strWifiName = strName;
				clsWifiInfo.strWifiContent = " ";
			}
			m_listWifiInfo.add(clsWifiInfo);
		}
		m_WifiAdapter.notifyDataSetChanged();
	}

	protected void onCreate(Bundle savedInstanceState) {
		AppDatPara = (App_DataPara) getApplicationContext();

		setFinishOnTouchOutside(false);
		super.onCreate(savedInstanceState);
		// 隐去标题栏（应用程序的名字）
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐去状态栏部分(电池等图标和一切修饰部分)
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.ui_net_wifi_sel);

		SharedPreferences sharedPreferences = getSharedPreferences("HC_DT5X_SYSDAT", 0);
		AppDatPara.sysPara.strNetCloudWifiName = sharedPreferences.getString("strNetCloudWifiName", "");
		AppDatPara.sysPara.strNetCloudWifiPswd = sharedPreferences.getString("strNetCloudWifiPswd", "");
		
		m_listView_Wifi = (ListView) findViewById(R.id.wifiList);

		netReceiver = new NetReceiver();
		netFilter = new IntentFilter();
		netFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		netFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// netFilter.addAction("intent.net.wifi.selected");
		registerReceiver(netReceiver, netFilter);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		m_listWifiInfo = new ArrayList<clasWifiInfo>();
		m_WifiAdapter = new Adapter_wifi_sel(this, m_listWifiInfo);
		m_listView_Wifi.setAdapter(m_WifiAdapter);

		m_listView_Wifi.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//
				m_WifiAdapter.setSelectItem(arg2);

				m_iHighLightIndex = arg2;
			}
		});

		try {
			Thread.sleep(300); // 等待 一会300
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		m_listWifiInfo.clear();
		m_WifiAdapter.notifyDataSetChanged();

		// 打开wifi，并启动 startscan
		mclsWifi = new classWiFi(ui_net_wifi_sel.this);
		mclsWifi.StartScan();

		// getAllNetWorkList();
	};

	void initData() { // item 的点击监听
		m_iHighLightIndex = -1;
		m_iSavedWifiNameIndex = -2;

		m_WifiAdapter = new Adapter_wifi_sel(this, m_listWifiInfo);
		m_listView_Wifi.setAdapter(m_WifiAdapter);
		m_WifiAdapter.setSelectItem(m_iHighLightIndex);
		m_WifiAdapter.notifyDataSetChanged();
	}

	public void WifiOK(View View) {
		String strWifiName;
		clasWifiInfo tclasDevData;
		if (m_iHighLightIndex >= 0 && m_iHighLightIndex < m_listWifiInfo.size()) {
			tclasDevData = m_listWifiInfo.get(m_iHighLightIndex);
			strWifiName = tclasDevData.strWifiName;
		} else {
			Toast.makeText(ui_net_wifi_sel.this, "请先选择WIFI名称", Toast.LENGTH_LONG).show();
			return;
		}

		mclsWifi.AkConnetWifi(strWifiName, "");

		sendBroadcast(new Intent("intent.net.wifi.selected"));

		ui_net_wifi_sel.this.finish(); // 关闭 当前 activity
	}

	public void WifiBrush(View View) {
		// getAllNetWorkList();

		mclsWifi.StartScan();

		m_listWifiInfo.clear();
		m_WifiAdapter.notifyDataSetChanged();

	}

	public void NewWifi(View View) {
		int iSize = m_listWifiInfo.size();
		if (m_iHighLightIndex >= iSize) {
			return;
		}

		String strWifiName;
		clasWifiInfo tclasDevData;
		if (m_iHighLightIndex < 0) {
			strWifiName = "WifiName";
		} else {
			tclasDevData = m_listWifiInfo.get(m_iHighLightIndex);
			strWifiName = tclasDevData.strWifiName;
		}

		final dlg_wifi_name_pswd psDialog = new dlg_wifi_name_pswd(ui_net_wifi_sel.this);
		psDialog.setCanceledOnTouchOutside(false);
		psDialog.show();

		final EditText EditTextWifiName = (EditText) psDialog.findViewById(R.id.wifi_name);
		final EditText EditTextWifiPswd = (EditText) psDialog.findViewById(R.id.strinput);

		EditTextWifiName.setText(strWifiName);

		if (m_iSavedWifiNameIndex == m_iHighLightIndex) {
			EditTextWifiPswd.setText(AppDatPara.sysPara.strNetCloudWifiPswd);
		} else {
			EditTextWifiPswd.setText("");
		}

		Button Okbtn = (Button) psDialog.findViewById(R.id.btn_ok);
		Button Cancelbtn = (Button) psDialog.findViewById(R.id.btn_cancel);
		Cancelbtn.setOnClickListener(new OnClickListener() { // 取消按钮
					public void onClick(android.view.View arg0) {
						psDialog.cancel();
					}
				});
		Okbtn.setOnClickListener(new OnClickListener() { // 确认按钮
			public void onClick(android.view.View arg0) {
				String strName = EditTextWifiName.getText().toString();
				strName = strName.trim();
				String strPassword = EditTextWifiPswd.getText().toString();
				strPassword = strPassword.trim();

				psDialog.cancel();

				AppDatPara.sysPara.strNetCloudWifiName = strName;
				AppDatPara.sysPara.strNetCloudWifiPswd = strPassword;

				SharedPreferences sharedPreferences = getSharedPreferences("HC_DT5X_SYSDAT", 0);
				Editor edit = sharedPreferences.edit();
				edit.putString("strNetCloudWifiName", AppDatPara.sysPara.strNetCloudWifiName);
				edit.putString("strNetCloudWifiPswd", AppDatPara.sysPara.strNetCloudWifiPswd);
				edit.commit();

				m_listWifiInfo.clear();

				// addNetWorkbyNamePassWord(AppDatPara.sysPara.strNetCloudWifiName,AppDatPara.sysPara.strNetCloudWifiPswd);
				mclsWifi.AkConnetWifi(AppDatPara.sysPara.strNetCloudWifiName, AppDatPara.sysPara.strNetCloudWifiPswd);

				sendBroadcast(new Intent("intent.net.wifi.selected"));

				ui_net_wifi_sel.this.finish(); // 关闭 当前 activity

			}
		});
	}

	public void BtnReturn(View View) {
		ui_net_wifi_sel.this.finish(); // 关闭 当前 activity
	}

	protected void onDestroy() { // 按对话框以外区域时， 销毁函数
		// Log.i("ui_browse10_upload_file", "onDestroy");
		unregisterReceiver(netReceiver);
		super.onDestroy();
	}
}
