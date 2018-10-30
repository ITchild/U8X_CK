package com.ck.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ck.activity_key.KeyCollectActivity;
import com.ck.adapter.FileListGJAdapter;
import com.ck.adapter.FileListProjectAdapter;
import com.ck.base.TitleBaseActivity;
import com.ck.collect.DLG_FileProgress;
import com.ck.dlg.DLG_Alert;
import com.ck.dlg.ShowPicDialog;
import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.utils.BroadcastAction;
import com.ck.utils.FileUtil;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fei
 * 为文件管理的Activity
 */

public class FileBowerActivity extends TitleBaseActivity implements View.OnClickListener {

    protected List<ClasFileProjectInfo> proData;
    private TextView fileBower_objName_tv;
    private TextView fileBower_gjName_tv;
    protected ClasFileProjectInfo fileData;
    private ListView fileBower_proList_lv;
    private FileListProjectAdapter mProjectAdapter;
    private ListView fileBower_fileList_lv;
    private FileListGJAdapter mGJAdapter;
    private Button fileBower_select_bt;
    private Button fileBower_toUsb_bt;
    private Button fileBower_del_bt;
    private Button fileBower_back_bt;
    private ShowPicDialog picDialog;
    private DLG_FileProgress m_ProgressDialog;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.UpdataProgress)) {
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
                    DLG_Alert alert = new DLG_Alert(FileBowerActivity.this,
                            getStr(R.string.str_ToUError), getStr(R.string.str_pleaseOnHand));
                    alert.show();
                }
            }
        }
    };

    @Override
    protected int initLayout() {
        return R.layout.ac_filebower;
    }

    @Override
    protected boolean isBackshow() {
        return false;
    }

    @Override
    protected void initView() {
        super.initView();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.UpdataProgress);
        registerReceiver(mReceiver, filter);
        fileBower_objName_tv = findView(R.id.fileBower_objName_tv);
        fileBower_gjName_tv = findView(R.id.fileBower_gjName_tv);
        fileBower_select_bt = findView(R.id.fileBower_select_bt);
        fileBower_toUsb_bt = findView(R.id.fileBower_toUsb_bt);
        fileBower_del_bt = findView(R.id.fileBower_del_bt);
        fileBower_back_bt = findView(R.id.fileBower_back_bt);
        fileBower_proList_lv = findView(R.id.fileBower_proList_lv);
        fileBower_fileList_lv = findView(R.id.fileBower_fileList_lv);
        if (null == proData) {
            proData = new ArrayList<>();
        }
        mProjectAdapter = new FileListProjectAdapter(this, proData);
        fileBower_proList_lv.setAdapter(mProjectAdapter);
        if (null == fileData) {
            fileData = new ClasFileProjectInfo();
        }
        mGJAdapter = new FileListGJAdapter(this, fileData);
        fileBower_fileList_lv.setAdapter(mGJAdapter);
        picDialog = new ShowPicDialog(this);
    }

    @Override
    protected void initData() {
        super.initData();
        refreshProListData(0);
        refreshFileListData(0, -1);
    }

    /**
     * 刷新工程列表的数据
     */
    private void refreshProListData(int position) {
        proData = PathUtils.getProFileList();
        if (null != proData && proData.size() > position) {
            fileBower_objName_tv.setText("工程名称("+proData.size()+")");
            mProjectAdapter.setData(proData, position);
        }
    }

    /**
     * 刷新文件列表数据
     */
    private void refreshFileListData(int position, int backgroundPosition) {
        if (null != proData && proData.size() > 0 && proData.size() > position) {
            fileData = proData.get(position);
        }
        fileBower_gjName_tv.setText("构件名称("+fileData.mstrArrFileGJ.size()+")");
        mGJAdapter.setData(fileData, backgroundPosition);
    }

    @Override
    protected void initListener() {
        super.initListener();
        fileBower_select_bt.setOnClickListener(this);
        fileBower_toUsb_bt.setOnClickListener(this);
        fileBower_del_bt.setOnClickListener(this);
        fileBower_back_bt.setOnClickListener(this);
        //TODO : 工程列表的监听
        mProjectAdapter.setOnFileProItemClick(new FileListProjectAdapter.OnFileProItemClick() {
            @Override
            public void onClickIsChoice(boolean isChoice, int position) {
                if (isChoice) {//是否进行勾选
                    choiceObjList(position);
                } else {//点击一整项
                    clickObjList(position);
                }
            }
        });
        //TODO : 文件列表的监听
        mGJAdapter.setOnFileGJItemClick(new FileListGJAdapter.OnFileGJItemClick() {
            @Override
            public void onGJSelect(boolean isSelect, int position) {
                if (isSelect) {
                    choiceFileList(position);
                } else {//点击一整项
                    cilickFileObjList(position, true);
                }
            }
        });
    }

    /**
     * 工程列表的点击（非选中）
     *
     * @param position
     */
    protected void clickObjList(int position) {
        mProjectAdapter.setSelect(position);
        refreshFileListData(position, -1);
    }

    /**
     * 工程列表的选中
     *
     * @param position
     */
    protected void choiceObjList(int position) {
        int choiceState = proData.get(position).nIsSelect;
        if (choiceState == 0 || choiceState == 1) {//进行完全选中
            optFileListChoice(position, true);
        } else {//完全不选中
            optFileListChoice(position, false);
        }
        mProjectAdapter.setSelect(position);
        refreshFileListData(position, mGJAdapter.getSelect());
        isSelectAllObj();//改变全选按钮的提示
    }

    /**
     * 文件列表的点击事件
     *
     * @param position
     * @param isShowPic
     */
    protected void cilickFileObjList(int position, boolean isShowPic) {
        mGJAdapter.setSelect(position);
//        if (null != picDialog && !picDialog.isShowing() && isShowPic) {
//            String path = PathUtils.PROJECT_PATH + File.separator
//                    + proData.get(mProjectAdapter.getSelect()).mFileProjectName
//                    + File.separator + fileData.mstrArrFileGJ.get(position).mFileGJName;
//            Bitmap bmp = BitmapFactory.decodeFile(path);
//            picDialog.show();
//            picDialog.showPicBmp(bmp);
//        }
        Intent intent = new Intent(this,KeyCollectActivity.class);
        intent.putExtra("objName",proData.get(mProjectAdapter.getSelect()).mFileProjectName);
        intent.putExtra("gjName",fileData.mstrArrFileGJ.get(mGJAdapter.getSelect()).mFileGJName);
        startActivity(intent);
        finish();
    }

    protected void choiceFileList(int position) {
        int proPosition = mProjectAdapter.getSelect();
        fileData.mstrArrFileGJ.get(position).bIsSelect
                = !fileData.mstrArrFileGJ.get(position).bIsSelect;
        boolean isAllSelect = true;
        boolean isNoneSelect = true;
        for (ClasFileGJInfo gjInfo : fileData.mstrArrFileGJ) {
            if (!gjInfo.bIsSelect) {
                isAllSelect = false;
            }
            if (gjInfo.bIsSelect) {
                isNoneSelect = false;
            }
        }
        if (!isAllSelect && !isNoneSelect) {//部分被选中
            proData.get(proPosition).nIsSelect = 1;
        } else if (isAllSelect) {//全部被选中
            proData.get(proPosition).nIsSelect = 2;
        } else if (isNoneSelect) {//全部没有被选中
            proData.get(proPosition).nIsSelect = 0;
        }
        mProjectAdapter.notifyDataSetChanged();
        mGJAdapter.notifyDataSetChanged();
        isSelectAllObj();//改变全选按钮的提示
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fileBower_select_bt: // TODO : 全选 / 取消全选
                selectAllOrCancel();
                break;
            case R.id.fileBower_toUsb_bt: //TODO : 转到U盘中
                onSaveSDcard();
                break;
            case R.id.fileBower_del_bt://TODO : 删除选中项
                onDelete();
                break;
            case R.id.fileBower_back_bt: //TODO ： 返回
                finish();
                break;
        }
    }

    /**
     * 对某一工程目录下的文件列表进行多选操作
     *
     * @param proPosition
     * @param isSelect
     */
    private void optFileListChoice(int proPosition, boolean isSelect) {
        List<ClasFileGJInfo> listData = proData.get(proPosition).mstrArrFileGJ;
        for (ClasFileGJInfo info : listData) {
            info.bIsSelect = isSelect;
        }
        proData.get(proPosition).nIsSelect = isSelect ? 2 : 0;
    }

    /**
     * 判断是是否进行了全选
     * 主要用于改变全选按钮的提示
     */
    private void isSelectAllObj() {
        boolean isSelectAllObj = true;
        for (ClasFileProjectInfo projectInfo : proData) {
            if (projectInfo.nIsSelect != 2) {
                isSelectAllObj = false;
            }
        }
        fileBower_select_bt.setText(getStr(isSelectAllObj ?
                R.string.str_cancelAll : R.string.str_selectAll));
    }

    /**
     * 进行全选，以及取消全选
     */
    protected void selectAllOrCancel() {
        if (null == proData) {
            return;
        }
        boolean isSelect = false;
        if (fileBower_select_bt.getText().toString().equals(getStr(R.string.str_selectAll))) {
            isSelect = true;
        }
        for (int i = 0; i < proData.size(); i++) {
            optFileListChoice(i, isSelect);
        }
        mProjectAdapter.notifyDataSetChanged();
        mGJAdapter.notifyDataSetChanged();
        fileBower_select_bt.setText(isSelect ? getStr(R.string.str_cancelAll) : getStr(R.string.str_selectAll));
    }

    /**
     * {功能}是否删除文件的Dialog
     *
     * @return void
     * @throw
     */
    protected void onDelete() {
        if (!isHaveSeclect()) {
            return;
        }
        final DLG_Alert alert = new DLG_Alert(this,
                getStr(R.string.str_prompt), getStr(R.string.str_delMsg));
        alert.show();
        alert.setBtnOKOnClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delSelectFile();
                alert.dismiss();
            }
        });
    }

    /**
     * 删除选中文件的具体方法
     */
    private void delSelectFile() {
        FileUtil.delSeleceFile(proData);
        refreshProListData(0);
        refreshFileListData(0, -1);
    }

    /**
     * 将文件转存的U盘
     */
    protected void onSaveSDcard() {
        if (!isHaveSeclect()) {//判断是否有文件被选中
            return;
        }
        copyFile();
    }

    // 拷贝选中文件
    private void copyFile() {
        if (AppDatPara.GetExternalStorageDirectory() == null) {
            showMsgCon(getStr(R.string.str_NoneUPan));
            return;
        }
        final String targetDir = AppDatPara.GetExternalStorageDirectory() + File.separator + "测宽数据";
        Boolean canWrite = FileUtil.canWrite(new File(targetDir));
        if (!canWrite) {
            showMsgCon(getStr(R.string.str_System_Low));
            return;
        }
        if (null != AppDatPara.GetExternalStorageDirectory()) {
            File targetFile = new File(targetDir);
            targetFile.mkdirs();
            // 将文件大小归0
            FileUtil.getInstance().fileSize = 0;
            // 如果存在选中转U盘状态,显示进度条
            if (new File(targetDir).exists()) {
                if (targetFile.exists() && targetFile.canRead() && targetFile.canWrite()) {
                    copyFileToUPan(targetDir);
                } else {
                    sendBroadcast(new Intent(BroadcastAction.UpdataProgress).putExtra("ProgressValue", -1));
                }
            }
        } else {
            showToast(getStr(R.string.str_pleaseUseU));
        }
    }

    private void copyFileToUPan(final String targetDir) {
        // 获取拷贝文件总共大小值 ，将值传入拷贝文件中
        long lTotalfileSize = FileUtil.GetFileSize(proData);
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
                for (int i = 0; i < proData.size(); i++) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    if (proData.get(i).nIsSelect == 2) {
                        soDir = PathUtils.PROJECT_PATH + "/" + proData.get(i).mFileProjectName;
                        tarDir = targetDir + File.separator + proData.get(i).mFileProjectName;
                        try {
                            if (null != soDir && soDir.length() != 0 && null != tarDir && tarDir.length() != 0) {
                                // 拷贝文件
                                FileUtil.getInstance().copyDirectiory(soDir, tarDir, AppDatPara);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (proData.get(i).nIsSelect == 1) {
                        for (int j = 0; j < proData.get(i).mstrArrFileGJ.size(); j++) {
                            if (proData.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
                                targetPathF = new File(targetDir
                                        + File.separator + File.separator + proData.get(i).mFileProjectName);
                                sourceF = new File(PathUtils.PROJECT_PATH
                                        + "/" + proData.get(i).mFileProjectName
                                        + "/" + proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
                                targetF = new File(targetPathF.toString()
                                        + File.separator + proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
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
            }
        }.start();
    }

    private void showMsgCon(String str) {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        dialog.setTitle("提示");
        dialog.setMessage(str);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "我知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    /**
     * 判断是否有文件被选中
     *
     * @return
     */
    private boolean isHaveSeclect() {
        boolean bISSelect = false;
        for (int i = 0; i < proData.size(); i++) {
            if (proData.get(i).nIsSelect > 0) {
                bISSelect = true;
                break;
            }
        }
        if (!bISSelect) {
            showToast(getStr(R.string.str_noChice));
            return false;
        }
        return true;
    }

    /**
     * 设置底部按钮的按键选中状态
     *
     * @param focusPosition
     */
    protected void changeBottomBtView(int focusPosition) {
        switch (focusPosition) {
            case 2:
                fileBower_select_bt.setPressed(true);
                fileBower_toUsb_bt.setPressed(false);
                fileBower_del_bt.setPressed(false);
                fileBower_back_bt.setPressed(false);
                break;
            case 3:
                fileBower_select_bt.setPressed(false);
                fileBower_toUsb_bt.setPressed(true);
                fileBower_del_bt.setPressed(false);
                fileBower_back_bt.setPressed(false);
                break;
            case 4:
                fileBower_select_bt.setPressed(false);
                fileBower_toUsb_bt.setPressed(false);
                fileBower_del_bt.setPressed(true);
                fileBower_back_bt.setPressed(false);
                break;
            case 5:
                fileBower_select_bt.setPressed(false);
                fileBower_toUsb_bt.setPressed(false);
                fileBower_del_bt.setPressed(false);
                fileBower_back_bt.setPressed(true);
                break;
            default:
                fileBower_select_bt.setPressed(false);
                fileBower_toUsb_bt.setPressed(false);
                fileBower_del_bt.setPressed(false);
                fileBower_back_bt.setPressed(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
