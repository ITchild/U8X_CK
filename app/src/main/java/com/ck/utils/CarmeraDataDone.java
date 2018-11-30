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
    public native static int[] delLittleSquareJni(int []data,int width,int hight,int littleSq,int whiteLittleSq);

    /**
     * * model 和 dev的类型
     * //  /dev/ck102_pwr        dev : 1
     #define GPIO_F3   0 // usb charge pumb  //充电的控制
     #define GPIO_F5   1 //camera usb power  U口的控制
     #define GPIO_E11  2 //usart power   串口的控制
     //  /dev/ck102_led       dev:   2
     #define GPIO_E8   0 //red led //控制的红灯
     #define GPIO_E9   1 //charge red led //充电的红灯
     #define GPIO_E10  2 //blue led //控制的蓝灯
     #define GPIO_H7   3 //beep   //蜂鸣器
     *  cmd  : 0 关闭   1：打开
     *  return  : true : 成功  false ： 失败
     */
    public native static boolean openHardDevJni(int dev,int model,int cmd);
}
