package com.ck.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.ThumbnailUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DecodeUtil {

    /**
     * 将彩色图转换为纯黑白二色	* 	* @param 位图	* @return 返回转换好的位图
     */
    public static List<Integer> greenData = new ArrayList<>();
    public static List<Integer> buleData = new ArrayList<>();

    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = bitmap2RGB(bmp);
        int avage = FindLieFenUtils.bytGrayAve;
        greenData.clear();
        buleData.clear();
        for (int i = 0; i < pixels.length; i++) {
            int allFlag = (((((pixels[i] >> 16) & 0xFF) * 30) + (((pixels[i] >> 8) & 0xFF) * 59)
                    + ((pixels[i] >> 0) & 0xFF) * 11) / 100);
            pixels[i] = allFlag < avage ? 0x00 : 0xFFFFFF;
            if (i > 0) {
                if (pixels[i - 1] > pixels[i]) {
                    greenData.add(i);
                } else if (pixels[i - 1] < pixels[i]) {
                    buleData.add(i);
                }
            }
        }
        //新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
        return resizeBmp;
    }

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
     * 中值滤波
     *
     * @param pix 像素矩阵数组
     * @param w   矩阵的宽
     * @param h   矩阵的高
     * @return 处理后的数组
     */
    public static int[] medianFiltering(int pix[], int w, int h) {
        int newpix[] = new int[w * h];
        int[] temp = new int[9];
        int r = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (x != 0 && x != w - 1 && y != 0 && y != h - 1) {
                    temp[0] = (pix[x - 1 + (y - 1) * w] >> 16) & 0xFF;
                    temp[1] = (pix[x + (y - 1) * w]>> 16) & 0xFF;
                    temp[2] = (pix[x + 1 + (y - 1) * w]>> 16) & 0xFF;
                    temp[3] = (pix[x - 1 + (y) * w]>> 16) & 0xFF;
                    temp[4] = (pix[x + (y) * w]>> 16) & 0xFF;
                    temp[5] = (pix[x + 1 + (y) * w]>> 16) & 0xFF;
                    temp[6] = (pix[x - 1 + (y + 1) * w]>> 16) & 0xFF;
                    temp[7] = (pix[x + (y + 1) * w]>> 16) & 0xFF;
                    temp[8] = (pix[x + 1 + (y + 1) * w]>> 16) & 0xFF;
                    Arrays.sort(temp);
                    r = temp[4];
                    newpix[y * w + x] = 255 << 24 | r << 16 | r << 8 | r;
                } else {
                    newpix[y * w + x] = pix[y * w + x];
                }
            }
        }
        return newpix;
    }

    /**
     * @方法描述 Bitmap转int像素组
     */
    public static int[] bitmap2RGB(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();

        byte[] pixels = new byte[w * h * 3]; // Allocate for RGB

        int k = 0;

        for (int x = 0; x < h; x++) {
            for (int y = 0; y < w; y++) {
                int color = bmp.getPixel(y, x);
                pixels[k * 3] = (byte) Color.red(color);
                pixels[k * 3 + 1] = (byte) Color.green(color);
                pixels[k * 3 + 2] = (byte) Color.blue(color);
                k++;
            }
        }
//        return convertByteToColor(pixels);
        return CarmeraDataDone.convertByteToColorJni(pixels,pixels.length);
    }

    // 将纯RGB数据数组转化成int像素数组
    public static int[] convertByteToColor(byte[] data) {
        int size = data.length;
        if (size == 0) {
            return null;
        }

        int arg = 0;
        if (size % 3 != 0) {
            arg = 1;
        }

        // 一般RGB字节数组的长度应该是3的倍数，
        // 不排除有特殊情况，多余的RGB数据用黑色0XFF000000填充
        int[] color = new int[size / 3 + arg];
        int red, green, blue;
        int colorLen = color.length;
        if (arg == 0) {
            for (int i = 0; i < colorLen; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);

                // 获取RGB分量值通过按位或生成int的像素值
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }
        } else {
            for (int i = 0; i < colorLen - 1; ++i) {
                red = convertByteToInt(data[i * 3]);
                green = convertByteToInt(data[i * 3 + 1]);
                blue = convertByteToInt(data[i * 3 + 2]);
                color[i] = (red << 16) | (green << 8) | blue | 0xFF000000;
            }

            color[colorLen - 1] = 0xFF000000;
        }

        return color;
    }

    // 将一个byte数转成int
    // 实现这个函数的目的是为了将byte数当成无符号的变量去转化成int
    public static int convertByteToInt(byte data) {

        int heightBit = (int) ((data >> 4) & 0x0F);
        int lowBit = (int) (0x0F & data);
        return heightBit * 16 + lowBit;
    }


}
