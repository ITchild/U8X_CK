package com.ck.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author fei
 * @date on 2018/11/6 0006
 * @describe TODO :
 **/
public class MorePicShowView extends View {

    private int m_nScreenWidth, m_nScreenHeight;
    private Bitmap m_DrawBitmap;
    private float m_nLeftX ,m_nRightX;
    private Paint m_PaintDrawLine;
    private String name;
    public MorePicShowView(Context context) {
        super(context);
    }

    public MorePicShowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MorePicShowView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        m_nScreenHeight = h;
        m_nScreenWidth = w;
    }

    public void setBitmap(Bitmap m_DrawBitmap ,float leftX , float rightX,String name){
        this.m_DrawBitmap = m_DrawBitmap;
        this.m_nLeftX = leftX;
        this.m_nRightX = rightX;
        this.name = name;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(null != m_DrawBitmap && !m_DrawBitmap.isRecycled()){
            // w和h分别是屏幕的宽和高，也就是你想让图片显示的宽和高
            RectF rectF = new RectF(0, 0, m_nScreenWidth, m_nScreenHeight);
            canvas.drawBitmap(m_DrawBitmap, null, rectF, null);

            float nL = (m_nLeftX/ m_DrawBitmap.getWidth()) * m_nScreenWidth;
            float nR = (m_nRightX / m_DrawBitmap.getWidth()) * m_nScreenWidth;
            int nYMid = m_nScreenHeight / 2;
            if (m_PaintDrawLine == null) {
                m_PaintDrawLine = getPaint(Style.FILL, 2, Color.RED);
            }
            canvas.drawLine(nL, nYMid - m_nScreenHeight / 20, nL, nYMid + m_nScreenHeight / 20, m_PaintDrawLine);
            canvas.drawLine(nL - 50, nYMid, nL, nYMid, m_PaintDrawLine);
            canvas.drawLine(nL - 10, nYMid - 10, nL, nYMid, m_PaintDrawLine);
            canvas.drawLine(nL - 10, nYMid + 10, nL, nYMid, m_PaintDrawLine);


            canvas.drawLine(nL, nYMid, nR, nYMid, m_PaintDrawLine);
            canvas.drawLine(nR, nYMid - m_nScreenHeight / 20, nR, nYMid + m_nScreenHeight / 20, m_PaintDrawLine);
            canvas.drawLine(nR + 50, nYMid, nR, nYMid, m_PaintDrawLine);
            canvas.drawLine(nR + 10, nYMid - 10, nR, nYMid, m_PaintDrawLine);
            canvas.drawLine(nR + 10, nYMid + 10, nR, nYMid, m_PaintDrawLine);
            m_PaintDrawLine.setTextSize(25);
            canvas.drawText(name,m_nScreenWidth/2,50,m_PaintDrawLine);
        }
    }
    /**
     * 获得画笔
     *
     * @param style
     * @param size
     * @param color
     * @return
     */
    public Paint getPaint(Style style, float size, int color) {
        Paint paint = new Paint();
        paint.setStyle(style);
        paint.setStrokeWidth(size);
        paint.setColor(color);
        paint.setDither(true);
        return paint;
    }
}
