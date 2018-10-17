package com.ck.collect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ck.adapter.ListGJAdapter;
import com.ck.adapter.ListProjectAdapter;
import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.App_DataPara;
import com.ck.main.BaseActivity;
import com.ck.ui.CameraView;
import com.ck.utils.FileUtil;
import com.ck.utils.PathUtils;
import com.ck.utils.PreferenceHelper;
import com.hc.u8x_ck.R;

import java.io.File;
import java.util.List;

public class Ui_Collect extends BaseActivity {
    public String m_strSaveProName = "默认工程";
    public String m_strSaveGJName = "默认构件1";
    boolean m_bCountMode = true;
    boolean m_bIsZCursor = true;
    int m_nDrawFlag = 0;
    DLG_SetPar m_Dlg_SetPar;
    boolean m_bStart = false;
    /**
     * 防止没有测量时，点击保存，保存黑色背景图片
     */
    boolean m_bSaveImage = false;
    private LinearLayout m_LayStart; //
    private LinearLayout m_LayStop;
    private CameraView m_CameraView;
    private TextView m_tvProName;
    private TextView m_tvGJName;
    private ListView m_ListViewPro;
    private ListView m_ListViewFile;
    private Button m_btnCursor1;
    private Button m_btnCursor2;
    private Button m_btnJiSuan;
    private SurfaceView collect_sfv;
    private View_LongButton m_btnZ1;
    private View_LongButton m_btnZ2;
    private View_LongButton m_btnY1;
    private View_LongButton m_btnY2;
    private List<ClasFileProjectInfo> m_ListProject;
    private ListProjectAdapter m_ProjectAdapter;
    private ListGJAdapter m_GJAdapter;
    private SurfaceHolder mHolder;

    private CheckBox uiCollect_blackWrite_cb;//黑白图的选择框
    private Button uiCollect_enlarge_bt;//图片放大的按钮
    private Button uiCollect_Lessen_bt; //图片缩小的按钮

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDatPara = (App_DataPara) getApplicationContext();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        AppDatPara.fDispDensity = dm.density;
        setContentView(R.layout.ui_collect);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        initView();
        initListener();
    }

    public void initView() {
        collect_sfv =  findViewById(R.id.collect_sfv);
        m_btnJiSuan = findViewById(R.id.btn_JiSuan);
        m_btnY1 = findViewById(R.id.long_btn1);
        m_btnY2 =  findViewById(R.id.long_btn2);
        m_btnZ1 =  findViewById(R.id.long_btn3);
        m_btnZ2 = findViewById(R.id.long_btn4);
        m_btnCursor1 = findViewById(R.id.btn_Cursor1);
        m_btnCursor2 = findViewById(R.id.btn_Cursor2);
        m_CameraView = findViewById(R.id.CameraView);
        m_LayStart = findViewById(R.id.ui_collect_start);
        m_LayStop = findViewById(R.id.ui_collect_stop);
        m_tvProName = findViewById(R.id.tv_proName);
        m_tvGJName = findViewById(R.id.tv_fileName);
        m_ListViewPro = findViewById(R.id.list_pro);
        m_ListViewFile = findViewById(R.id.list_file);

        uiCollect_blackWrite_cb = findViewById(R.id.uiCollect_blackWrite_cb);
        uiCollect_enlarge_bt = findViewById(R.id.uiCollect_enlarge_bt);
        uiCollect_Lessen_bt = findViewById(R.id.uiCollect_Lessen_bt);
    }

    private void initListener(){
        uiCollect_blackWrite_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                m_CameraView.setBlackWrite(b);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        if (!m_CameraView.isStart) {
            showPic();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (m_CameraView.isStart) {
                    onCollectStart(new View(Ui_Collect.this));
                }
            }
        }).start();
    }

    private void initData() {
        m_strSaveProName = PreferenceHelper.getProName();
        m_strSaveGJName = PreferenceHelper.getGJName();
        m_ListProject = PathUtils.getProFileList();

        m_ProjectAdapter = new ListProjectAdapter(this, m_ListProject);
        m_ListViewPro.setAdapter(m_ProjectAdapter);
        m_ProjectAdapter.setSelect(AppDatPara.m_nProjectSeleteNidx);
        m_tvProName.setText("工程名: " + m_strSaveProName);
        m_tvGJName.setText("文件名: " + m_strSaveGJName);

        if (m_ListProject.size() == 0) {
            if (m_GJAdapter != null)
                m_GJAdapter.clearProInfo();
            return;
        }

        m_GJAdapter = new ListGJAdapter(this, m_ListProject.get(AppDatPara.m_nProjectSeleteNidx));
        m_ListViewFile.setAdapter(m_GJAdapter);
        m_GJAdapter.setSelect(AppDatPara.m_nGJSeleteNidx);
        m_ListViewPro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (m_bStart)
                    return;
                AppDatPara.m_nProjectSeleteNidx = position;
                m_GJAdapter = new ListGJAdapter(Ui_Collect.this, m_ListProject.get(position));
                m_ListViewFile.setAdapter(m_GJAdapter);
                m_ProjectAdapter.setSelect(position);
                AppDatPara.m_nGJSeleteNidx = 0;
                showPic();
            }
        });
        m_ListViewFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (m_bStart)
                    return;
                AppDatPara.m_nGJSeleteNidx = position;
                m_GJAdapter.setSelect(AppDatPara.m_nGJSeleteNidx);
                showPic();
            }
        });
    }

    /**
     * 显示图片
     */
    public void showPic() {
        if (AppDatPara.m_nProjectSeleteNidx < 0 || m_ListProject.size() <= AppDatPara.m_nProjectSeleteNidx) {
            return;  //判断工程list有没有被选中
        }
        ClasFileProjectInfo pro = m_ListProject.get(AppDatPara.m_nProjectSeleteNidx);
        if (AppDatPara.m_nGJSeleteNidx < 0 || pro.mstrArrFileGJ.size() <= AppDatPara.m_nGJSeleteNidx) {
            return;//判断工程中的构件List有没有被选中
        }
        ClasFileGJInfo file = pro.mstrArrFileGJ.get(AppDatPara.m_nGJSeleteNidx);
        String path = PathUtils.PROJECT_PATH + File.separator + pro.mFileProjectName + File.separator + file.mFileGJName;
        Bitmap bmp = BitmapFactory.decodeFile(path);
        m_CameraView.setBitmap(bmp);  //正常图像
        m_tvProName.setText("工程名: " + pro.mFileProjectName);
        m_tvGJName.setText("文件名: " + file.mFileGJName.substring(0, file.mFileGJName.length() - 4));
    }

    /**
     * 开始进行测量
     *
     * @param v
     */
    public void onCollectStart(View v) {
        m_CameraView.setStartView();
        collect_sfv.setVisibility(View.VISIBLE);
        mHolder = collect_sfv.getHolder();
        m_CameraView.setHolder(mHolder);
        uiCollect_blackWrite_cb.setClickable(false);
        m_CameraView.onenCamera(new OnOpenCameraListener() {
            @Override
            public void OnOpenCameraResultListener(boolean bResult) {
                if (bResult) {

                    m_LayStart.setVisibility(View.GONE);
                    m_LayStop.setVisibility(View.VISIBLE);
                    // if (m_bCountMode) {
                    m_bCountMode = true;
                    m_bIsZCursor = true;
                    m_nDrawFlag = 0;
                    // }
                    m_btnJiSuan.setText("●自动计算\n  手动计算");
                    m_btnCursor2.setText("●左侧游标\n  右侧游标");
                    m_CameraView.setCountMode(m_bCountMode);
                    m_CameraView.setZY(m_nDrawFlag);
                    m_bStart = true;
                    m_bSaveImage = true;
                } else {
                    Toast.makeText(Ui_Collect.this, "请安装指定的摄像头", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (m_CameraView.isStart) {
            m_CameraView.closeCamera();
        }
    }

    @Override
    protected void onDestroy() {
        m_CameraView.closeCamera();
        AppDatPara.m_nGJSeleteNidx = -1;
        super.onDestroy();
    }

    /**
     * 拍照
     *
     * @param v
     */
    public void onTakePic(View v) {
        if (!m_bSaveImage) {
            return;
        }
        m_CameraView.setDrawingCacheEnabled(true);
        m_CameraView.onTakePic(true);
        m_CameraView.buildDrawingCache();
        Bitmap drawingCache = m_CameraView.getDrawingCache();
        FileUtil.saveBmpImageFile(drawingCache, m_strSaveProName, m_strSaveGJName, "%s.bmp");
        m_CameraView.onTakePic(false);
        m_CameraView.setDrawingCacheEnabled(false);
        AppDatPara.m_nProjectSeleteNidx = 0;
        AppDatPara.m_nGJSeleteNidx = 0;
        initData();
        m_strSaveGJName = FileUtil.GetDigitalPile(m_strSaveGJName);
        PreferenceHelper.setGJName( m_strSaveGJName);
        m_CameraView.setDrawingCacheEnabled(false);
    }

    /**
     * 裂缝的位置是手动计算还是自动识别
     *
     * @param v
     */
    public void onCountMode(View v) {
        if (m_bCountMode) {
            m_bCountMode = false;
            m_btnJiSuan.setText("  自动计算\n●手动计算");
            m_bIsZCursor = true;
            m_nDrawFlag = 1;
        } else {
            m_btnJiSuan.setText("●自动计算\n  手动计算");
            m_nDrawFlag = 0;
            m_bCountMode = true;
        }
        m_CameraView.setCountMode(m_bCountMode);
        m_CameraView.setZY(m_nDrawFlag);
        m_btnY1.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnY2.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnZ1.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnZ2.onMoveMode(m_bIsZCursor, m_CameraView);
    }

    /**
     * 人为移动光标的时候，左右两个裂缝标志的切换
     *
     * @param v
     */
    public void onSelectCursor(View v) {
        if (m_bCountMode) {
            return;
        }
        if (m_bIsZCursor) {
            m_btnCursor1.setText("  左侧游标\n●右侧游标");
            m_btnCursor2.setText("  左侧游标\n●右侧游标");
            m_bIsZCursor = false;
            m_nDrawFlag = 2;
        } else {
            m_btnCursor1.setText("●左侧游标\n  右侧游标");
            m_btnCursor2.setText("●左侧游标\n  右侧游标");
            m_nDrawFlag = 1;
            m_bIsZCursor = true;
        }
        m_CameraView.setZY(m_nDrawFlag);
        m_btnY1.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnY2.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnZ1.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnZ2.onMoveMode(m_bIsZCursor, m_CameraView);
    }

    /**
     * 向左侧移动光标
     *
     * @param v
     */
    public void onMoveLeft(View v) {
        m_CameraView.onMove();
    }

    /**
     * 向右侧移动光标
     *
     * @param v
     */
    public void onMoveRight(View v) {
        m_CameraView.onMove();

    }

    /**
     * 参数的设置Dialog的显示
     *
     * @param v
     */
    public void onSetPar(View v) {
        if (m_Dlg_SetPar == null || !m_Dlg_SetPar.isShowing()) {
            m_Dlg_SetPar = new DLG_SetPar(this, "参数设置");
        }
        m_Dlg_SetPar.show();
    }

    /**
     * 跳转到文件的列表的界面
     *
     * @param v
     */
    public void onOpenFile(View v) {
        startActivity(new Intent(this, Ui_FileSelete.class));
    }

    /**
     * 停止测量
     *
     * @param v
     */
    public void onCollectStop(View v) {
        uiCollect_blackWrite_cb.setClickable(false);
        collect_sfv.setVisibility(View.GONE);
        stopCameraView();
        m_CameraView.showOriginalView();
    }


    public void onTakePicBefor(View view){
        stopCameraView();
        uiCollect_blackWrite_cb.setClickable(true);
        if (m_bIsZCursor) {
            m_nDrawFlag = 1;
        } else {
            m_nDrawFlag = 2;
        }
        m_CameraView.setZY(m_nDrawFlag);
        m_btnY1.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnY2.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnZ1.onMoveMode(m_bIsZCursor, m_CameraView);
        m_btnZ2.onMoveMode(m_bIsZCursor, m_CameraView);
    }

    /**
     * 停止进行拍摄
     */
    private void stopCameraView(){
        m_CameraView.setStopView();
        m_CameraView.closeCamera();
        m_bCountMode = false;
        m_bStart = false;
        m_LayStop.setVisibility(View.GONE);
        m_LayStart.setVisibility(View.VISIBLE);
    }
    /**
     * 退出Activity
     *
     * @param v
     */
    public void activityFinish(View v) {
        m_CameraView.closeCamera();
        this.finish();
    }
}
