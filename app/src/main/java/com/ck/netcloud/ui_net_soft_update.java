package com.ck.netcloud;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ck.main.App_DataPara;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class ui_net_soft_update extends Activity {

	App_DataPara AppDatPara;

	public static final int NO_GPRS = 0;
	public static final int NO_WIFI = 111;

	private boolean isGPRSConnect = false;
	private boolean isWifiConnect = false;

	private ProgressBar mProgressBar;
	private TextView mTotalTextView, mPercent;

	public classWiFi mclsWifi;
	private IntentFilter netFilter;
	NetReceiver netReceiver;

	public static final int HANDLE_SUCESS = 0;
	public static final int HANDLE_LOADING = 1;
	public static final int HANDEL_ERROR = 2;
	public static final int HANDEL_CHECK_WIFI_STATUS = 3;

	public String mstrUpdateApkName = "";
	public String mstrUpdateInfoVersion = "1.00";
	public boolean bFindNewVersionFlag = false;

	public boolean bCurDownLoadApkFile = false; // 当前 下载 的是 APK 文件？
	public int iDownloadFileStatus = 0;

	// public boolean m_bRunWifiUploadWaitingThread = false; //运行wifi 等待上传 线程
	// public boolean m_bEnWifiUploadTimeStart = false;

	public class NetReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// ui_net_wifi_sel 界面，wifi 选择有效
			if (intent.getAction().equals("intent.net.wifi.selected")) {
				IntentWifiSoftUpdate();
			}

			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (null != parcelableExtra) {
					NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
					State state = networkInfo.getState();
					if (state == State.CONNECTED) {
						// 不是采集器 wifi 名称
						if (0 == mclsWifi.IsWifiConnectNameOK("")) {
							isWifiConnect = true;
							isGPRSConnect = false;
							// ((TextView)
							// findViewById(R.id.cur_version_no)).setTextColor(Color.rgb(0xF6,0xB8,0x00));
							// //海创黄色
							// ((TextView)
							// findViewById(R.id.cur_version_no)).setText("获取数据...");
						}
					}
				}
			}
			if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
				ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				if (networkInfo != null) {
					State GPRSState = networkInfo.getState();
					if (GPRSState == State.CONNECTED) {
						isGPRSConnect = true;
						isWifiConnect = false;
						// ((TextView)
						// findViewById(R.id.cur_version_no)).setText("获取数据...");
					}
				}
			}
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_SUCESS:
				if (bCurDownLoadApkFile == false) { // 下载的是 UpdateInfo.txt
													// ，解包、显示
					ReadTxtFileInfo2DispInfo(1);
					iDownloadFileStatus = 1; // 接收OK
					if (bFindNewVersionFlag == false) {
						((Button) findViewById(R.id.btn_wifi_update)).setTextColor(Color.WHITE);
						((Button) findViewById(R.id.btn_gprs_update)).setTextColor(Color.WHITE);
						((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.WHITE);
						((Button) findViewById(R.id.btn_wifi_update)).setClickable(true);
						((Button) findViewById(R.id.btn_gprs_update)).setClickable(true);
						((Button) findViewById(R.id.btn_cancel)).setClickable(true);

						if (isWifiConnect == true) {
							mclsWifi.disCurConnectWifi();
						}
					}
					return;
				}
				((Button) findViewById(R.id.btn_wifi_update)).setTextColor(Color.WHITE);
				((Button) findViewById(R.id.btn_gprs_update)).setTextColor(Color.WHITE);
				((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.WHITE);
				((Button) findViewById(R.id.btn_wifi_update)).setClickable(true);
				((Button) findViewById(R.id.btn_gprs_update)).setClickable(true);
				((Button) findViewById(R.id.btn_cancel)).setClickable(true);
				// 下载完成安装apk
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(PathUtils.FILE_PATH + "/" + mstrUpdateApkName)), "application/vnd.android.package-archive");
				startActivity(intent);
				ui_net_soft_update.this.finish(); // 关闭 当前 activity
				break;

			case HANDLE_LOADING:
				long total = msg.arg1;
				long current = msg.arg2;
				if (total <= 0 || current <= 0) {
					return;
				}
				float progress = (float) current / total;
				int num = (int) (progress * 100);
				if (num < mProgressBar.getProgress()) {
					return;
				}
				if (num > 100) {
					num = 100;
				}

				float all = (float) (total / 1024) / 1024;
				float cur = (float) (current / 1024) / 1024;
				mPercent.setText(num + " %");
				mTotalTextView.setText((Math.round(cur * 100) / 100.0) + " / " + (Math.round(all * 100) / 100.0) + "M");
				mProgressBar.setProgress(num);
				break;
			case HANDEL_ERROR:
				((Button) findViewById(R.id.btn_wifi_update)).setTextColor(Color.WHITE);
				((Button) findViewById(R.id.btn_gprs_update)).setTextColor(Color.WHITE);
				((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.WHITE);
				((Button) findViewById(R.id.btn_wifi_update)).setClickable(true);
				((Button) findViewById(R.id.btn_gprs_update)).setClickable(true);
				((Button) findViewById(R.id.btn_cancel)).setClickable(true);
				((TextView) findViewById(R.id.cur_version_no)).setText("数据更新失败！");

				iDownloadFileStatus = -1;
				mProgressBar.setProgress(0);
				mPercent.setText("0 %");
				break;
			}

		};
	};

	protected void onCreate(Bundle savedInstanceState) {

		AppDatPara = (App_DataPara) getApplicationContext();
		setFinishOnTouchOutside(false);
		super.onCreate(savedInstanceState);
		// 隐去标题栏（应用程序的名字）
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐去状态栏部分(电池等图标和一切修饰部分)
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.ui_net_soft_update);

		mTotalTextView = (TextView) findViewById(R.id.check_update_textAPKTotal);
		mPercent = (TextView) findViewById(R.id.check_update_textPercent);

//		AppDatPara.m_SysPara.iEnableWifiSampleDev = 0;

		netReceiver = new NetReceiver();

		netFilter = new IntentFilter();
		netFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		netFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		netFilter.addAction("intent.net.wifi.selected");
		registerReceiver(netReceiver, netFilter);

		mclsWifi = new classWiFi(ui_net_soft_update.this);

		initProgressBar();
		ReadTxtFileInfo2DispInfo(0);

	};

	// 返回值 大于零， 表示发现新版本
	public boolean ReadTxtFileInfo2DispInfo(int bNewVersion) {
		String strUpdateInfoPath = PathUtils.FILE_PATH + "/" + "UpdateInfo.txt";
		List<String> listStrInfo = new ArrayList<String>();
		listStrInfo.clear();
		bFindNewVersionFlag = false;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(strUpdateInfoPath), "gbk"));
			String readLine = null;
			StringBuffer buffer = new StringBuffer();
			String[] ResultSplit;

			if ((readLine = reader.readLine()) != null) { // 读取第一行 内容， 不加回车符
				ResultSplit = readLine.split("/"); // 按照 "/" 分割 第一行字符。
			} else {
				readLine = "test/test";
				ResultSplit = readLine.split("/");
				;
			}

			if (ResultSplit.length >= 3) {
				listStrInfo.add(ResultSplit[0]);
				listStrInfo.add(ResultSplit[1]);
				listStrInfo.add(ResultSplit[2]); // 目前，只取前面3个字段 ,APK 文件大小 Mbytes
			}

			while ((readLine = reader.readLine()) != null) { // 后续按照整行 读取
				buffer.append(readLine + "\r\n");
			}
			readLine = buffer.toString(); // 第二行以后的所有内容 作为一个 字段
			listStrInfo.add(readLine);
		} catch (Exception e) {
		}

		if (listStrInfo.size() < 4) {
			((TextView) findViewById(R.id.ver_update_info)).setText("---");
			((TextView) findViewById(R.id.cur_version_no)).setTextColor(Color.rgb(0xF6, 0xB8, 0x00)); // 海创黄色
			((TextView) findViewById(R.id.cur_version_no)).setText("当前版本: V" + getVersion());
			return false;
		}

		String strInfo;
		String strFileSize;
		strInfo = listStrInfo.get(3);
		strFileSize = listStrInfo.get(2);
		mstrUpdateApkName = listStrInfo.get(1);
		mstrUpdateInfoVersion = listStrInfo.get(0);

		boolean bRetNewVersion = false;
		((TextView) findViewById(R.id.ver_update_info)).setText(strInfo);
		String strkkk = getVersion();
		if (mstrUpdateInfoVersion.equalsIgnoreCase(strkkk)) { // 版本号相同
			if (bNewVersion == 0) {
				strkkk = "当前版本: V" + getVersion() + "      大小: " + strFileSize + " MB";
				((TextView) findViewById(R.id.cur_version_no)).setTextColor(Color.rgb(0xF6, 0xB8, 0x00)); // 海创黄色
			} else {
				strkkk = "已是最新版本: V" + getVersion() + "      大小: " + strFileSize + " MB";
				((TextView) findViewById(R.id.cur_version_no)).setTextColor(Color.RED); //
			}
			bRetNewVersion = false;
		} else {
			strkkk = "发现新版本: V" + mstrUpdateInfoVersion + "      大小: " + strFileSize + " MB";
			((TextView) findViewById(R.id.cur_version_no)).setTextColor(Color.RED);
			bRetNewVersion = true;
		}
		((TextView) findViewById(R.id.cur_version_no)).setText(strkkk);

		bFindNewVersionFlag = bRetNewVersion;
		return bRetNewVersion = true;
	}

	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "-.--";
	}

	private void initProgressBar() {
		mProgressBar = (ProgressBar) findViewById(R.id.check_update_progressBar);
		mProgressBar.setIndeterminate(false);
		mProgressBar.setMax(100);
		mProgressBar.setProgress(0);

		mPercent.setText("0 %");
	}

	public void WiFiUpdate(View View) {
		isWifiConnect = false;
		isGPRSConnect = false;
		// 创建 net_wifi_sel界面，通过 sendintent 来传递操作wifi的信息
		Intent intent = new Intent(ui_net_soft_update.this, ui_net_wifi_sel.class);
		startActivity(intent);
		return;
	}

	public void IntentWifiSoftUpdate() {
		((Button) findViewById(R.id.btn_wifi_update)).setTextColor(Color.GRAY);
		((Button) findViewById(R.id.btn_gprs_update)).setTextColor(Color.GRAY);
		// ((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.GRAY);
		((Button) findViewById(R.id.btn_wifi_update)).setClickable(false);
		((Button) findViewById(R.id.btn_gprs_update)).setClickable(false);
		// ((Button) findViewById(R.id.btn_cancel)).setClickable(false);

		((TextView) findViewById(R.id.cur_version_no)).setTextColor(Color.rgb(0xF6, 0xB8, 0x00)); // 海创黄色
		((TextView) findViewById(R.id.cur_version_no)).setText("网络连接中...");

		initProgressBar();

		Check2UpdateSoft();
	}

	public void GprsUpdate(View View) {

		((Button) findViewById(R.id.btn_wifi_update)).setTextColor(Color.GRAY);
		((Button) findViewById(R.id.btn_gprs_update)).setTextColor(Color.GRAY);
		((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.GRAY);
		((Button) findViewById(R.id.btn_wifi_update)).setClickable(false);
		((Button) findViewById(R.id.btn_gprs_update)).setClickable(false);
		((Button) findViewById(R.id.btn_cancel)).setClickable(false);

		((TextView) findViewById(R.id.cur_version_no)).setTextColor(Color.rgb(0xF6, 0xB8, 0x00)); // 海创黄色
		((TextView) findViewById(R.id.cur_version_no)).setText("网络连接中...");

		isGPRSConnect = false;
		isWifiConnect = false;

		// final WifiManager wifiManager = (WifiManager)
		// getSystemService(WIFI_SERVICE);
		// wifiManager.setWifiEnabled(false);
		mclsWifi.CloseWifi();

		initProgressBar();

		// 判断 GPRS 是否打开
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (networkInfo != null) {
			State GPRSState = networkInfo.getState();
			if (GPRSState == State.CONNECTED) {
				isGPRSConnect = true;
				isWifiConnect = false;
			}
		}

		if (isGPRSConnect == false) {
			// 使能 GPRS 网络
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			Method setMobileDataEnabl;
			try {
				setMobileDataEnabl = cm.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
				setMobileDataEnabl.invoke(cm, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Check2UpdateSoft();
	}

	void Check2UpdateSoft() {
		final clasNetConfig con = new clasNetConfig();
		new Thread() {
			public void run() {
				for (int i = 0; i < 250; i++) { // 等待 GPRS 打开时间是 50秒超时
					Log.i("main", "isWifiConnect = " + isWifiConnect + "/isGPRSConnect = " + isGPRSConnect);
					
					if (isGPRSConnect == true) {
						break;
					}
					if (isWifiConnect == true) {
						break;
					}
					handler.sendEmptyMessage(HANDEL_CHECK_WIFI_STATUS);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (isGPRSConnect == false) {
					if (isWifiConnect == false) {
						handler.sendEmptyMessage(HANDEL_ERROR);
						return;
					}
				}

				if (isWifiConnect == true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				iDownloadFileStatus = 0;
				bCurDownLoadApkFile = false;
				// con.strServerPath =
				// "UpdateFile\\PhoneUpdate\\HCDT5X\\UpdateInfo.txt";
				con.strServerPath = PathUtils.NET_SOFT_UPDATE_PATH + "UpdateInfo.txt";
				con.strClientFilePath = PathUtils.FILE_PATH + "/" + "UpdateInfo.txt";
				con.strDevID = AppDatPara.sysPara.strDevRegistSN;
				con.strDevVer = getVersion();
				clasNetOp.DownLoadFile(con, new interfaceNetOpListener() {
					@Override
					public void onSuccess() {
						handler.sendEmptyMessage(HANDLE_SUCESS);
					}

					@Override
					public void onLoading(long total, long current) {
						// Message Handle_ms =Message.obtain();
						// Handle_ms.what = HANDLE_LOADING;
						// Handle_ms.arg1 = (int) total;
						// Handle_ms.arg2 = (int) current;
						// handler.sendMessage(Handle_ms);
					}

					@Override
					public void onFailure(String strErr) {
						handler.sendEmptyMessage(HANDEL_ERROR);
					}
				});

				for (int i = 0; i < 200; i++) { // 等待 GPRS 打开时间是 15秒超时
					if (iDownloadFileStatus != 0) {
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (iDownloadFileStatus < 1) { // 接收错误，或者没有接收到文件，这退出
					return;
				}

				if (bFindNewVersionFlag == false) { // 不是新版本，则退出

					return;
				}

				bCurDownLoadApkFile = true;
				con.strServerPath = PathUtils.NET_SOFT_UPDATE_PATH + mstrUpdateApkName;
				con.strClientFilePath = PathUtils.FILE_PATH + "/" + mstrUpdateApkName;
				clasNetOp.DownLoadFile(con, new interfaceNetOpListener() {
					@Override
					public void onSuccess() {
						handler.sendEmptyMessage(HANDLE_SUCESS);
					}

					@Override
					public void onLoading(long total, long current) {
						Message Handle_ms = Message.obtain();
						Handle_ms.what = HANDLE_LOADING;
						Handle_ms.arg1 = (int) total;
						Handle_ms.arg2 = (int) current;
						handler.sendMessage(Handle_ms);
					}

					@Override
					public void onFailure(String strErr) {
						// Log.i("main", "onFailure = " + strErr);
						handler.sendEmptyMessage(HANDEL_ERROR);
					}
				});

			};
		}.start();
	}

	public void BtnReturn(View View) {

//		AppDatPara.m_SysPara.iEnableWifiSampleDev = 1;
		ui_net_soft_update.this.finish(); // 关闭 当前 activity
	}

	protected void onDestroy() { // 按对话框以外区域时， 销毁函数
//		AppDatPara.m_SysPara.iEnableWifiSampleDev = 1;
		// Log.i("ui_paraset_soft_update", "onDestroy");
		unregisterReceiver(netReceiver);
		super.onDestroy();
	}
}
