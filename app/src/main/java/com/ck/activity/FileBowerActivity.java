package com.ck.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import com.ck.dlg.TwoBtMsgDialog;
import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.ui.OpenCvCameraView;
import com.ck.utils.BroadcastAction;
import com.ck.utils.Catition;
import com.ck.utils.FileUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.PathUtils;
import com.ck.utils.Stringutil;
import com.google.gson.Gson;
import com.hc.u8x_ck.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fei
 * 为文件管理的Activity
 */

public class FileBowerActivity extends TitleBaseActivity implements View.OnClickListener {

    private List<ClasFileGJInfo> fileGJData;
    private RecyclerView fileBower_proList_rv;
    private FileListProjectAdapter mProjectAdapter;
    private RecyclerView fileBower_fileList_rv;
    private FileListGJAdapter mGJAdapter;
    private DLG_FileProgress m_ProgressDialog;

    private OpenCvCameraView fileBower_camera;
    private TextView fileBower_allChoice_tv;
    private LinearLayout fileBower_allChoice_ll;
    private TextView fileBower_del_tv;
    private TextView fileBower_toUPan_tv;

    private final int pageIndex = 12;//分页加载的每一页的个数
    private int currPageNum = 0;//当前工程的第几页
    private int gjPosition = 0;//构件文件的选中项

    private Intent intent;//发送复制进度广播的Intent

    private LoadingDialog loading;//loading框
    private boolean isCanSecelt = false;//是否出现选框
    private boolean isBreakGetListData = false;//加载的子线程是否打断

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
        fileBower_camera = findView(R.id.fileBower_camera);
        fileBower_allChoice_tv = findView(R.id.fileBower_allChoice_tv);
        fileBower_allChoice_ll = findView(R.id.fileBower_allChoice_ll);
        fileBower_proList_rv = findView(R.id.fileBower_proList_rv);
        fileBower_fileList_rv = findView(R.id.fileBower_fileList_rv);
        fileBower_del_tv = findView(R.id.fileBower_del_tv);
        fileBower_toUPan_tv = findView(R.id.fileBower_toUPan_tv);
        if (null == loading) {
            loading = new LoadingDialog(this);
        }
        mProjectAdapter = new FileListProjectAdapter(this, App_DataPara.getApp().proData);
        fileBower_proList_rv.setLayoutManager(new LinearLayoutManager(this));
        fileBower_proList_rv.setAdapter(mProjectAdapter);
        if (null == fileGJData) {
            fileGJData = new ArrayList<>();
        }
        mGJAdapter = new FileListGJAdapter(this, fileGJData);
        fileBower_fileList_rv.setLayoutManager(new GridLayoutManager(this, 3));
        fileBower_fileList_rv.setAdapter(mGJAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        startLoading();
        App_DataPara.getApp().proData = PathUtils.getProFileList();
        refreshList(3);
        baseTitle_title_tv.setText("文件管理");
        if(null != App_DataPara.getApp().proData && App_DataPara.getApp().proData.size()>0) {
            initListData();
        }
    }


    private void initListData(){
        startLoading();
        isBreakGetListData = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (null != App_DataPara.getApp().proData) {
                    int flagPosition = 0;
                    for(int i=0;i<App_DataPara.getApp().proData.size();i++) {
                        ClasFileProjectInfo proInfo = App_DataPara.getApp().proData.get(i);
                        for (ClasFileGJInfo gjInfo : proInfo.mstrArrFileGJ) {
                            if (isBreakGetListData) {
                                return;
                            }
                            if (Stringutil.isEmpty(gjInfo.getWidth())) {
                                String jsons = FileUtil.readData(PathUtils.PROJECT_PATH + "/"
                                        + proInfo.mFileProjectName + "/" + gjInfo.mFileGJName);
                                if (null != jsons) {
                                    if (!Stringutil.isEmpty(jsons)) {
                                        MeasureDataBean bean = new Gson().fromJson(jsons, MeasureDataBean.class);
                                        gjInfo.setWidth(bean.getWidth() + "");
                                    }
                                }
                            }
                            if (null == gjInfo.getSrc()) {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 7;
                                byte[] picByte = FileUtil.readPicData(PathUtils.PROJECT_PATH + "/"
                                        + proInfo.mFileProjectName + "/" + gjInfo.mFileGJName);
                                if (null != picByte) {
                                    gjInfo.setSrc(BitmapFactory.decodeByteArray(picByte,
                                            0, picByte.length, options));
                                }
                            }
                            flagPosition++;
                            if (i==0 && flagPosition == pageIndex) {
                                fileGJData.clear();
                                for(int mm=0;mm<pageIndex;mm++){
                                    fileGJData.add(App_DataPara.getApp().proData.get(0).mstrArrFileGJ.get(mm));
                                }
                                gjPosition = 0;
                                refreshList(2);
                            }
                        }
                        if(i==0 && flagPosition < pageIndex){
                            fileGJData.clear();
                            for(int mm=0;mm<flagPosition;mm++){
                                fileGJData.add(App_DataPara.getApp().proData.get(0).mstrArrFileGJ.get(mm));
                            }
                            gjPosition = 0;
                            refreshList(2);
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 开启子线程提前加载文件系统的列表
     */
    private void loadMorePicData() {
        startLoading();
        isBreakGetListData = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (null != App_DataPara.getApp().proData) {
                    int flagPosition = 0;
                    ClasFileProjectInfo proInfo = App_DataPara.getApp().proData.get(mProjectAdapter.getSelect());
                    for (int i=pageIndex*currPageNum;i<proInfo.mstrArrFileGJ.size();i++) {
                        ClasFileGJInfo gjInfo = proInfo.mstrArrFileGJ.get(i);
                        if (isBreakGetListData) {
                            return;
                        }
                        if(Stringutil.isEmpty(gjInfo.getWidth())) {
                            String jsons = FileUtil.readData(PathUtils.PROJECT_PATH + "/"
                                    + proInfo.mFileProjectName + "/" + gjInfo.mFileGJName);
                            if (null != jsons) {
                                if (!Stringutil.isEmpty(jsons)) {
                                    MeasureDataBean bean = new Gson().fromJson(jsons, MeasureDataBean.class);
                                    gjInfo.setWidth(bean.getWidth() + "");
                                }
                            }
                        }
                        if(null == gjInfo.getSrc()) {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 7;
                            byte[] picByte = FileUtil.readPicData(PathUtils.PROJECT_PATH + "/"
                                    + proInfo.mFileProjectName + "/" + gjInfo.mFileGJName);
                            if (null != picByte) {
                                gjInfo.setSrc(BitmapFactory.decodeByteArray(picByte,
                                        0, picByte.length, options));
                            }
                        }
                        flagPosition++;
                        fileGJData.add(gjInfo);
                        if (flagPosition == pageIndex) {
                            refreshList(2);
                            return;
                        }
                    }
                    loadNoMore(flagPosition);
                }
            }
        }).start();
    }

    private void loadNoMore(final int flagPosition){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoading();
                if(flagPosition == 0){
                    currPageNum --;
                    showToast("没有更多了");
                }else {
                    refreshList(2);
                    showToast("已经加载到最后");
                }
            }
        });
    }

    /**
     * 刷新列表
     *
     * @Param type  1:所有都刷新   2：刷新图片  3：刷新工程列表
     */
    private void refreshList(final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null != mProjectAdapter && null != mGJAdapter) {
                    if (type == 1) {
                        mProjectAdapter.setData(App_DataPara.getApp().proData, mProjectAdapter.getSelect());
                        mGJAdapter.setData(fileGJData, gjPosition);
                    } else if (type == 2) {
                        if (App_DataPara.getApp().proData.size() > mProjectAdapter.getSelect()) {
                            mGJAdapter.setData(fileGJData, gjPosition);
                        }
                    } else if (type == 3) {
                        mProjectAdapter.setData(App_DataPara.getApp().proData, mProjectAdapter.getSelect());
                    }
                    stopLoading();
                }
            }
        });
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
                clickObjList(position);
                if (isCanSecelt) {//是否进行勾选
                    choiceObjList(position);
                } else {//点击一整项
//                    clickObjList(position);
                }
            }

            @Override
            public void onLongClick(int position) {
                changeViewToSecelect();
            }
        });
        //TODO : 文件列表的监听
        mGJAdapter.setOnFileGJItemClick(new FileListGJAdapter.OnFileGJItemClick() {
            @Override
            public void onGJSelect(boolean isSelect, int position) {
                if (isCanSecelt) {
                    choiceFileList(position);
                } else {//点击一整项
                    cilickFileObjList(position);
                }
            }

            @Override
            public void onGJLongClick(int position) {
                changeViewToSecelect();
            }
        });
        fileBower_fileList_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {//滑动到底部的处理
                    currPageNum ++ ;
                    loadMorePicData();
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
        currPageNum = 0;
        gjPosition = 0;
        fileGJData.clear();
        loadMorePicData();
    }

    /**
     * 工程列表的选中
     *
     * @param position
     */
    protected void choiceObjList(int position) {
        if (null == App_DataPara.getApp().proData || App_DataPara.getApp().proData.size() == 0) {
            return;
        }
        int choiceState = App_DataPara.getApp().proData.get(position).nIsSelect;
        if (choiceState == 0 || choiceState == 1) {//进行完全选中
            optFileListChoice(position, true);
        } else {//完全不选中
            optFileListChoice(position, false);
        }
        mProjectAdapter.setSelect(position);
        mGJAdapter.setData(fileGJData, mGJAdapter.getSelect());
        if (isSelectAllObj()) {
            //改变全选按钮的提示
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_all_true));
        } else {
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_false));
        }
    }

    /**
     * 文件列表的点击事件
     *
     * @param position
     */
    protected void cilickFileObjList(int position) {
        mGJAdapter.setSelect(position);
        Intent intent = new Intent(this, EditPicActivity.class);
        intent.putExtra("objPosition", mProjectAdapter.getSelect());
        intent.putExtra("gjPosition", mGJAdapter.getSelect());
        startActivityForResult(intent, Catition.FLIETURNTOEDIT);
    }

    protected void choiceFileList(int position) {
        int proPosition = mProjectAdapter.getSelect();
        App_DataPara.getApp().proData.get(mProjectAdapter.getSelect())
                .mstrArrFileGJ.get(position).bIsSelect
                = ! App_DataPara.getApp().proData.get(mProjectAdapter.getSelect())
                .mstrArrFileGJ.get(position).bIsSelect;
        fileGJData.get(position).bIsSelect =  App_DataPara.getApp().proData.
                get(mProjectAdapter.getSelect()).mstrArrFileGJ.get(position).bIsSelect;
        boolean isAllSelect = true;
        boolean isNoneSelect = true;
        for (ClasFileGJInfo gjInfo : App_DataPara.getApp().proData.get(mProjectAdapter.getSelect()).mstrArrFileGJ) {
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
        if (isSelectAllObj()) {
            //改变全选按钮的提示
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_all_true));
        } else {
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
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F1://切换按键，功能为删除
                onSaveSDcard();
                return true;
            case KeyEvent.KEYCODE_F2: //存储按键,功能为转U盘
                onDelete();
                return true;
            case KeyEvent.KEYCODE_BACK://返回键
                if (isCanSecelt) {
                    changeViewToNoSecelct();
                } else {
                    finishActivity();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return true;
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
        for (ClasFileGJInfo info : fileGJData){
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
        if (!isSelect) {
            //改变全选按钮的提示
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_all_true));
        } else {
            fileBower_allChoice_tv.setBackground(getResources()
                    .getDrawable(R.drawable.checkbox_false));
        }
        mProjectAdapter.setData(App_DataPara.getApp().proData, mProjectAdapter.getSelect());
        mGJAdapter.setData(fileGJData, mGJAdapter.getSelect());
    }

    /**
     * {功能}是否删除文件的Dialog
     *
     * @return void
     * @throw
     */
    protected void onDelete() {
        if (!isCanSecelt) {
            changeViewToSecelect();
            return;
        }
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
     * 改变布局到选择状态
     */
    private void changeViewToSecelect() {
        isCanSecelt = true;
        mGJAdapter.toSelectView(true);
        mProjectAdapter.toSelectView(true);
        fileBower_allChoice_tv.setVisibility(View.VISIBLE);
    }

    /**
     * 改变布局到不能选择状态
     */
    private void changeViewToNoSecelct() {
        isCanSecelt = false;
        mGJAdapter.toSelectView(false);
        mProjectAdapter.toSelectView(false);
        fileBower_allChoice_tv.setVisibility(View.GONE);
        fileBower_allChoice_tv.setBackground(getResources()
                .getDrawable(R.drawable.checkbox_false));
        for (ClasFileProjectInfo proInfo : App_DataPara.getApp().proData) {
            for (ClasFileGJInfo gjInfo : proInfo.mstrArrFileGJ) {
                gjInfo.bIsSelect = false;
            }
            proInfo.nIsSelect = 0;
        }
    }

    /**
     * 删除选中文件的具体方法
     */
    private void delSelectFile() {
        FileUtil.delSeleceFile(App_DataPara.getApp().proData);
        for (int i=0;i<App_DataPara.getApp().proData.size();i++) {
            ClasFileProjectInfo projectInfo = App_DataPara.getApp().proData.get(i);
            if (projectInfo.nIsSelect == 2) {
                App_DataPara.getApp().proData.remove(projectInfo);
            } else if (projectInfo.nIsSelect == 1) {
                for (int j=0;j<projectInfo.mstrArrFileGJ.size();j++) {
                    ClasFileGJInfo fileGJInfo = projectInfo.mstrArrFileGJ.get(j);
                    if (fileGJInfo.bIsSelect) {
                        projectInfo.mstrArrFileGJ.remove(fileGJInfo);
                    }
                }
            }
        }
        int position = mProjectAdapter.getSelect();
        int allLength = App_DataPara.getApp().proData.size();
        Log.i("fei", "总体长度" + allLength);
        if (allLength == 0) {
            position = 0;
        } else {
            if (position >= allLength) {
                position = allLength - 1;
            }
        }
        mProjectAdapter.setData(App_DataPara.getApp().proData, position);
        currPageNum = 0;
        gjPosition = 0;
        loadMorePicData();
    }

    /**
     * 删除数据中的文件
     */
    private void delDbMeasure() {
        for (ClasFileProjectInfo info : App_DataPara.getApp().proData) {
            if (info.nIsSelect == 2) {
                DBService.getInstence(this).delMeasureData(
                        info.mFileProjectName, null);
            } else if (info.nIsSelect == 1) {
                List<ClasFileGJInfo> ArrFileGJ = info.mstrArrFileGJ;
                for (int j = 0; j < ArrFileGJ.size(); j++) {
                    ClasFileGJInfo clasFileGJInfo = ArrFileGJ.get(j);
                    if (clasFileGJInfo.bIsSelect) {
                        DBService.getInstence(this).delMeasureData(
                                info.mFileProjectName, clasFileGJInfo.mFileGJName);
                    }
                }
            }
        }
    }

    /**
     * 将文件转存的U盘
     */
    protected void onSaveSDcard() {
        if (!isCanSecelt) {
            changeViewToSecelect();
            return;
        }
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
        final File drawFile = new File(targetDir+"/Draw工程");
        final File file = new File(targetDir+"/工程");
        if(drawFile.exists() || file.exists()){
            final TwoBtMsgDialog btMsgDialog = new TwoBtMsgDialog(FileBowerActivity.this);
            btMsgDialog.show();
            btMsgDialog.setMsg("已存在目标文件\n是否重新复制？");
            btMsgDialog.setBtCancelTxt("取消");
            btMsgDialog.setBtOkTxt("确定");
            btMsgDialog.setOnBtClickListener(new TwoBtMsgDialog.OnBtClickListener() {
                @Override
                public void onBtClick(boolean isOk) {
                    if(isOk){
                        FileUtil.deleteDirectory(drawFile.getAbsolutePath());
                        FileUtil.deleteDirectory(file.getAbsolutePath());
                        choiceCopyType(targetDir);
                    }
                    btMsgDialog.dismiss();
                }
            });
        }else{
            choiceCopyType(targetDir);
        }
    }

    /**
     * 选择要导出的类型
     * @param targetDir
     */
    private void choiceCopyType(final String targetDir){
        final ChoiceSaveTypeDialog dialog = new ChoiceSaveTypeDialog(FileBowerActivity.this);
        dialog.show();
        dialog.setOnChoiceSaveClick(new ChoiceSaveTypeDialog.OnChoiceSaveClick() {
            @Override
            public void isOk(boolean isOk, int type) {
                if (isOk) {
                    if (type == -1) {
                        showToast("请选择导入U盘的数据类型");
                    } else {
                        dialog.dismiss();
                        copyFile(targetDir, type);
                    }
                } else {
                    dialog.dismiss();
                }
            }
        });
    }

    // 拷贝选中文件
    private void copyFile(final String targetDir, final int type) {
        if (null != AppDatPara.GetExternalStorageDirectory()) {
            File targetFile = new File(targetDir);
            targetFile.mkdirs();
            // 将文件大小归0
            FileUtil.getInstance().fileSize = 0;
            // 如果存在选中转U盘状态,显示进度条
            if (new File(targetDir).exists()) {
                if (targetFile.exists() && targetFile.canRead() && targetFile.canWrite()) {
                    copyFileToUPan(targetDir, type);
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
        long lTotalfileSize = FileUtil.GetFileSize(App_DataPara.getApp().proData, type);
        // 新建目标目录
        if (null == m_ProgressDialog || !m_ProgressDialog.isShowing()) {
            m_ProgressDialog = new DLG_FileProgress(this, lTotalfileSize);
            m_ProgressDialog.setCanceledOnTouchOutside(false);
            m_ProgressDialog.show();
            m_ProgressDialog.setProgressValue((long) 0);
        }
        new Thread() {
            public void run() {
//                FileUtil.copyFileInThread(targetDir, type, AppDatPara);
                copyFileInThread(targetDir, type, AppDatPara);
            }
        }.start();
    }

    /**
     * 文件管理U盘导出文件的最后阶段 ，根据type确定导出的类型
     *
     * @param targetDir
     * @param type       1；导出可直接使用的图片 2：导出到上位机的文件  3： 两种都进行导出
     * @param AppDatPara
     */
    private void copyFileInThread(String targetDir, int type, App_DataPara AppDatPara) {
        File ckSourceF = null;
        File targetPathF = null;
        File drawTargetPathF = null;
        File ckTargetF = null;
        File drawTargetF = null;
        for (int i = 0; i < App_DataPara.getApp().proData.size(); i++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (App_DataPara.getApp().proData.get(i).nIsSelect != 0) {
                for (int j = 0; j < App_DataPara.getApp().proData.get(i).mstrArrFileGJ.size(); j++) {
                    if (App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).bIsSelect == true) {
                        targetPathF = new File(targetDir + File.separator +
                                "工程" + File.separator + App_DataPara.getApp().proData.get(i).mFileProjectName);
                        drawTargetPathF = new File(targetDir + File.separator +
                                "Draw工程" + File.separator + App_DataPara.getApp().proData.get(i).mFileProjectName);
                        ckSourceF = new File(PathUtils.PROJECT_PATH
                                + "/" + App_DataPara.getApp().proData.get(i).mFileProjectName
                                + "/" + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
                        ckTargetF = new File(targetPathF.toString()
                                + File.separator + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName);
                        drawTargetF = new File(drawTargetPathF.toString()
                                + File.separator + App_DataPara.getApp().proData.get(i).mstrArrFileGJ.get(j).mFileGJName
                                .replace(".CK", ".png"));

                        if (type == 1) { //导出之后可直接使用的图片
                            // 拷贝含有标志的文件
                            if (null != ckSourceF && null != drawTargetF) {
                                drawTargetPathF.mkdirs();// 新建目标目录
                                makeDrawPicToFile(ckSourceF, drawTargetF, AppDatPara);
                            }
                        } else if (type == 2) { //导出到电脑的图片
                            if (null != ckSourceF && null != ckTargetF) {
                                targetPathF.mkdirs();// 新建目标目录
                                FileUtil.getInstance().copyFile(ckSourceF, ckTargetF, AppDatPara);// 拷贝文件
                            }
                        } else if (type == 3) {//以上两种都有的图片
                            // 拷贝原图文件
                            if (null != ckSourceF && null != ckTargetF) {
                                targetPathF.mkdirs();// 新建目标目录
                                FileUtil.getInstance().copyFile(ckSourceF, ckTargetF, AppDatPara);// 拷贝文件
                            }
                            // 拷贝含有标志的文件
                            if (null != ckSourceF && null != drawTargetF) {
                                drawTargetPathF.mkdirs();// 新建目标目录
                                makeDrawPicToFile(ckSourceF, drawTargetF, AppDatPara);
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * 导出图片的时候进行图片和数据的合成
     *
     * @param source
     * @param target
     * @param m_Context
     */
    public void makeDrawPicToFile(File source, File target, Context m_Context) {
        if (null == source || null == target || null == m_Context) {
            return;
        }
        byte[] bytes = FileUtil.readPicData(source.getPath());
        Bitmap picBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        String json = FileUtil.readData(source.getPath());
        MeasureDataBean dataBean = new Gson().fromJson(json, MeasureDataBean.class);
        fileBower_camera.setBitmap(picBitmap, true);
        FindLieFenUtils.m_nCLXLineSite = dataBean.getLeftX();
        FindLieFenUtils.m_nCLYLineSite = dataBean.getLeftY();
        FindLieFenUtils.m_nCRXLineSite = dataBean.getRightX();
        FindLieFenUtils.m_nCRYLineSite = dataBean.getRightY();
        FindLieFenUtils.bytGrayAve = dataBean.getAvage();
        fileBower_camera.makeInitSetting();
        fileBower_camera.setZY(0, true);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileBower_camera.setDrawingCacheEnabled(true);
        FileUtil.saveDrawToUFile(fileBower_camera.getDrawingCache(), target);
        fileBower_camera.setDrawingCacheEnabled(false);
        FileUtil.getInstance().fileSize ++;
        if (null == intent) {
            intent = new Intent(BroadcastAction.UpdataProgress);
        }
        intent.putExtra("ProgressValue",  FileUtil.getInstance().fileSize);
        m_Context.sendBroadcast(intent);
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
        if (resultCode == Catition.EDITBACKTOFILE && requestCode == Catition.FLIETURNTOEDIT) {
            int filePosition = 0;
            if (null != data) {
                filePosition = data.getIntExtra("filePosition", 0);
            }
            if(filePosition >= fileGJData.size()) {
                ClasFileProjectInfo projectInfo = App_DataPara.getApp().proData.get(mProjectAdapter.getSelect());
                fileGJData.clear();
                for (int i=0;i<filePosition ;i++){
                    fileGJData.add(projectInfo.mstrArrFileGJ.get(i));
                }
                mGJAdapter.setData(fileGJData, filePosition);
            }else{
                mGJAdapter.setData(fileGJData, filePosition);
            }
            fileBower_fileList_rv.scrollToPosition(filePosition);
        }
    }

    private void stopLoading() {
        if (null != loading && loading.isShowing()) {
            loading.dismiss();
        }
    }

    private void startLoading() {
        if (null != loading && !loading.isShowing()) {
            loading.show();
        }
    }

    private void finishActivity() {
        App_DataPara.getApp().proData.clear();
        isBreakGetListData = true;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
