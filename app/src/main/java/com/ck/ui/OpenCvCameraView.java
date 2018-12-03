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

import com.ck.utils.DecodeUtil;
import com.ck.utils.FindLieFenUtils;
import com.ck.utils.PreferenceHelper;
import com.ck.utils.Stringutil;
import com.hc.u8x_ck.R;

import org.opencv.core.Mat;


public class OpenCvCameraView extends View {

    public byte m_Buffer[];
    public int m_nTextureBuffer[];
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
    int m_nDrawFlag = 0; //0自动，左右线红色，1手动，左侧游标，2手动，右侧游标
    boolean m_bOpenOldFile = false;
    private boolean isCalibration = false; //是否进行标定
    private int m_DraBitMapWith = 0;
    private int m_DraBitMapHight = 0;
    private int max_X = 100;
    float m_fXDensity = (float) (m_nScreenWidth / (max_X * 1.0)); //
    private int m_nBufferSize;
    private float m_fDispDensity;
    private Paint m_PaintDrawLine;
    private byte[] getBytes = null;
    private int [] rgb ;
    private AppCompatActivity mContext;

    private float startX = 0 ,startY = 0; //拖动时单点按下时的坐标
    private int BaseX = 0, BaseY = 0;  //完成一次拖动后需要保存的坐标
    private int x = 0, y = 0;  //  实际拖动时的X,Y 轴的增量
    private float baseSideLength = 0;  //双点触控的按下的两点距离
    private boolean doublePoit = false; // 是否为双点触控
    private float toLargeSize = 1f;//放大系数
    private boolean isDoubleTwoLength = false; //是否为双点触控第二次
    private float doubleFristLength = 0;//双点触控第一次的长度
    private float doubleSecondLength = 0;//双点触控第二次的长度


    public OpenCvCameraView(Context context) {
        super(context);
        init((AppCompatActivity)context);
    }

    public OpenCvCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init((AppCompatActivity)context);
    }

    public OpenCvCameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init((AppCompatActivity)context);
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
    public void onenCamera() {
        m_bOpenOldFile = false;
        makeInitSetting();
        initCarmera();
    }

    /**
     * 回复一些控制的初始值
     */
    public void makeInitSetting(){
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
        checkTaskHandler.post(checkTaskRunnable);
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
                blackWriteBitmap = DecodeUtil.convertToBlackWhite(m_DrawBitmap);
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
        if (null == blackWriteBitmap || blackWriteBitmap.isRecycled()) {
            if(isRefresh) {
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
     * @param isFindSide
     * @param isRefresh
     */
    public void setFindSide(boolean isFindSide, boolean isRefresh) {
        if (null == blackWriteBitmap || blackWriteBitmap.isRecycled()) {
            if(isRefresh) {
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
                blackWriteBitmap = DecodeUtil.convertToBlackWhite(m_DrawBitmap);
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
        Log.i("fei","自定义布局的大小："+h+"*"+w);
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
//            canvas.drawColor(0xFF000000, PorterDuff.Mode.CLEAR);
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
            if (isFindSide) { //描边
                Paint paint = getPaint(Paint.Style.FILL, 2, Color.GREEN, 25);
                int buleNum = DecodeUtil.buleData.size();
                int greenNum = DecodeUtil.greenData.size();
                if(null != DecodeUtil.greenData && DecodeUtil.greenData.size() > 1) {
                    for (int i = 3; i < greenNum; i+=3) {
                        float greenX = DecodeUtil.greenData.get(i) % m_DraBitMapWith - 1;
                        float greenY = DecodeUtil.greenData.get(i) / m_DraBitMapWith + 1;
                        greenX = (float) (((greenX * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
                        greenY = (float) (((greenY * 1.0) / m_DraBitMapHight) * m_nScreenHeight);

                        float greenX_ = DecodeUtil.greenData.get(i - 3) % m_DraBitMapWith - 1;
                        float greenY_ = DecodeUtil.greenData.get(i - 3) / m_DraBitMapWith + 1;
                        greenX_ = (float) (((greenX_ * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
                        greenY_ = (float) (((greenY_ * 1.0) / m_DraBitMapHight) * m_nScreenHeight);

                        canvas.drawLine(greenX_, greenY_,greenX,greenY, paint);
                    }
                }
                paint.setColor(Color.BLUE);
                for (int i = 3; i < buleNum; i+=3) {
                    float buleX = DecodeUtil.buleData.get(i) % m_DraBitMapWith + 1;
                    float buleY = DecodeUtil.buleData.get(i) / m_DraBitMapWith + 1;
                    buleX = (float) (((buleX * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
                    buleY = (float) (((buleY * 1.0) / m_DraBitMapHight) * m_nScreenHeight);

                    float buleX_ = DecodeUtil.buleData.get(i-3) % m_DraBitMapWith + 1;
                    float buleY_ = DecodeUtil.buleData.get(i-3) / m_DraBitMapWith + 1;
                    buleX_ = (float) (((buleX_ * 1.0) / m_DraBitMapWith) * m_nScreenWidth);
                    buleY_ = (float) (((buleY_ * 1.0) / m_DraBitMapHight) * m_nScreenHeight);

                    canvas.drawLine(buleX_,buleY_,buleX, buleY, paint);
                }
            }
        }

        drawRuleAndFlag(canvas);//画刻度和标志
    }

    /**
     * 画刻度和数据值以及标志
     *
     * @param canvas
     */
    private void drawRuleAndFlag(Canvas canvas) {
        float nL = (FindLieFenUtils.m_nLLineSite / m_DraBitMapWith) * m_nScreenWidth;
        float nR = (FindLieFenUtils.m_nRLineSite / m_DraBitMapWith) * m_nScreenWidth;
        float nRX = (FindLieFenUtils.m_nCRXLineSite /m_DraBitMapWith)  * m_nScreenWidth;
        float nRY= (FindLieFenUtils.m_nCRYLineSite  / m_DraBitMapHight) * m_nScreenHeight;
        float mf_fXDensity = m_fXDensity;
        if (isCalibration) {//标定
            isCalibration = false;
            mf_fXDensity = m_fXDensity = (float) (Math.abs(nR - nL) / 20.0);
            PreferenceHelper.setFXDensity(m_fXDensity);
            max_X = (int) (m_nScreenWidth / m_fXDensity);
        }
        int nYMid = m_nScreenHeight / 2;
        if (m_PaintDrawLine == null) {
            m_PaintDrawLine = getPaint(Paint.Style.FILL, 3, Color.RED, 25);
        }
        m_PaintDrawLine.setColor(Color.RED);
        m_PaintDrawLine.setStrokeWidth(2);
        if (isFindSide) { //描边
            //自动判别时的倾斜连线
            canvas.drawLine(nL, nYMid, nRX, nRY, m_PaintDrawLine);
        }else {
            canvas.drawLine(nL, nYMid, nR, nYMid, m_PaintDrawLine); //左右两个卡标中间的连线
        }
        if (m_nDrawFlag == 1) {
            m_PaintDrawLine.setColor(Color.BLUE);
        }
        //左卡标的绘制
        canvas.drawLine(nL, nYMid - m_nScreenHeight / 20, nL, nYMid + m_nScreenHeight / 20, m_PaintDrawLine);
        canvas.drawLine(nL - 50, nYMid, nL, nYMid, m_PaintDrawLine);
        canvas.drawLine(nL - 10, nYMid - 10, nL, nYMid, m_PaintDrawLine);
        canvas.drawLine(nL - 10, nYMid + 10, nL, nYMid, m_PaintDrawLine);
        m_PaintDrawLine.setColor(Color.RED);
        if (m_nDrawFlag == 2) {
            m_PaintDrawLine.setColor(Color.BLUE);
        }
        //右卡标的绘制
        canvas.drawLine(nR, nYMid - m_nScreenHeight / 20, nR, nYMid + m_nScreenHeight / 20, m_PaintDrawLine);
        canvas.drawLine(nR + 50, nYMid, nR, nYMid, m_PaintDrawLine);
        canvas.drawLine(nR + 10, nYMid - 10, nR, nYMid, m_PaintDrawLine);
        canvas.drawLine(nR + 10, nYMid + 10, nR, nYMid, m_PaintDrawLine);

        m_PaintDrawLine.setColor(Color.RED);
        m_PaintDrawLine.setTextSize(Stringutil.getDimens(R.dimen.x20));
        String str = String.format("%.02f", (float) (Math.abs((nR - nL) / mf_fXDensity) / 10.00000000)) + "mm";
        str = str.equals("NaNmm") ? "0.00mm" : str; //有时format的返回值为NaN
        canvas.drawText(str, m_nScreenWidth - 40 - m_PaintDrawLine.measureText(str), 50, m_PaintDrawLine);
        width = Float.valueOf(str.replace("mm", ""));
        m_PaintDrawLine.setTextSize(20);
        m_PaintDrawLine.setStrokeWidth(2);
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
     * 图像的放大与缩小
     *
     * @param isToLarge
     */
    public void setLargeOrSmall(boolean isToLarge, boolean isRefresh) {
        if(isStart){
            return;
        }
        toLargeSize = isToLarge ? toLargeSize + 0.3f : toLargeSize-0.3f;
        if(toLargeSize > 2.5f){
            toLargeSize = 2.5f;
        }else if(toLargeSize < 1f){
            toLargeSize = 1f;
        }
        if (isRefresh) {
            float flagX = (FindLieFenUtils.m_nLLineSite + FindLieFenUtils.m_nRLineSite)/2;
            flagX = (flagX / m_DraBitMapWith) * m_nScreenWidth ;
            setScaleX(this.toLargeSize);
            setScaleY(this.toLargeSize);
            int centeryX = BaseX + (m_nScreenWidth/2) ;
            if(!(centeryX <= flagX+2 && centeryX >= flagX-2)) {
                BaseX = (int) (flagX - (m_nScreenWidth / 2) );
                BaseY = 0;
                setScrollX(BaseX);
                setScrollY(BaseY);
            }
        }
    }

    /**
     * 进行标定（2mm）
     * @param isCalibration
     */
    public void setCalibration(boolean isCalibration) {
        this.isCalibration = isCalibration;
        invalidate();
    }

    public void setStartView() {
        isStart = true;
    }

    public void setStopView() {
        isStart = false;
    }

    public void setDataMat(Mat RGBData,Mat grayData){
        if(null == m_DrawBitmap){
            m_DrawBitmap = Bitmap.createBitmap(RGBData.cols(), RGBData.rows(), Bitmap.Config.ARGB_8888);
        }
        if(null == m_DrawGrayBitmap){
            m_DrawGrayBitmap = Bitmap.createBitmap(grayData.cols(), grayData.rows(), Bitmap.Config.ARGB_8888);
        }
        org.opencv.android.Utils.matToBitmap(RGBData, m_DrawBitmap);
        org.opencv.android.Utils.matToBitmap(grayData, m_DrawGrayBitmap);
    }

    private Handler checkTaskHandler = new Handler();
    //开启一个线程  用来查询摄像机是否停止了
    private Runnable checkTaskRunnable = new Runnable() {
        @Override
        public void run() {
//            if (null != getBytes) {
                synchronized (this) {
                    try {
                        if (!isStart) {
                            return;
                        }
                        m_DraBitMapWith = m_DrawBitmap.getWidth();
                        m_DraBitMapHight = m_DrawBitmap.getHeight();
//                        int[] rgb = DecodeUtil.decodeYUV420SP(getBytes, m_DraBitMapWith, m_DraBitMapHight, m_nTextureBuffer);
//                        int[] rgb = CarmeraDataDone.decodeYUV420SPJni(getBytes, m_DraBitMapWith, m_DraBitMapHight, m_nTextureBuffer);
                        rgb = DecodeUtil.bitmap2RGBAtCenter(m_DrawGrayBitmap);
                        FindLieFenUtils.findLieFenAtCenter(rgb, m_DraBitMapWith, m_DraBitMapHight, m_bCountMode);
                        postInvalidate();//刷新OnDraw，重新绘图
                        getBytes = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                }
            }
            checkTaskHandler.post(checkTaskRunnable);
        }
    };


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanMove){
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
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (!doublePoit) {
                            x = (int) (startX - event.getX()) + BaseX;
                            y = (int) (startY - event.getY()) + BaseY;
                            setScrollX(x);
                            setScrollY(y);
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
