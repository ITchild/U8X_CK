package com.ck.collect;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.utils.PathUtils;
import com.ck.utils.PreferenceHelper;
import com.hc.u8x_ck.R;

import java.io.File;
import java.util.ArrayList;

public class DLG_SetPar extends Dialog implements android.view.View.OnClickListener {

	private Context mContext = null;
	private TextView m_tv_title = null;
	private Button m_bt_yes = null;
	private Button m_bt_no = null;
	private Button m_bt_cancel = null;
	private EditText m_et_Pro_name;
	private EditText m_et_gj_name = null;
	private String m_strTitle = ""; // Dialog 标题

	Ui_Collect m_Ui_Collect;

	// 保存提示框
	public DLG_SetPar(Context context, String strTitle) {
		super(context);
		this.mContext = context;
		this.m_strTitle = strTitle;
		m_Ui_Collect = (Ui_Collect) context;
	}

	protected void onCreate(Bundle savedInstanceState) {
		// 隐去标题栏（应用程序的名字）
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCanceledOnTouchOutside(false);
		setContentView(R.layout.dlg_save_file);
		super.onCreate(savedInstanceState);
		InitData();
		InitView();
	};

	// 初始化工程名、构件名信息
	private void InitData() {

	}

	private void InitView() {
		m_et_Pro_name = (EditText) findViewById(R.id.et_Pro_name);
		if (null != m_et_Pro_name) {
			m_et_Pro_name.setText("" + m_Ui_Collect.m_strSaveProName);
		}

		m_et_gj_name = (EditText) findViewById(R.id.et_gj_name);
		if (null != m_et_gj_name) {
			m_et_gj_name.setText("" + m_Ui_Collect.m_strSaveGJName);
		}
		m_tv_title = (TextView) findViewById(R.id.tv_title);
		if (null != m_tv_title) {
			m_tv_title.setText(" " + m_strTitle);
		}
		m_bt_yes = (Button) findViewById(R.id.bt_yes);
		if (null != m_bt_yes) {
			m_bt_yes.setOnClickListener(this);
		}
		m_bt_no = (Button) findViewById(R.id.bt_no);
		if (null != m_bt_no) {
			m_bt_no.setOnClickListener(this);
		}
		// 删除提示框，隐藏取消按钮
		m_bt_cancel = (Button) findViewById(R.id.bt_cancel);
		if (null != m_bt_cancel) {
			m_bt_cancel.setOnClickListener(this);
		}

		m_et_Pro_name.clearFocus();
		m_et_gj_name.clearFocus();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			this.cancel();
			return false;
		}
		return false;
	}

	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.bt_yes:
			m_Ui_Collect.m_strSaveProName = m_et_Pro_name.getText().toString().trim();
			m_Ui_Collect.m_strSaveGJName = m_et_gj_name.getText().toString().trim();
			if (m_Ui_Collect.m_strSaveProName.length() == 0 || m_Ui_Collect.m_strSaveGJName.length() == 0) {
				Toast.makeText(mContext, "不能为空", Toast.LENGTH_SHORT).show();
				return;
			}
			boolean bSame = false;
			ArrayList<ClasFileProjectInfo> proFileList = PathUtils.getProFileList();
			for (int i = 0; i < proFileList.size(); i++) {
				ClasFileProjectInfo proInfo = proFileList.get(i);
				if (!proInfo.mFileProjectName.equals(m_Ui_Collect.m_strSaveProName)) {
					continue;
				}
				for (int j = 0; j < proInfo.mstrArrFileGJ.size(); j++) {
					ClasFileGJInfo fjInfo = proInfo.mstrArrFileGJ.get(j);
					if (fjInfo.mFileGJName.equals(m_Ui_Collect.m_strSaveGJName + ".UP")) {
						bSame = true;
						break;
					}
				}
			}
			if (bSame) {
				Toast.makeText(mContext, "工程名已存在", Toast.LENGTH_SHORT).show();
				return;
			}
			File path = new File(PathUtils.PROJECT_PATH, m_Ui_Collect.m_strSaveProName);
			path.mkdirs();
			PreferenceHelper.setGJName(mContext, m_Ui_Collect.m_strSaveGJName);
			PreferenceHelper.setProName(mContext, m_Ui_Collect.m_strSaveProName);
			m_Ui_Collect.initView();
			this.dismiss();
			break;
		case R.id.bt_no:
			this.dismiss();
			break;

		case R.id.bt_cancel:
			this.dismiss();
			break;
		}
	}
}
