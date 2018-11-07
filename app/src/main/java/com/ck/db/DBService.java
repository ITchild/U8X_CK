package com.ck.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ck.bean.MeasureDataBean;
import com.ck.info.UserInfo;
import com.fei.feilibs_1_0_0.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class DBService {

    public static DBService instence = null;
    static Context mContext;
    static DBOpenHelper dbOpenHelper;

    public static DBService getInstence(Context context) {
        if (instence == null) {
            if (instence == null) {
                mContext = context;
                instence = new DBService();
                dbOpenHelper = new DBOpenHelper(context);
            }
        }
        return instence;
    }

    private void DBService() {
    }

    public void SetUserInfo(UserInfo userInfo) {
        ContentValues values = new ContentValues();
        values.put("strdevno", userInfo.strDevNo);
        values.put("strusername", userInfo.strUserName);
        values.put("strpassword", userInfo.strPassWord);
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();
        readableDatabase.insert("UserID", null, values);
        readableDatabase.close();
    }

    public List<UserInfo> GetUserInfo() {
        List<UserInfo> mList = new ArrayList<UserInfo>();
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();
        Cursor rawQuery = readableDatabase.rawQuery("Select * from UserID", new String[]{});
        while (rawQuery.moveToNext()) {
            UserInfo info = new UserInfo();
            info.strDevNo = rawQuery.getString(rawQuery.getColumnIndex("strdevno"));
            info.strPassWord = rawQuery.getString(rawQuery.getColumnIndex("strpassword"));
            info.strUserName = rawQuery.getString(rawQuery.getColumnIndex("strusername"));
            mList.add(info);
        }
        rawQuery.close();
        readableDatabase.close();
        return mList;
    }

    public void DeleteUserInfo(UserInfo userInfo) {
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();
        readableDatabase.delete("UserID", "strdevno = ?", new String[]{userInfo.strDevNo});
        readableDatabase.close();
    }


    /**
     * 根据工程名，构件名，文件名 查找所存储的检测文件
     *
     * @param objName
     * @param gjName
     * @param fileName 可以为空
     * @return
     */
    public List<MeasureDataBean> getMeasureData(String objName, String gjName, String fileName,String fileState) {
        if(null == objName || null == gjName){
            return new ArrayList<>();
        }
        List<MeasureDataBean> mList = new ArrayList<>();
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();
        Cursor rawQuery;
        if (null == fileName || fileName.equals("")) {
            rawQuery = readableDatabase.rawQuery(
                    "Select * from MeasureData where objName=? and gjName=? and fileState=?",
                    new String[]{objName, gjName,fileState});
        } else {
            rawQuery = readableDatabase.rawQuery(
                    "Select * from MeasureData where objName=? and gjName=? and fileName=? and fileState=?",
                    new String[]{objName, gjName, fileName, fileState});
        }
        while (rawQuery.moveToNext()) {
            MeasureDataBean bean = new MeasureDataBean();
            bean.setId(rawQuery.getInt(rawQuery.getColumnIndex("id")));
            bean.setObjName(rawQuery.getString(rawQuery.getColumnIndex("objName")));
            bean.setGjName(rawQuery.getString(rawQuery.getColumnIndex("gjName")));
            bean.setFileName(rawQuery.getString(rawQuery.getColumnIndex("fileName")));
            bean.setObjCreateDate(rawQuery.getString(rawQuery.getColumnIndex("objCreateDate")));
            bean.setGjCreateDate(rawQuery.getString(rawQuery.getColumnIndex("gjCreateDate")));
            bean.setFileCreateDate(rawQuery.getString(rawQuery.getColumnIndex("fileCreateDate")));
            bean.setJudgeStyle(rawQuery.getString(rawQuery.getColumnIndex("judgeStyle")));
            bean.setMeasureDate(rawQuery.getString(rawQuery.getColumnIndex("measureDate")));
            bean.setWidth(rawQuery.getFloat(rawQuery.getColumnIndex("width")));
            bean.setAvage(rawQuery.getInt(rawQuery.getColumnIndex("avage")));
            bean.setLeftX(rawQuery.getFloat(rawQuery.getColumnIndex("leftX")));
            bean.setLeftY(rawQuery.getFloat(rawQuery.getColumnIndex("leftY")));
            bean.setRightX(rawQuery.getFloat(rawQuery.getColumnIndex("rightX")));
            bean.setRightY(rawQuery.getFloat(rawQuery.getColumnIndex("rightY")));
            bean.setCheckStyle(rawQuery.getString(rawQuery.getColumnIndex("checkStyle")));
            bean.setFileState(rawQuery.getString(rawQuery.getColumnIndex("fileState")));
            bean.setFileSize(rawQuery.getFloat(rawQuery.getColumnIndex("fileSize")));
            bean.setDelDate(rawQuery.getString(rawQuery.getColumnIndex("delDate")));
            mList.add(bean);
        }
        rawQuery.close();
        readableDatabase.close();
        return mList;
    }

    /**
     * 存储检测文件的数据
     * @param bean
     */
    public void SetMeasureData(MeasureDataBean bean) {
        if(null == bean){
            return;
        }
        ContentValues values = new ContentValues();
        values.put("objName", bean.getObjName());
        values.put("gjName", bean.getGjName());
        values.put("fileName", bean.getFileName());
        values.put("objCreateDate",bean.getObjCreateDate());
        values.put("gjCreateDate",bean.getGjCreateDate());
        values.put("fileCreateDate",bean.getFileCreateDate());
        values.put("judgeStyle",bean.getJudgeStyle());
        values.put("measureDate",bean.getMeasureDate());
        values.put("width",bean.getWidth());
        values.put("avage",bean.getAvage());
        values.put("leftX",bean.getLeftX());
        values.put("leftY",bean.getLeftY());
        values.put("rightX",bean.getRightX());
        values.put("rightY",bean.getRightY());
        values.put("checkStyle",bean.getCheckStyle());
        values.put("fileState",bean.getFileState());
        values.put("fileSize",bean.getFileSize());
        values.put("delDate",bean.getDelDate());
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();
        readableDatabase.insert("MeasureData", null, values);
        readableDatabase.close();
    }

    /**
     * 更新检测表的信息
     */
    public void upDateMeasureData(String flag , MeasureDataBean bean){
        if(null == flag || null == bean){
            return;
        }
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();
        ContentValues cv = new ContentValues();
        if(flag.equals("changeData")){ //更新测量裂缝的数据信息
            cv.put("width", bean.getWidth());
            cv.put("leftX", bean.getLeftX());
            cv.put("leftY", bean.getLeftY());
            cv.put("rightX", bean.getRightX());
            cv.put("rightY", bean.getRightY());
        }else{ //更新文件被删除信息
            //待定（和新建的文件有冲突）
            cv.put("fileState", bean.getFileState());
            cv.put("delDate", bean.getDelDate());
        }
        String[] args = {bean.getObjName(),bean.getGjName(),bean.getFileName()};
        String where = "objName=? and gjName=? and fileName=?";
        readableDatabase.update("MeasureData",cv, where,args);
        readableDatabase.close();
    }

    /**
     * 删除测量的数据
     * @param proName  不可为空
     * @param gjName   可为空
     * @param fileName   可为空
     */
    public void delMeasureData(String proName,String gjName,String fileName){
        if(StringUtil.isEmpty(proName)){
            return;
        }
        SQLiteDatabase readableDatabase = dbOpenHelper.getReadableDatabase();
        if(!StringUtil.isEmpty(proName) && StringUtil.isEmpty(gjName) && StringUtil.isEmpty(fileName)){
            //删除工程
            readableDatabase.delete("MeasureData",
                    "objName=?", new String[]{proName});
        }else if (!StringUtil.isEmpty(proName) && !StringUtil.isEmpty(gjName) && StringUtil.isEmpty(fileName)){
            //删除构件
            readableDatabase.delete("MeasureData",
                    "objName=? and gjName=?", new String[]{proName,gjName});
        }else if (!StringUtil.isEmpty(proName) && !StringUtil.isEmpty(gjName) && !StringUtil.isEmpty(fileName)){
            //删除文件
            readableDatabase.delete("MeasureData",
                    "objName=? and gjName=? and fileName=?", new String[]{proName,gjName,fileName});
        }
        readableDatabase.close();
    }



}
