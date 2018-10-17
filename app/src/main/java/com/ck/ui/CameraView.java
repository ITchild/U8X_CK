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
    public int m_nTextureBuffer[];
    public int m_nScreenWidth, m_nScreenHeight;
    public Bitmap m_DrawBitmap;
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
    private boolean m_bTakePic = false;
    private SurfaceHolder holder;
    private int m_nBufferSize;
    private Camera.Parameters m_Parameters;
    private float m_fDispDensity;
    private Paint m_PaintDrawLine;
    private Canvas m_YCanvas;
    private Bitmap m_YBitmap;
    private boolean isBlackWrite = false;
    private CameraTask mCameraTask;
    private Context mContext;
    private float toLargeSize = 2;//放大系数
    private Thread carmeraThread;

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
     *
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
                Camera.Size size = DecodeUtil.pickBestSizeCandidate(m_nScreenWidth, m_nScreenHeight, preSize);
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
            m_Camera.setPreviewCallbackWithBuffer(null);
            m_Camera.stopPreview();
            m_Camera.release();
            m_Camera = null;
        }
    }

    /**
     * 拍照
     *
     * @param bTakePic
     */
    public void onTakePic(boolean bTakePic) {
        m_bTakePic = bTakePic;
        isToLarge = false;
    }

    public boolean getOnTakePic() {
        return m_bTakePic;
    }

    public void setBlackWrite(boolean isBlackWrite) {
        this.isBlackWrite = isBlackWrite;
        invalidate();
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
            if (m_bTakePic || isStart) {
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
            canvas.drawBitmap(isBlackWrite ?
                            (isToLarge ? Bitmap.createBitmap(DecodeUtil.convertToBlackWhite(m_DrawBitmap), x, y, width, hight)
                                    : DecodeUtil.convertToBlackWhite(m_DrawBitmap))
                            : (isToLarge ? Bitmap.createBitmap(m_DrawBitmap, x, y, width, hight)
                            : m_DrawBitmap)
                    , null, rectF, null);
        }
        if (m_bTakePic) {
            canvas.drawBitmap(m_DrawBitmap, null, rectF, null);
            m_bTakePic = false;
        }
        if (m_bOpenOldFile) {
            return;
        }
        float nL = (FindLieFenUtils.m_nLLineSite / m_DrawBitmap.getWidth()) * m_nScreenWidth;
        float nR = (FindLieFenUtils.m_nRLineSite / m_DrawBitmap.getWidth()) * m_nScreenWidth;
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
        m_PaintDrawLine.setStrokeWidth(2);
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
        canvas.drawText(str, m_nScreenWidth - 50 - m_PaintDrawLine.measureText(str), 60, m_PaintDrawLine);
        if (m_YCanvas == null) {
            m_YCanvas = new Canvas();
        }
        if (null != m_YBitmap) {
            m_YBitmap.recycle();
            m_YBitmap = null;
        }
        m_YBitmap = Bitmap.createBitmap(m_nScreenWidth, m_nScreenHeight, Bitmap.Config.ARGB_4444);
        m_YCanvas.setBitmap(m_YBitmap);
        Paint paint = getPaint(Style.FILL, 3, Color.RED, 20);
        int nKDY = nYMid + nYMid / 2;

        m_YCanvas.drawLine(0, nKDY, m_nScreenWidth, nKDY, paint);//打底线
        m_YCanvas.drawLine(1, nKDY, 1, nKDY - 60, paint); // 0刻度线
        m_YCanvas.drawText("0", 1, nKDY + 40, paint);
        float unit = 10;
        for (int i = 1; i <= max_X; i++) {
            if (i % unit == 0) {
                m_YCanvas.drawLine(i * mf_fXDensity, nKDY, i * mf_fXDensity, nKDY - 60, paint);
                if (i == 100) {
                    m_YCanvas.drawText((i / unit) + "", i * mf_fXDensity - 20, nKDY + 40, paint);
                } else {
                    m_YCanvas.drawText((i / unit) + "", i * mf_fXDensity - 15, nKDY + 40, paint);
                }
            } else {
                m_YCanvas.drawLine(i * mf_fXDensity, nKDY, i * mf_fXDensity, nKDY - 30, paint);
            }
        }
//        }
        canvas.drawBitmap(m_YBitmap, null, rectF, null);
    }

    /**
     * 获得画笔
     *
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
    public void setLargeOrSmall(boolean isToLarge) {
        this.isToLarge = isToLarge;
        invalidate();

    }

    public void setStartView() {
        isStart = true;
        isToLarge = false;//放大置位
    }

    public void setStopView() {
        isStart = false;
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        // TODO Auto-generated method stub
        camera.addCallbackBuffer(m_Buffer); // <----这句一点要加上.
        mCameraTask = new CameraTask(bytes);
        mCameraTask.execute((Void) null);
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
                    int w = m_Camera.getParameters().getPreviewSize().width;
                    int h = m_Camera.getParameters().getPreviewSize().height;
                    int[] rgb = DecodeUtil.decodeYUV420SP(mData, w, h, m_nTextureBuffer);
                    if (null != m_DrawBitmap) {
                        m_DrawBitmap.recycle();
                        m_DrawBitmap = null;
                    }
                    m_DrawBitmap = Bitmap.createBitmap(rgb, w, h, Bitmap.Config.RGB_565);
                    FindLieFenUtils.findLieFen(rgb, w, h, m_bCountMode);
                    if (!m_bTakePic) {
                        postInvalidate();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}