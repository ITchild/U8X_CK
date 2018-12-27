package com.ck.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.ck.bean.PointBean;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
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

    private static int width = 0, heght = 0;
    private static Bitmap bitmap;
    private static Mat mat;

    public static Bitmap getDonePic(Bitmap pic) {
        width = pic.getWidth();
        heght = pic.getHeight();
        if (null == mat) {
            mat = new Mat();
        }
        org.opencv.android.Utils.bitmapToMat(pic, mat);
        mat = removeNoiseGaussianBlur(mat); //高斯滤波
        mat = removeBlur(mat); //模糊，去除毛刺
        mat = grayImage(mat);
        mat = equalizeHist(mat);
//        Log.i("fei","开始增加边界"+System.currentTimeMillis());
//        double[] matPoint = new double[mat.channels()];
//        for (int i = 0; i < mat.channels(); i ++) {
//            matPoint[i] = 255;
//        }
//        for(int i=0 ;i<mat.cols() ;i++){
//            mat.put(0, i , matPoint);
//            mat.put(mat.rows()-1, i , matPoint);
//        }
//        for(int i=0 ;i<mat.rows() ;i++){
//            mat.put(i, 0 , matPoint);
//            mat.put(i, mat.cols()-1 , matPoint);
//        }
//        Log.i("fei","开始寻找边界"+System.currentTimeMillis());
//        Mat cannyMat = cannyEdge(mat);
//        List<MatOfPoint> sideList = new ArrayList<>();
//        Mat hierarchy = new Mat();
//        Log.i("fei","开始获取边界数据"+System.currentTimeMillis());
//        Imgproc.findContours(cannyMat,sideList,hierarchy,Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//        allData.clear();
//        buleData.clear();
//        greenData.clear();
//        FindLieFenUtils.m_nCRXLineSite = 0;
//        FindLieFenUtils.m_nCRYLineSite = 0;
//        FindLieFenUtils.m_nCLXLineSite = 0;
//        FindLieFenUtils.m_nCLYLineSite = 0;
//        double maxArea = 0;
//        int maxPisition = 0;
//        Log.i("fei","开始获取最大面积"+System.currentTimeMillis());
//        for (int i=0;i<sideList.size();i++){
//            MatOfPoint matOfPoint = sideList.get(i);
//            double area = Imgproc.contourArea(matOfPoint);
//            if(maxArea < area){
//                maxArea = area;
//                maxPisition = i;
//            }
//        }
//        Log.i("fei","开始获取最优边界"+System.currentTimeMillis());
//        boolean[] checkFlag = new boolean[width*heght];
//        MatOfPoint matOfPoint = sideList.get(maxPisition);
//        for (int j=0;j<matOfPoint.toList().size();j++) {
//            float x = (float) matOfPoint.toList().get(j).x;
//            float y = (float) matOfPoint.toList().get(j).y;
//            if(!checkFlag[(int)(x*y)] && y > 1 && y < heght - 2 ) {
//                PointBean point = new PointBean();
//                point.setX(x);
//                point.setY(y);
//                allData.add(point);
//                checkFlag[(int)(x*y)] = true;
//            }
//        }
//        Log.i("fei","所有边缘设置的个数"+allData.size());
        if (null == bitmap) {
            bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        }
        org.opencv.android.Utils.matToBitmap(mat, bitmap);
//        getLRSidePoint();
        return bitmap;
    }

    /**
     * 对图片畸变的处理
     *
     * @param bitmap
     * @return
     */
    public static Bitmap doneExChange(Bitmap bitmap) {
        Mat src = new Mat();
        org.opencv.android.Utils.bitmapToMat(bitmap, src);
        Mat dst = src.clone();
        // 去畸变并保留最大图
        Mat cameraMatrix = Mat.eye(3, 3, CvType.CV_64F);
        cameraMatrix.put(0, 0,new double[]{8525.063595825412});
        cameraMatrix.put(0, 1,new double[]{0});
        cameraMatrix.put(0, 2,new double[]{398.686424711802});
        cameraMatrix.put(1, 1,new double[]{8355.455842424593});
        cameraMatrix.put(1, 2,new double[]{181.1043127788724});
        cameraMatrix.put(2, 2,new double[]{1});
//        cameraMatrix.put(2, 2,new double[]{1});
        Mat distCoeffs = Mat.zeros(5, 1, CvType.CV_64F);
        distCoeffs.put(0, 0,new double[]{-0.7641166321905001});
        distCoeffs.put(1, 0,new double[]{-11031.81353719961});
        distCoeffs.put(2, 0,new double[]{0.3643900469852158});
        distCoeffs.put(3, 0,new double[]{-0.01579503223510068});
//        distCoeffs.put(4, 0,new double[]{33050.1296180429});
        distCoeffs.put(4, 0,new double[]{0});
        Imgproc.undistort(src, dst, cameraMatrix, distCoeffs);
        org.opencv.android.Utils.matToBitmap(dst, bitmap);
        return bitmap;
    }

    public static void getLRSidePoint() {
        int minPoint = 0;
        int minPosition = 0;
        for (int i = 0; i < allData.size(); i++) {
            float flagY = allData.get(i).getY();
            if (minPoint < flagY) {
                minPoint = (int) flagY;
                minPosition = i;
            }
        }
        for (int i = 0; i < allData.size(); i++) {
            if (i <= minPosition) {
                greenData.add(allData.get(i));
            } else {
                PointBean endBean = allData.get(i);
                if (Math.abs(endBean.getX() - greenData.get(0).getX()) < 4
                        && Math.abs(endBean.getY() - greenData.get(0).getY()) < 4) {
                    break;
                } else {
                    buleData.add(endBean);
                }
            }
        }
        int flagLSite = 0;
        for (int i = 0; i < greenData.size(); i++) {
            PointBean bean = greenData.get(i);
            if (bean.getY() == heght / 2) {
                flagLSite = i;
            }
        }
        getLAndRSite(flagLSite);
    }

    /**
     * 计算左右垂直的坐标点
     */
    public static void getLAndRSite(int flagLSite) {
        float leftUpX = greenData.get(flagLSite - 2).getX();
        float leftUpY = greenData.get(flagLSite - 2).getY();
        float leftDownX = greenData.get(flagLSite + 2).getX();
        float leftDownY = greenData.get(flagLSite + 2).getY();
        float leftX = greenData.get(flagLSite).getX();
        float leftY = greenData.get(flagLSite).getY();
        //垂直线的斜率K    TODO：正斜率和负斜率相差90°
        float K = (float) -((leftDownX - leftUpX) * 1.0 / (leftDownY - leftUpY));
        float b = leftY - K * leftX;

        for (PointBean flag : buleData) {
            float flagX = flag.getX();
            float flagY = flag.getY();
            float flagSY = flagX * K + b;
            if (flagSY <= flagY + 0.5 && flagSY >= flagY - 0.5) {
                FindLieFenUtils.m_nCRXLineSite = flagX;
                FindLieFenUtils.m_nCRYLineSite = flagY;
                FindLieFenUtils.m_nCLXLineSite = leftX;
                FindLieFenUtils.m_nCLYLineSite = leftY;
                float tanFlag = (flagY - leftY) / (flagX - leftX);
                float dreege = (float) (Math.atan(tanFlag) / Math.PI * 180);
                Log.i("fei", "*****************************" + dreege);
                break;
            }
        }
    }


    //直方图均衡化
    public static Mat equalizeHist(Mat srcMat) {
        final Mat equalizeImage = new Mat();
        Imgproc.equalizeHist(srcMat, equalizeImage);
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
        Imgproc.blur(srcMat, blurredImage, size);
        return blurredImage;
    }

    //灰度处理
    public static Mat grayImage(Mat srcMat) {
        Mat grayImg = new Mat();
        Imgproc.cvtColor(srcMat, grayImg, Imgproc.COLOR_RGB2GRAY);
        Mat bWImg = new Mat();
        Imgproc.threshold(grayImg, bWImg, 50, 255.0, Imgproc.THRESH_BINARY);
        return bWImg;
    }

    //canny边缘检测
    public static Mat cannyEdge(Mat srcMat) {
        final Mat edgeImage = new Mat(); //100低阈值 200：高阈值
        Imgproc.Canny(srcMat, edgeImage, 70, 70, 3, true);
        return edgeImage;
    }

}
