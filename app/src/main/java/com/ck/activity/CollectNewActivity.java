package com.ck.activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ck.base.TitleBaseActivity;
import com.ck.bean.MeasureDataBean;
import com.ck.dlg.CreateObjFileDialog;
import com.ck.dlg.LoadingDialog;
import com.ck.dlg.SigleBtMsgDialog;
import com.ck.listener.OnOpenCameraListener;
import com.ck.ui.OpenCvCameraView;
import com.ck.utils.CarmeraDataDone;
import com.ck.utils.DateUtil;
import com.ck.utils.FileUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.PathUtils;
import com.ck.utils.PreferenceHelper;
import com.ck.utils.Stringutil;
import com.google.gson.Gson;
import com.hc.u8x_ck.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * @author fei
 * @date on 2018/12/7 0007
 * @describe TODO :
 **/
public class CollectNewActivity extends TitleBaseActivity implements View.OnClickListener,View.OnLongClickListener{

    private String TAG = CollectNewActivity.class.getSimpleName();
    private JavaCameraView collect_OpenCvCamera;
    private OpenCvCameraView collect_cameraView;

//    private LinearLayout collect_key_ll;
    private Button collect_blackWrite_bt;
    private boolean isLoading = false;
    private LinearLayout colleact_drag_ll;
    private LinearLayout collect_boot_ll;
    private LoadingDialog mLoadingDialog;
    private SigleBtMsgDialog cameraErrorDialog;

    private String proName = "工程1", fileName = "构件1";

    private int collectState = 0;  //0:初始进图状态 1：预拍图状态

    private LoaderCallbackInterface mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // OpenCV引擎初始化加载成功
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "OpenCV loaded successfully.");
                    // 连接到Camera
                    collect_OpenCvCamera.enableView();
                    colleact_drag_ll.setVisibility(View.GONE);
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected int initLayout() {
        return R.layout.ac_acoolectnew;
    }

    @Override
    protected void initView() {
        super.initView();
        collect_OpenCvCamera = findView(R.id.collect_OpenCvCamera);
        collect_cameraView = findView(R.id.collect_cameraView);
//        collect_key_ll = findView(R.id.collect_key_ll);
        collect_blackWrite_bt = findView(R.id.collect_blackWrite_bt);
        colleact_drag_ll = findView(R.id.colleact_drag_ll);
        collect_boot_ll = findView(R.id.collect_boot_ll);
        mLoadingDialog = new LoadingDialog(this);
        initCamera();
    }


    @Override
    protected void initData() {
        super.initData();
        proName = PreferenceHelper.getString("obj", "proname");
        if (Stringutil.isEmpty(proName)) {
            proName = "工程1";
        }
        fileName = PreferenceHelper.getString("obj", "gjname");
        if (Stringutil.isEmpty(fileName)) {
            fileName = "构件1";
        }
    }


    @Override
    protected void initListener() {
        super.initListener();
        collect_blackWrite_bt.setOnClickListener(this);
        collect_blackWrite_bt.setOnLongClickListener(this);
        colleact_drag_ll.setOnLongClickListener(this);
        collect_boot_ll.setOnDragListener(new myDragEventListener());
    }

    private void initCamera() {
        // 注册Camera连接状态事件监听器
        collect_OpenCvCamera.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {

            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public Mat onCameraFrame(final CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//                if (isLoading) {
//                    isLoading = false;
//                    stopLoading();
//                }
                if(null != cameraErrorDialog && cameraErrorDialog.isShowing()) {
                    cameraErrorDialog.dismiss();
                }
                if (collectState == 0) {
                     collect_cameraView.setDataMat(inputFrame.rgba(), inputFrame.gray());
                }
                return inputFrame.rgba();
            }

            @Override
            public void onCameraError() {
                Log.i("fei","");
                CollectNewActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showCameraErrorDia();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        showLoading();
//        isLoading = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                onCollectStart();
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.collect_blackWrite_bt: //TODO:黑白图
                if (collect_cameraView.isBlackWrite) {
                    collect_cameraView.setBlackWrite(false, true);
                } else {
                    collect_cameraView.setBlackWrite(true, true);
                }
                break;
        }
    }

    @Override
        public boolean onLongClick(View view) {
        switch (view.getId()){
            case R.id.collect_blackWrite_bt :
            case R.id.colleact_drag_ll:
                ClipData.Item item = new ClipData.Item("11");
                ClipData dragData = new ClipData("11", new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
                // Instantiates the drag shadow builder.
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(colleact_drag_ll);
                colleact_drag_ll.startDrag(dragData, myShadow, null, 0);
                colleact_drag_ll.setVisibility(View.GONE);
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

            }
            @Override
            public void onCarameError() {
                Log.i("fei", "摄像机已停止，正在重启");
                if (null != collect_OpenCvCamera) {
                    collect_OpenCvCamera.disableView();
                }
                onCollectStart();
            }
        });
//        if (!OpenCVLoader.initDebug()) {
//            Log.w(TAG, "static loading library fail,Using Manager for initialization");
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
//        } else {
//            Log.w(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//        }
        collectState = 0;
    }

    /**
     * 进行预拍
     */
    private void onBeforTakePic() {
        stopCameraView();
//        collect_cameraView.changeEx();//处理畸变
        collect_cameraView.setZY(1,false);
        collect_cameraView.onBeforTakePic();//预拍之后的预处理
        collect_OpenCvCamera.disableView();
        collect_OpenCvCamera.setVisibility(View.GONE);
        collect_cameraView.setVisibility(View.VISIBLE);
        colleact_drag_ll.setVisibility(View.VISIBLE);
        collectState = 1;//更新状态标志
    }

    /**
     * 进行存储
     */
    private void onSaveFile() {
        if (Stringutil.isEmpty(proName) || Stringutil.isEmpty(fileName)) {
            //进行新建
            onMakeNews();
        } else {
            //进行保存
            onSave();
        }
    }

    /**
     * 保存照片数据
     */
    private void onSave() {
        File isHaveFile = new File(PathUtils.PROJECT_PATH + "/" + proName + "/" + fileName + ".CK");
        if (isHaveFile.exists()) {
            showToast("文件名重复");
            return;
        }
        collect_cameraView.setZY(0,false); //恢复光标的颜色
//        collect_cameraView.setDrawingCacheEnabled(true);
//        FileUtil.saveBmpImageFile(collect_cameraView.m_DrawBitmap,
//                "/" + proName, fileName, "%s.bmp");
//        FileUtil.saveDrawBmpFile(collect_cameraView.getDrawingCache(),
//                "/" + proName, fileName, "%s.bmp");
//        collect_cameraView.setDrawingCacheEnabled(false);
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        MeasureDataBean dataBean = new MeasureDataBean();
        dataBean.setObjName(proName);
        dataBean.setFileName(fileName + ".bmp");
        dataBean.setObjCreateDate(format.format(new File(PathUtils.PROJECT_PATH + "/" + proName).lastModified()));
        File file = new File(PathUtils.PROJECT_PATH + "/" + proName + "/" + fileName + ".bmp");
        dataBean.setFileCreateDate(format.format(file.lastModified()));
        dataBean.setJudgeStyle(MeasureDataBean.JUDGESTYLE_HORIZ);
        dataBean.setMeasureDate(DateUtil.getDate("yyyy/MM/dd"));
        dataBean.setWidth(collect_cameraView.width);
        dataBean.setAvage(FindLieFenUtils.bytGrayAve);
        dataBean.setLeftY(FindLieFenUtils.m_nCLYLineSite);
        dataBean.setLeftX(FindLieFenUtils.m_nCLXLineSite);
        dataBean.setRightY(FindLieFenUtils.m_nCRYLineSite);
        dataBean.setRightX(FindLieFenUtils.m_nCRXLineSite);
        dataBean.setCheckStyle(MeasureDataBean.CHECKSTYLE_WIDTH);
        dataBean.setFileState(MeasureDataBean.FILESTATE_USERING);
        dataBean.setFileSize(file.length());
        dataBean.setDelDate("0000/00/00");
//        dataBean.setImageBytes(BMPUtils.bitmapToByteArr(collect_cameraView.m_DrawBitmap));
        Gson gson = new Gson();
        FileUtil.saveCKFile(gson.toJson(dataBean), "/" + proName, fileName,
                "%s.CK",collect_cameraView.m_DrawBitmap);
//        DBService.getInstence(this).SetMeasureData(dataBean);

        //刷新列表
        collect_cameraView.setBlackWrite(false, false);
        fileName = FileUtil.GetDigitalPile(fileName);//文件名称默认增加1
        PreferenceHelper.setString("obj","gjname",fileName);
        PreferenceHelper.setString("obj","proname",proName);
        showToast(getStr(R.string.str_saveSuccess));
        collect_cameraView.setStartView();
        collect_OpenCvCamera.enableView();
        collect_OpenCvCamera.setVisibility(View.VISIBLE);
        collect_cameraView.setVisibility(View.VISIBLE);
        colleact_drag_ll.setVisibility(View.GONE);
        collectState = 0;//更新状态标志
    }

    /**
     * 进行新建
     */
    private void onMakeNews() {
        showObj_Gj_FileListOrCreate("测试新建");
    }


    /**
     * 用于显示选择工程列表，构件列表，新建文件名称的Dialog
     *
     * @param title 1： 工程列表选择以及新建
     *              2： 构件列表选择以及新建
     *              3： 文件名称的新建
     */
    private void showObj_Gj_FileListOrCreate(final String title) {
        if (null == title) {
            return;
        }
        final CreateObjFileDialog dialog = new CreateObjFileDialog(CollectNewActivity.this);
        dialog.show();
    }

    /**
     * 停止进行拍摄
     */
    private void stopCameraView() {
        collect_cameraView.setStopView();
        collect_cameraView.closeCamera();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F3) { //摄像头上的按键
            if (collectState == 0) { //0:正在采集
                onBeforTakePic();//预拍
            } else if (collectState == 1) { //1：处于预拍的照片状态
                onSaveFile();//进行存储
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) { //返回按键
            // 断开与Camera的连接
            finishActivity();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (!collect_cameraView.isStart) {
                onMove(collect_cameraView.m_nDrawFlag, 1);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (!collect_cameraView.isStart) {
                onMove(collect_cameraView.m_nDrawFlag, 2);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (!collect_cameraView.isStart) {
                onMove(collect_cameraView.m_nDrawFlag, 3);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (!collect_cameraView.isStart) {
                onMove(collect_cameraView.m_nDrawFlag, 4);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_F1) {
            //存储
            if(collectState == 1){
                onSaveFile();//进行存储
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_F2) {
            //切换
            if(collectState == 1) {
                if (collect_cameraView.m_nDrawFlag == 2 || collect_cameraView.m_nDrawFlag == 0) {
                    collect_cameraView.setZY(1,false);
                } else {
                    collect_cameraView.setZY(2,false);
                }
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 退出检测界面
     */
    private void finishActivity(){
        if (collect_OpenCvCamera != null) {
            collect_OpenCvCamera.disableView();
        }
        collect_cameraView.setStopView();
        collect_cameraView.closeCamera();
        CarmeraDataDone.openHardDevJni(1,1,0);
        finish();
    }

    /**
     * 光标的左右移动
     *
     * @param isLeft 1:左侧光标  2：右侧光标
     * @param ori    ：1：向上  2 ： 向下  3：向左  4： 向右
     */
    private void onMove(int isLeft, int ori) {
        if (isLeft == 1) {
            FindLieFenUtils.LLineToLRUpOrD(ori);
        } else if (isLeft == 2) {
            FindLieFenUtils.RLineToLOrRUpOrD(ori);
        }
        collect_cameraView.onMove();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected class myDragEventListener implements View.OnDragListener {
        public boolean onDrag(View v, DragEvent event) {
            if (0 != event.getX() && 0 != event.getY()) {
                colleact_drag_ll.setX(event.getX()-40);
                colleact_drag_ll.setY(event.getY()-20);
            } else {
                colleact_drag_ll.setVisibility(View.VISIBLE);
            }
            final int action = event.getAction();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                        v.invalidate();
                        return true;
                    }
                    return false;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    return true;
                case DragEvent.ACTION_DROP:
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    }

    private void showLoading(){
        if(null != mLoadingDialog && !mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }

    private void stopLoading(){
        if(null != mLoadingDialog && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    /**
     * 摄像头失去连接的时候进行弹窗
     */
    private void showCameraErrorDia(){
        if(null == cameraErrorDialog) {
            cameraErrorDialog = new SigleBtMsgDialog(this);
        }
        if(!cameraErrorDialog.isShowing()) {
            cameraErrorDialog.show();
            cameraErrorDialog.setTitleMsg("错误");
            cameraErrorDialog.setCancelable(false);
            cameraErrorDialog.setMsg("摄像机连接异常\n请确认摄像机是否连接");
            cameraErrorDialog.setBtTxt("退出");
            cameraErrorDialog.setOnBtClickListener(new SigleBtMsgDialog.OnBtClickListener() {
                @Override
                public void onBtClick() {
                    finishActivity();
                }
            });
        }
    }

    /**
     * 单按钮信息提示的弹框
     * @param msg
     */
    private void showSigleMsg(String msg) {
        final SigleBtMsgDialog dialog = new SigleBtMsgDialog(CollectNewActivity.this);
        dialog.show();
        dialog.setTitleMsg(getStr(R.string.str_prompt));
        dialog.setMsg(msg);
        dialog.setBtTxt(getStr(R.string.str_ok));
        dialog.setOnBtClickListener(new SigleBtMsgDialog.OnBtClickListener() {
            @Override
            public void onBtClick() {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onStop() {
        stopLoading();
        super.onStop();
    }
}
