package com.ck.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.ThumbnailUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class DecodeUtil {

    /**
     * 将彩色图转换为纯黑白二色	* 	* @param 位图	* @return 返回转换好的位图
     */
    public static List<Integer> greenData = new ArrayList<>();
    public static List<Integer> buleData = new ArrayList<>();
    private static int width , height;
    private static int m_nLLineSite , m_nRLineSite;

    public static Bitmap convertToBlackWhite(Bitmap bmp) {
//        Log.i("fei","黑白图处理开始：" + System.currentTimeMillis());
        if (null == bmp) {
            return bmp;
        }
        width = bmp.getWidth(); // 获取位图的宽
        height = bmp.getHeight(); // 获取位图的高
        int red, green, blue;
//        int[] pixels = bitmap2RGB(bmp);
        int[] pixels = new int[width * height];
        int avage = FindLieFenUtils.bytGrayAve;
//        Log.i("fei","获取整体的bitmap的int数组START：" + System.currentTimeMillis());
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
//        Log.i("fei","获取整体的bitmap的int数组END：" + System.currentTimeMillis());
        for (int i = 0; i < pixels.length; i++) {
            int color = bmp.getPixel(i % width, i / width);
            red = Color.red(color);
            green = Color.green(color);
            blue = Color.blue(color);
            //获取RGB分量值通过按位或生成灰阶int的像素值
            int allFlag = ((((red & 0xFF) * 30) + ((green & 0xFF) * 59) + ((blue & 0xFF) * 11)) / 100);
            pixels[i] = allFlag < avage ? 0x00 : 0xFFFFFF;
        }
//        Log.i("fei","开始使用连通域处理图像：" + System.currentTimeMillis());
        //计算连通区域，小于阈值的直接改为白色
//        pixels = CarmeraDataDone.delLittleSquareJni(pixels,width,height,30,25);
//        Log.i("fei","开始边缘检测：" + System.currentTimeMillis());
        //去掉孤立的点（没有连接点或只有一个连接的点）
        delSiglePoint(pixels, width);

        int flagLeng = width * height / 2;
        int nLeft = -1, nRight = -1;
        for (int i = flagLeng; i < flagLeng + width; i++) {
            if (pixels[i] == 0x00 && nLeft < 0) {
                nLeft = i;
            }
            if (pixels[i] == 0xFFFFFF && nLeft >= 0) {
                nRight = i == 0 ? 0 : i - 1;
            }
            if (nLeft >= 0 && nRight >= 0) {
                if (nRight - nLeft >= 1 && nRight - nLeft >= m_nRLineSite - m_nLLineSite) {//过滤小范围值得波动
                    m_nLLineSite = nLeft - flagLeng;
                    m_nRLineSite = nRight - flagLeng;
                }
                nLeft = -1;
                nRight = -1;
            }
        }
        boolean[] isChecks = new boolean[width * height];
        for (int i = 0; i < isChecks.length; i++) {
            isChecks[i] = false;
        }
        List<Integer> allData = new ArrayList<>();
        //获取左右连续边界   顺序不能乱
        //获取左侧向上的边
        List<Integer> greenUpData = getGreenDataUp(pixels, isChecks, width, height, m_nLLineSite, height / 2);
        allData.addAll(greenUpData);
        //获取右侧向上的边
        List<Integer> buleUpData = getBuleDataUp(pixels, isChecks, width, height, m_nRLineSite, height / 2);
        if(null != buleUpData && buleUpData.size()>0){
            Collections.reverse(buleUpData); // 倒序排列
            allData.addAll(buleUpData);
        }
//        //获取右侧向下的边
        List<Integer> buleDownData = getBuleDataDown(pixels, isChecks, width, height, m_nRLineSite, height / 2);
        if(null != buleDownData && buleDownData.size()>0){
            allData.addAll(buleDownData);
        }
//        //获取左侧向下的边
        List<Integer> greenDownData = getGreenDataDown(pixels, isChecks, width, height, m_nLLineSite, height / 2);
        if(null != greenDownData && greenDownData.size()>0){
            Collections.reverse(greenDownData); // 倒序排列
            allData.addAll(greenDownData);
        }
        //获取所有数据中的最高点和最低点
        int maxhightY = 10000;  //最高点Y最小
        int minHightY = -1;
        for (int i=0;i<allData.size();i++){
            int y = allData.get(i)/width;
            if(y<maxhightY){
                maxhightY = y;
            }
            if(y>minHightY){
                minHightY = y;
            }
        }
        //将描边点进行分类
        boolean isArrivedHight = false;
        boolean isArrivedLow = false;
        greenData.clear();
        buleData.clear();
        greenUpData.clear();
        greenDownData.clear();
        for(int x= 0;x<allData.size();x++){
            if(!isArrivedHight){
                if(allData.get(x)/width >  maxhightY){
                    greenUpData.add(allData.get(x));
                    continue;
                }else{
                    isArrivedHight = true;
                }
            }
            if(isArrivedHight && isArrivedLow){
                greenDownData.add(allData.get(x));
                continue;
            }
            if(isArrivedHight && !isArrivedLow) {
                if(allData.get(x)/width < minHightY) {
                    buleData.add(allData.get(x));
                }else{
                    isArrivedLow = true;
                }
            }
        }
        Collections.reverse(greenUpData); // 倒序排列
        greenData.addAll(greenUpData);
        Collections.reverse(greenDownData); // 倒序排列
//        greenDownData.set(0,height/2*width+m_nLLineSite);
        greenData.addAll(greenDownData);

        int flagLSite = 0;
        int lSite = m_nLLineSite+height*width/2;
        for(int i=0;i<greenData.size();i++){
            if(greenData.get(i) == lSite){
                flagLSite = i;
                Log.i("fei","列表的："+greenData.get(i) + "     左侧的点:"+ flagLSite);
                break;
            }
        }
        //计算左右垂直的坐标点
        if(flagLSite != 0){
            getLAndRSite(flagLSite);
        }else{
            Log.i("fei","左侧的边缘："+ greenData.size() + "      右侧的边缘：" + buleData.size());
        }
//        Log.i("fei","开始新建bitmap图像：" + System.currentTimeMillis());
        //新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        //设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width, height);
//        Log.i("fei","处理结束：" + System.currentTimeMillis());
        return resizeBmp;
    }

    /**
     * 计算左右垂直的坐标点
     */
    public static void getLAndRSite(int flagLSite ){
        int leftUp = greenData.get(flagLSite-2);
        int leftDown = greenData.get(flagLSite+2);
        int leftUpX = leftUp % width;
        int leftUpY = leftUp / width;
        int leftDownX = leftDown % width;
        int leftDownY = leftDown / width;
        //垂直线的斜率K    TODO：正斜率和负斜率相差90°
        float K = (float) -((leftDownX-leftUpX)*1.0/(leftDownY-leftUpY));
        float b = height/2-K*m_nLLineSite;

        for (int flag : buleData) {
            float flagX = (float) (flag % width);
            float flagY = (float) (flag / width);
            float flagSY = flagX*K + b;
            Log.i("fei","列表的："+flagY + "     计算的点:"+ flagSY);
            if(flagSY <= flagY+0.5 && flagSY >= flagY-0.5 ) {
                Log.i("fei","进入     列表的："+flagY + "     计算的点:"+ flagSY);
                FindLieFenUtils.m_nCRXLineSite = flagX;
                FindLieFenUtils.m_nCRYLineSite = flagY;
                FindLieFenUtils.m_nCLXLineSite = m_nLLineSite;
                FindLieFenUtils.m_nCLYLineSite = height / 2;
                float tanFlag =  (flagY-height / 2) / (flagX - m_nLLineSite);
                float dreege = (float) (Math.atan(tanFlag)/Math.PI*180);
                Log.i("fei","*****************************"+dreege);

                break;
            }
        }
    }


    private static void delSiglePoint(int[] pixels, int width) {
        for (int i = width + 1; i < pixels.length - width - 1; i++) {
            int flag = 0;
            if (pixels[i] == 0) {
                flag = pixels[i - 1] == 0 ? flag + 1 : flag;
                flag = pixels[i + 1] == 0 ? flag + 1 : flag;
                flag = pixels[i - width] == 0 ? flag + 1 : flag;
                flag = pixels[i + width] == 0 ? flag + 1 : flag;
                flag = pixels[i - width - 1] == 0 ? flag + 1 : flag;
                flag = pixels[i - width + 1] == 0 ? flag + 1 : flag;
                flag = pixels[i + width - 1] == 0 ? flag + 1 : flag;
                flag = pixels[i + width + 1] == 0 ? flag + 1 : flag;
                pixels[i] = flag <= 3 ? 0xFFFFFF : 0;
            }else if(pixels[i] == 0xFFFFFF){
                flag = 0;
                flag = pixels[i-1] == 0xFFFFFF ? flag+1 : flag;
                flag = pixels[i+1] == 0xFFFFFF ? flag+1 : flag;
                flag = pixels[i-width] == 0xFFFFFF ? flag+1 : flag;
                flag = pixels[i+width] == 0xFFFFFF ? flag+1 : flag;
                flag = pixels[i-width-1] == 0xFFFFFF ? flag+1 : flag;
                flag = pixels[i-width+1] == 0xFFFFFF ? flag+1 : flag;
                flag = pixels[i+width-1] == 0xFFFFFF ? flag+1 : flag;
                flag = pixels[i+width+1] == 0xFFFFFF ? flag+1 : flag;
                pixels[i] = flag<=3 ? 0 : 0xFFFFFF;
            }
        }
    }

    /**
     * 获取左侧向上的描边的点
     * @param pixels
     * @param x
     * @param y      TODO :::: 必须在子线程中
     */
    private static List<Integer> getGreenDataUp(int[] pixels, boolean[] isChecks, int width, int hight, int x, int y) {
        List<Integer> greenUpData = new ArrayList<>();
        if(!isChecks[y * width + x]) {
            greenUpData.add(y * width + x);
            isChecks[y * width + x] = true;
        }
        boolean stopFlag = true;
        int flagy, flagx;
        while (stopFlag) {
            if (x <= 1 || y <= 1 || y >= hight - 2 || x >= width - 1) {
                break;
            }
            //先往上检测
            flagy = y - 1;
            flagx = x;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            //左上
            flagy = y - 1;
            flagx = x - 1;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            //右上
            flagy = y - 1;
            flagx = x + 1;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            //右
            flagy = y;
            flagx = x + 1;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            //右下
            flagy = y + 1;
            flagx = x + 1;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            //下
            flagy = y + 1;
            flagx = x;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            //左下
            flagy = y + 1;
            flagx = x - 1;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            //左
            flagy = y;
            flagx = x - 1;
            if (!isChecks[flagy * width + flagx] && ((pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy + 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[(flagy - 1) * width + flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))) {
                x = flagx;
                y = flagy;
                isChecks[flagy * width + flagx] = true;
                greenUpData.add(flagy * width + flagx);
                continue;
            }
            stopFlag = false;
        }
        Log.i("fei", "getGreenDataUp: 左侧向上运行完");
        return greenUpData;
    }

    /**
     * 获取左侧向下的描边的点
     * @param pixels
     * @param x
     * @param y
     *  TODO :::: 必须在子线程中
     */
    private static List<Integer> getGreenDataDown(int [] pixels,boolean [] isChecks,int width,int hight,int x,int y) {
        List<Integer> greenDownData = new ArrayList<>();
//        greenDownData.add(y * width + x);
        boolean stopFlag = true;
        int flagy  , flagx ;
        while(stopFlag){
            if (x <= 1 || y <= 1 || y >= hight-2 || x >= width-1) {
                break;
            }
            //下
            flagy = y+1;
            flagx = x;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            //左下
            flagy = y+1;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            //右下
            flagy = y+1;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            //左
            flagy = y;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            //先往上检测
            flagy = y -1;
            flagx = x;
            if( !isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            //左上
            flagy = y-1;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            //右上
            flagy = y-1;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            //右
            flagy = y;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx-1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx + 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                greenDownData.add(flagy * width +flagx);
                continue;
            }
            stopFlag = false;
        }
        Log.i("fei", "getGreenDataUp: 左侧向下运行完");
        return greenDownData;
    }

    /**
     * 获取右侧向上的描边的点
     * @param pixels
     * @param x
     * @param y
     *  TODO :::: 必须在子线程中
     */
    private static List<Integer> getBuleDataUp(int [] pixels,boolean [] isChecks,int width,int hight,int x,int y) {
        List<Integer> buleUpData = new ArrayList<>();
        if(!isChecks[y * width + x]) {
            buleUpData.add(y * width + x);
            isChecks[y * width + x] = true;
        }
        boolean stopFlag = true;
        int flagy  , flagx ;
        while(stopFlag){
            if (x <= 1 || y <= 1 || y >= hight-2 || x >= width-1) {
                break;
            }
            //先往上检测
            flagy = y -1;
            flagx = x;
            if( !isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            //左上
            flagy = y-1;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            //右上
            flagy = y-1;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            //右
            flagy = y;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            //右下
            flagy = y+1;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            //下
            flagy = y+1;
            flagx = x;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            //左下
            flagy = y+1;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            //左
            flagy = y;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleUpData.add(flagy * width +flagx);
                continue;
            }
            stopFlag = false;
        }
        return buleUpData;
    }


    /**
     * 获取右侧向下的描边的点
     * @param pixels
     * @param x
     * @param y
     *  TODO :::: 必须在子线程中
     */
    private static List<Integer> getBuleDataDown(int [] pixels,boolean [] isChecks,int width,int hight,int x,int y) {
        List<Integer> buleDownData = new ArrayList<>();
//        buleDownData.add(y * width + x);
        boolean stopFlag = true;
        int flagy  , flagx ;
        while(stopFlag){
            if (x <= 1 || y <= 1 || y >= hight -2 || x >= width) {
                break;
            }
            //下
            flagy = y+1;
            flagx = x;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            //左下
            flagy = y+1;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            //右下
            flagy = y+1;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            //左
            flagy = y;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            //先往上检测
            flagy = y -1;
            flagx = x;
            if( !isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            //左上
            flagy = y-1;
            flagx = x-1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            //右上
            flagy = y-1;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            //右
            flagy = y;
            flagx = x+1;
            if(!isChecks[flagy * width +flagx] && ((pixels[flagy*width+flagx] == 0x00 && pixels[flagy*width+flagx+1] == 0xFFFFFF)
                    || (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy+1)*width+flagx] == 0xFFFFFF)
                    ||  (pixels[flagy*width+flagx] == 0x00 && pixels[(flagy-1)*width+flagx] == 0xFFFFFF)
                    || (pixels[flagy * width + flagx] == 0x00 && pixels[flagy * width + flagx - 1] == 0xFFFFFF))){
                x = flagx;
                y = flagy;
                isChecks[flagy * width +flagx] = true;
                buleDownData.add(flagy * width +flagx);
                continue;
            }
            stopFlag = false;
        }
        return buleDownData;
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
     * Bitmap转int像素组
     * @param bmp
     * @param w
     * @param h
     * @param flagB 取像素的点的间隔1：为累加 2：隔一个取一个
     * @return
     */
    public static int[] bitmap2RGBAtCenter(Bitmap bmp,int w,int h,int flagB) {
        int[] pixels = new int[w]; // Allocate for RGB
        int y = h/2;
        for (int x = 0; x < w; x+=flagB) {
            pixels[x] = bmp.getPixel(x,y);
        }
        return pixels;
    }
}
