package com.ck.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ck.App_DataPara;
import com.ck.adapter.FileListGJAdapter;
import com.ck.adapter.FileListProjectAdapter;
import com.ck.base.TitleBaseActivity;
import com.ck.bean.MeasureDataBean;
import com.ck.collect.DLG_FileProgress;
import com.ck.db.DBService;
import com.ck.dlg.ChoiceSaveTypeDialog;
import com.ck.dlg.DLG_Alert;
import com.ck.dlg.LoadingDialog;
import com.ck.dlg.SigleBtMsgDialog;
import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.utils.BroadcastAction;
import com.ck.utils.Catition;
import com.ck.utils.FileUtil;
import com.ck.utils.PathUtils;
import com.ck.utils.Stringutil;
import com.google.gson.Gson;
import com.hc.u8x_ck.R;

import java.io.File;
import java.util.List;

/**
 * @author fei
 * 为文件管理的Activity
 */

public class FileBowerActivity extends TitleBaseActivity implements View.OnClickListener{

    protected ClasFileProjectInfo fileData;
    private RecyclerView fileBower_proList_rv;
    private FileListProjectAdapter mProjectAdapter;
    private RecyclerView fileBower_fileList_rv;
    private FileListGJAdapter mGJAdapter;
    private DLG_FileProgress m_ProgressDialog;

    private TextView fileBower_allChoice_tv;
    private LinearLayout fileBower_allChoice_ll;
    private TextView fileBower_del_tv;
    private TextView fileBower_toUPan_tv;

    private LoadingDialog loading ;

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
    protected void initView() {
        super.initView();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastAction.UpdataProgress);
        registerReceiver(mReceiver, filter);
        fileBower_allChoice_tv = findView(R.id.fileBower_allChoice_tv);
        fileBower_allChoice_ll = findView(R.id.fileBower_allChoice_ll);
        fileBower_proList_rv = findView(R.id.fileBower_proList_rv);
        fileBower_fileList_rv = findView(R.id.fileBower_fileList_rv);
        fileBower_del_tv = findView(R.id.fileBower_del_tv);
        fileBower_toUPan_tv = findView(R.id.fileBower_toUPan_tv);
        if(null == loading){
            loading = new LoadingDialog(this);
        }
        mProjectAdapter = new FileListProjectAdapter(this, App_DataPara.getApp().proData);
        fileBower_proList_rv.setLayoutManager(new LinearLayoutManager(this));
        fileBower_proList_rv.setAdapter(mProjectAdapter);
        if (null == fileData) {
            fileData = new ClasFileProjectInfo();
        }
        if (null != App_DataPara.getApp().proData && App_DataPara.getApp().proData.size() > 0) {
            fileData = App_DataPara.getApp().proData.get(0);
        }else {
            fileData = new ClasFileProjectInfo();
        }
        mGJAdapter = new FileListGJAdapter(this, fileData);
        fileBower_fileList_rv.setLayoutManager(new GridLayoutManager(this,3));
        fileBower_fileList_rv.setAdapter(mGJAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        baseTitle_title_tv.setText("文件管理");
        setFileDataBefor();
    }

    /**
     * 刷新文件列表数据
     */
    private void refreshFileListData(int position, int backgroundPosition) {
        if (null != App_DataPara.getApp().proData && App_DataPara.getApp().proData.size() > 0) {
            fileData = App_DataPara.getApp().proData.get(position);
        }else {
            fileData = new ClasFileProjectInfo();
        }
        mGJAdapter.setData(fileData, backgroundPosition);
        if(fileData.mstrArrFileGJ.size()>0) {
            fileBower_fileList_rv.scrollToPosition(0);
        }
    }

    /**
     * 开启子线程提前加载文件系统的列表
     */
    private void setFileDataBefor() {
        startLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                App_DataPara.getApp().proData = PathUtils.getProFileList();
                if (null != App_DataPara.getApp().proData) {
                    boolean flagRefresh = true;
                    for (ClasFileProjectInfo proInfo : App_DataPara.getApp().proData) {
                        for (ClasFileGJInfo gjInfo : proInfo.mstrArrFileGJ) {
                            String path = PathUtils.PROJECT_PATH + "/" + proInfo.mFileProjectName
                                    + "/" + gjInfo.mFileGJName + ".bmp";
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 15;

                            gjInfo.setSrc(BitmapFactory.decodeFile(path, options));
                            String json = FileUtil.readData(PathUtils.PROJECT_PATH + "/"
                                    + proInfo.mFileProjectName + "/" + gjInfo.mFileGJName + ".CK");
                            if (!Stringutil.isEmpty(json)) {
                                MeasureDataBean bean = new Gson().fromJson(json, MeasureDataBean.class);
                                gjInfo.setWidth(bean.getWidth() + "");
                            }
                            if(flagRefresh){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(null != mProjectAdapter) {
                                            mProjectAdapter.setData(App_DataPara.getApp().proData,0);
                                            mGJAdapter.setData(App_DataPara.getApp().proData.
                                                    get(mProjectAdapter.getSelect()),0);
                                            stopLoading();
                                        }
                                    }
                                });
                            }
                        }
                        flagRefresh = false;
                    }
                }
                stopLoading();
            }
        }).start();
    }

    @Override
    protected void initListener() {
        super.initListener();
        fileBower_allChoice_ll.setOnClickListener(this);
        fileBower_del_tv.setOnClickListener(this);
        fileBower_toUPan_tv.setOnClickListener(this);
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
                    cilickFileObjList(position);
                }
            }
        });
    }

    /**
     * 工程列表的点击（非选中）
     *
     * @param position
     */
    protected void clickObjList(final int position) {
        mProjectAdapter.setSelect(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshFileListData(position, -1);
                    }
                });
            }
        }).start();
    }

    /**
     * 工程列表的选中
     *
     * @param position
     */
    protected void choiceObjList(int position) {
        if(null == App_DataPara.getApp().proData || App_DataPara.getApp().proData.size() ==0){
            return;
        }
        int choiceState = App_DataPara.getApp().proData.get(position).nIsSelect;
        if (choiceState == 0 || choiceState == 1) {//进行完全选中
            optFileListChoice(position, true);
        } else {//完全不选中
            optFileListChoice(position, false);
        }
        mProjectAdapter.setSelect(position);
        refreshFileListData(position, mGJAdapter.getSelect());
        if(isSelectAllObj()){
            //改变全选按钮的提示
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_all_true));
        }else{
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_false));
        }
    }

    /**
     * 文件列表的点击事件
     * @param position
     */
    protected void cilickFileObjList(int position) {
        mGJAdapter.setSelect(position);
        Intent intent = new Intent(this,EditPicActivity.class);
        intent.putExtra("objPosition",mProjectAdapter.getSelect());
        intent.putExtra("gjPosition",mGJAdapter.getSelect());
        startActivityForResult(intent, Catition.FLIETURNTOEDIT);
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
            App_DataPara.getApp().proData.get(proPosition).nIsSelect = 1;
        } else if (isAllSelect) {//全部被选中
            App_DataPara.getApp().proData.get(proPosition).nIsSelect = 2;
        } else if (isNoneSelect) {//全部没有被选中
            App_DataPara.getApp().proData.get(proPosition).nIsSelect = 0;
        }
        mProjectAdapter.notifyDataSetChanged();
//        mGJAdapter.notifyDataSetChanged();
        if(isSelectAllObj()){
            //改变全选按钮的提示
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_all_true));
        }else{
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_false));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fileBower_allChoice_ll: // TODO : 全选 / 取消全选
                selectAllOrCancel();
                break;
            case R.id.fileBower_toUPan_tv: //TODO : 转到U盘中
                onSaveSDcard();
                break;
            case R.id.fileBower_del_tv://TODO : 删除选中项
                onDelete();
                break;
//            case R.id.fileBower_back_bt: //TODO ： 返回
//                finishActivity();
//                break;
        }
    }

    /**
     * 按键的监听
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_F1 ://切换按键，功能为删除
                onSaveSDcard();
                return true;
            case KeyEvent.KEYCODE_F2 : //存储按键,功能为转U盘
                onDelete();
                return true;
            case KeyEvent.KEYCODE_BACK ://返回键
                finishActivity();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP :
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_ENTER:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 对某一工程目录下的文件列表进行多选操作
     *
     * @param proPosition
     * @param isSelect
     */
    private void optFileListChoice(int proPosition, boolean isSelect) {
        List<ClasFileGJInfo> listData = App_DataPara.getApp().proData.get(proPosition).mstrArrFileGJ;
        for (ClasFileGJInfo info : listData) {
            info.bIsSelect = isSelect;
        }
        App_DataPara.getApp().proData.get(proPosition).nIsSelect = isSelect ? 2 : 0;
    }

    /**
     * 判断是是否进行了全选
     * 主要用于改变全选按钮的提示
     */
    private boolean isSelectAllObj() {
        boolean isSelectAllObj = true;
        for (ClasFileProjectInfo projectInfo : App_DataPara.getApp().proData) {
            if (projectInfo.nIsSelect != 2) {
                isSelectAllObj = false;
            }
        }
        return isSelectAllObj;
    }

    /**
     * 进行全选，以及取消全选
     */
    protected void selectAllOrCancel() {
        if (null == App_DataPara.getApp().proData) {
            return;
        }
        boolean isSelect = isSelectAllObj();
        for (int i = 0; i < App_DataPara.getApp().proData.size(); i++) {
            optFileListChoice(i, !isSelect);
        }
        if(!isSelect){
            //改变全选按钮的提示
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_all_true));
        }else{
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_false));
        }
        mProjectAdapter.setData(App_DataPara.getApp().proData,mProjectAdapter.getSelect());
        mGJAdapter.setData(App_DataPara.getApp().proData.get(mProjectAdapter.getSelect()),mGJAdapter.getSelect());
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
        FileUtil.delSeleceFile(App_DataPara.getApp().proData);
        int position = mProjectAdapter.getSelect();
        int allLength = App_DataPara.getApp().proData.size();
        Log.i("fei","总体长度"+allLength);
        if(allLength == 0){
            position = 0;
        }else {
            if (position >= allLength) {
                position = allLength - 1;
            }
        }
        mProjectAdapter.setData(App_DataPara.getApp().proData, position);
        refreshFileListData(0, -1);//刷新文件列表
    }

    /**
     * 删除数据中的文件
     */
    private void delDbMeasure(){
        for(ClasFileProjectInfo info : App_DataPara.getApp().proData){
            if(info.nIsSelect == 2){
                DBService.getInstence(this).delMeasureData(
                        info.mFileProjectName,null);
            }else if(info.nIsSelect == 1){
                List<ClasFileGJInfo> ArrFileGJ = info.mstrArrFileGJ;
                for (int j = 0; j < ArrFileGJ.size(); j++) {
                    ClasFileGJInfo clasFileGJInfo = ArrFileGJ.get(j);
                    if (clasFileGJInfo.bIsSelect) {
                        DBService.getInstence(this).delMeasureData(
                                info.mFileProjectName,clasFileGJInfo.mFileGJName);
                    }
                }
            }
        }
    }
    /**
     * 将文件转存的U盘
     */
    protected void onSaveSDcard() {
        if (!isHaveSeclect()) {//判断是否有文件被选中
            return;
        }
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
        final ChoiceSaveTypeDialog dialog = new ChoiceSaveTypeDialog(FileBowerActivity.this);
        dialog.show();
        dialog.setOnChoiceSaveClick(new ChoiceSaveTypeDialog.OnChoiceSaveClick() {
            @Override
            public void isOk(boolean isOk, int type) {
                if(isOk){
                    if(type == -1){
                        showToast("请选择导入U盘的数据类型");
                    }else{
                        dialog.dismiss();
                        copyFile(targetDir,type);
                    }
                }else{
                    dialog.dismiss();
                }
            }
        });
    }

    // 拷贝选中文件
    private void copyFile(String targetDir , int type) {
        if (null != AppDatPara.GetExternalStorageDirectory()) {
            File targetFile = new File(targetDir);
            targetFile.mkdirs();
            // 将文件大小归0
            FileUtil.getInstance().fileSize = 0;
            // 如果存在选中转U盘状态,显示进度条
            if (new File(targetDir).exists()) {
                if (targetFile.exists() && targetFile.canRead() && targetFile.canWrite()) {
                    copyFileToUPan(targetDir,type);
                } else {
                    sendBroadcast(new Intent(BroadcastAction.UpdataProgress).putExtra("ProgressValue", -1));
                }
            }
        } else {
            showToast(getStr(R.string.str_pleaseUseU));
        }
    }

    /***
     * 将文件保存到U盘
     * @param targetDir
     */
    private void copyFileToUPan(final String targetDir, final int type) {
        // 获取拷贝文件总共大小值 ，将值传入拷贝文件中
        long lTotalfileSize = FileUtil.GetFileSize(App_DataPara.getApp().proData,type);
        // 新建目标目录
        if (null == m_ProgressDialog || !m_ProgressDialog.isShowing()) {
            m_ProgressDialog = new DLG_FileProgress(this, lTotalfileSize);
            m_ProgressDialog.setCanceledOnTouchOutside(false);
            m_ProgressDialog.show();
            m_ProgressDialog.setProgressValue((long) 0);
        }
        new Thread() {
            public void run() {
                FileUtil.copyFileInThread(targetDir,type,AppDatPara);
            }
        }.start();
    }

    private void showMsgCon(String str) {
        final SigleBtMsgDialog alert = new SigleBtMsgDialog(this);
        alert.show();
        alert.setTitleMsg(getStr(R.string.str_prompt));
        alert.setMsg(str);
        alert.setBtTxt("我知道了");
        alert.setOnBtClickListener(new SigleBtMsgDialog.OnBtClickListener() {
            @Override
            public void onBtClick() {
                alert.dismiss();
            }
        });
    }


    /**
     * 判断是否有文件被选中
     *
     * @return
     */
    private boolean isHaveSeclect() {
        boolean bISSelect = false;
        for (int i = 0; i < App_DataPara.getApp().proData.size(); i++) {
            if (App_DataPara.getApp().proData.get(i).nIsSelect > 0) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Catition.EDITBACKTOFILE && requestCode == Catition.FLIETURNTOEDIT){
            mProjectAdapter.notifyDataSetChanged();
            mGJAdapter.notifyDataSetChanged();
        }
    }

    private void stopLoading(){
        if(null != loading && loading.isShowing()){
            loading.dismiss();
        }
    }

    private void startLoading(){
        if(null != loading && !loading.isShowing()){
            loading.show();
        }
    }

    private void finishActivity(){
        App_DataPara.getApp().proData.clear();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
