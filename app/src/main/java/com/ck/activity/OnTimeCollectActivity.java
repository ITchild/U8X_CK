package com.ck.activity;

import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ck.base.TitleBaseActivity;
import com.ck.bean.MeasureDataBean;
import com.ck.db.DBService;
import com.ck.listener.OnOpenCameraListener;
import com.ck.ui.CameraView;
import com.ck.ui.WheelView;
import com.ck.ui.adapter.NumericWheelAdapter;
import com.ck.utils.DateUtil;
import com.ck.utils.FileUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.PathUtils;
import com.ck.utils.Stringutil;
import com.hc.u8x_ck.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author fei
 * @date on 2018/11/12 0012
 * @describe TODO :
 **/
public class OnTimeCollectActivity extends TitleBaseActivity implements View.OnClickListener, SurfaceHolder.Callback{

    private TextView collect_proName_tv;//工程名称
    private TextView collect_gjName_tv;//构件名称
    private TextView collect_fileName_tv;//文件名称
    private TextView ontime_state_tv;//监测状态
    private TextView ontime_alreadtime_tv;//已测时间
    private TextView ontime_alltimeMin_tv;//监测时长 （分）
    private TextView ontime_alltimeSec_tv;//监测时长 （秒）
    private TextView ontime_oneTimeMin_tv;//间隔时长 （分）
    private TextView ontime_oneTimeSec_tv;//间隔时长 （秒）

    private WheelView ontime_wheel_wv;
    private Button ontime_startOrEnd_bt;
    private CameraView ontime_cameraView_cv;
    private SurfaceView ontime_sfv;
    private SurfaceHolder mHolder;


    private String proName; //工程名称
    private String gjName; // 构件名称
    private String fileName; //文件名称
    private int alreadTime = 0; // 已测试间
    private int allTime = 0; //监测总时长
    private int takeTime = 0; //监测间隔时间

    @Override
    protected int initLayout() {
        return R.layout.ac_ontimecollect;
    }

    @Override
    protected void initView() {
        super.initView();
        ontime_wheel_wv = findView(R.id.ontime_wheel_wv);
        ontime_startOrEnd_bt = findView(R.id.ontime_startOrEnd_bt);
        ontime_cameraView_cv = findView(R.id.ontime_cameraView_cv);
        ontime_sfv = findView(R.id.ontime_sfv);

        collect_proName_tv = findView(R.id.collect_proName_tv);//工程名称
        collect_gjName_tv = findView(R.id.collect_gjName_tv);//构件名称
        collect_fileName_tv = findView(R.id.collect_fileName_tv);//文件名称
        ontime_state_tv = findView(R.id.ontime_state_tv);//监测状态
        ontime_alreadtime_tv = findView(R.id.ontime_alreadtime_tv);//已测时间
        ontime_alltimeMin_tv = findView(R.id.ontime_alltimeMin_tv);//监测时长 （分）
        ontime_alltimeSec_tv = findView(R.id.ontime_alltimeSec_tv);//监测时长 （秒）
        ontime_oneTimeMin_tv = findView(R.id.ontime_oneTimeMin_tv);//间隔时长 （分）
        ontime_oneTimeSec_tv = findView(R.id.ontime_oneTimeSec_tv);//间隔时长 （秒）
    }

    @Override
    protected void initData() {
        super.initData();
        initWheelView();
        baseTitle_title_tv.setText("定时监测");
        ontime_cameraView_cv.isCanMove = false;
    }

    @Override
    protected void initListener() {
        super.initListener();
        ontime_startOrEnd_bt.setOnClickListener(this);
    }

    /**
     * 初始化数字选择框（选择轮）
     */
    private void initWheelView(){
        NumericWheelAdapter adapter = new NumericWheelAdapter(0,60,"%02d");
        ontime_wheel_wv.setAdapter(adapter);
        ontime_wheel_wv.setVisibleItems(7);
        ontime_wheel_wv.setCyclic(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ontime_startOrEnd_bt :
                if(ontime_startOrEnd_bt.getText().toString().equals(getStr(R.string.str_ontimeCollect_start))){
                    //开始
                    onCollectStart();
                }else{
                    //结束
                    onCollectStop();
                }
                break;
        }
    }


    /**
     * 开始进行测量
     */
    private void onCollectStart() {
        if(!checkAndMakeMsg()){
            return;
        }
        onTimeHandler.postDelayed(onTimeRunnable,2000);
        ontime_state_tv.setText("检测中");
        ontime_cameraView_cv.setStartView();
        ontime_startOrEnd_bt.setText(getStr(R.string.str_ontimeCpllect_stop));
        ontime_cameraView_cv.onenCamera(new OnOpenCameraListener() {
            @Override
            public void OnOpenCameraResultListener(boolean bResult) {
                if (bResult) {
                    if (null == mHolder) {
                        mHolder = ontime_sfv.getHolder();
                        mHolder.addCallback(OnTimeCollectActivity.this);
                    }
                    ontime_cameraView_cv.setHolder(mHolder);
                    ontime_cameraView_cv.setCountMode(true);
                    ontime_cameraView_cv.setZY(0);
                } else {
                    showToast("请安装指定的摄像头");
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
     * 检测开始监测之前各项配置是否正确
     * @return
     */
    private boolean checkAndMakeMsg(){
        proName = collect_proName_tv.getText().toString();
        if(Stringutil.isEmpty(proName)){
            showToast("请填写工程名称");
            return false;
        }
        gjName = collect_gjName_tv.getText().toString();
        if(Stringutil.isEmpty(gjName)){
            showToast("请填写构件名称");
            return false;
        }
        fileName = collect_fileName_tv.getText().toString();
        if(Stringutil.isEmpty(fileName)){
            showToast("请填写文件名称");
            return false;
        }
        String allTimeF = ontime_alltimeMin_tv.getText().toString();
        String allTimeS = ontime_alltimeSec_tv.getText().toString();
        allTime = Integer.parseInt(allTimeF)*60 + Integer.parseInt(allTimeS);
        if(allTime <= 0){
            showToast("请填写监测时长");
            return false;
        }
        String oneTimeF = ontime_oneTimeMin_tv.getText().toString();
        String oneTimeS = ontime_oneTimeSec_tv.getText().toString();
        takeTime = Integer.parseInt(oneTimeF) * 60 + Integer.parseInt(oneTimeS);
        if(takeTime <= 0){
            showToast("请填写监测间隔");
            return false;
        }
        alreadTime = 0;
        return true;
    }

    /**
     * 停止测量
     */
    private void onCollectStop() {
        ontime_startOrEnd_bt.setText(getStr(R.string.str_ontimeCollect_start));
        onTimeHandler.removeCallbacks(onTimeRunnable);
        ontime_state_tv.setText("未开启");
        stopCameraView();
        ontime_cameraView_cv.showOriginalView();
    }

    /**
     * 停止进行拍摄
     */
    private void stopCameraView() {
        ontime_cameraView_cv.setStopView();
        ontime_cameraView_cv.closeCamera();
    }


    private final static int TAKEPIC = 10003;
    private final static int MAKESTOP = 10004;
    private Handler onTimeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case TAKEPIC : //进行拍照
                    onTakePic();
                    break;
                case MAKESTOP : //停止
                    onCollectStop();
                    break;
            }
        }
    };
    private Runnable onTimeRunnable = new Runnable() {
        @Override
        public void run() {
            alreadTime ++;
            ontime_alreadtime_tv.setText(alreadTime/60 + "分"+ alreadTime%60+"秒");
            if(alreadTime%takeTime == 0){
                //进行拍照
                onTimeHandler.sendEmptyMessage(TAKEPIC);
            }
            if(alreadTime <allTime) {
                onTimeHandler.postDelayed(onTimeRunnable, 1000);
            }else{
                //
                onTimeHandler.sendEmptyMessage(MAKESTOP);
            }
        }
    };

    /**
     * 进行保存
     */
    private void onTakePic() {
        File isHaveFile = new File(PathUtils.PROJECT_PATH+"/" + proName + "/" + gjName+"/"+fileName+".bmp");
        if(isHaveFile.exists()){
            showToast("文件名重复");
            return;
        }
        List<MeasureDataBean> isHaveData = DBService.getInstence(this).
                getMeasureData(proName,gjName,fileName,MeasureDataBean.FILESTATE_USERING);
        if(null != isHaveData && isHaveData.size()>0){
            showToast("文件名重复");
            return;
        }
        FileUtil.saveBmpImageFile(ontime_cameraView_cv.m_DrawBitmap,
                "/" + proName + "/" + gjName, fileName, "%s.bmp");
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        MeasureDataBean dataBean = new MeasureDataBean();
        dataBean.setObjName(proName);
        dataBean.setGjName(gjName);
        dataBean.setFileName(fileName + ".bmp");
        dataBean.setObjCreateDate(format.format(new File(PathUtils.PROJECT_PATH + "/" + proName).lastModified()));
        dataBean.setGjCreateDate(format.format(new File(PathUtils.PROJECT_PATH + "/" + proName + "/" + gjName).lastModified()));
        File file = new File(PathUtils.PROJECT_PATH + "/" + proName + "/" + gjName + "/" + fileName + ".bmp");
        dataBean.setFileCreateDate(format.format(file.lastModified()));
        dataBean.setJudgeStyle(MeasureDataBean.JUDGESTYLE_HORIZ);
        dataBean.setMeasureDate(DateUtil.getDate("yyyy/MM/dd"));
        dataBean.setWidth(ontime_cameraView_cv.width);
        dataBean.setAvage(FindLieFenUtils.bytGrayAve);
        dataBean.setLeftY(FindLieFenUtils.m_nY);
        dataBean.setLeftX(FindLieFenUtils.m_nLLineSite);
        dataBean.setRightY(FindLieFenUtils.m_nY);
        dataBean.setRightX(FindLieFenUtils.m_nRLineSite);
        dataBean.setCheckStyle(MeasureDataBean.CHECKSTYLE_WIDTH);
        dataBean.setFileState(MeasureDataBean.FILESTATE_USERING);
        dataBean.setFileSize(file.length());
        dataBean.setDelDate("0000/00/00");
        DBService.getInstence(this).SetMeasureData(dataBean);
        //刷新列表
        showToast(getStr(R.string.str_saveSuccess));
        fileName = FileUtil.GetDigitalPile(fileName);
        collect_fileName_tv.setText(fileName);
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (null != ontime_cameraView_cv.m_Camera) {
            ontime_cameraView_cv.m_Camera.addCallbackBuffer(ontime_cameraView_cv.m_Buffer);
            ontime_cameraView_cv.m_Camera.setPreviewCallbackWithBuffer(ontime_cameraView_cv);
            ontime_cameraView_cv.m_Camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }
}
