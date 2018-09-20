package com.ck.dlg;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hc.u8x_ck.R;

public class DLG_Alert extends Dialog {

	private Context mContext = null;
	private String m_strTitle = ""; // Dialog 标题
	private String m_strMessage = ""; // 输入框提示
	private String m_strGjDirNow = ""; // 之前路径

	private TextView m_tv_title = null;
	private TextView m_tv_message_value = null;
	private Button m_bt_yes = null;
	private Button m_bt_cancel = null;

	public DLG_Alert(Context context, String strTitle, String strMessage) {
		super(context);
		this.mContext = context;
		this.m_strTitle = strTitle;
		this.m_strMessage = strMessage;
	}

	protected void onCreate(Bundle savedInstanceState) {
		// 隐去标题栏（应用程序的名字）
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 隐去状态栏部分(电池等图标和一切修饰部分)
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setCanceledOnTouchOutside(false);
		setContentView(R.layout.dlg_alert);
		super.onCreate(savedInstanceState);
		InitView();
	};

	private void InitView() {
		m_tv_title = (TextView) findViewById(R.id.tv_title);
		if (null != m_tv_title) {
			m_tv_title.setText(" " + m_strTitle);
		}
		m_tv_message_value = (TextView) findViewById(R.id.tv_message_value);
		if (null != m_tv_message_value) {
			m_tv_message_value.setText("" + m_strMessage);
		}
		m_bt_yes = (Button) findViewById(R.id.bt_yes);
		m_bt_cancel = (Button) findViewById(R.id.bt_cancel);
		m_bt_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancel();
			}
		});
	}

	public void setBtnOKOnClick(View.OnClickListener clickListener) {
		if (clickListener != null){
			m_bt_yes.setOnClickListener(clickListener);
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
}
