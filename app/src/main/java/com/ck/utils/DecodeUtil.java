package com.ck.utils;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.ThumbnailUtils;

import java.util.Iterator;
import java.util.List;

public class DecodeUtil {

    public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height, int[] m_nTextureBuffer) {

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


    public static Camera.Size pickBestSizeCandidate(int targetWidth, int targetHeight, List<Camera.Size> sizeList) {
        Camera.Size candidate = null;
        if (null != sizeList) {
            float minRatioOfTarget = Math.min(targetWidth / targetHeight, targetHeight / targetWidth);
            float bestRatioDifference = 1;
            int targetBiggerSideLength = Math.max(targetWidth, targetHeight);
            int bestLengthDifference = targetBiggerSideLength;

            Iterator<Camera.Size> iterator = sizeList.listIterator();
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


    /**
     * 将彩色图转换为纯黑白二色	* 	* @param 位图	* @return 返回转换好的位图
     */
    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];          //分离三原色
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);            //转化成灰度像素
                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        //新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
        return resizeBmp;
    }

}
