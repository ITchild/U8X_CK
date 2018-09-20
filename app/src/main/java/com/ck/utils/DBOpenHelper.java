/* 
 * @Title:  DBOpenHelper.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2015-9-17 下午4:41:05 
 * @version:  V1.0 
 */
package com.ck.utils;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
