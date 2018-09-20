package com.ck.collect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ck.info.ClasFileGJInfo;
import com.ck.info.ClasFileProjectInfo;
import com.ck.main.App_DataPara;
import com.ck.main.BaseActivity;
import com.ck.utils.PathUtils;
import com.ck.utils.PreferenceHelper;
import com.hc.u8x_ck.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class Ui_Collect extends BaseActivity implements SurfaceHolder.Callback {
    private final static String BMP_IMAGE_NAME_FORMAT = "%s.bmp";
    public String m_strSaveProName = "默认工程";
    public String m_strSaveGJName = "默认构件1";
    Handler mActivityHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            }
        }

        ;
    };
    boolean m_bCountMode = true;
    boolean m_bIsZCursor = true;
    int m_nDrawFlag = 0;
    DLG_SetPar m_Dlg_SetPar;
    boolean m_bStart = false;
    /**
     * 防止没有测量时，点击保存，保存黑色背景图片
     */
    boolean m_bSaveImage = false;
    private LinearLayout m_FLayout;
    private LinearLayout m_LayStart;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        initView();
        initData();
        showPic();
    }

    public void initView() {
        collect_sfv = (SurfaceView) findViewById(R.id.collect_sfv);

        m_btnJiSuan = (Button) findViewById(R.id.btn_JiSuan);
        m_btnY1 = (View_LongButton) findViewById(R.id.long_btn1);
        m_btnY2 = (View_LongButton) findViewById(R.id.long_btn2);
        m_btnZ1 = (View_LongButton) findViewById(R.id.long_btn3);
        m_btnZ2 = (View_LongButton) findViewById(R.id.long_btn4);
        m_btnCursor1 = (Button) findViewById(R.id.btn_Cursor1);
        m_btnCursor2 = (Button) findViewById(R.id.btn_Cursor2);
        m_CameraView = (CameraView) findViewById(R.id.CameraView);
        m_FLayout = (LinearLayout) findViewById(R.id.Layout);
        m_LayStart = (LinearLayout) findViewById(R.id.ui_collect_start);
        m_LayStop = (LinearLayout) findViewById(R.id.ui_collect_stop);
        m_tvProName = (TextView) findViewById(R.id.tv_proName);
        m_tvGJName = (TextView) findViewById(R.id.tv_fileName);
        m_ListViewPro = (ListView) findViewById(R.id.list_pro);
        m_ListViewFile = (ListView) findViewById(R.id.list_file);

    }

    private void initData(){
        m_strSaveProName = PreferenceHelper.getProName(this);
        m_strSaveGJName = PreferenceHelper.getGJName(this);
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

    public void showPic() {
        if (AppDatPara.m_nProjectSeleteNidx < 0 || m_ListProject.size() <= AppDatPara.m_nProjectSeleteNidx) {
            return;
        }
        ClasFileProjectInfo pro = m_ListProject.get(AppDatPara.m_nProjectSeleteNidx);
        if (AppDatPara.m_nGJSeleteNidx < 0 || pro.mstrArrFileGJ.size() <= AppDatPara.m_nGJSeleteNidx) {
            return;
        }
        ClasFileGJInfo file = pro.mstrArrFileGJ.get(AppDatPara.m_nGJSeleteNidx);
        String path = PathUtils.PROJECT_PATH + File.separator + pro.mFileProjectName + File.separator + file.mFileGJName;
        Bitmap bmp = BitmapFactory.decodeFile(path);
        m_CameraView.setBitmap(bmp);
        m_tvProName.setText("工程名: " + pro.mFileProjectName);
        m_tvGJName.setText("文件名: " + file.mFileGJName.substring(0, file.mFileGJName.length() - 4));
    }

    @Override
    protected void onDestroy() {
        AppDatPara.m_nGJSeleteNidx = -1;
        super.onDestroy();
    }

    public void onTakePic(View v) {
        if (!m_bSaveImage) {
            return;
        }
		m_CameraView.setDrawingCacheEnabled(true);
		m_CameraView.onTakePic(true);
		Bitmap drawingCache = m_CameraView.getDrawingCache();
		saveBmpImageFile(drawingCache);
		m_CameraView.onTakePic(false);
        m_CameraView.setDrawingCacheEnabled(false);
        AppDatPara.m_nProjectSeleteNidx = 0;
        AppDatPara.m_nGJSeleteNidx = 0;
        initData();
        m_strSaveGJName = GetDigitalPile(m_strSaveGJName);
        PreferenceHelper.setGJName(this, m_strSaveGJName);
		m_CameraView.setDrawingCacheEnabled(false);
    }

    private void saveBmpImageFile(Bitmap bmp) {

        String mediaState = Environment.getExternalStorageState();
        if ((!mediaState.equals(Environment.MEDIA_MOUNTED)) || (mediaState.equals(Environment.MEDIA_MOUNTED_READ_ONLY))) {
            Log.d("main", "Media storage not ready:" + mediaState);
            return;
        }
        File path = null;
        File imageFile = null;
        path = new File(PathUtils.PROJECT_PATH, m_strSaveProName);
        path.mkdirs();

        String fileName = String.format(BMP_IMAGE_NAME_FORMAT, m_strSaveGJName);

        imageFile = new File(path, fileName);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(imageFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String GetDigitalPile(String strData) {
        String strName = ""; // 汉字部分
        String strDigital = ""; // 数字部分
        int nDigital = 1; // 数字部分
        for (int i = strData.length() - 1; i >= 0; i--) {
            if (Character.isDigit(strData.charAt(i))) {
                strDigital = String.valueOf(strData.charAt(i)) + strDigital;
            } else {
                strName = strData.substring(0, i + 1);
                break;
            }
        }

        if (!strDigital.equals("")) {
            nDigital = Integer.parseInt(strDigital) + 1;
        }
        return strName + nDigital;
    }

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

    public void onMoveLeft(View v) {
        m_CameraView.onMove();
    }

    public void onMoveRight(View v) {
        m_CameraView.onMove();

    }

    public void onSetPar(View v) {
        if (m_Dlg_SetPar == null || !m_Dlg_SetPar.isShowing()) {
            m_Dlg_SetPar = new DLG_SetPar(this, "参数设置");
        }
        m_Dlg_SetPar.show();
    }

    public void onOpenFile(View v) {
        startActivity(new Intent(this, Ui_FileSelete.class));
    }

    public void onCollectStart(View v) {
        m_CameraView.setStartView();
        m_CameraView.onenCamera(new OnOpenCameraListener() {
            @Override
            public void OnOpenCameraResultListener(boolean bResult) {
                if (bResult) {
                    mHolder = collect_sfv.getHolder();
                    m_CameraView.setHolder(mHolder);
                    mHolder.addCallback(Ui_Collect.this);

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

    public void onCollectStop(View v) {
        m_CameraView.setStopView();
        m_CameraView.closeCamera();
        m_bCountMode = false;
        m_bStart = false;
        m_LayStop.setVisibility(View.GONE);
        m_LayStart.setVisibility(View.VISIBLE);

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

    public void activityFinish(View v) {
        m_CameraView.closeCamera();
        this.finish();
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (null != m_CameraView.m_Camera) {
            m_CameraView.m_Camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    class ListProjectAdapter extends BaseAdapter {
        public static final int File = 0;
        public static final int Folder = 0;
        ViewHolder holder;
        private Context mContext;
        private List<ClasFileProjectInfo> mProjects;
        private int nSelect = 0;

        public ListProjectAdapter(Context context, List<ClasFileProjectInfo> projects) {
            mContext = context;
            mProjects = projects;
        }

        public void setSelect(int nSelect) {
            this.nSelect = nSelect;
            this.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mProjects.size();
        }        @Override
        public Object getItem(int position) {
            return mProjects.get(position);
        }

        class ViewHolder {
            TextView m_TVProject;
            LinearLayout m_LL;

            public ViewHolder(View view) {
                m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
                m_LL = (LinearLayout) view.findViewById(R.id.ui_list_project);
            }
        }        @Override
        public long getItemId(int position) {
            return position;
        }



        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            if (view == null) {
                view = View.inflate(mContext, R.layout.ui_file_select_list_project, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            if (nSelect == position) {
                holder.m_LL.setBackgroundColor(Color.rgb(246, 184, 00));
            } else {
                if (AppDatPara.nTheme == R.style.AppTheme_Black)
                    holder.m_LL.setBackgroundColor(Color.BLACK);
                else
                    holder.m_LL.setBackgroundColor(Color.WHITE);
            }
            holder.m_TVProject.setText(mProjects.get(position).mFileProjectName);

            return view;
        }


    }

    class ListGJAdapter extends BaseAdapter {
        public static final int File = 0;
        public static final int Folder = 0;
        ClasFileProjectInfo mProject;
        ViewHolder holder;
        private Context mContext;
        private int nSelect = 0;

        public ListGJAdapter(Context context, ClasFileProjectInfo project) {
            mContext = context;
            mProject = project;
        }

        public void clearProInfo() {
            mProject.mstrArrFileGJ.clear();
            this.notifyDataSetChanged();
        }

        public void setSelect(int nSelect) {
            this.nSelect = nSelect;
            this.notifyDataSetChanged();
        }

        public void initSelect(boolean flag) {
            for (int i = 0; i < mProject.mstrArrFileGJ.size(); i++) {
                mProject.mstrArrFileGJ.get(i).bIsSelect = flag;
            }
            notifyDataSetChanged();
        }        @Override
        public int getCount() {
            return mProject.mstrArrFileGJ.size();
        }

        class ViewHolder {
            TextView m_TVProject;
            LinearLayout m_LL;

            public ViewHolder(View view) {
                m_TVProject = (TextView) view.findViewById(R.id.tv_projectName);
                m_LL = (LinearLayout) view.findViewById(R.id.ui_list_gj);
            }
        }        @Override
        public Object getItem(int position) {
            return mProject.mstrArrFileGJ.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }



        @Override
        public View getView(final int position, View view, ViewGroup arg2) {
            if (view == null) {
                view = View.inflate(mContext, R.layout.ui_file_select_list_gj, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            if (nSelect == position) {
                holder.m_LL.setBackgroundColor(Color.rgb(246, 184, 00));
            } else {
                if (AppDatPara.nTheme == R.style.AppTheme_Black)
                    holder.m_LL.setBackgroundColor(Color.BLACK);
                else
                    holder.m_LL.setBackgroundColor(Color.WHITE);
            }
            holder.m_TVProject.setText(mProject.mstrArrFileGJ.get(position).mFileGJName.substring(0, mProject.mstrArrFileGJ.get(position).mFileGJName.length() - 4));

            return view;
        }


    }

}
