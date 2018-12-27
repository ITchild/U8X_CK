/* 
 * @Title:  DBOpenHelper.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2015-9-17 下午4:41:05 
 * @version:  V1.0 
 */
package com.ck.db;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ck.utils.PathUtils;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author
 * @data: 2015-9-17 下午4:41:05
 * @version: V1.0
 */
public class DBOpenHelper extends SQLiteOpenHelper {
	public static final String m_strDbName = PathUtils.DB_PATH_NAME;

	public DBOpenHelper(Context context) {
		super(context, m_strDbName, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS UserID(" 
				+ "id integer PRIMARY KEY AUTOINCREMENT," 
				+ "strdevno nvarchar," 
				+ "strusername nvarchar," 
				+ "strpassword nvarchar)");

		db.execSQL("CREATE TABLE IF NOT EXISTS MeasureGJData(" +
				"id integer PRIMARY KEY AUTOINCREMENT," +
				"objName nvarchar," +   //工程名称
				"fileName nvarchar," +   // 文件名称
				"fileCreateDate nvarchar," +  //文件创建时间
				"judgeStyle nvarchar," + //判别方式 (中间水平上判别   全自动判别)
				"measureDate nvarchar," +  //测量时间
				"width float," +  // 缝宽
				"avage integer," + //测量的均值
				"leftX float," +  // 左侧X坐标
				"leftY float," +  // 左侧Y坐标
				"rightX float," +  // 右侧X坐标
				"rightY float," +  // 右侧Y坐标
				"checkStyle nvarvhar," +  // 检测类型 (宽度检测   实时检测)
				"fileState nvarvhar," +  //  文件状态
				"fileSize float," +  //  文件大小
				"delDate nvarchar," + // 删除时间
				"image BLOB)");  //图片的二进制

		db.execSQL("CREATE TABLE IF NOT EXISTS MeasureOBJData(" +
				"id integer PRIMARY KEY AUTOINCREMENT," +
				"objName nvarchar," +   //工程名称
				"objCreateDate nvarchar)");   //工程创建时间
	}



	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
