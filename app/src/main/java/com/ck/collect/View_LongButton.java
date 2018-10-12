/*
 * @Title:  ClasLongButton.java
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * @Description:  TODO<请描述此文件是做什么的>
 * @author:
 * @data:  2016-4-8 上午10:29:58
 * @version:  V1.0
 */
package com.ck.collect;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

import com.ck.ui.CameraView;
import com.ck.utils.FindLieFenUtils;

/**
 * TODO<请描述这个类是干什么的>
 *
 * @author
 * @data: 2016-4-8 上午10:29:58
 * @version: V1.0
 */
public class View_LongButton extends Button {
    String m_strName;
    int nTDNidx = -1;
    int nGDNidx;
    int m_nDrawType;
    boolean m_bIsZCursor = true;
    CameraView m_CameraView;
    private boolean m_bThreadRun = true;
    private boolean m_bWork = false;
    private WorkThread m_WorkThread = null;

    public View_LongButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        m_strName = getText() + "";
    }

    @Override
    protected void onDetachedFromWindow() {
        this.m_bThreadRun = false;
        super.onDetachedFromWindow();
    }

    public void onMoveMode(boolean bIsZCursor, CameraView view) {
        m_bIsZCursor = bIsZCursor;
        m_CameraView = view;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            m_bWork = true;
            if (m_WorkThread == null || !m_WorkThread.isAlive()) {
                m_WorkThread = new WorkThread();
                m_WorkThread.start();
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            m_bWork = false;
        }
        return super.onTouchEvent(event);
    }

    class WorkThread extends Thread {
        @Override
        public void run() {
            while (m_bThreadRun) {
                if (m_bWork) {
                    if (m_strName.equals("左移")) {
                        if (m_bIsZCursor) {
                            // 左侧
                            if (FindLieFenUtils.m_nLLineSite > 0)
                                FindLieFenUtils.m_nLLineSite--;
                        } else {
                            if (FindLieFenUtils.m_nRLineSite > 0)
                                FindLieFenUtils.m_nRLineSite--;
                        }
                    }
                    if (m_strName.equals("右移")) {
                        if (m_bIsZCursor) {
                            // 左侧
                            FindLieFenUtils.m_nLLineSite++;
                        } else {
                            FindLieFenUtils.m_nRLineSite++;
                        }
                    }
                    if (m_CameraView != null)
                        m_CameraView.postInvalidate();
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            super.run();
        }
    }
}
