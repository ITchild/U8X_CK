package com.ck.utils;

/**
 * @author fei
 * @date on 2018/11/14 0014
 * @describe TODO :
 **/
public class CarmeraDataDone {

    static {
        System.loadLibrary("native-lib");
    }



    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     * 此方法为将摄像机的流转为int类型的数组
     */
    public native static int[] decodeYUV420SPJni(byte[] yuv420sp,int width,int hight,int[] buff);

    /**
     * 将纯RGB数据数组转化成int像素数组
     * @return
     */
    public native static int[] convertByteToColorJni(byte[] data,int size);

    /**
     * 将二值图像的联通区域较小的去除掉
     */
    public native static int[] delLittleSquareJni(int []data,int width,int hight,int littleSq);
}
