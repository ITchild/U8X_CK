package com.ck.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ck.adapter.ListGJAdapter;
import com.ck.adapter.ListProjectAdapter;
import com.ck.base.TitleBaseActivity;
import com.ck.collect.OnOpenCameraListener;
import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.ui.CameraView;
import com.ck.utils.Catition;
import com.ck.utils.FileUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class CollectActivity extends TitleBaseActivity implements View.OnClickListener, View.OnLongClickListener,SurfaceHolder.Callback{

    public String m_strSaveProName = "默认工程"; //保存图片的默认工程名称
    public String m_strSaveGJName = "默认构件1"; //保存图片的默认文件名称
    private SurfaceView collect_sfv;
    private SurfaceHolder mHolder;
    private CameraView collect_cameraView;
    private Button collect_startStop_bt; //开始\停止 按钮
    private Button collect_save_bt; //预拍 \ 存储  按钮
    private Button collect_autoOrhand_bt;//自动计算或手动计算裂缝位置
    private Button collect_Cursor_bt;//选择检测光标
    private Button collect_left_bt;//光标向左移
    private Button collect_right_bt;//光标向右移
    private LinearLayout collect_filelist_ll; //文件列表的布局
    private EditText collect_proName_et;
    private EditText collect_fileName_et;
    private CheckBox collect_blackWrite_cb;// 是否显示黑白图
    private Button collect_enlarge_bt; //放大
    private Button collect_Lessen_bt; //缩小

    private ListView collect_proList_lv;//工程列表
    private ListProjectAdapter mProjectAdapter; //工程列表的Apapter
    private List<ClasFileProjectInfo> proData; //工程列表的数据源

    private ListView collect_fileList_lv;//工程中的文件列表
    private ListGJAdapter mGJAdapter; //工程中文件列表的Adapter
    private ClasFileProjectInfo fileData;//工程中文件列表的数据源
    private int filePosition = -1; //文件列表的选择位置

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1000: //长按的循环操作
                    boolean isLeft = (boolean) msg.obj;//true:左移 false : 右移
                    if (isLeft && !collect_left_bt.isPressed()) {
                        return;
                    } else if (!isLeft && !collect_right_bt.isPressed()) {
                        return;
                    }
                    onMoveLeftRight(isLeft, true);
                    break;
            }
        }
    };

    @Override
    protected int initLayout() {
        return R.layout.ac_collect;
    }

    @Override
    protected boolean isBackshow() {
        return false;
    }

    @Override
    protected void initView() {
        super.initView();
        collect_sfv = findView(R.id.collect_sfv);
        collect_cameraView = findView(R.id.collect_cameraView);
        collect_startStop_bt = findView(R.id.collect_startStop_bt);
        collect_save_bt = findView(R.id.collect_save_bt);
        collect_filelist_ll = findView(R.id.collect_filelist_ll);
        collect_blackWrite_cb = findView(R.id.collect_blackWrite_cb);
        collect_enlarge_bt = findView(R.id.collect_enlarge_bt);
        collect_Lessen_bt = findView(R.id.collect_Lessen_bt);
        collect_proName_et = findView(R.id.collect_proName_et);
        collect_fileName_et = findView(R.id.collect_fileName_et);
        collect_autoOrhand_bt = findView(R.id.collect_autoOrhand_bt);
        collect_Cursor_bt = findView(R.id.collect_Cursor_bt);
        collect_left_bt = findView(R.id.collect_left_bt);
        collect_right_bt = findView(R.id.collect_right_bt);
        collect_proList_lv = findView(R.id.collect_proList_lv);
        collect_fileList_lv = findView(R.id.collect_fileList_lv);
        if (null == proData) {
            proData = new ArrayList<>();
        }
        mProjectAdapter = new ListProjectAdapter(this, proData);
        collect_proList_lv.setAdapter(mProjectAdapter);
        if (null == fileData) {
            fileData = new ClasFileProjectInfo();
        }
        mGJAdapter = new ListGJAdapter(this, fileData);
        collect_fileList_lv.setAdapter(mGJAdapter);
    }

    @Override
    protected void initData() {
        super.initData();
        refreshProListData(AppDatPara.m_nProjectSeleteNidx);
        refreshFileListData(AppDatPara.m_nProjectSeleteNidx);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (collect_cameraView.isStart) {
                    onCollectStart();
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (collect_cameraView.isStart) {
            collect_cameraView.closeCamera();
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        collect_startStop_bt.setOnClickListener(this);
        findViewById(R.id.collect_back_bt).setOnClickListener(this);
        collect_save_bt.setOnClickListener(this);
        collect_autoOrhand_bt.setOnClickListener(this);
        collect_Cursor_bt.setOnClickListener(this);
        collect_left_bt.setOnClickListener(this);
        collect_left_bt.setOnLongClickListener(this);
        collect_right_bt.setOnClickListener(this);
        collect_right_bt.setOnLongClickListener(this);
        collect_blackWrite_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isChecked = collect_blackWrite_cb.isChecked();
                collect_blackWrite_cb.setChecked(isChecked);
                collect_cameraView.setBlackWrite(isChecked,true);
            }
        });
        collect_enlarge_bt.setOnClickListener(this);
        collect_Lessen_bt.setOnClickListener(this);
        collect_proList_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //获取工程列表的选中位置
                if (collect_cameraView.isStart) {
                    return;
                }
                if (AppDatPara.m_nProjectSeleteNidx == i) {
                    return;
                }
                mProjectAdapter.setSelect(i);
                getSaveProFileName(i);
                AppDatPara.m_nProjectSeleteNidx = i;
                filePosition = -1;
                refreshFileListData(AppDatPara.m_nProjectSeleteNidx);
                showPic();
            }
        });
        collect_fileList_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (collect_cameraView.isStart) {
                    return;
                }
                if (filePosition == i) {
                    return;
                }
                filePosition = i;
                mGJAdapter.setSelect(i);
                showPic();
            }
        });
    }

    /**
     * 刷新工程列表的数据
     */
    private void refreshProListData(int position) {
        proData = PathUtils.getProFileList();
        if (null != proData && proData.size() > position) {
            mProjectAdapter.setData(proData, position);
        }
    }

    /**
     * 刷新文件列表数据
     */
    private void refreshFileListData(int position) {
        if (null != proData && proData.size() > 0 && proData.size() > position) {
            fileData = proData.get(position);
        }
        mGJAdapter.setData(fileData, filePosition);
    }

    /**
     * 获取存储照片时工程名称和文件名称
     * @param position 工程列表数据的位置
     */
    private void getSaveProFileName(int position) {
        if (position < 0 || null == proData || proData.size() == 0 || proData.size() <= position) {
            return;
        }
        String proName = proData.get(position).mFileProjectName;
        m_strSaveProName = isStrEmpty(proName) ? "" : proName;
        List<ClasFileGJInfo> mstrArrFileGJ = proData.get(position).mstrArrFileGJ;
        if (null != mstrArrFileGJ && mstrArrFileGJ.size() > 0) {
            String fileName = mstrArrFileGJ.get(mstrArrFileGJ.size() - 1).mFileGJName;
            m_strSaveGJName = isStrEmpty(fileName) ? "" : fileName.substring(0,fileName.length()-4);
            m_strSaveGJName = FileUtil.GetDigitalPile(m_strSaveGJName);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.collect_startStop_bt://TODO：开始监测以及停止监测
                if (collect_startStop_bt.getText().toString().equals(getStr(R.string.str_startCollect))) {
                    onCollectStart(); // 开始进行检测
                } else {
                    onCollectStop(); // 停止进行检测并回复初始视图
                }
                break;
            case R.id.collect_save_bt: // TODO： 预拍以及保存
                if (collect_save_bt.getText().toString().equals(getStr(R.string.str_takePhoto))) {
                    onBeforTakePic();//预拍
                } else if (collect_save_bt.getText().toString().equals(getStr(R.string.str_save))) {
                    onTakePic();//保存
                }
                break;
            case R.id.collect_autoOrhand_bt: // TODO: 手动计算还是自动计算
                onCountMode();
                break;
            case R.id.collect_Cursor_bt: //TODO： 进行游标的切换
                onSelectCursor();
                break;
            case R.id.collect_left_bt: //TODO : 左移
                if (collect_left_bt.getText().toString().equals(getStr(R.string.str_toLeft))) {
                    onMoveLeftRight(true, false);
                }
                break;
            case R.id.collect_right_bt: //TODO : 右移
                if (collect_right_bt.getText().toString().equals(getStr(R.string.str_toRight))) {
                    onMoveLeftRight(false, false);
                }
                break;
            case R.id.collect_back_bt: //TODO: 返回
                activityFinish(); //返回
                break;
            case R.id.collect_enlarge_bt : //TODO : 放大
                if(!collect_cameraView.isToLarge) {
                    collect_cameraView.setLargeOrSmall(true,true);
                }
                break;
            case R.id.collect_Lessen_bt : //TODO : 缩小
                if(collect_cameraView.isToLarge) {
                    collect_cameraView.setLargeOrSmall(false,true);
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.collect_left_bt://TODO : 长按左移
                if (collect_left_bt.getText().toString().equals(getStr(R.string.str_toLeft))) {
                    onMoveLeftRight(true, true);
                }
                break;
            case R.id.collect_right_bt://TODO ：长按右移
                if (collect_right_bt.getText().toString().equals(getStr(R.string.str_toRight))) {
                    onMoveLeftRight(false, true);
                }
                break;
        }
        return false;
    }

    /**
     * 开始进行测量
     */
    private void onCollectStart() {
        collect_cameraView.setStartView();
        collect_cameraView.onenCamera(new OnOpenCameraListener() {
            @Override
            public void OnOpenCameraResultListener(boolean bResult) {
                if (bResult) {
                    if (null == mHolder){
                        mHolder = collect_sfv.getHolder();
                        mHolder.addCallback(CollectActivity.this);
                    }
                    collect_cameraView.setHolder(mHolder);
                    collect_cameraView.setCountMode(true);
                    collect_cameraView.setZY(0);
                    changeStartStopTakeView(Catition.CollectView.START);
                } else {
                    Toast.makeText(CollectActivity.this, "请安装指定的摄像头", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCarameError() {
                stopCameraView(); // 停止进行检测
                onCollectStart();
            }
        });
    }

    /**
     * 停止测量
     */
    private void onCollectStop() {
        stopCameraView();
        changeStartStopTakeView(Catition.CollectView.STOP);
        collect_cameraView.showOriginalView();
    }

    /**
     * 进行预拍
     */
    private void onBeforTakePic() {
        stopCameraView();
        collect_cameraView.setZY(1);
        getSaveProFileName(AppDatPara.m_nProjectSeleteNidx);
        changeStartStopTakeView(Catition.CollectView.TAKEPHOTO);
    }

    /**
     * 进行保存
     */
    private void onTakePic() {
        String proName = collect_proName_et.getText().toString();
        String fileName = collect_fileName_et.getText().toString();
        if (isStrEmpty(proName)) {
            showToast("工程名不能为空");
            return;
        } else if (isStrEmpty(fileName)) {
            showToast("构件名不能为空");
            return;
        }
        collect_cameraView.setDrawingCacheEnabled(true);
        collect_cameraView.onTakePic(true);
        collect_cameraView.buildDrawingCache();
        Bitmap drawingCache = collect_cameraView.getDrawingCache();
        FileUtil.saveBmpImageFile(drawingCache, proName, fileName, "%s.bmp");
        collect_cameraView.onTakePic(false);
        collect_cameraView.setDrawingCacheEnabled(false);
        //刷新列表
        onCollectStart();//保存成功之后继续进行检测
        refreshProListData(AppDatPara.m_nProjectSeleteNidx);
        refreshFileListData(AppDatPara.m_nProjectSeleteNidx);
        if(collect_blackWrite_cb.isChecked()) {
            collect_blackWrite_cb.setChecked(false);
        }
        collect_cameraView.setBlackWrite(false,false);
        collect_cameraView.setLargeOrSmall(false,false);
        showToast(getStr(R.string.str_saveSuccess));
    }

    /**
     * 显示图片
     */
    public void showPic() {
        if (AppDatPara.m_nProjectSeleteNidx < 0 || proData.size() <= AppDatPara.m_nProjectSeleteNidx) {
            collect_cameraView.showOriginalView();
            return;  //判断工程list有没有被选中
        }
        ClasFileProjectInfo pro = proData.get(AppDatPara.m_nProjectSeleteNidx);
        if (filePosition < 0 || pro.mstrArrFileGJ.size() <= filePosition) {
            collect_cameraView.showOriginalView();
            return;//判断工程中的构件List有没有被选中
        }
        ClasFileGJInfo file = pro.mstrArrFileGJ.get(filePosition);
        String path = PathUtils.PROJECT_PATH + File.separator + pro.mFileProjectName + File.separator + file.mFileGJName;
        Bitmap bmp = BitmapFactory.decodeFile(path);
        collect_cameraView.setBitmap(bmp);  //正常图像
    }

    /**
     * 设置检测模式，自动检测还是手动检测
     */
    private void onCountMode() {
        if (collect_autoOrhand_bt.getText().toString().equals(getStr(R.string.str_Auto))) {
            collect_cameraView.setCountMode(false);//手动计算
            collect_cameraView.setZY(1);
            collect_autoOrhand_bt.setText(getStr(R.string.str_Hand));
            collect_Cursor_bt.setText(getStr(R.string.str_leftCursor));
            collect_left_bt.setText(getStr(R.string.str_toLeft));
            collect_right_bt.setText(getStr(R.string.str_toRight));
        } else if (collect_autoOrhand_bt.getText().toString().equals(getStr(R.string.str_Hand))) {
            collect_cameraView.setCountMode(true);//自动计算
            collect_cameraView.setZY(0);
            collect_autoOrhand_bt.setText(getStr(R.string.str_Auto));
            collect_Cursor_bt.setText("");
            collect_left_bt.setText("");
            collect_right_bt.setText("");
        }
    }

    /**
     * 光标的左右移动
     */
    private void onMoveLeftRight(boolean isToLeft, boolean isLongClick) {
        if (isToLeft) { //光标左移的处理
            if (collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_leftCursor))) {
                if (FindLieFenUtils.m_nLLineSite > 0) {
                    FindLieFenUtils.LLineToLOrR(true);
                }
            } else if (collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_rightCursor))) {
                if (FindLieFenUtils.m_nRLineSite > 0) {
                    FindLieFenUtils.RLineToLOrR(true);
                }
            }
        } else { //光标右移的处理
            if (collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_leftCursor))) {
                FindLieFenUtils.LLineToLOrR(false);
            } else if (collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_rightCursor))) {
                FindLieFenUtils.RLineToLOrR(false);
            }
        }
        collect_cameraView.onMove();
        if (isLongClick) {
            Message msg = new Message();
            msg.what = 1000;
            msg.obj = isToLeft;
            mHandler.sendMessageDelayed(msg, 50);
        }
    }

    /**
     * 退出Activity
     */
    private void activityFinish() {
        stopCameraView();
        this.finish();
    }


    /**
     * 人为移动光标的时候，左右两个裂缝标志的切换
     */
    private void onSelectCursor() {
        if (collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_leftCursor))) {
            collect_cameraView.setZY(2);
            collect_Cursor_bt.setText(getStr(R.string.str_rightCursor));
        } else if (collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_rightCursor))) {
            collect_cameraView.setZY(1);
            collect_Cursor_bt.setText(getStr(R.string.str_leftCursor));
        }
    }

    /**
     * @param type Catition.CollectView.START   1:开始监测之后的布局
     *             Catition.CollectView.STOP    2：停止监测之后的布局（即初始布局）
     *             Catition.CollectView.TAKEPHOTO   3：进行照片预拍的布局
     */
    private void changeStartStopTakeView(int type) {
        switch (type) {
            case Catition.CollectView.START:
                collect_sfv.setVisibility(View.VISIBLE);
                collect_filelist_ll.setVisibility(View.VISIBLE);
                collect_startStop_bt.setText(getStr(R.string.str_stopCollect));
                collect_save_bt.setText(getStr(R.string.str_takePhoto));
                collect_autoOrhand_bt.setText(getStr(R.string.str_Auto));
                collect_Cursor_bt.setText("");
                collect_left_bt.setText("");
                collect_right_bt.setText("");
                break;
            case Catition.CollectView.STOP:
                collect_sfv.setVisibility(View.GONE);
                collect_filelist_ll.setVisibility(View.VISIBLE);
                collect_startStop_bt.setText(getStr(R.string.str_startCollect));
                collect_save_bt.setText("");
                collect_autoOrhand_bt.setText("");
                collect_Cursor_bt.setText("");
                collect_left_bt.setText("");
                collect_right_bt.setText("");
                break;
            case Catition.CollectView.TAKEPHOTO:
                collect_sfv.setVisibility(View.GONE);
                collect_filelist_ll.setVisibility(View.GONE);
                collect_proName_et.setText(m_strSaveProName);
                collect_fileName_et.setText(m_strSaveGJName);
                collect_autoOrhand_bt.setText("");
                collect_startStop_bt.setText(getStr(R.string.str_startCollect));
                collect_save_bt.setText(getStr(R.string.str_save));
                collect_Cursor_bt.setText(getStr(R.string.str_leftCursor));
                collect_left_bt.setText(getStr(R.string.str_toLeft));
                collect_right_bt.setText(getStr(R.string.str_toRight));
                break;
        }
    }

    /**
     * 停止进行拍摄
     */
    private void stopCameraView() {
        collect_cameraView.setStopView();
        collect_cameraView.closeCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if(null != collect_cameraView.m_Camera) {
            collect_cameraView.m_Camera.addCallbackBuffer(collect_cameraView.m_Buffer);
            collect_cameraView.m_Camera.setPreviewCallbackWithBuffer(collect_cameraView);
            collect_cameraView.m_Camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
