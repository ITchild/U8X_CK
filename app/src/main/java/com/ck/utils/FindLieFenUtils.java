/*
 * @Title:  FindLieFenUtils.java
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * @Description:  TODO<请描述此文件是做什么的>
 * @author:
 * @data:  2016-11-24 下午4:57:47
 * @version:  V1.0
 */
package com.ck.utils;

import android.util.Log;

/**
 * TODO 获取裂缝所在的位置
 *
 * @author
 * @data: 2016-11-24 下午4:57:47
 * @version: V1.0
 */
public class FindLieFenUtils {
    public static float m_nLLineSite = 0;
    public static float m_nRLineSite = 0;

    public static float m_nCLXLineSite = 0;
    public static float m_nCLYLineSite = 0;
    public static float m_nCRXLineSite = 0;
    public static float m_nCRYLineSite = 0;
    public static int m_nY = 0;
    public static float unitF;
    public static int bytGrayAve;
    static boolean m_bCursorFlag = true;
    static int GRAY_DIF = 50;
    public static int ERROR_COUNT = 2;

    public static void findLieFen(int[] m_lpBaseBuf, int nWidth, int nHeight, boolean mCountMode) {
        m_bCursorFlag = mCountMode;
        unitF = (float) (nWidth * 1.0 / 1000);
        m_nY = nHeight / 2;
        int nLeft = -1, nRight = -1, nError = 0;
        int bytGrayMax = 0, bytGrayMin = 0x7fffffFF;
        int[] bytTemp  = new int[nWidth];
        if (m_bCursorFlag) {    // 手动还是自动
            m_nLLineSite = m_nRLineSite = 0;  ///左右两侧位置的初始化
            for (int i = 0, j = nWidth * m_nY; i < nWidth; i++, j++) {
                bytTemp[i] = (((((m_lpBaseBuf[j] >> 16) & 0xFF) * 30) + (((m_lpBaseBuf[j] >> 8) & 0xFF) * 59)
                        + ((m_lpBaseBuf[j] >> 0) & 0xFF) * 11) / 100); // 颜色灰度化  (R*30 + G*59 +B*11)/100
                if (bytGrayMax < bytTemp[i]) {
                    bytGrayMax = bytTemp[i];//灰度最大值
                }
                if (bytGrayMin > bytTemp[i]) {
                    bytGrayMin = bytTemp[i];//灰度最小值
                }
            }
            if (bytGrayMax - bytGrayMin > GRAY_DIF) { //判断灰度最小值和最大值是否符合包含裂缝的标准

                bytGrayAve = (((bytGrayMax) + (bytGrayMin)) >> 1);// -
                bytGrayAve = bytGrayMin + (bytGrayAve - bytGrayMin) / 3;
                // GRAY_DIF
                // 3;
                //设置阈值
                int nMinAve = 0, nMaxAve = 0, nMinCnt = 0, nMaxCnt = 0;
                for (int i = 0; i < nWidth; i++) // 计算最佳阈值
                {
                    if (bytTemp[i] < bytGrayAve) {
                        nMinAve += (bytTemp[i] & 0xFF);
                        nMinCnt++;
                    } else {
                        nMaxAve += (bytTemp[i] & 0xFF);
                        nMaxCnt++;
                    }
                }
                bytGrayAve = ((nMinAve / nMinCnt + nMaxAve / nMaxCnt) / 2);// /////
                bytGrayAve = bytGrayAve - (bytGrayAve - bytGrayMin) / 2;
                for (int i = 0; i < nWidth; i++) {
                    bytTemp[i] = (bytTemp[i] <= bytGrayAve ? 0 : 0xFF);//转为黑白二值
                    if (bytTemp[i] == 0x00 && nLeft < 0) {
                        nLeft = i==0 ? 0 : i-1;
                        nError = 0;
                    }
                    if (bytTemp[i] == 0xFF && nLeft >= 0) {
                        nError++;
                        if (nError > ERROR_COUNT) {
                            nRight = i;
                        }
                    }
                    if (nLeft >= 0 && nRight >= 0) {
                        if (nRight - nLeft >= 1 && nRight - nLeft >= m_nRLineSite - m_nLLineSite) {//过滤小范围值得波动
                            m_nLLineSite = nLeft;
                            m_nRLineSite = nRight;
                            Log.i("fei",m_nLLineSite +"      "+m_nRLineSite);
                        }
                        nLeft = -1;
                        nRight = -1;
                    }
                }
            }
        } else {
            if (m_nLLineSite == 0 && m_nRLineSite == 0) {
                m_nLLineSite = 180;
                m_nRLineSite = 220;
            }
            return;
        }
    }

    /**
     * 依据设置的图片大小，设置最小的偏移量
     * @param nWidth
     */
    public static void setBitmapWidth( int nWidth){
        unitF = (float) (nWidth * 1.0 / 1000);
    }

    /**
     * 左侧线向左向右移动
     *
     * @param isLeft true ： 向左   false: 向右
     */
    public static void LLineToLOrR(boolean isLeft) {
        m_nLLineSite = isLeft ? m_nLLineSite - unitF : m_nLLineSite + unitF;
    }

    /**
     * 右侧侧线向左向右移动
     *
     * @param isLeft true ： 向左   false: 向右
     */
    public static void RLineToLOrR(boolean isLeft) {
        m_nRLineSite = isLeft ? m_nRLineSite - unitF : m_nRLineSite + unitF;
    }
}
