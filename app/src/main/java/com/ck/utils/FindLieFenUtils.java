/*
 * @Title:  FindLieFenUtils.java
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved
 * @Description:  TODO<请描述此文件是做什么的>
 * @author:
 * @data:  2016-11-24 下午4:57:47
 * @version:  V1.0
 */
package com.ck.utils;

/**
 * TODO<请描述这个类是干什么的>
 *
 * @author
 * @data: 2016-11-24 下午4:57:47
 * @version: V1.0
 */
public class FindLieFenUtils {
    public static int m_nLLineSite;
    public static int m_nRLineSite;
    static boolean m_bCursorFlag = true;
    static int GRAY_DIF = 10;
    static int ERROR_COUNT = 2;

    public static void findLieFen(byte[] m_lpBaseBuf, int nWidth, int nHeight, boolean mCountMode) {
        m_bCursorFlag = mCountMode;
//		byte bytGrayMax = 0, bytGrayMin = (byte) 0xFF, bytGrayAve;
        int nLeft = -1, nRight = -1, nError = 0;
//		byte bytTemp[] = new byte[BMP_WIDTH];
        int bytGrayMax = 0, bytGrayMin = 0x7fffffFF, bytGrayAve;
        int bytTemp[] = new int[nWidth];
        int nXSite, nYSite = nHeight / 2;

        if (m_bCursorFlag)// 手动还是自动
        {
            m_nLLineSite = m_nRLineSite = 0;
            for (int i = 0, j = nWidth * nYSite * 3; i < nWidth; i++, j += 3) {
//				bytTemp[i] = (byte) (((m_lpBaseBuf[j + 1] >> 3) * 30 + (((m_lpBaseBuf[j + 1] & 0x07) << 5) + (m_lpBaseBuf[j] >> 5)) * 59 + (m_lpBaseBuf[j] & 0x1F) * 11) / 100);

                bytTemp[i] = ((((m_lpBaseBuf[j + 2] & 0xFF) * 30) + ((m_lpBaseBuf[j + 1] & 0xFF) * 59)
                        + (m_lpBaseBuf[j] & 0xFF) * 11) / 100); // 颜色灰度化  (R*30 + G*59 +B*11)/100
                if (bytGrayMax < bytTemp[i]) {
                    bytGrayMax = bytTemp[i];
                }
                if (bytGrayMin > bytTemp[i]) {
                    bytGrayMin = bytTemp[i];
                }
            }
            if (bytGrayMax - bytGrayMin > GRAY_DIF) {
                bytGrayAve = (((bytGrayMax) + (bytGrayMin)) >> 1);// -
                // GRAY_DIF
                // / 3;
                // //设置阈值
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

                for (int i = 0; i < nWidth; i++) {
                    bytTemp[i] = (bytTemp[i] <= bytGrayAve ? 0 : 0xFF);
                }
                for (int i = 0; i < nWidth; i++) {
                    if (bytTemp[i] == 0x00 && nLeft < 0) {
                        nLeft = i;
                        nError = 0;
                    }
                    if (bytTemp[i] == 0xFF && nLeft >= 0) {
                        nError++;
                        if (nError > ERROR_COUNT) {
                            nRight = i - nError;
                        }
                    }

                    if (nLeft >= 0 && nRight >= 0) {
                        if (nRight - nLeft >= 5 && nRight - nLeft > m_nRLineSite - m_nLLineSite) {
                            m_nLLineSite = nLeft;
                            m_nRLineSite = nRight;
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


    public static void findLieFen(int[] m_lpBaseBuf, int nWidth, int nHeight, boolean mCountMode) {
        m_bCursorFlag = mCountMode;
//		byte bytGrayMax = 0, bytGrayMin = (byte) 0xFF, bytGrayAve;
        int nLeft = -1, nRight = -1, nError = 0;
//		byte bytTemp[] = new byte[BMP_WIDTH];
        int bytGrayMax = 0, bytGrayMin = 0x7fffffFF, bytGrayAve;
        int bytTemp[] = new int[nWidth];
        int nXSite, nYSite = nHeight / 2;

        if (m_bCursorFlag) {    // 手动还是自动
            m_nLLineSite = m_nRLineSite = 0;  ///左右两侧位置的初始化
            for (int i = 0, j = nWidth * nYSite; i < nWidth; i++, j++) {
//				bytTemp[i] = (byte) (((m_lpBaseBuf[j + 1] >> 3) * 30 + (((m_lpBaseBuf[j + 1] & 0x07) << 5) + (m_lpBaseBuf[j] >> 5)) * 59 + (m_lpBaseBuf[j] & 0x1F) * 11) / 100);

                bytTemp[i] = (((((m_lpBaseBuf[j] >> 16) & 0xFF) * 30) + (((m_lpBaseBuf[j] >> 8) & 0xFF) * 59)
                        + ((m_lpBaseBuf[j] >> 0) & 0xFF) * 11) / 100); // 颜色灰度化  (R*30 + G*59 +B*11)/100
                if (bytGrayMax < bytTemp[i]) {
                    bytGrayMax = bytTemp[i];
                }
                if (bytGrayMin > bytTemp[i]) {
                    bytGrayMin = bytTemp[i];
                }
            }
            if (bytGrayMax - bytGrayMin > GRAY_DIF) {
                bytGrayAve = (((bytGrayMax) + (bytGrayMin)) >> 1);// -
                // GRAY_DIF
                // / 3;
                // //设置阈值
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

                for (int i = 0; i < nWidth; i++) {
                    bytTemp[i] = (bytTemp[i] <= bytGrayAve ? 0 : 0xFF);
                }
                for (int i = 0; i < nWidth; i++) {
                    if (bytTemp[i] == 0x00 && nLeft < 0) {
                        nLeft = i;
                        nError = 0;
                    }
                    if (bytTemp[i] == 0xFF && nLeft >= 0) {
                        nError++;
                        if (nError > ERROR_COUNT) {
                            nRight = i - nError;
                        }
                    }

                    if (nLeft >= 0 && nRight >= 0) {
                        if (nRight - nLeft >= 5 && nRight - nLeft > m_nRLineSite - m_nLLineSite) {
                            m_nLLineSite = nLeft;
                            m_nRLineSite = nRight;
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


////		byte bytGrayMax = 0, bytGrayMin = (byte) 0xFF, bytGrayAve;
//		int nLeft = -1, nRight = -1, nError = 0;
////		byte bytTemp[] = new byte[BMP_WIDTH];
//		int bytGrayMax = 0, bytGrayMin = 0x7fffffFF, bytGrayAve;
//		int bytTemp[] = new int[nWidth];
//		int nXSite, nYSite = nHeight / 2;
//
//		if (m_bCursorFlag)// 手动还是自动
//		{
//			m_nLLineSite = m_nRLineSite = 0;
//			for (int i = 0, j = nWidth * nYSite; i < nWidth; i++, j++) {
////				bytTemp[i] = (byte) (((m_lpBaseBuf[j + 1] >> 3) * 30 + (((m_lpBaseBuf[j + 1] & 0x07) << 5) + (m_lpBaseBuf[j] >> 5)) * 59 + (m_lpBaseBuf[j] & 0x1F) * 11) / 100);
//				bytTemp[i] = ((m_lpBaseBuf[j] & 0x00FFFFFF) / 25500);
//				if (bytGrayMax < bytTemp[i]) {
//					bytGrayMax = bytTemp[i];
//				}
//				if (bytGrayMin > bytTemp[i]) {
//					bytGrayMin = bytTemp[i];
//				}
//			}
//			if (bytGrayMax - bytGrayMin > GRAY_DIF) {
//				bytGrayAve = (((bytGrayMax) + (bytGrayMin)) >> 1);// -
//																		// GRAY_DIF
//																		// / 3;
//																		// //设置阈值
//				int nMinAve = 0, nMaxAve = 0, nMinCnt = 0, nMaxCnt = 0;
//				for (int i = 0; i < nWidth; i++) // 计算最佳阈值
//				{
//					if (bytTemp[i] < bytGrayAve) {
//						nMinAve += (bytTemp[i] & 0xFF);
//						nMinCnt++;
//					} else {
//						nMaxAve += (bytTemp[i] & 0xFF);
//						nMaxCnt++;
//					}
//				}
//				bytGrayAve = ((nMinAve / nMinCnt + nMaxAve / nMaxCnt) / 2);// /////
//
//				for (int i = 0; i < nWidth; i++) {
//					bytTemp[i] = (bytTemp[i] <= bytGrayAve ? 0 : 0xFF);
//				}
//				for (int i = 0; i < nWidth; i++) {
//					if (bytTemp[i] == 0x00 && nLeft < 0) {
//						nLeft = i;
//						nError = 0;
//					}
//					if (bytTemp[i] == 0xFF && nLeft >= 0) {
//						nError++;
//						if (nError > ERROR_COUNT) {
//							nRight = i - nError;
//						}
//					}
//
//					if (nLeft >= 0 && nRight >= 0) {
//						if (nRight - nLeft >= 5 && nRight - nLeft > m_nRLineSite - m_nLLineSite) {
//							m_nLLineSite = nLeft;
//							m_nRLineSite = nRight;
//						}
//						nLeft = -1;
//						nRight = -1;
//					}
//				}
//			}
//		} else {
//			m_nLLineSite = 180;
//			m_nRLineSite = 220;
//			return;
//		}
//
//		Log.i("LR", "L = " + m_nLLineSite + "  R = " + m_nRLineSite);
    }
}
