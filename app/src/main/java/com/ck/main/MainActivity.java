package com.ck.main;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.ck.collect.Ui_Collect;
import com.ck.collect.Ui_FileSelete;
import com.ck.dlg.DLG_Alert;
import com.ck.netcloud.ui_net_soft_update;
import com.ck.utils.BroadcastAction;
import com.hc.u8x_ck.R;

public class MainActivity extends BaseActivity {
	public TextView m_tvYIQIDY;

	BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BroadcastAction.Reload)) {
				bIsReload = true;
			}

			if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
				Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (null != parcelableExtra) {
					NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
					State state = networkInfo.getState();
					boolean isConnected = state == State.CONNECTED;
					// true连接到WIFI false未连接或者正在连接
					if (isConnected) {
					} else {
					}
				}
			}
		}
	};
	boolean bIsReload = false;
	App_DataPara mApp;
	IntentFilter m_Filter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ui_main);
		mApp = (App_DataPara) getApplication();
		m_Filter = new IntentFilter();
		m_Filter.addAction(BroadcastAction.Reload);
		registerReceiver(mWifiReceiver, m_Filter);
		TextView versionTV = (TextView) findViewById(R.id.soft_version);
		versionTV.setText("软件版本号:" + getVersion());

		bIsReload = false;
		m_tvYIQIDY = (TextView) findViewById(R.id.yiqiDY);
	}

	/**
	 * 
	 * {功能}<"基桩检测"按钮的点击事件>
	 * 
	 * @throw
	 * @return void
	 */
	public void startCollect(View View) {
		Intent intent = new Intent(this, Ui_Collect.class);
		startActivity(intent);

	}

	/**
	 * 
	 * {功能}<"数据浏览"按钮的点击事件>
	 * 
	 * @throw
	 * @return void
	 */
	public void startBrowse(View View) {
		startActivity(new Intent(this, Ui_FileSelete.class));
	}

	public void onReturn(View View) {
		final DLG_Alert alert = new DLG_Alert(this, "提示信息", "确定要退出吗？");
		alert.show();
		alert.setBtnOKOnClick(new android.view.View.OnClickListener() {
			@Override
			public void onClick(android.view.View v) {
				App_DataPara app_DataPara = (App_DataPara) getApplication();
				app_DataPara.UnRegistDiskReceiver();
				MainActivity.this.finish();
				System.exit(0);
			}
		});

	}

	/**
	 * 
	 * {功能}<"参数设置"按钮的点击事件>
	 * 
	 * @throw
	 * @return void
	 */
	public void ParaSet(View View) {
		Intent intent = new Intent(this,ui_net_soft_update.class);
        startActivity(intent);  
	}

	@Override
	protected void onResume() {
		if (bIsReload) {
			bIsReload = false;
			reload();
		}
		super.onResume();
	}

	/**
	 * 
	 * {功能}<"退出"按钮的点击事件>
	 * 
	 * @throw
	 * @return void
	 */
	public void ExitProgram(View View) {
		Exitdialog();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			onReturn(null);
		}
		return false;
	}

	/**
	 * 
	 * {功能}<获取手机软件版本号>
	 * 
	 * @throw
	 * @return String 版本号
	 */
	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return "V" + version;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * {功能}<退出对话框>
	 * 
	 * @throw
	 * @return void
	 */
	protected void Exitdialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle(" 提示信息");
		dialog.setMessage("确定要退出吗？");
		dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				App_DataPara app_DataPara = (App_DataPara) getApplication();
				app_DataPara.UnRegistDiskReceiver();
				MainActivity.this.finish();
				System.exit(0);
			}
		});
		dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// 这里添加点击确定后的逻辑
			}
		});
		dialog.create().show();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mWifiReceiver);
		super.onDestroy();
	}

	public void ThreadSleep(long nTime) {
		try {
			Thread.sleep(nTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
