package com.ck.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ck.info.UserInfo;

import java.util.ArrayList;
import java.util.List;

public class DBService {

	public static DBService instence = null;
	static Context mContext;
	static DBOpenHelper dbOpenHelper;

	private void DBService() {
	}

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
		Cursor rawQuery = readableDatabase.rawQuery("Select * from UserID", new String[] {});
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
		readableDatabase.delete("UserID", "strdevno = ?", new String[] {userInfo.strDevNo});
		readableDatabase.close();
	}
}
