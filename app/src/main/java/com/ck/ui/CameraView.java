package com.ck.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Toast;

import com.ck.collect.OnOpenCameraListener;
import com.ck.utils.DecodeUtil;
import com.ck.utils.FindLieFenUtils;
import com.hc.u8x_ck.R;

import java.io.IOException;
import java.util.List;

/**
 * @author fei
 * @date on 2018/10/12 0012
 * @describe TODO :
 **/
public class CameraView extends View implements Camera.PreviewCallback {

    public static final int OPEN_TRUE = 0;  //开启成功的标志
    public static final int OPEN_FALSE = 1; //开启失败的标志
    private static final int MAGIC_TEXTURE_ID = 10; //SurfaceTexture的ID
    public SurfaceTexture m_SurfaceTexture;
    public Camera m_Camera;
    public byte m_Buffer[];
    public int sycTaskNum = 0;  //监听摄像机是否运行
    public int m_nTextureBuffer[];
    public int m_nScreenWidth, m_nScreenHeight;
    public Bitmap m_DrawBitmap;
    public Bitmap showBitmap;
    public Bitmap blackWriteBitmap;
    private int m_DraBitMapWith = 0;
    public boolean isStart = false; //是否开始检测
    public boolean isToLarge = false; //是否要放大
    OnOpenCameraListener m_Listener;
    boolean m_bCountMode = true; // 自动计算还是手动计算
    int m_nDrawFlag = 0; //0自动，左右线红色，1手动，左侧游标，2手动，右侧游标
    boolean m_bOpenOldFile = false;
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case OPEN_TRUE:
                    if (m_Listener != null) {
                        m_Listener.OnOpenCameraResultListener(true);
                    }
                    break;
                case OPEN_FALSE:
                    if (m_Listener != null) {
                        m_Listener.OnOpenCameraResultListener(false);
                    }
                    break;
            }
        }
    };
    private int max_X = 100;
    float m_fXDensity = (float) (m_nScreenWidth / (max_X * 1.0));
    private SurfaceHolder holder;
    private int m_nBufferSize;
    private Camera.Parameters m_Parameters;
    private float m_fDispDensity;
    private Paint m_PaintDrawLine;
    private boolean isBlackWrite = false;
    private CameraTask mCameraTask;
    private Context mContext;
    private float toLargeSize = 2;//放大系数
    private Thread carmeraThread;
    public float width = 0;

    public CameraView(Context context, int screenWidth, int screenHeight) {
        super(context);
        init(context);
    }

    public CameraView(Context context) {
        super(context);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        m_SurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_fDispDensity = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
    }

    /**
     * 打开相机
     * @param listener
     */
    public void onenCamera(OnOpenCameraListener listener) {
        m_bOpenOldFile = false;
        m_Listener = listener;
        initCarmera();
    }

    /**
     * 初始化相机
     */
    private void initCarmera() {
        if (null != carmeraThread && carmeraThread.isAlive()) {
            Toast.makeText(mContext, "操作过于频繁", Toast.LENGTH_SHORT).show();
        } else {
            carmeraThreadStart();
        }
    }

    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    /**
     * 关闭相机
     */
    public void closeCamera() {
        if (null != carmeraThread && carmeraThread.isAlive()) {
            Toast.makeText(mContext, "操作过于频繁", Toast.LENGTH_SHORT).show();
            return;
        }
        if (m_Camera != null) {
            checkTaskHandler.removeCallbacks(checkTaskRunnable);
            m_Camera.setPreviewCallbackWithBuffer(null);
            m_Camera.stopPreview();
            m_Camera.release();
            m_Camera = null;
        }
    }

    /**
     * 进行预拍处理
     */
    public void onBeforTakePic(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                blackWriteBitmap = DecodeUtil.convertToBlackWhite(m_DrawBitmap);
            }
        }).start();
    }

    /**
     * 设置黑白图
     * @param isBlackWrite
     * @param isRefresh
     */
    public void setBlackWrite(boolean isBlackWrite,boolean isRefresh) {
        this.isBlackWrite = isBlackWrite;
        if(isRefresh) {
            invalidate();
        }
    }

    /**
     * 计算模式，true自动计算，false手动计算
     *
     * @param bCountMode
     */
    public void setCountMode(boolean bCountMode) {
        m_bCountMode = bCountMode;
    }

    /**
     * @param nDrawFlag 0自动，左右线红色，1手动，左侧游标，2手动，右侧游标
     */
    public void setZY(int nDrawFlag) {
        m_nDrawFlag = nDrawFlag;
        invalidate();
    }

    public void onMove() {
        invalidate();
    }

    public void setBitmap(Bitmap map) {
        m_bOpenOldFile = true;
        m_DrawBitmap = map;
        invalidate();
    }

    /**
     * 显示初始图像
     */
    public void showOriginalView() {
        if (null != m_DrawBitmap) {
            m_DrawBitmap.recycle();
            m_DrawBitmap = null;
            System.gc();
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        m_nScreenHeight = h;
        m_nScreenWidth = w;
        m_fXDensity = (float) (m_nScreenWidth / (max_X * 1.0));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (m_DrawBitmap == null || m_DrawBitmap.isRecycled()) {
            if (isStart) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                drawRuleAndFlag(canvas);//画刻度和标志
                return; //防止异步线程中将m_DrawBitmap回收（在这里进行检测）
            }
            String str = "裂缝宽度检测";
            Paint paint = getPaint(Style.FILL, 5, Color.RED,
                    mContext.getResources().getDimension(R.dimen.x50));
            float fWidth = paint.measureText(str);
            canvas.drawText(str, m_nScreenWidth / 2 - fWidth / 2, m_nScreenHeight / 2, paint);
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        RectF rectF = new RectF(0, 0, m_nScreenWidth, m_nScreenHeight); // w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
        if (!isStart) {
            int x = 0, y = 0;
            int width = 0, hight = 0;
            if (isToLarge) {
                width = (int) (m_DrawBitmap.getWidth() / toLargeSize);
                hight = (int) (m_DrawBitmap.getHeight() / toLargeSize);
                x = width - (width / 2);
                y = hight - (hight / 2);
            }
            showBitmap = isBlackWrite ?
                    (isToLarge ? Bitmap.createBitmap(blackWriteBitmap, x, y, width, hight)
                            : blackWriteBitmap)
                    : (isToLarge ? Bitmap.createBitmap(m_DrawBitmap, x, y, width, hight)
                    : m_DrawBitmap);
            canvas.drawBitmap(showBitmap, null, rectF, null);
        }
        if (m_bOpenOldFile) {
            return;
        }
        drawRuleAndFlag(canvas);//画刻度和标志
    }

    /**
     * 画刻度和数据值以及标志
     * @param canvas
     */
    private void drawRuleAndFlag(Canvas canvas){
        float nL = (FindLieFenUtils.m_nLLineSite / m_DraBitMapWith) * m_nScreenWidth;
        float nR = (FindLieFenUtils.m_nRLineSite / m_DraBitMapWith) * m_nScreenWidth;
        float mf_fXDensity = m_fXDensity;
        if (isToLarge && !isStart) {//放大
            nL = (toLargeSize * nL - ((toLargeSize - 1) * m_nScreenWidth / 2));
            nR = (toLargeSize * nR - ((toLargeSize - 1) * m_nScreenWidth / 2));
            mf_fXDensity = mf_fXDensity * toLargeSize;
        }
        int nYMid = m_nScreenHeight / 2;
        if (m_PaintDrawLine == null) {
            m_PaintDrawLine = getPaint(Style.FILL, 3, Color.RED, 25);
        }
        m_PaintDrawLine.setColor(Color.RED);
        m_PaintDrawLine.setStrokeWidth(1);
        if (m_nDrawFlag == 1) {
            m_PaintDrawLine.setColor(Color.BLUE);
        }
        canvas.drawLine(nL, nYMid - m_nScreenHeight / 10, nL, nYMid + m_nScreenHeight / 10, m_PaintDrawLine);
        canvas.drawLine(nL - 100, nYMid, nL, nYMid, m_PaintDrawLine);
        canvas.drawLine(nL - 20, nYMid - 20, nL, nYMid, m_PaintDrawLine);
        canvas.drawLine(nL - 20, nYMid + 20, nL, nYMid, m_PaintDrawLine);
        m_PaintDrawLine.setColor(Color.RED);
        if (m_nDrawFlag == 2) {
            m_PaintDrawLine.setColor(Color.BLUE);
        }
        canvas.drawLine(nR, nYMid - m_nScreenHeight / 10, nR, nYMid + m_nScreenHeight / 10, m_PaintDrawLine);
        canvas.drawLine(nR + 100, nYMid, nR, nYMid, m_PaintDrawLine);
        canvas.drawLine(nR + 20, nYMid - 20, nR, nYMid, m_PaintDrawLine);
        canvas.drawLine(nR + 20, nYMid + 20, nR, nYMid, m_PaintDrawLine);

        m_PaintDrawLine.setColor(Color.RED);
        m_PaintDrawLine.setTextSize(30);
        String str = String.format("%.02f", (float) (Math.abs((nR - nL) / mf_fXDensity) / 10.00000000)) + "mm";
        str = str.equals("NaNmm") ? "0.00mm" : str; //有时format的返回值为NaN
        canvas.drawText(str, m_nScreenWidth - 50 - m_PaintDrawLine.measureText(str), 60, m_PaintDrawLine);
        width = Float.valueOf(str.replace("mm",""));
        m_PaintDrawLine.setTextSize(20);
        m_PaintDrawLine.setStrokeWidth(3);
        int nKDY = nYMid + nYMid / 2;
        canvas.drawLine(0, nKDY, m_nScreenWidth, nKDY, m_PaintDrawLine);//打底线
        canvas.drawLine(1, nKDY, 1, nKDY - 60, m_PaintDrawLine); // 0刻度线
        canvas.drawText("0", 1, nKDY + 40, m_PaintDrawLine);

        float unit = 10;
        for (int i = 1; i <= max_X; i++) {
            if (i % unit == 0) {
                canvas.drawLine(i * mf_fXDensity, nKDY, i * mf_fXDensity, nKDY - 60, m_PaintDrawLine);
                if (i == 100) {
                    canvas.drawText((i / unit) + "", i * mf_fXDensity - 20, nKDY + 40, m_PaintDrawLine);
                } else {
                    canvas.drawText((i / unit) + "", i * mf_fXDensity - 15, nKDY + 40, m_PaintDrawLine);
                }
            } else {
                canvas.drawLine(i * mf_fXDensity, nKDY, i * mf_fXDensity, nKDY - 30, m_PaintDrawLine);
            }
        }
    }

    /**
     * 获得画笔
     * @param style
     * @param size
     * @param color
     * @param textSize
     * @return
     */
    public Paint getPaint(Style style, float size, int color, float textSize) {
        Paint paint = new Paint();
        paint.setStyle(style);
        paint.setStrokeWidth(size);
        paint.setColor(color);
        paint.setDither(true);
        if (textSize != 0)
            paint.setTextSize(textSize * m_fDispDensity);
        return paint;
    }

    /**
     * 图像的放大与缩小
     *
     * @param isToLarge
     */
    public void setLargeOrSmall(boolean isToLarge,boolean isRefresh) {
        this.isToLarge = isToLarge;
        if(isRefresh) {
            invalidate();
        }
    }

    public void setStartView() {
        isStart = true;
        isToLarge = false;//放大置位
    }

    public void setStopView() {
        isStart = false;
    }

    /**
     * 开启摄像机的子线程
     */
    private void carmeraThreadStart() {
        carmeraThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (m_Camera == null) {
                        m_Camera = Camera.open(0);
                    }
                    mHandler.sendEmptyMessage(OPEN_TRUE);
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(OPEN_FALSE);
                    return;
                }
                try {
                    m_Camera.setPreviewTexture(m_SurfaceTexture);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                m_Parameters = m_Camera.getParameters();
                List<Size> preSize = m_Parameters.getSupportedPreviewSizes();
                for (Size size : preSize) {
                    Log.i("fei", size.width + "*" + size.height);
                }
//                Camera.Size size = DecodeUtil.pickBestSizeCandidate(m_nScreenWidth, m_nScreenHeight, preSize);
                Camera.Size size = DecodeUtil.pickBestSizeCandidate(640, 480, preSize);
                Log.i("fei", "我选择的" + size.width + "*" + size.height + "屏幕自己的" + m_nScreenWidth + "*" + m_nScreenHeight);
                m_Parameters.setPreviewSize(size.width, size.height);
                m_Camera.setParameters(m_Parameters);

                m_nBufferSize = size.width * size.height;
                m_nTextureBuffer = new int[m_nBufferSize];
                m_nBufferSize = m_nBufferSize * ImageFormat.getBitsPerPixel(m_Parameters.getPreviewFormat()) / 8;
                m_Buffer = new byte[m_nBufferSize];
                m_Camera.addCallbackBuffer(m_Buffer);
                m_Camera.setPreviewCallbackWithBuffer(CameraView.this);
                try {
                    m_Camera.setPreviewDisplay(holder);
                    m_Camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                    m_Camera.release();
                    m_Camera = null;
                }
            }
        });
        carmeraThread.start();
        checkTaskHandler.postDelayed(checkTaskRunnable,5000);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        // TODO Auto-generated method stub
//        byte[] copyData = new byte[bytes.length];
//        System.arraycopy(bytes, 0, copyData, 0, bytes.length);
//        if(maxSycTaskNum <= 5) {
        mCameraTask = new CameraTask(bytes);
        mCameraTask.execute((Void) null);
        sycTaskNum ++;
//        }
        camera.addCallbackBuffer(m_Buffer); // <----这句一点要加上.
    }

    /*自定义的CameraTask类，开启一个线程分析数据*/
    private class CameraTask extends AsyncTask<Void, Void, Void> {
        private byte[] mData;

        //构造函数
        CameraTask(byte[] data) {
            this.mData = data;
        }

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (this) {
                try {
                    if (!isStart) {
                        return null;
                    }
                    if (m_Camera == null) {
                        return null;
                    }
                    if (null == m_Camera.getParameters()) {
                        return null;
                    }
                    m_DraBitMapWith = m_Camera.getParameters().getPreviewSize().width;
                    int h = m_Camera.getParameters().getPreviewSize().height;
                    int[] rgb = DecodeUtil.decodeYUV420SP(mData, m_DraBitMapWith, h, m_nTextureBuffer);
                    m_DrawBitmap = Bitmap.createBitmap(rgb, m_DraBitMapWith, h, Bitmap.Config.RGB_565);
                    FindLieFenUtils.findLieFen(rgb, m_DraBitMapWith, h, m_bCountMode);
                    postInvalidate();//刷新OnDraw，重新绘图
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private Handler checkTaskHandler = new Handler();
    private Runnable checkTaskRunnable = new Runnable() {
        @Override
        public void run() {
            if(sycTaskNum != 0){
                sycTaskNum = 0;
                checkTaskHandler.postDelayed(checkTaskRunnable,500);
                Log.i("select timeout","正常");
            }else{
                Log.i("select timeout","进行初始化相机");
                if(null != m_Listener){
                    m_Listener.onCarameError();
                }
            }
        }
    };
}
