package com.ck.activity;

import android.graphics.Bitmap;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ck.collect.CameraView;
import com.ck.collect.OnOpenCameraListener;
import com.ck.collect.View_LongButton;
import com.ck.utils.Catition;
import com.hc.u8x_ck.R;

public class CollectActivity extends TitleBaseActivity implements View.OnClickListener {

    private SurfaceView collect_sfv;
    private SurfaceHolder mHolder;
    private CameraView collect_cameraView;

    private Button collect_startStop_bt; //开始\停止 按钮
    private Button collect_save_bt; //预拍 \ 存储  按钮

    private Button collect_autoOrhand_bt;//自动计算或手动计算裂缝位置
    private Button collect_Cursor_bt;//选择检测光标
    private View_LongButton collect_left_lbt;//光标向左移
    private View_LongButton collect_right_lbt;//光标向右移


    private LinearLayout collect_filelist_ll; //文件列表的布局

    private CheckBox collect_blackWrite_cb;// 是否显示黑白图

    @Override
    protected int setLayout() {
        return R.layout.ac_collect;
    }

    @Override
    protected void initView() {
        super.initView();
        collect_sfv = findViewById(R.id.collect_sfv);
        collect_cameraView = findViewById(R.id.collect_cameraView);
        collect_startStop_bt = findViewById(R.id.collect_startStop_bt);
        collect_save_bt = findViewById(R.id.collect_save_bt);
        collect_filelist_ll = findViewById(R.id.collect_filelist_ll);
        collect_blackWrite_cb = findViewById(R.id.collect_blackWrite_cb);
        collect_autoOrhand_bt = findViewById(R.id.collect_autoOrhand_bt);
        collect_Cursor_bt = findViewById(R.id.collect_Cursor_bt);
        collect_left_lbt = findViewById(R.id.collect_left_lbt);
        collect_right_lbt = findViewById(R.id.collect_right_lbt);
    }

    @Override
    protected void initListener() {
        super.initListener();
        collect_startStop_bt.setOnClickListener(this);
        findViewById(R.id.collect_back_bt).setOnClickListener(this);
        collect_save_bt.setOnClickListener(this);
        collect_autoOrhand_bt.setOnClickListener(this);
        collect_Cursor_bt.setOnClickListener(this);
        collect_blackWrite_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                collect_cameraView.setBlackWrite(b);
            }
        });
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
            case R.id.collect_autoOrhand_bt : // TODO: 手动计算还是自动计算
                onCountMode();
                break;
            case R.id.collect_Cursor_bt : //TODO： 进行游标的切换
                onSelectCursor();
                break;
            case R.id.collect_back_bt: //TODO: 返回
                activityFinish(); //返回
                break;
        }
    }

    /**
     * 开始进行测量
     */
    private void onCollectStart() {
        collect_cameraView.setStartView();
        mHolder = collect_sfv.getHolder();
        collect_cameraView.setHolder(mHolder);
        collect_cameraView.onenCamera(new OnOpenCameraListener() {
            @Override
            public void OnOpenCameraResultListener(boolean bResult) {
                if (bResult) {
                    collect_cameraView.setCountMode(true);
                    collect_cameraView.setZY(0);
                    changeStartStopTakeView(Catition.CollectView.START);
                } else {
                    Toast.makeText(CollectActivity.this, "请安装指定的摄像头", Toast.LENGTH_SHORT).show();
                }
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
    }

    /**
     * 进行保存
     */
    private void onTakePic() {
        collect_cameraView.setDrawingCacheEnabled(true);
        collect_cameraView.onTakePic(true);
        collect_cameraView.buildDrawingCache();
        Bitmap drawingCache = collect_cameraView.getDrawingCache();

        changeStartStopTakeView(Catition.CollectView.STOP);
        showToast(getStr(R.string.str_saveSuccess));
        onCollectStart();//保存成功之后继续进行检测
    }

    /**
     * 设置检测模式，自动检测还是手动检测
     */
    private void onCountMode() {
        if(collect_autoOrhand_bt.getText().toString().equals(getStr(R.string.str_Auto))){
            collect_cameraView.setCountMode(false);//手动计算
            collect_cameraView.setZY(1);
            collect_autoOrhand_bt.setText(getStr(R.string.str_Hand));
            collect_Cursor_bt.setText(getStr(R.string.str_leftCursor));
            collect_left_lbt.setText(getStr(R.string.str_toLeft));
            collect_right_lbt.setText(getStr(R.string.str_toRight));
        }else if(collect_autoOrhand_bt.getText().toString().equals(getStr(R.string.str_Hand))){
            collect_cameraView.setCountMode(true);//自动计算
            collect_cameraView.setZY(0);
            collect_autoOrhand_bt.setText(getStr(R.string.str_Auto));
            collect_Cursor_bt.setText("");
            collect_left_lbt.setText("");
            collect_right_lbt.setText("");
        }
    }

    /**
     * 退出Activity
     */
    private void activityFinish() {
        collect_cameraView.closeCamera();
        this.finish();
    }

    /**
     * 人为移动光标的时候，左右两个裂缝标志的切换
     */
    private void onSelectCursor() {
        if(collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_leftCursor))){
            collect_cameraView.setZY(2);
            collect_Cursor_bt.setText(getStr(R.string.str_rightCursor));
        }else if(collect_Cursor_bt.getText().toString().equals(getStr(R.string.str_rightCursor))){
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
        collect_Cursor_bt.setText("");
        collect_left_lbt.setText("");
        collect_right_lbt.setText("");
        switch (type) {
            case Catition.CollectView.START:
                collect_sfv.setVisibility(View.VISIBLE);
                collect_filelist_ll.setVisibility(View.VISIBLE);
                collect_startStop_bt.setText(getStr(R.string.str_stopCollect));
                collect_save_bt.setText(getStr(R.string.str_takePhoto));
                collect_autoOrhand_bt.setText(getStr(R.string.str_Auto));
                break;
            case Catition.CollectView.STOP:
                collect_sfv.setVisibility(View.GONE);
                collect_filelist_ll.setVisibility(View.VISIBLE);
                collect_startStop_bt.setText(getStr(R.string.str_startCollect));
                collect_save_bt.setText("");
                collect_autoOrhand_bt.setText("");
                break;
            case Catition.CollectView.TAKEPHOTO:
                collect_sfv.setVisibility(View.GONE);
                collect_filelist_ll.setVisibility(View.GONE);
                collect_autoOrhand_bt.setText("");
                collect_startStop_bt.setText(getStr(R.string.str_startCollect));
                collect_save_bt.setText(getStr(R.string.str_save));
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

}
