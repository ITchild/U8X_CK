package com.ck.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.ck.bean.PointBean;
import com.ck.listener.OnOpenCameraListener;
import com.ck.utils.DecodeUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.OpenCvLieFUtil;
import com.ck.utils.PreferenceHelper;
import com.ck.utils.Stringutil;
import com.hc.u8x_ck.R;

import org.opencv.core.Mat;


public class OpenCvCameraView extends View {

    public int m_nScreenWidth, m_nScreenHeight;
    public Bitmap m_DrawBitmap;
    public Bitmap m_DrawGrayBitmap;
    public Bitmap showBitmap;
    public Bitmap blackWriteBitmap;
    public boolean isBlackWrite = false;
    public boolean isFindSide = false;
    public boolean isCanMove = true;
    public boolean isStart = false; //是否开始检测
    public float width = 0;
    boolean m_bCountMode = true; // 自动计算还是手动计算
    public int m_nDrawFlag = 0; //0自动，左右线红色，1手动，左侧游标，2手动，右侧游标
    boolean m_bOpenOldFile = false;
    private boolean isCalibration = false; //是否进行标定
    private int m_DraBitMapWith = 0;
    private int m_DraBitMapHight = 0;
    private int max_X = 100;
    float m_fXDensity = (float) (m_nScreenWidth / (max_X * 1.0)); //
    private float m_fDispDensity;
    private Paint m_PaintDrawLine;
    private int[] rgb;
    private AppCompatActivity mContext;

    private float startX = 0, startY = 0; //拖动时单点按下时的坐标
    private int BaseX = 0, BaseY = 0;  //完成一次拖动后需要保存的坐标
    private float LeftFlagX = 0, LeftFlagY = 0, RightFlagX = 0, RightFlagY = 0; //拖动光标的偏移量
    private int x = 0, y = 0;  //  实际拖动时的X,Y 轴的增量
    private float baseSideLength = 0;  //双点触控的按下的两点距离
    private boolean doublePoit = false; // 是否为双点触控
    private float toLargeSize = 1f;//放大系数
    private boolean isDoubleTwoLength = false; //是否为双点触控第二次
    private float doubleFristLength = 0;//双点触控第一次的长度
    private float doubleSecondLength = 0;//双点触控第二次的长度

    private int checkFlag = -1;
    private OnOpenCameraListener listener;

    //左右检测光标的xy坐标
    private float nLX = 0, nLY = 0, nRX = 0, nRY = 0;

    private float nLUpX = 0, nLUpY = 0, nLDownX = 0, nLDownY = 0;
    private float nLLx = 0, nLLy = 0;
    private float nLCUpX = 0, nLCUpY = 0, nLCDownX = 0, nLCDownY = 0;

    private float nRUpX = 0, nRUpY = 0, nRDownX = 0, nRDownY = 0;
    private float nRLx = 0, nRLy = 0;
    private float nRCUpX = 0, nRCUpY = 0, nRCDownX = 0, nRCDownY = 0;

    private float nToUpLength = 30, toRLLength = 50, toSorrow = 10;

    private float length = 0;

    private Handler checkTaskHandler = new Handler();
    //开启一个线程  用来查询摄像机是否停止了
    private Runnable checkTaskRunnable = new Runnable() {
        @Override
        public void run() {
            if (checkFlag > 0) {
                checkFlag = -1;
                checkTaskHandler.postDelayed(checkTaskRunnable, 400);
            } else {
                listener.onCarameError();
            }
        }
    };

    public OpenCvCameraView(Context context) {
        super(context);
        init((AppCompatActivity) context);
    }

    public OpenCvCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init((AppCompatActivity) context);
    }

    public OpenCvCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init((AppCompatActivity) context);
    }

    private void init(AppCompatActivity context) {
        mContext = context;
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_fDispDensity = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        float flag = PreferenceHelper.getFXDensity();
        if (flag != 0) {
            m_fXDensity = flag;
            max_X = (int) (m_nScreenWidth / m_fXDensity);
        }
    }

    /**
     * 打开相机
     */
    public void onenCamera(OnOpenCameraListener listener) {
        this.listener = listener;
        m_bOpenOldFile = false;
        makeInitSetting();
        initCarmera();
    }

    /**
     * 回复一些控制的初始值
     */
    public void makeInitSetting() {
        //移动量清空
        BaseX = 0;
        BaseY = 0;
        x = 0;
        y = 0;
        setScrollX(0);
        setScrollY(0);
        //放大倍数清空
        toLargeSize = 1f;
        setScaleX(toLargeSize);
        setScaleY(toLargeSize);
        //黑白图清空
        blackWriteBitmap = null;
        //黑白图标志
        isBlackWrite = false;
        //描边的标志
        isFindSide = false;
    }

    /**
     * 初始化相机
     */
    private void initCarmera() {
        isStart = true;
        checkTaskHandler.postDelayed(checkTaskRunnable, 5000);
    }

    /**
     * 关闭相机
     */
    public void closeCamera() {
        checkTaskHandler.removeCallbacks(checkTaskRunnable);
    }

    /**
     * 进行预拍处理
     */
    public void onBeforTakePic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                blackWriteBitmap = DecodeUtil.convertToBlackWhite(m_DrawBitmap);
                blackWriteBitmap = OpenCvLieFUtil.getDonePic(m_DrawBitmap);
            }
        }).start();
    }

    /**
     * 设置黑白图
     *
     * @param isBlackWrite
     * @param isRefresh
     */
    public void setBlackWrite(boolean isBlackWrite, boolean isRefresh) {
        if (isStart) {
            return;
        }
        if (null == blackWriteBitmap || blackWriteBitmap.isRecycled()) {
            if (isRefresh) {
                Toast.makeText(mContext, "数据处理中，请稍后再试", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        this.isBlackWrite = isBlackWrite;
        if (isRefresh) {
            invalidate();
        }
    }

    /**
     * 设置显示描边
     *
     * @param isFindSide
     * @param isRefresh
     */
    public void setFindSide(boolean isFindSide, boolean isRefresh) {
        if (isStart) {
            return;
        }
        if (null == blackWriteBitmap || blackWriteBitmap.isRecycled()) {
            if (isRefresh) {
                Toast.makeText(mContext, "数据处理中，请稍后再试", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        this.isFindSide = isFindSide;
        if (isRefresh) {
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
        m_DraBitMapWith = m_DrawBitmap.getWidth();
        m_DraBitMapHight = m_DrawBitmap.getHeight();
        new Thread(new Runnable() {
            @Override
            public void run() {
//                blackWriteBitmap = DecodeUtil.convertToBlackWhite(m_DrawBitmap);
                blackWriteBitmap = OpenCvLieFUtil.getDonePic(m_DrawBitmap);
            }
        }).start();
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
        Log.i("fei", "自定义布局的大小：" + h + "*" + w);
        m_fXDensity = (float) (m_nScreenWidth / (max_X * 1.0));
        float flag = PreferenceHelper.getFXDensity();
        if (flag != 0) {
            m_fXDensity = flag;
            max_X = (int) (m_nScreenWidth / m_fXDensity);
        }
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
            Paint paint = getPaint(Paint.Style.FILL, 5, Color.RED,
                    mContext.getResources().getDimension(R.dimen.carmeraStartText));
            float fWidth = paint.measureText(str);
            canvas.drawText(str, m_nScreenWidth / 2 - fWidth / 2, m_nScreenHeight / 2, paint);
            return;
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        RectF rectF = new RectF(0, 0, m_nScreenWidth, m_nScreenHeight); // w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
        if (!isStart) {
//        if(! isStart) {
            showBitmap = isBlackWrite ? blackWriteBitmap : m_DrawBitmap;
//        }else{
//            showBitmap = m_DrawBitmap;
//        }
            canvas.drawBitmap(showBitmap, null, rectF, null);
//            if (isFindSide) { //描边
//                Paint paint = getPaint(Paint.Style.FILL, 2, Color.GREEN, 25);
//                List<PointBean> greenData = OpenCvLieFUtil.greenData;
//                List<PointBean> buleData = OpenCvLieFUtil.buleData;
//                if (null != greenData && greenData.size() > 1) {
//                    for (int i = 3; i < greenData.size(); i += 3) {
//                        float greenX = greenData.get(i).getX();
//                        float greenY = greenData.get(i).getY();
//                        greenX = (float) (((greenX * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
//                        greenY = (float) (((greenY * 1.0) / m_DraBitMapHight) * m_nScreenHeight);
//
//                        float greenX_ = greenData.get(i - 3).getX();
//                        float greenY_ = greenData.get(i - 3).getY();
//                        greenX_ = (float) (((greenX_ * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
//                        greenY_ = (float) (((greenY_ * 1.0) / m_DraBitMapHight) * m_nScreenHeight);
//
//                        canvas.drawLine(greenX_, greenY_, greenX, greenY, paint);
//                    }
//                }
//                paint.setColor(Color.BLUE);
//                for (int i = 3; i < buleData.size(); i += 3) {
//                    float buleX = buleData.get(i).getX();
//                    float buleY = buleData.get(i).getY();
//                    buleX = (float) (((buleX * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
//                    buleY = (float) (((buleY * 1.0) / m_DraBitMapHight) * m_nScreenHeight);
//
//                    float buleX_ = buleData.get(i - 3).getX();
//                    float buleY_ = buleData.get(i - 3).getY();
//                    buleX_ = (float) (((buleX_ * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
//                    buleY_ = (float) (((buleY_ * 1.0) / m_DraBitMapHight) * m_nScreenHeight);
//
//                    canvas.drawLine(buleX_, buleY_, buleX, buleY, paint);
//                }
//            }
        }

        drawRuleAndFlag(canvas);//画刻度和标志
    }

    /**
     * 画刻度和数据值以及标志
     *
     * @param canvas
     */
    private void drawRuleAndFlag(Canvas canvas) {
        nLX = (FindLieFenUtils.m_nCLXLineSite / m_DraBitMapWith) * m_nScreenWidth;
        nLY = (FindLieFenUtils.m_nCLYLineSite / m_DraBitMapHight) * m_nScreenHeight;
        nRX = (FindLieFenUtils.m_nCRXLineSite / m_DraBitMapWith) * m_nScreenWidth;
        nRY = (FindLieFenUtils.m_nCRYLineSite / m_DraBitMapHight) * m_nScreenHeight;

        length = (float) Math.sqrt((nRY - nLY) * (nRY - nLY) + (nRX - nLX) * (nRX - nLX));

        reMakeArrow();

        float mf_fXDensity = m_fXDensity;
        if (!isStart) {
            if (isCalibration) {//标定
                isCalibration = false;
                if (nLY == nRY) { //水平线上的标定
                    mf_fXDensity = m_fXDensity = (float) (Math.abs(nRX - nLX) / 20.0);
                } else {//非水平线上的标定
                    mf_fXDensity = m_fXDensity = (float) (length / 20.0);
                }
                PreferenceHelper.setFXDensity(m_fXDensity);
                max_X = (int) (m_nScreenWidth / m_fXDensity);
            }
        }
        if (m_PaintDrawLine == null) {
            m_PaintDrawLine = getPaint(Paint.Style.FILL, 3, Color.RED, 25);
        }
        m_PaintDrawLine.setColor(Color.RED);
        m_PaintDrawLine.setStrokeWidth(2);
        canvas.drawLine(nLX, nLY, nRX, nRY, m_PaintDrawLine);
        if (m_nDrawFlag == 1) {
            m_PaintDrawLine.setColor(Color.BLUE);
        }
        //左卡标的绘制
        canvas.drawLine(nLUpX, nLUpY, nLDownX, nLDownY, m_PaintDrawLine);
        canvas.drawLine(nLLx, nLLy, nLX, nLY, m_PaintDrawLine);
        canvas.drawLine(nLCUpX, nLCUpY, nLX, nLY, m_PaintDrawLine);
        canvas.drawLine(nLCDownX, nLCDownY, nLX, nLY, m_PaintDrawLine);
        m_PaintDrawLine.setColor(Color.RED);
        if (m_nDrawFlag == 2) {
            m_PaintDrawLine.setColor(Color.BLUE);
        }
        //右卡标的绘制
        canvas.drawLine(nRUpX, nRUpY, nRDownX, nRDownY, m_PaintDrawLine);
        canvas.drawLine(nRLx, nRLy, nRX, nRY, m_PaintDrawLine);
        canvas.drawLine(nRCUpX, nRCUpY, nRX, nRY, m_PaintDrawLine);
        canvas.drawLine(nRCDownX, nRCDownY, nRX, nRY, m_PaintDrawLine);

        m_PaintDrawLine.setColor(Color.RED);
        m_PaintDrawLine.setTextSize(Stringutil.getDimens(R.dimen.x20));
        String str = String.format("%.03f", length / mf_fXDensity / 10.00000000) + "mm";
        str = str.equals("NaNmm") ? "0.00mm" : str; //有时format的返回值为NaN
        canvas.drawText(str, 40, 50, m_PaintDrawLine);
        width = Float.valueOf(str.replace("mm", ""));
        m_PaintDrawLine.setTextSize(20);
        m_PaintDrawLine.setStrokeWidth(2);
        int nYMid = m_nScreenHeight / 2;
        int nKDY = nYMid + nYMid * 3 / 4;
        canvas.drawLine(0, nKDY, m_nScreenWidth, nKDY, m_PaintDrawLine);//打底线
        canvas.drawLine(1, nKDY, 1, nKDY - 30, m_PaintDrawLine); // 0刻度线
        canvas.drawText("0", 1, nKDY + 20, m_PaintDrawLine);
        //底部标度尺的绘制
        float unit = 10;
        for (int i = 1; i <= max_X; i++) {
            if (i % unit == 0) {
                canvas.drawLine(i * mf_fXDensity, nKDY, i * mf_fXDensity, nKDY - 30, m_PaintDrawLine);
                if (i == 100) {
                    canvas.drawText((i / unit) + "", i * mf_fXDensity - 20, nKDY + 20, m_PaintDrawLine);
                } else {
                    canvas.drawText((i / unit) + "", i * mf_fXDensity - 15, nKDY + 20, m_PaintDrawLine);
                }
            } else {
                canvas.drawLine(i * mf_fXDensity, nKDY, i * mf_fXDensity, nKDY - 15, m_PaintDrawLine);
            }
        }
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
    public Paint getPaint(Paint.Style style, float size, int color, float textSize) {
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
     * 重新计算左右箭头的坐标
     */
    private void reMakeArrow() {
        nToUpLength = 30 / toLargeSize;
        toRLLength = 50 / toLargeSize;
        toSorrow = 10 / toLargeSize;

        nLUpX = nLX - nToUpLength * (nRY - nLY) / length;
        nLUpY = nToUpLength * (nRX - nLX) / length + nLY;
        nLDownX = nLX + nToUpLength * (nRY - nLY) / length;
        nLDownY = nLY - nToUpLength * (nRX - nLX) / length;

        nLLx = nLX - toRLLength * (nRX - nLX) / length;
        nLLy = nLY - toRLLength * (nRY - nLY) / length;

        nRUpX = nRX - nToUpLength * (nRY - nLY) / length;
        nRUpY = nToUpLength * (nRX - nLX) / length + nRY;
        nRDownX = nRX + nToUpLength * (nRY - nLY) / length;
        nRDownY = nRY - nToUpLength * (nRX - nLX) / length;

        nRLx = nRX + toRLLength * (nRX - nLX) / length;
        nRLy = nRY + toRLLength * (nRY - nLY) / length;

        float nLL10X = nLX - toSorrow * (nRLx - nLX) / (length + toRLLength);
        float nLL10Y = nLY - toSorrow * (nRLy - nLY) / (length + toRLLength);

        nLCUpX = nLL10X - toSorrow * (nRLy - nLL10Y) / (length + toRLLength);
        nLCUpY = toSorrow * (nRLx - nLL10X) / (length + toRLLength) + nLL10Y;

        nLCDownX = nLL10X + toSorrow * (nRLy - nLL10Y) / (length + toRLLength);
        nLCDownY = nLL10Y - toSorrow * (nRLx - nLL10X) / (length + toRLLength);


        float nRL10X = nRX + toSorrow * (nRX - nLLx) / (length + toRLLength);
        float nRL10Y = nRY + toSorrow * (nRY - nLLy) / (length + toRLLength);

        nRCUpX = nRL10X - toSorrow * (nLLy - nRL10Y) / (length + toRLLength);
        nRCUpY = toSorrow * (nLLx - nRL10X) / (length + toRLLength) + nRL10Y;

        nRCDownX = nRL10X + toSorrow * (nLLy - nRL10Y) / (length + toRLLength);
        nRCDownY = nRL10Y - toSorrow * (nLLx - nRL10X) / (length + toRLLength);

    }

    /**
     * 图像的放大与缩小
     *
     * @param isToLarge
     */
    public void setLargeOrSmall(boolean isToLarge, boolean isRefresh) {
        if (isStart) {
            return;
        }
        toLargeSize = isToLarge ? toLargeSize + 0.3f : toLargeSize - 0.3f;
        if (toLargeSize > 2.5f) {
            toLargeSize = 2.5f;
        } else if (toLargeSize < 1f) {
            toLargeSize = 1f;
        }
        if (isRefresh) {
            float flagX = (FindLieFenUtils.m_nLLineSite + FindLieFenUtils.m_nRLineSite) / 2;
            flagX = (flagX / m_DraBitMapWith) * m_nScreenWidth;
            setScaleX(this.toLargeSize);
            setScaleY(this.toLargeSize);
            int centeryX = BaseX + (m_nScreenWidth / 2);
            if (!(centeryX <= flagX + 2 && centeryX >= flagX - 2)) {
                BaseX = (int) (flagX - (m_nScreenWidth / 2));
                BaseY = 0;
                setScrollX(BaseX);
                setScrollY(BaseY);
            }
        }
    }

    /**
     * 进行标定（2mm）
     *
     * @param isCalibration
     */
    public void setCalibration(boolean isCalibration) {
        if (isStart) {
            return;
        }
        this.isCalibration = isCalibration;
        invalidate();
    }

    public void setStartView() {
        isStart = true;
    }

    public void setStopView() {
        isStart = false;
        checkTaskHandler.removeCallbacks(checkTaskRunnable);
    }

    /**
     */
    private void getSiteLAnfR() {
        int flagY = (int) ((startY + BaseY) / m_nScreenHeight * m_DraBitMapHight);
        int flagX = (int) ((startX + BaseX) / m_nScreenWidth * m_DraBitMapWith);
        if (m_nDrawFlag == 1) {
            int flagSite = -1;
            for (int i = 0; i < OpenCvLieFUtil.greenData.size(); i++) {
                PointBean bean = OpenCvLieFUtil.greenData.get(i);
                if (flagY == bean.getY()) {
                    flagSite = i;
                }
            }
            if (flagSite != -1) {
                OpenCvLieFUtil.getLAndRSite(flagSite);
            } else {
                FindLieFenUtils.m_nCLXLineSite = flagX;
                FindLieFenUtils.m_nCLYLineSite = flagY;
            }
        } else if (m_nDrawFlag == 2) {
            FindLieFenUtils.m_nCRXLineSite = flagX;
            FindLieFenUtils.m_nCRYLineSite = flagY;
        }
        invalidate();
    }

    public void setDataMat(Mat RGBData, Mat grayData) {
        if (null == m_DrawBitmap) {
            m_DrawBitmap = Bitmap.createBitmap(RGBData.cols(), RGBData.rows(), Bitmap.Config.ARGB_8888);
        }
        org.opencv.android.Utils.matToBitmap(RGBData, m_DrawBitmap);
        RGBData.release();
        grayData.release();
        synchronized (this) {
            try {
                if (!isStart && null == m_DrawBitmap) {
                } else {
                    m_DraBitMapWith = m_DrawBitmap.getWidth();
                    m_DraBitMapHight = m_DrawBitmap.getHeight();
                    rgb = DecodeUtil.bitmap2RGBAtCenter(m_DrawBitmap, m_DraBitMapWith, m_DraBitMapHight, 1);
                    FindLieFenUtils.findLieFenAtCenter(rgb, m_DraBitMapWith, m_DraBitMapHight, m_bCountMode, 1);
                    checkFlag++;
                    postInvalidate();//刷新OnDraw，重新绘图
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanMove) {
            return super.onTouchEvent(event);
        }
        if (m_DrawBitmap == null || m_DrawBitmap.isRecycled()) {
            return super.onTouchEvent(event);
        }
        if (!isStart) {
            if (event.getPointerCount() == 1) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        if (Math.abs(nLX - (startX + BaseX)) < 50 && Math.abs((startY + BaseY) - nLY) < 50) {
                            setZY(1);
                        } else if (Math.abs((startX + BaseX) - nRX) < 50 && Math.abs((startY + BaseY) - nRY) < 50) {
                            setZY(2);
                        }
                        LeftFlagX = startX - nLX;
                        LeftFlagY = startY - nLY;
                        RightFlagX = startX - nRX;
                        RightFlagY = startY - nRY;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!doublePoit) {
                            if (m_nDrawFlag == 1 || m_nDrawFlag == 2) {
                                if (m_nDrawFlag == 1) {
                                    FindLieFenUtils.m_nCLXLineSite = ((event.getX() - LeftFlagX + BaseX) / m_nScreenWidth) * m_DraBitMapWith;
                                    FindLieFenUtils.m_nCLYLineSite = ((event.getY() - LeftFlagY + BaseY) / m_nScreenHeight) * m_DraBitMapHight;
                                } else if (m_nDrawFlag == 2) {
                                    FindLieFenUtils.m_nCRXLineSite = ((event.getX() - RightFlagX + BaseX) / m_nScreenWidth) * m_DraBitMapWith;
                                    FindLieFenUtils.m_nCRYLineSite = ((event.getY() - RightFlagY + BaseY) / m_nScreenHeight) * m_DraBitMapHight;
                                }
                                postInvalidate();
                            } else {
                                x = (int) (startX - event.getX()) + BaseX;
                                y = (int) (startY - event.getY()) + BaseY;
                                setScrollX(x);
                                setScrollY(y);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        BaseX = x;
                        BaseY = y;
                        doublePoit = false;
                        break;
                }
            } else if (event.getPointerCount() == 2) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        float startX = Math.abs(event.getX(0) - event.getX(1));
                        float startY = Math.abs(event.getY(0) - event.getY(1));
                        baseSideLength = (float) Math.sqrt(startX * startX + startY * startY);
                        float doubleCenteryX = (event.getX(0) + event.getX(1)) / 2;
                        float doubleCenteryY = (event.getY(0) + event.getY(1)) / 2;
                        setPivotX(doubleCenteryX);
                        setPivotY(doubleCenteryY);
                        doublePoit = true;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float lengthX = Math.abs(event.getX(0) - event.getX(1));
                        float lengthY = Math.abs(event.getY(0) - event.getY(1));
                        float side = (float) Math.sqrt(lengthX * lengthX + lengthY * lengthY);
                        side = side - baseSideLength;
                        Log.i("fei", side - baseSideLength + "");
                        if (!isDoubleTwoLength) {
                            doubleFristLength = side;
                            isDoubleTwoLength = true;
                        } else {
                            doubleSecondLength = side;
                            isDoubleTwoLength = false;
                            toLargeSize = toLargeSize + (doubleSecondLength - doubleFristLength) / 500;
                            if (toLargeSize <= 0.8) {
                                toLargeSize = 0.8f;
                            }
                            setScaleX(toLargeSize);
                            setScaleY(toLargeSize);

                            postInvalidate();
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        if (toLargeSize <= 1) {
                            toLargeSize = 1f;
                        } else if (toLargeSize >= 2.5) {
                            toLargeSize = 2.5f;
                        }
                        setScaleX(toLargeSize);
                        setScaleY(toLargeSize);
                        isDoubleTwoLength = false;
                        doubleSecondLength = 0;
                        doubleFristLength = 0;
                        break;
                }
            }
        }
        return super.onTouchEvent(event);
    }

}
