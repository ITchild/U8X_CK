package com.ck.bean;

/**
 * @author fei
 * @date on 2018/10/29 0029
 * @describe TODO :
 **/
public class MeasureDataBean {

    public static String JUDGESTYLE_AUTO = "auto";  //判别方式的去自动判别
    public static String JUDGESTYLE_HORIZ = "horizontal"; // 判别方式的水平判决

    public static String  CHECKSTYLE_ONTIME = "ontime"; // 检测类型的实时检测
    public static String  CHECKSTYLE_WIDTH = "width";  // 检测类型的宽度检测

    public static String FILESTATE_DEL = "del" ;//文件类型  已经删除
    public static String FILESTATE_USERING = "useing" ;// 文件类型  使用中


    private int id;    //主键ID
    private String objName;  //工程名称
//    private String gjName;  //构件名称
    private String fileName;   // 文件名称
    private String objCreateDate;  //工程创建时间
//    private String gjCreateDate;  // 构件创建时间
    private String fileCreateDate;   //文件创建时间
    private String judgeStyle;   //判别方式 (中间水平上判别   全自动判别)
    private String measureDate;   //测量时间
    private float width;   // 缝宽
    private int avage;   // 检测到的平均颜色值
    private float leftX;    // 左侧X坐标
    private float leftY;     // 左侧Y坐标
    private float rightX;   // 右侧X坐标
    private float rightY;   // 右侧Y坐标
    private String checkStyle;  // 检测类型 (宽度检测   实时检测)
    private String fileState;    //  文件状态
    private float fileSize;   //  文件大小
    private String delDate;   // 删除时间


    public int getId() {
        //其它类型返回字段值本身
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getObjName() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return objName == null ? "" : objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

//    public String getGjName() {
//        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
//        return gjName == null ? "" : gjName;
//    }
//
//    public void setGjName(String gjName) {
//        this.gjName = gjName;
//    }

    public String getFileName() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return fileName == null ? "" : fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getAvage() {
        //其它类型返回字段值本身
        return avage;
    }

    public void setAvage(int avage) {
        this.avage = avage;
    }

    public String getObjCreateDate() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return objCreateDate == null ? "" : objCreateDate;
    }

    public void setObjCreateDate(String objCreateDate) {
        this.objCreateDate = objCreateDate;
    }

//    public String getGjCreateDate() {
//        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
//        return gjCreateDate == null ? "" : gjCreateDate;
//    }
//
//    public void setGjCreateDate(String gjCreateDate) {
//        this.gjCreateDate = gjCreateDate;
//    }

    public String getFileCreateDate() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return fileCreateDate == null ? "" : fileCreateDate;
    }

    public void setFileCreateDate(String fileCreateDate) {
        this.fileCreateDate = fileCreateDate;
    }

    public String getJudgeStyle() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return judgeStyle == null ? "" : judgeStyle;
    }

    public void setJudgeStyle(String judgeStyle) {
        this.judgeStyle = judgeStyle;
    }

    public String getMeasureDate() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return measureDate == null ? "" : measureDate;
    }

    public void setMeasureDate(String measureDate) {
        this.measureDate = measureDate;
    }

    public float getWidth() {
        //其它类型返回字段值本身
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getLeftX() {
        //其它类型返回字段值本身
        return leftX;
    }

    public void setLeftX(float leftX) {
        this.leftX = leftX;
    }

    public float getLeftY() {
        //其它类型返回字段值本身
        return leftY;
    }

    public void setLeftY(float leftY) {
        this.leftY = leftY;
    }

    public float getRightX() {
        //其它类型返回字段值本身
        return rightX;
    }

    public void setRightX(float rightX) {
        this.rightX = rightX;
    }

    public float getRightY() {
        //其它类型返回字段值本身
        return rightY;
    }

    public void setRightY(float rightY) {
        this.rightY = rightY;
    }

    public String getCheckStyle() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return checkStyle == null ? "" : checkStyle;
    }

    public void setCheckStyle(String checkStyle) {
        this.checkStyle = checkStyle;
    }

    public String getFileState() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return fileState == null ? "" : fileState;
    }

    public void setFileState(String fileState) {
        this.fileState = fileState;
    }

    public float getFileSize() {
        //其它类型返回字段值本身
        return fileSize;
    }

    public void setFileSize(float fileSize) {
        this.fileSize = fileSize;
    }

    public String getDelDate() {
        //如果是String类型，那么判断是否为空，为空返回"",否则返回字段值本身
        return delDate == null ? "" : delDate;
    }

    public void setDelDate(String delDate) {
        this.delDate = delDate;
    }
}
