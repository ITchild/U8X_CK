package com.ck.collect;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ck.dlg.DLG_Alert;
import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.main.App_DataPara;
import com.ck.main.BaseActivity;
import com.ck.utils.BroadcastAction;
import com.ck.utils.FileUtil;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Ui_FileSelete extends BaseActivity {
	private ListView m_LVProject;
	private ListView m_LVGJ;
	private List<ClasFileProjectInfo> m_ListProject;
	private ListProjectAdapter m_ProjectAdapter;
	private ListGJAdapter m_GJAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppDatPara = (App_DataPara) getApplicationContext();
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		AppDatPara.fDispDensity = dm.density;
		setContentView(R.layout.ui_file_selete);
		IntentFilter filter = new IntentFilter();
		filter.addAction(BroadcastAction.UpdataProgress);
		registerReceiver(mReceiver, filter);
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
	}

	private void initView() {
		m_ListProject = PathUtils.getProFileList();

		m_LVProject = (ListView) findViewById(R.id.ui_selete_folder);
		m_LVGJ = (ListView) findViewById(R.id.ui_selete_file);
		m_ProjectAdapter = new ListProjectAdapter(this, m_ListProject);
		m_LVProject.setAdapter(m_ProjectAdapter);
		m_ProjectAdapter.setSelect(AppDatPara.m_nProjectSeleteNidx);
		if (m_ListProject.size() == 0) {
			if (m_GJAdapter != null) {
				m_GJAdapter.clearProInfo();
			}
			return;
		}
		m_GJAdapter = new ListGJAdapter(this, m_ListProject.get(AppDatPara.m_nProjectSeleteNidx));
		m_LVGJ.setAdapter(m_GJAdapter);
		m_GJAdapter.setSelect(AppDatPara.m_nGJSeleteNidx);
		m_LVProject.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AppDatPara.m_nProjectSeleteNidx = position;
				m_GJAdapter = new ListGJAdapter(Ui_FileSelete.this, m_ListProject.get(position));
				m_LVGJ.setAdapter(m_GJAdapter);
				m_ProjectAdapter.setSelect(position);
			}
		});
		m_LVGJ.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (m_ReadDialog != null && m_ReadDialog.isShowing()) {
					return;
				}
				AppDatPara.m_nGJSeleteNidx = position;
				m_GJAdapter.setSelect(position);
				Ui_FileSelete.this.finish();
			}
		});
	}

	ProgressDialog m_ReadDialog;
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				// m_ReadDialog.dismiss();
				// //读取数据后初始化管道数量
				// AppDatPara.initPipeNOIdx();
				// Intent intent = new Intent(Ui_FileSelete.this,
				// Ui_Collect.class);
				// intent.putExtra("TYPE", ClasInfo.TYPE_BROWSE);
				// startActivity(intent);
				break;
			}
		};
	};

	/**
	 * 
	 * {功能}<删除>
	 * 
	 * @throw
	 * @return void
	 */
	public void onDelete(View view) {
		boolean bISSelect = false;
		for (int i = 0; i < m_ListProject.size(); i++) {
			if (m_ListProject.get(i).nIsSelect > 0) {
				bISSelect = true;
				break;
			}
		}
		if (!bISSelect) {
			Toast.makeText(this, "没有选中项", 0).show();
			return;
		}

		final DLG_Alert alert = new DLG_Alert(this, "提示信息", "确定要删除选中的工程或者构件吗？");
		alert.show();
		alert.setBtnOKOnClick(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				for (int i = 0; i < m_ListProject.size(); i++) {
					if (m_ListProject.get(i).nIsSelect == 1) {
						List<ClasFileGJInfo> ArrFileGJ = m_ListProject.get(i).mstrArrFileGJ;
						for (int j = 0; j < ArrFileGJ.size(); j++) {
							ClasFileGJInfo clasFileGJInfo = ArrFileGJ.get(j);
							if (clasFileGJInfo.bIsSelect) {
								String path = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName + "/" + clasFileGJInfo.mFileGJName;
								File file = new File(path);
								file.delete();
							}
						}

					}
					if (m_ListProject.get(i).nIsSelect == 2) {
						List<ClasFileGJInfo> ArrFileGJ = m_ListProject.get(i).mstrArrFileGJ;
						for (int j = 0; j < ArrFileGJ.size(); j++) {
							ClasFileGJInfo clasFileGJInfo = ArrFileGJ.get(j);
							if (clasFileGJInfo.bIsSelect) {
								String path = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName + "/" + clasFileGJInfo.mFileGJName;
								File file = new File(path);
								file.delete();
							}
						}
						String path2 = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
						File file = new File(path2);
						file.delete();
						AppDatPara.m_nProjectSeleteNidx = 0;
					}
				}
				AppDatPara.m_nGJSeleteNidx = 0;
				initView();
				alert.dismiss();
			}
		});
	}

	/**
	 * 
	 * {功能}<全选>
	 * 
	 * @throw
	 * @return void
	 */
	public void onAllSelect(View view) {
		Button btn = (Button) view;
		boolean bISProSelect = true;
		if (btn.getText().equals("全选")) {
			btn.setText("反选");
			bISProSelect = true;
		} else if (btn.getText().equals("反选")) {
			btn.setText("全选");
			bISProSelect = false;
		}
		boolean bISGJSelect = true;
		for (int i = 0; i < m_ListProject.size(); i++) {
			if (bISProSelect) {
				m_ListProject.get(i).nIsSelect = 2;
				bISGJSelect = true;
			} else {
				m_ListProject.get(i).nIsSelect = 0;
				bISGJSelect = false;
			}
			for (int j = 0; j < m_ListProject.get(i).mstrArrFileGJ.size(); j++) {
				m_ListProject.get(i).mstrArrFileGJ.get(j).bIsSelect = bISGJSelect;
			}
		}
		if (m_ProjectAdapter != null)
			m_ProjectAdapter.notifyDataSetChanged();
		if (m_GJAdapter != null)
			m_GJAdapter.notifyDataSetChanged();
	}

	public void initSelectFlag() {
		Button btnSelect = (Button) findViewById(R.id.btn_select);
		int nAllSelect = 0;
		int nNoSelect = 0;
		
		for (int i = 0; i < m_ListProject.size(); i++) {
			//2完全选中
			if(m_ListProject.get(i).nIsSelect == 2){
				nAllSelect ++;
			}
			//0未选中
			if(m_ListProject.get(i).nIsSelect == 0){
				nNoSelect++;
			}
		}
		if(nAllSelect == m_ListProject.size()){
			btnSelect.setText("反选");
		}
		if(nNoSelect == m_ListProject.size()){
			btnSelect.setText("全选");
		}
	}

	public void onSaveSDcard(View view) {
		boolean bISSelect = false;
		for (int i = 0; i < m_ListProject.size(); i++) {
			if (m_ListProject.get(i).nIsSelect > 0) {
				bISSelect = true;
				break;
			}
		}
		if (!bISSelect) {
			Toast.makeText(this, "没有选中项", 0).show();
			return;
		}
		CopyFile();
	}

	DLG_FileProgress m_ProgressDialog;
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(BroadcastAction.UpdataProgress)) {
				// Log.i(ClasAppDataPara.TAG,
				// "lProgressValue ="+lProgressValue);
				int lProgressValue = intent.getIntExtra("ProgressValue", 0);
				if (lProgressValue >= 0) {
					if (null != m_ProgressDialog && m_ProgressDialog.isShowing()) {
						m_ProgressDialog.setProgressValue((long) lProgressValue);
					}
				} else {
					if (null != m_ProgressDialog && m_ProgressDialog.isShowing()) {
						m_ProgressDialog.dismiss();
					}
					// 转U盘出现异常
					DLG_Alert alert = new DLG_Alert(Ui_FileSelete.this, "转U盘失败", "请手动拷贝文件。");
					alert.show();
				}
			}
		}
	};

	// 拷贝选中文件
	private void CopyFile() {
		if(AppDatPara.GetExternalStorageDirectory() == null){
			Toast.makeText(this, "未检测到U盘", 0).show();
			return;
		}
		final String targetDir = AppDatPara.GetExternalStorageDirectory() + File.separator + "测宽数据";
		Boolean canWrite = FileUtil.canWrite(new File(targetDir));
		if(!canWrite){
			Toast.makeText(this, "该安卓版本无法拷贝文件，请手动复制", 0).show();
			return;
		}
		if (null != AppDatPara.GetExternalStorageDirectory()) {
			File targetFile = new File(targetDir);
			targetFile.mkdirs();
			// 将文件大小归0
			FileUtil.getInstance().fileSize = 0;
			// 获取拷贝文件总共大小值 ，将值传入拷贝文件中
			long lTotalfileSize = GetFileSize();
			// 如果存在选中转U盘状态,显示进度条
			if (new File(targetDir).exists()) {
				if (targetFile.exists() && targetFile.canRead() && targetFile.canWrite()) {
					
				// 新建目标目录
				if (null == m_ProgressDialog || !m_ProgressDialog.isShowing()) {
					m_ProgressDialog = new DLG_FileProgress(this, lTotalfileSize);
					m_ProgressDialog.setCanceledOnTouchOutside(false);
					m_ProgressDialog.show();
					m_ProgressDialog.setProgressValue((long) 0);
				}
				new Thread() {
					public void run() {
						File sourceF = null;
						File targetPathF = null;
						File targetF = null;
						String soDir = null;
						String tarDir = null;
						for (int i = 0; i < m_ListProject.size(); i++) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							if (m_ListProject.get(i).nIsSelect == 2) {
								soDir = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
								tarDir = targetDir + File.separator + m_ListProject.get(i).mFileProjectName;
								try {
									if (null != soDir && soDir.length() != 0 && null != tarDir && tarDir.length() != 0) {
										// 拷贝文件
										FileUtil.getInstance().copyDirectiory(soDir, tarDir, AppDatPara);
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							} else if (m_ListProject.get(i).nIsSelect == 1) {
								for (int j = 0; j < m_ListProject.get(i).mstrArrFileGJ.size(); j++) {
									if (m_ListProject.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
										targetPathF = new File(targetDir + File.separator + File.separator + m_ListProject.get(i).mFileProjectName);
										sourceF = new File(PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName + "/" + m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName);
										targetF = new File(targetPathF.toString() + File.separator + m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName);
										if (null != sourceF && null != targetF) {
											if (targetPathF.exists()) {
												// 拷贝文件
												FileUtil.getInstance().copyFile(sourceF, targetF, AppDatPara);
											} else {
												// 新建目标目录
												(targetPathF).mkdirs();
												// 拷贝文件
												FileUtil.getInstance().copyFile(sourceF, targetF, AppDatPara);
											}
										}
									}
								}
							}
						}
					};
				}.start();
				} else {
//					Toast.makeText(this, "发送广播", 0).show();
					sendBroadcast(new Intent(BroadcastAction.UpdataProgress).putExtra("ProgressValue", -1));
				}

			}
		} else {
			Toast.makeText(this, "请插入U盘", 0).show();
		}
	}

	// 获取所有构件文件大小
	private long GetFileSize() {
		long lProFileSize = 0;
		long lGjFileSize = 0;
		for (int i = 0; i < m_ListProject.size(); i++) {
			if (m_ListProject.get(i).nIsSelect == 2) {
				// isExistSelect = true;
				String soDir = PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName;
				lProFileSize += FileUtil.getInstance().getFileSize(new File(soDir));
			} else if (m_ListProject.get(i).nIsSelect == 1) {
				// isExistSelect = true;
				for (int j = 0; j < m_ListProject.get(i).mstrArrFileGJ.size(); j++) {
					if (m_ListProject.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
						File sourceF = new File(PathUtils.PROJECT_PATH + "/" + m_ListProject.get(i).mFileProjectName + "/" + m_ListProject.get(i).mstrArrFileGJ.get(j).mFileGJName);
						lGjFileSize += FileUtil.getInstance().getFileSizes(sourceF);
					}
				}
			}
		}
		FileUtil.getInstance().m_lTotalfileSize = lProFileSize + lGjFileSize;
		return lProFileSize + lGjFileSize;
	}

	public void onReturn(View view) {
		this.finish();
	}

	@Override
	protected void onDestroy() {
		this.finish();
		if (mReceiver != null)
			unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	class ListProjectAdapter extends BaseAdapter {
		private Context mContext;
		public static final int File = 0;
		public static final int Folder = 0;
		private List<ClasFileProjectInfo> mProjects;

		public ListProjectAdapter(Context context, List<ClasFileProjectInfo> projects) {
			mContext = context;
			mProjects = projects;
		}

		private int nSelect;

		public void setSelect(int nSelect) {
			this.nSelect = nSelect;
			this.notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mProjects.size();
		}

		@Override
		public Object getItem(int position) {
			return mProjects.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		ViewHolder holder;

		@Override
		public View getView(final int position, View view, ViewGroup arg2) {
			if (view == null) {
				view = View.inflate(mContext, R.layout.ui_file_select_list_project, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			holder.mCBNidx.setText(" " + (position + 1));
			holder.mCBNidx.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					boolean flag = true;
					if (mProjects.get(position).nIsSelect == 1 || mProjects.get(position).nIsSelect == 2) {
						mProjects.get(position).nIsSelect = 0;
						flag = false;
					} else {
						mProjects.get(position).nIsSelect = 2;
						flag = true;
					}
					for (int i = 0; i < mProjects.get(position).mstrArrFileGJ.size(); i++) {
						mProjects.get(position).mstrArrFileGJ.get(i).bIsSelect = flag;
					}
					if (AppDatPara.m_nProjectSeleteNidx == position) {
						if (mProjects.get(position).nIsSelect == 0)
							flag = false;
						if (mProjects.get(position).nIsSelect == 2)
							flag = true;
						m_GJAdapter.initSelect(flag);
					}
					AppDatPara.m_nProjectSeleteNidx = position;
					nSelect = position;
					m_GJAdapter = new ListGJAdapter(Ui_FileSelete.this, m_ListProject.get(position));
					m_LVGJ.setAdapter(m_GJAdapter);
					notifyDataSetChanged();
					initSelectFlag();
				}
			});
			if (mProjects.get(position).nIsSelect == 2) {
				holder.mCBNidx.setButtonDrawable(R.drawable.checkbox_all_true);
			}
			if (mProjects.get(position).nIsSelect == 1) {
				holder.mCBNidx.setButtonDrawable(R.drawable.checkbox_half_true);
			}
			if (mProjects.get(position).nIsSelect == 0) {
				holder.mCBNidx.setButtonDrawable(R.drawable.checkbox_false);
			}
			if (nSelect == position) {
				holder.m_LL.setBackgroundColor(Color.rgb(246, 184, 00));
			} else {
				if (AppDatPara.nTheme == R.style.AppTheme_Black)
					holder.m_LL.setBackgroundColor(Color.BLACK);
				else
					holder.m_LL.setBackgroundColor(Color.WHITE);
			}
			holder.m_TVProject.setText(mProjects.get(position).mFileProjectName);
			holder.m_TVGJNum.setText(mProjects.get(position).mstrArrFileGJ.size() + "");
			holder.m_TVTime.setText(mProjects.get(position).mLastModifiedDate);
			return view;
		}

		class ViewHolder {
			CheckBox mCBNidx;
			TextView m_TVProject;
			TextView m_TVGJNum;
			TextView m_TVTime;
			LinearLayout m_LL;

			public ViewHolder(View view) {
				mCBNidx = (CheckBox) view.findViewById(R.id.project_checkboxID);
				m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
				m_TVGJNum = (TextView) view.findViewById(R.id.tv_gjNum);
				m_TVTime = (TextView) view.findViewById(R.id.tv_time);
				mCBNidx.setVisibility(View.VISIBLE);
				m_TVGJNum.setVisibility(View.VISIBLE);
				m_TVTime.setVisibility(View.VISIBLE);
				m_LL = (LinearLayout) view.findViewById(R.id.ui_list_project);
			}
		}
	}

	class ListGJAdapter extends BaseAdapter {
		private Context mContext;
		public static final int File = 0;
		public static final int Folder = 0;
		ClasFileProjectInfo mProject;

		public ListGJAdapter(Context context, ClasFileProjectInfo project) {
			mContext = context;
			mProject = project;
		}

		public void clearProInfo() {
			mProject.mstrArrFileGJ.clear();
			this.notifyDataSetChanged();
		}

		private int nSelect;

		public void setSelect(int nSelect) {
			this.nSelect = nSelect;
			this.notifyDataSetChanged();
		}

		public void initSelect(boolean flag) {
			for (int i = 0; i < mProject.mstrArrFileGJ.size(); i++) {
				mProject.mstrArrFileGJ.get(i).bIsSelect = flag;
			}
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mProject.mstrArrFileGJ.size();
		}

		@Override
		public Object getItem(int position) {
			return mProject.mstrArrFileGJ.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		ViewHolder holder;

		@Override
		public View getView(final int position, View view, ViewGroup arg2) {
			if (view == null) {
				view = View.inflate(mContext, R.layout.ui_file_select_list_gj, null);
				holder = new ViewHolder(view);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
			holder.mCBNidx.setText(" " + (position + 1));
			holder.mCBNidx.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mProject.mstrArrFileGJ.get(position).bIsSelect)
						mProject.mstrArrFileGJ.get(position).bIsSelect = false;
					else
						mProject.mstrArrFileGJ.get(position).bIsSelect = true;

					int nTrue = 0;
					int nFalse = 0;
					for (int i = 0; i < mProject.mstrArrFileGJ.size(); i++) {
						if (mProject.mstrArrFileGJ.get(i).bIsSelect)
							nTrue++;
						else
							nFalse++;
					}
					if (nTrue == mProject.mstrArrFileGJ.size()) {
						mProject.nIsSelect = 2;
					}
					if (nTrue > 0 && nFalse > 0) {
						mProject.nIsSelect = 1;
					}
					if (nFalse == mProject.mstrArrFileGJ.size()) {
						mProject.nIsSelect = 0;
					}
					notifyDataSetChanged();
					m_ProjectAdapter.notifyDataSetChanged();
					initSelectFlag();
				}
			});

			if (nSelect == position) {
				holder.m_LL.setBackgroundColor(Color.rgb(246, 184, 00));
			} else {
				if (AppDatPara.nTheme == R.style.AppTheme_Black)
					holder.m_LL.setBackgroundColor(Color.BLACK);
				else
					holder.m_LL.setBackgroundColor(Color.WHITE);
			}
			holder.mCBNidx.setChecked(mProject.mstrArrFileGJ.get(position).bIsSelect);
			holder.m_TVProject.setText(mProject.mstrArrFileGJ.get(position).mFileGJName.substring(0, mProject.mstrArrFileGJ.get(position).mFileGJName.length() - 4));
			holder.m_TVTime.setText("" + mProject.mstrArrFileGJ.get(position).mLastModifiedDate + "");

			return view;
		}

		class ViewHolder {
			CheckBox mCBNidx;
			TextView m_TVProject;
			TextView m_TVTime;
			LinearLayout m_LL;

			public ViewHolder(View view) {
				mCBNidx = (CheckBox) view.findViewById(R.id.project_checkboxID);
				m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
				m_TVTime = (TextView) view.findViewById(R.id.tv_time);
				mCBNidx.setVisibility(View.VISIBLE);
				m_TVTime.setVisibility(View.VISIBLE);
				m_LL = (LinearLayout) view.findViewById(R.id.ui_list_gj);
			}
		}
	}
}
