/* 
 * @Title:  Err.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-9-1 上午11:13:03 
 * @version:  V1.0 
 */
package com.ck.network;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO<请描述这个类是干什么的>
 * 
 * @author
 * @data: 2016-9-1 上午11:13:03
 * @version: V1.0
 */
public class Info {
//	public static String IP = "192.168.1.189";
	public static String IP = "115.28.234.127";
	public static int Port = 2234;
	/**
	 * 发送或读取的数据包最大长度
	 */
	public static int nPacketLenth = 2048;
	
	public static Map<String, String> mErrMap = new HashMap<String, String>();
	public static String mRead0 = "服务器读取失败";
	public static String getErrInfo(String key) {
		initMap();
		return mErrMap.get(key);
	}

	private static void initMap() {
		mErrMap.clear();
		mErrMap.put("NULL ", "没有数据");
		mErrMap.put("ERR-3", "接收应答错误");
		mErrMap.put("ERR-2", "MySQL语句执行失败");
		mErrMap.put("ERR-1", "MySQL获取查询结果集失败");
		mErrMap.put("ERR01", "数据长度小于规定值");
		mErrMap.put("ERR02", "小于 数据包头的长度");
		mErrMap.put("ERR03", "版本号不对");
		mErrMap.put("ERR04", "数据条数小于1");
		mErrMap.put("ERR05", "数据长度大于规定值");
		mErrMap.put("ERR06", "SQL_CMD 影响行数为0");
		mErrMap.put("ERR11", "关键词不对");
		mErrMap.put("ERR12", "数据长度不匹配");
		mErrMap.put("ERR13", "产品型号或指令异常");
		mErrMap.put("ERR14", "数据类型异常");
		mErrMap.put("ERR15", "插入数据库失败");
		mErrMap.put("ERR16", "配置TW20失败");
		mErrMap.put("ERR20", "图片长度大于1M或者总包数小于1");
		mErrMap.put("ERR21", "创建临时文件失败");
		mErrMap.put("ERR22", "续写临时文件打开失败");
		mErrMap.put("ERR24", "传输完成后打开临时文件失败");
		mErrMap.put("ERR25", "大数据插入数据库失败");
		mErrMap.put("ERR26", "文件打开失败");
		mErrMap.put("ERR30", "检验位不对");
	}
}
