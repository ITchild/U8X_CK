package com.ck.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.ck.bean.PointBean;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fei
 * @date on 2018/12/5 0005
 * @describe TODO :
 **/
public class OpenCvLieFUtil {

    public static List<PointBean> allData = new ArrayList<>();

    public static List<PointBean> greenData = new ArrayList<>();
    public static List<PointBean> buleData = new ArrayList<>();

    private static int width = 0 , heght = 0;
    private static Bitmap bitmap ;
    private static Mat mat;

    public static Bitmap getDonePic(Bitmap pic){
        width = pic.getWidth();
        heght = pic.getHeight();
        if(null == mat) {
            mat = new Mat();
        }
        org.opencv.android.Utils.bitmapToMat(pic,mat);
        mat = removeNoiseGaussianBlur(mat); //高斯滤波
        mat = removeBlur(mat); //模糊，去除毛刺
        mat = grayImage(mat);
//        mat = equalizeHist(mat);
//        mat.channels();
//        byte[] matPoint = new byte[mat.channels()];
//        for (int i = 0; i < mat.channels(); i ++) {
//            matPoint[i] = 127;
//        }
//        for(int i=0 ;i<mat.cols() ;i++){
//            mat.put(0, i , matPoint);
//            mat.put(mat.rows()-1, i , matPoint);
//        }
//        for(int i=0 ;i<mat.rows() ;i++){
//            mat.put(i, 0 , matPoint);
//            mat.put(, mat.cols()-1 , matPoint);
//        }
        Mat cannyMat = cannyEdge(mat);
        List<MatOfPoint> sideList = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyMat,sideList,hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        allData.clear();
        boolean [] checked = new boolean[width * heght];
        for (int i=0;i<sideList.size();i++){
            MatOfPoint matOfPoint = sideList.get(i);
            double area = Imgproc.contourArea(matOfPoint);
            Log.i("fei","第"+i+"个Mat的面积"+ area);
            if(area < 15){
                continue;
            }
            for (int j=0;j<matOfPoint.toList().size();j++) {
                PointBean point = new PointBean();
                point.setX((float) matOfPoint.toList().get(j).x);
                point.setY((float) matOfPoint.toList().get(j).y);
                if(!checked[(int) (point.getX()*point.getY())]) {
                    allData.add(point);
                    checked[(int) (point.getX()*point.getY())] = true;
                }

            }
        }
        Log.i("fei","所有边缘设置的个数"+allData.size());
        if(null == bitmap) {
            bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        }
        org.opencv.android.Utils.matToBitmap(mat,bitmap);
        return bitmap;
    }


    public static void getLRSidePoint(){
        List<PointBean> lSidePoints = new ArrayList<>();
        List<PointBean> rSidePoints = new ArrayList<>();
        for (int i=0 ;i< OpenCvLieFUtil.allData.size() ; i++){
            PointBean pointBean = OpenCvLieFUtil.allData.get(i);
            int x =(int) pointBean.getX();
            int y =(int) pointBean.getY();
            if(pointBean.getY() == heght/2){
                if(bitmap.getPixel(x+1,y) == 0 && bitmap.getPixel(x-2,y)== 0xFF){
                    lSidePoints.add(new PointBean(x,y,i));
                }else if (bitmap.getPixel(x-2,y) == 0 && bitmap.getPixel(x+1,y)== 0xFF){
                    if(lSidePoints.size() != 0){//如果左侧还没有点，则右侧进行采集
                        rSidePoints.add(new PointBean(x,y,i));
                    }
                }
            }
        }
        if(lSidePoints.size() > rSidePoints.size()){ //如果左侧多于右侧，去掉最后的一个点
            lSidePoints.remove(lSidePoints.size()-1);
        }
        Log.i("fei","左侧个数：" +lSidePoints.size()+"   右侧个数：" +rSidePoints.size());
        float length = 0;
        PointBean lPoint = new PointBean(0,heght/2,0);
        PointBean rPoint = new PointBean(0,heght/2,0);
        for(int i=0;i<lSidePoints.size();i++){
            float flagLength = rSidePoints.get(i).getX() -lSidePoints.get(i).getX();
            if(length < flagLength){
                length = flagLength;
                lPoint.setX(lSidePoints.get(i).getX());   //找到左右边界最大的两个点
                lPoint.setSite(lSidePoints.get(i).getSite());
                rPoint.setX(rSidePoints.get(i).getX());
                rPoint.setSite(rSidePoints.get(i).getSite());
            }
        }
        Log.i("fei","左侧的点：" +lPoint.getX()+"*"+lPoint.getY() +"   右侧的点：" + rPoint.getX()+"*" + rPoint.getY());
        //根据左右边界最大的点，找边界
        boolean [] checkFlag = new boolean[width*heght];
        greenData.clear();
        buleData.clear();
        List<PointBean> flagAllData = new ArrayList<>();
        int lpointSite = lPoint.getSite();
        for(int i= lpointSite;i<allData.size()-1;i++){
            if(!checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())]
                    &&(Math.abs(allData.get(i).getX()-allData.get(i+1).getX()) <=2
                    ||Math.abs(allData.get(i).getY()-allData.get(i+1).getY()) <=2)){
                flagAllData.add(allData.get(i));
                checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())] = true;
            }else{
                break;
            }
        }
        for(int i= lpointSite-1;i>0;i--){
            if(!checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())]
                    &&(Math.abs(allData.get(i).getX()-allData.get(i-2).getX()) <=2
                    ||Math.abs(allData.get(i).getY()-allData.get(i-2).getY()) <=2)){
                flagAllData.add(allData.get(i));
                checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())] = true;
            }else{
                break;
            }
        }
        int rpointSite = rPoint.getSite();
        for(int i= rpointSite;i<allData.size()-1;i++){
            if(!checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())]
                    &&(Math.abs(allData.get(i).getX()-allData.get(i+1).getX()) <=2
                    ||Math.abs(allData.get(i).getY()-allData.get(i+1).getY()) <=2)){
                flagAllData.add(allData.get(i));
                checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())] = true;
            }else{
                break;
            }
        }
        for(int i= rpointSite-1;i>0;i--){
            if(!checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())]
                    &&(Math.abs(allData.get(i).getX()-allData.get(i-1).getX()) <=2
                    ||Math.abs(allData.get(i).getY()-allData.get(i-1).getY()) <=2)){
                flagAllData.add(allData.get(i));
                checkFlag[(int) (allData.get(i).getX() * allData.get(i).getY())] = true;
            }else{
                break;
            }
        }
        allData .clear();
        allData.addAll(flagAllData);


    }



    //直方图均衡化
    public static Mat equalizeHist(Mat srcMat) {
        final Mat equalizeImage = new Mat();
        Imgproc.equalizeHist(srcMat,equalizeImage);
        return equalizeImage;
    }

    //高斯滤波
    public static Mat removeNoiseGaussianBlur(Mat srcMat) {
        final Mat blurredImage = new Mat();
        Size size = new Size(7, 7);
        Imgproc.GaussianBlur(srcMat, blurredImage, size, 0, 0);
        return blurredImage;
    }

    //模糊，去除毛刺
    public static Mat removeBlur(Mat srcMat) {
        final Mat blurredImage = new Mat();
        Size size = new Size(3, 3);
        Imgproc.blur( srcMat, blurredImage, size);
        return blurredImage;
    }

    //灰度处理
    public static Mat grayImage(Mat srcMat) {
        Mat grayImg = new Mat();
        Imgproc.cvtColor(srcMat, grayImg, Imgproc.COLOR_RGB2GRAY);
        Mat bWImg = new Mat();
        Imgproc.threshold(grayImg,bWImg,70, 255.0, Imgproc.THRESH_BINARY );
        return grayImg;
    }

    //canny边缘检测
    public static Mat cannyEdge(Mat srcMat) {
        final Mat edgeImage = new Mat(); //100低阈值 200：高阈值
        Imgproc.Canny(srcMat, edgeImage, 70, 70,3,true);
        return edgeImage;
    }

}
