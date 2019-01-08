package com.ck.collect;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hc.u8x_ck.R;

import java.text.DecimalFormat;

@SuppressLint("HandlerLeak")
public class DLG_FileProgress extends Dialog implements android.view.View.OnClickListener {

	private Context mContext = null;
	private Button m_bt_yes = null;
	private ProgressBar m_Progress;
	private long mlTotalfileSize = 0;
	private DecimalFormat df;
	private TextView m_tv_progress_value;

	public DLG_FileProgress(Context context, long lTotalfileSize) {
		super(context);
		mContext = context;
		mlTotalfileSize = lTotalfileSize;
	}

	protected void onCreate(Bundle savedInstanceState) {
		// 隐去标题栏（应用程序的名字）
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐去状态栏部分(电池等图标和一切修饰部分)
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setCanceledOnTouchOutside(false);
		setContentView(R.layout.dlg_progress);
		super.onCreate(savedInstanceState);
		if (null == df) {
			df = new DecimalFormat("######0.00");
		}
		InitView();
	};

	private void InitView() {
		m_tv_progress_value = (TextView) findViewById(R.id.tv_progress_value);
		if (null != m_tv_progress_value) {
			m_tv_progress_value.setText("0%");
		}
		m_bt_yes = (Button) findViewById(R.id.bt_yes);
		if (null != m_bt_yes) {
			m_bt_yes.setEnabled(false);
			m_bt_yes.setOnClickListener(this);
		}
		m_Progress = (ProgressBar) findViewById(R.id.progressBar1);
		if (null != m_Progress) {
			m_Progress.setMax(100);
			m_Progress.setProgress(0);
			m_Progress.setIndeterminate(false);
		}
	}

	public void setProgressValue(Long progressValue) {
		int nValue = (int) (Float.valueOf(df.format((double) progressValue / (double) mlTotalfileSize)) * 100);
		m_Progress.setProgress(nValue);
		if (null != m_tv_progress_value) {
			m_tv_progress_value.setText("" + nValue + "%");
		}
		if (m_bt_yes != null && nValue == 100) {
			m_bt_yes.setEnabled(true);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.cancel();
			return false;
		}
		return false;
	}

	// 跳转到安全卸载U盘界面
	public void InfoDialog2Btn(String strInfo) {
		AlertDialog.Builder build = new AlertDialog.Builder(mContext);
		build.setTitle(" 提示");
		build.setIcon(android.R.drawable.ic_dialog_info);
		build.setMessage(strInfo);

		build.setPositiveButton("安全卸载", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_MEMORY_CARD_SETTINGS);
				mContext.startActivity(intent);
				dialog.dismiss();
			}
		});
		build.setNeutralButton("返回", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

			}
		});
		build.show();
	}

	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.bt_yes:
//			InfoDialog2Btn("转存U盘成功。\n\n建议先安全卸载U盘，再拔出。" +
//					"\n1.点击\"安全卸载\"进入\"存储\"界面。\n" +
//					"2.拖动\"存储\"界面找到\"卸载USB存储设备\"。\n" +
//					"3.点击\"卸载\"，\"确定\",并返回。");
			this.dismiss();
			break;
		}
	}
}
