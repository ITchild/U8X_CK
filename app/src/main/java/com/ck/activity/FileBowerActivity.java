package com.ck.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.ck.adapter.FileListGJAdapter;
import com.ck.adapter.FileListProjectAdapter;
import com.ck.base.TitleBaseActivity;
import com.ck.collect.DLG_FileProgress;
import com.ck.db.DBService;
import com.ck.dlg.DLG_Alert;
import com.ck.dlg.SigleBtMsgDialog;
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

public class FileBowerActivity extends TitleBaseActivity implements View.OnClickListener{

    protected List<ClasFileProjectInfo> proData;
    protected ClasFileProjectInfo fileData;
    private RecyclerView fileBower_proList_rv;
    private FileListProjectAdapter mProjectAdapter;
    private RecyclerView fileBower_fileList_rv;
    private FileListGJAdapter mGJAdapter;
    private DLG_FileProgress m_ProgressDialog;

    private TextView fileBower_del_tv;
    private TextView fileBower_toUPan_tv;

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
        fileBower_proList_rv = findView(R.id.fileBower_proList_rv);
        fileBower_fileList_rv = findView(R.id.fileBower_fileList_rv);
        fileBower_del_tv = findView(R.id.fileBower_del_tv);
        fileBower_toUPan_tv = findView(R.id.fileBower_toUPan_tv);
        if (null == proData) {
            proData = new ArrayList<>();
        }
        mProjectAdapter = new FileListProjectAdapter(this, proData);
        fileBower_proList_rv.setLayoutManager(new LinearLayoutManager(this));
        fileBower_proList_rv.setAdapter(mProjectAdapter);
        if (null == fileData) {
            fileData = new ClasFileProjectInfo();
        }
        mGJAdapter = new FileListGJAdapter(this, fileData);
        fileBower_fileList_rv.setLayoutManager(new GridLayoutManager(this,3));
        fileBower_fileList_rv.setAdapter(mGJAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        refreshProListData(0);
        refreshFileListData(0, -1);
        baseTitle_title_tv.setText("文件管理");
    }
    /**
     * 刷新工程列表的数据
     */
    private void refreshProListData(int position) {
        proData = PathUtils.getProFileList();
        mProjectAdapter.setData(proData, position);
    }

    /**
     * 刷新文件列表数据
     */
    private void refreshFileListData(int position, int backgroundPosition) {
        if (null != proData && proData.size() > 0 && proData.size() > position) {
            fileData = proData.get(position);
        }else {
            fileData = new ClasFileProjectInfo();
        }
        mGJAdapter.setData(fileData, backgroundPosition);
    }

    @Override
    protected void initListener() {
        super.initListener();
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
        if(null == proData || proData.size() ==0){
            return;
        }
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
        Intent intent = new Intent(this,CollectActivity.class);
        intent.putExtra("objName",proData.get(mProjectAdapter.getSelect()).mFileProjectName);
        intent.putExtra("gjName",fileData.mstrArrFileGJ.get(mGJAdapter.getSelect()).mFileGJName);
        startActivity(intent);
//        finish();
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
//            case R.id.fileBower_select_bt: // TODO : 全选 / 取消全选
//                selectAllOrCancel();
//                break;
            case R.id.fileBower_toUPan_tv: //TODO : 转到U盘中
                onSaveSDcard();
                break;
            case R.id.fileBower_del_tv://TODO : 删除选中项
                onDelete();
                break;
//            case R.id.fileBower_back_bt: //TODO ： 返回
//                finish();
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
                onDelete();
                return true;
            case KeyEvent.KEYCODE_F2 : //存储按键,功能为转U盘
                onSaveSDcard();
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
    private boolean isSelectAllObj() {
        boolean isSelectAllObj = true;
        for (ClasFileProjectInfo projectInfo : proData) {
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
        if (null == proData) {
            return;
        }
        boolean isSelect = isSelectAllObj();
        for (int i = 0; i < proData.size(); i++) {
            optFileListChoice(i, isSelect);
        }
        mProjectAdapter.notifyDataSetChanged();
        mGJAdapter.notifyDataSetChanged();
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
        FileUtil.delSeleceFile(proData);//删除本地文件
        delDbMeasure();//删除数据库中的文件
        refreshProListData(0);//刷新工程列表
        refreshFileListData(0, -1);//刷新文件列表
    }

    /**
     * 删除数据中的文件
     */
    private void delDbMeasure(){
        for(ClasFileProjectInfo info : proData){
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

    /***
     * 将文件保存到U盘
     * @param targetDir
     */
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
                File drawSourceF = null;
                File targetPathF = null;
                File drawTargetPathF = null;
                File targetF = null;
                File drawTargetF = null;

                String soDir = null;
                String drawSoDir = null;
                String tarDir = null;
                String drawTarDir = null;
                for (int i = 0; i < proData.size(); i++) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                    if (proData.get(i).nIsSelect == 2) {
                        soDir = PathUtils.PROJECT_PATH + "/" + proData.get(i).mFileProjectName;
                        drawSoDir = PathUtils.DRAWPROJECT_PATH+"/"+proData.get(i).mFileProjectName;
                        tarDir = targetDir + File.separator +"工程"+File.separator + proData.get(i).mFileProjectName;
                        drawTarDir = targetDir + File.separator +"Draw工程"+ File.separator+ proData.get(i).mFileProjectName;
                        try {
                            if (null != soDir && soDir.length() != 0 && null != tarDir && tarDir.length() != 0) {
                                // 拷贝原图文件
                                FileUtil.getInstance().copyDirectiory(soDir, tarDir, AppDatPara);
                            }
                            if (null != drawSoDir && drawSoDir.length() != 0 && null != drawSoDir && drawSoDir.length() != 0) {
                                // 拷贝含有标志的文件
                                FileUtil.getInstance().copyDirectiory(drawSoDir, drawTarDir, AppDatPara);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (proData.get(i).nIsSelect == 1) {
                        for (int j = 0; j < proData.get(i).mstrArrFileGJ.size(); j++) {
                            if (proData.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
                                targetPathF = new File(targetDir + File.separator +
                                        "工程"+proData.get(i).mFileProjectName);
                                drawTargetPathF = new File(targetDir + File.separator +
                                        "Draw工程"+proData.get(i).mFileProjectName);
                                sourceF = new File(PathUtils.PROJECT_PATH
                                        + "/" + proData.get(i).mFileProjectName
                                        + "/" + proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
                                drawSourceF = new File(PathUtils.DRAWPROJECT_PATH
                                        + "/" + proData.get(i).mFileProjectName
                                        + "/" + proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
                                targetF = new File(targetPathF.toString()
                                        + File.separator + proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
                                drawTargetF = new File(drawTargetPathF.toString()
                                        + File.separator + proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
                                // 拷贝原图文件
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
                                // 拷贝含有标志的文件
                                if (null != drawSourceF && null != drawTargetF) {
                                    if (drawTargetPathF.exists()) {
                                        // 拷贝文件
                                        FileUtil.getInstance().copyFile(drawSourceF, drawTargetF, AppDatPara);
                                    } else {
                                        // 新建目标目录
                                        (drawTargetPathF).mkdirs();
                                        // 拷贝文件
                                        FileUtil.getInstance().copyFile(drawSourceF, drawTargetF, AppDatPara);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
