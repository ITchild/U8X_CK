package com.ck.activity;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ck.base.TitleBaseActivity;
import com.ck.bean.MeasureDataBean;
import com.ck.collect.OnOpenCameraListener;
import com.ck.db.DBService;
import com.ck.ui.CameraView;
import com.ck.utils.Catition;
import com.ck.utils.DateUtil;
import com.ck.utils.FileUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;


public class CollectActivity extends TitleBaseActivity implements View.OnClickListener, View.OnLongClickListener,SurfaceHolder.Callback{

    public String m_strSaveProName = "默认工程"; //保存图片的默认工程名称
    public String m_strSaveGJName = "默认构件"; //保存图片的默认文件名称
    private SurfaceView collect_sfv;
    private SurfaceHolder mHolder;
    private CameraView collect_cameraView;
    private Button collect_startStop_bt; //开始\停止 按钮
    private Button collect_save_bt; //预拍 \ 存储  按钮
    private Button collect_autoOrhand_bt;//自动计算或手动计算裂缝位置
    private Button collect_Cursor_bt;//选择检测光标
    private Button collect_left_bt;//光标向左移
    private Button collect_right_bt;//光标向右移
    private EditText collect_proName_et;
    private EditText collect_gjName_et;
    private EditText collect_fileName_et;
    private Button collect_blackWrite_bt;// 是否显示黑白图
    private Button collect_pic_bt;
    private Button collect_enlarge_bt; //放大
    private Button collect_Lessen_bt; //缩小

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
        collect_blackWrite_bt = findView(R.id.collect_blackWrite_bt);
        collect_pic_bt = findView(R.id.collect_pic_bt);
        collect_enlarge_bt = findView(R.id.collect_enlarge_bt);
        collect_Lessen_bt = findView(R.id.collect_Lessen_bt);
        collect_proName_et = findView(R.id.collect_proName_et);
        collect_gjName_et = findView(R.id.collect_gjName_et);
        collect_fileName_et = findView(R.id.collect_fileName_et);
        collect_autoOrhand_bt = findView(R.id.collect_autoOrhand_bt);
        collect_Cursor_bt = findView(R.id.collect_Cursor_bt);
        collect_left_bt = findView(R.id.collect_left_bt);
        collect_right_bt = findView(R.id.collect_right_bt);
    }

    @Override
    protected void initData() {
        super.initData();
        Intent intent = getIntent();
        if(null == intent){
            return;
        }
        String objName = intent.getStringExtra("objName");
        String gjName = intent.getStringExtra("gjName");
        if(!isStrEmpty(objName) && !isStrEmpty(gjName)){//从文件管理界面跳转过来
            File file = new File(PathUtils.PROJECT_PATH+"/"+objName+"/"+gjName);
            File[] files = file.listFiles();
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(files[0].getAbsolutePath());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            collect_cameraView.setBitmap(BitmapFactory.decodeStream(fis));
        }
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
        collect_blackWrite_bt.setOnClickListener(this);
        collect_pic_bt.setOnClickListener(this);
        collect_enlarge_bt.setOnClickListener(this);
        collect_Lessen_bt.setOnClickListener(this);
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
            case R.id.collect_blackWrite_bt : //TODO : 黑白图
                collect_cameraView.setBlackWrite(true,true);
                break;
            case R.id.collect_pic_bt :  // TODO : 原图
                collect_cameraView.setBlackWrite(false,true);
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
        changeStartStopTakeView(Catition.CollectView.TAKEPHOTO);
        collect_cameraView.onBeforTakePic();//预拍之后的预处理
    }

    /**
     * 进行保存
     */
    private void onTakePic() {
        String proName = collect_proName_et.getText().toString();
        String gjName = collect_gjName_et.getText().toString();
        String fileName = collect_fileName_et.getText().toString();
        if (isStrEmpty(proName)) {
            showToast("工程名不能为空");
            return;
        } else if (isStrEmpty(gjName)) {
            showToast("构件名不能为空");
            return;
        }else if (isStrEmpty(fileName)){
            showToast("文件名不能为空");
            return;
        }
        FileUtil.saveBmpImageFile(collect_cameraView.m_DrawBitmap,
                "/"+proName+"/"+gjName, fileName, "%s.bmp");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        MeasureDataBean dataBean = new MeasureDataBean();
        dataBean.setObjName(proName);
        dataBean.setGjName(gjName);
        dataBean.setFileName(fileName);
        dataBean.setObjCreateDate(format.format(new File(PathUtils.PROJECT_PATH+"/"+proName).lastModified()));
        dataBean.setGjCreateDate(format.format(new File(PathUtils.PROJECT_PATH+"/"+proName+"/"+gjName).lastModified()));
        File file = new File(PathUtils.PROJECT_PATH+"/"+proName+"/"+gjName+"/"+fileName + ".bmp");
        dataBean.setFileCreateDate(format.format(file.lastModified()));
        dataBean.setJudgeStyle(MeasureDataBean.JUDGESTYLE_HORIZ);
        dataBean.setMeasureDate(DateUtil.getDate("yyyy/MM/dd"));
        dataBean.setWidth(collect_cameraView.width);
        dataBean.setLeftY(FindLieFenUtils.m_nY);
        dataBean.setLeftX(FindLieFenUtils.m_nLLineSite);
        dataBean.setRightY(FindLieFenUtils.m_nY);
        dataBean.setRightX(FindLieFenUtils.m_nRLineSite);
        dataBean.setCheckStyle(MeasureDataBean.CHECKSTYLE_WIDTH);
        dataBean.setFileState(MeasureDataBean.FILESTATE_USERING);
        dataBean.setFileSize(file.getTotalSpace());
        dataBean.setDelDate("0000/00/00");
        DBService.getInstence(this).SetMeasureData(dataBean);
        //刷新列表
        onCollectStart();//保存成功之后继续进行检测
        collect_cameraView.setBlackWrite(false,false);
        collect_cameraView.setLargeOrSmall(false,false);
        showToast(getStr(R.string.str_saveSuccess));
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
                collect_startStop_bt.setText(getStr(R.string.str_stopCollect));
                collect_save_bt.setText(getStr(R.string.str_takePhoto));
                collect_autoOrhand_bt.setText(getStr(R.string.str_Auto));
                collect_Cursor_bt.setText("");
                collect_left_bt.setText("");
                collect_right_bt.setText("");
                break;
            case Catition.CollectView.STOP:
                collect_sfv.setVisibility(View.GONE);
                collect_startStop_bt.setText(getStr(R.string.str_startCollect));
                collect_save_bt.setText("");
                collect_autoOrhand_bt.setText("");
                collect_Cursor_bt.setText("");
                collect_left_bt.setText("");
                collect_right_bt.setText("");
                break;
            case Catition.CollectView.TAKEPHOTO:
                collect_sfv.setVisibility(View.GONE);
                collect_proName_et.setText(m_strSaveProName);
                collect_gjName_et.setText(m_strSaveGJName);
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
