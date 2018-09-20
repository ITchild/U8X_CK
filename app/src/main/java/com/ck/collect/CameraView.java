package com.ck.collect;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.ck.utils.FindLieFenUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CameraView extends View {

    public static final int BLUR = 0;
    public static final int CLEAR = BLUR + 1;
    public static final int OPEN_TRUE = 0;
    public static final int OPEN_FALSE = 1;
    private static final int MAGIC_TEXTURE_ID = 10;
    public SurfaceTexture m_SurfaceTexture;
    public Camera m_Camera;
    public byte m_Buffer[];
    public int m_nTextureBuffer[];
    public int m_nPreviewWidth, m_nPreviewHeight;
    public int m_nScreenWidth, m_nScreenHeight;
    public Bitmap m_DrawBitmap;
    OnOpenCameraListener m_Listener;
    /**
     * 计算模式，true自动计算，false手动计算
     */
    boolean m_bCountMode = true;
    /**
     * 0自动，左右线红色，1手动，左侧游标，2手动，右侧游标
     */
    int m_nDrawFlag = 0;
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case OPEN_TRUE:
                    if (m_Listener != null)
                        m_Listener.OnOpenCameraResultListener(true);
                    //点击屏幕的聚焦
                    setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            focus();
                        }
                    });
                    break;
                case OPEN_FALSE:
                    if (m_Listener != null)
                        m_Listener.OnOpenCameraResultListener(false);
                    break;
            }
        }
    };
    float m_fXDensity = (float) (m_nScreenWidth / 80.0);
    boolean m_bOpenOldFile = false;
    private boolean m_bTakePic = false;
    private SurfaceHolder holder;
    private boolean isStart = false;
    private int m_nBufferSize;
    private Camera.Parameters m_Parameters;
    private float m_fDispDensity;
    private Paint m_PaintDrawLine;
    private Canvas m_YCanvas;
    private Bitmap m_YBitmap;
    private long time = 0;
    private int num = 0;


    public CameraView(Context context, int screenWidth, int screenHeight) {
        super(context);
        init(context);
    }
//    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init(context);
//    }

    private void init(Context context) {
        m_SurfaceTexture = new SurfaceTexture(MAGIC_TEXTURE_ID);
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_fDispDensity = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
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

    private Camera.Size pickBestSizeCandidate(int targetWidth, int targetHeight, List<Size> sizeList) {
        Camera.Size candidate = null;
        if (null != sizeList) {

            float minRatioOfTarget = Math.min(targetWidth / targetHeight, targetHeight / targetWidth);
            float bestRatioDifference = 1;
            int targetBiggerSideLength = Math.max(targetWidth, targetHeight);
            int bestLengthDifference = targetBiggerSideLength;

            Iterator<Size> iterator = sizeList.listIterator();
            while (iterator.hasNext()) {
                Camera.Size size = iterator.next();
                if (null != size) {
                    // first we will check if the width/height match exactly

                    if ((size.width == targetWidth) && (size.height == targetHeight)) {
                        candidate = size;
                        break;
                    }

                    // secondly , we try to find the one with the closest ratio

                    float minRatioOfSize = Math.min(size.width / size.height, size.height / size.width);
                    float ratioDifference = Math.abs(minRatioOfSize - minRatioOfTarget);
                    if (ratioDifference < bestRatioDifference) {
                        bestRatioDifference = ratioDifference;
                        candidate = size;
                    } else if (ratioDifference == bestRatioDifference) {

                        // when the ratio is same, we check which one is closest
                        // to the legnth in the bigger side
                        int biggerSideLength = Math.max(size.width, size.height);
                        int lengthDifference = Math.abs(biggerSideLength - targetBiggerSideLength);
                        if (lengthDifference < bestLengthDifference) {
                            bestLengthDifference = lengthDifference;
                            candidate = size;
                        }
                    }
                }
            }
        } else {
        }
        return candidate;
    }

    public void onenCamera(OnOpenCameraListener listener) {
        m_bOpenOldFile = false;
        m_Listener = listener;
        CameraThread cameraThread = new CameraThread();
        cameraThread.start();
    }

    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
    }

    public void closeCamera() {
        if (m_Camera != null) {
            m_Camera.stopPreview();
            m_Camera.release();
            m_Camera = null;
        }
    }

    public void onTakePic(boolean bTakePic) {
        m_bTakePic = bTakePic;
    }

    public boolean getOnTakePic() {
        return m_bTakePic;
    }

    public void setCountMode(boolean bCountMode) {
        m_bCountMode = bCountMode;
    }

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

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        m_nScreenHeight = h;
        m_nScreenWidth = w;
        m_fXDensity = (float) (m_nScreenWidth / 80.0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (m_DrawBitmap == null || m_DrawBitmap.isRecycled()) {
            String str = "裂缝宽度检测";
            Paint paint = getPaint(Style.FILL, 3, Color.RED, 50);
            float fWidth = paint.measureText(str);
            canvas.drawText(str, m_nScreenWidth / 2 - fWidth / 2, m_nScreenHeight / 2, paint);
            return;
        }

        if (m_PaintDrawLine == null) {
            m_PaintDrawLine = getPaint(Style.FILL, 3, Color.RED, 25);
        }
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        RectF rectF = new RectF(0, 0, m_nScreenWidth, m_nScreenHeight); // w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
        if (!isStart) {
            canvas.drawBitmap(m_DrawBitmap, null, rectF, null);
        }
        if (m_bTakePic) {
            canvas.drawBitmap(m_DrawBitmap, null, rectF, null);
            m_bTakePic = false;
        }
        // canvas.drawBitmap(rgb, 0, w, 0, 0, w, h, false, null);
        if (m_bOpenOldFile) {
            return;
        }
        int nL = (int) ((float) FindLieFenUtils.m_nLLineSite / m_DrawBitmap.getWidth() * m_nScreenWidth);
        int nR = (int) ((float) FindLieFenUtils.m_nRLineSite / m_DrawBitmap.getWidth() * m_nScreenWidth);

        int nYMid = m_nScreenHeight / 2;
        m_PaintDrawLine.setColor(Color.RED);
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
        String str = String.format("%.02f", (float) (Math.abs((nR - nL) / m_fXDensity) / 10.00000000)) + "mm";
        canvas.drawText(str, m_nScreenWidth - 50 - m_PaintDrawLine.measureText(str), 60, m_PaintDrawLine);
        if (m_YCanvas == null) {
            m_YCanvas = new Canvas();
            m_YBitmap = Bitmap.createBitmap(m_nScreenWidth, m_nScreenHeight, Config.ARGB_4444);
            m_YCanvas.setBitmap(m_YBitmap);
            Paint paint = getPaint(Style.FILL, 3, Color.RED, 20);
            int nKDY = nYMid + nYMid / 2;

            m_YCanvas.drawLine(0, nKDY, m_nScreenWidth, nKDY, paint);
            m_YCanvas.drawLine(1, nKDY, 1, nKDY - 60, paint); // 0刻度线
            m_YCanvas.drawText("0", 1, nKDY + 40, paint);
            m_YCanvas.drawLine(m_nScreenWidth - 2, nKDY, m_nScreenWidth - 2, nKDY - 60, paint); // 8
            m_YCanvas.drawText("8", m_nScreenWidth - 20, nKDY + 40, paint); // 刻度线
            for (int i = 1; i < 80; i++) {
                if (i < 10) {
                    m_YCanvas.drawLine(i * m_fXDensity, nKDY, i * m_fXDensity, nKDY - 30, paint);
                } else {
                    if (i % 10 == 0) {
                        m_YCanvas.drawLine(i * m_fXDensity, nKDY, i * m_fXDensity, nKDY - 60, paint);
                        m_YCanvas.drawText((i / 10) + "", i * m_fXDensity - 15, nKDY + 40, paint);
                    } else {
                        m_YCanvas.drawLine(i * m_fXDensity, nKDY, i * m_fXDensity, nKDY - 30, paint);
                    }
                }
            }
        }
        canvas.drawBitmap(m_YBitmap, null, rectF, null);
    }

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

    public void setStartView() {
        isStart = true;
    }

    public void setStopView() {
        isStart = false;
    }

    /**
     * 摄像机的聚焦
     */
    public void focus() {
        if (null == m_Camera) {
            return;
        }
        m_Camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {

                }
            }
        });
    }

    private void changeByte(byte[] data, Camera camera) {
        synchronized (this) {
            try {
                if (m_Camera == null) { // if after release ,here also
                    return;
                }
                int w = camera.getParameters().getPreviewSize().width;
                int h = camera.getParameters().getPreviewSize().height;
                int[] rgb = decodeYUV420SP(data, w, h);
                if (m_DrawBitmap != null)
                    m_DrawBitmap.recycle();
                m_DrawBitmap = Bitmap.createBitmap(rgb, w, h, Config.ARGB_4444);

                FindLieFenUtils.findLieFen(rgb, w, h, m_bCountMode);
                if (!m_bTakePic) {
                    camera.addCallbackBuffer(m_Buffer); // <----这句一点要加上.
                    postInvalidate();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {

        int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width;
            int u = 0;
            int v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0) {
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) {
                    r = 0;
                } else if (r > 262143) {
                    r = 262143;
                }
                if (g < 0) {
                    g = 0;
                } else if (g > 262143) {
                    g = 262143;
                }
                if (b < 0) {
                    b = 0;
                } else if (b > 262143) {
                    b = 262143;
                }
                m_nTextureBuffer[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);

            }
        }
        return m_nTextureBuffer;
    }

    class CameraThread extends Thread implements Camera.PreviewCallback {

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
            m_Parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            List<Size> preSize = m_Parameters.getSupportedPreviewSizes();
            for (Size size : preSize) {
                Log.i("fei", size.width + "*" + size.height);
            }
            Camera.Size size = pickBestSizeCandidate(m_nScreenWidth / 3, m_nScreenHeight / 3, preSize);
//            Camera.Size size = preSize.get(preSize.size() - 1);
            Log.i("fei", "我选择的" + size.width + "*" + size.height + "屏幕自己的" + m_nScreenWidth + "*" + m_nScreenHeight);
            m_Parameters.setPreviewSize(size.width, size.height);
            m_Camera.setParameters(m_Parameters);
            m_nBufferSize = size.width * size.height;
            m_nTextureBuffer = new int[m_nBufferSize];
            m_nBufferSize = m_nBufferSize * ImageFormat.getBitsPerPixel(m_Parameters.getPreviewFormat()) / 8;
            m_Buffer = new byte[m_nBufferSize];
            m_Camera.addCallbackBuffer(m_Buffer);
            m_Camera.setPreviewCallbackWithBuffer(this);
            //设置显示
            try {
                m_Camera.setPreviewDisplay(holder);
                m_Camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
                m_Camera.release();
                m_Camera = null;
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if (time == 0) {
                time = System.currentTimeMillis();
            }
            num++;
            if (System.currentTimeMillis() - time > 1000) {
                time = 0;
                Log.i("fei", "当前帧数" + num);
                num = 0;
            }
            changeByte(data, camera);
        }
    }
}