/* 
 * @Title:  SQLConfiguration.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-8-30 下午2:41:03 
 * @version:  V1.0 
 */
package com.ck.network;

/**
 * TODO<-操作云数据库协议配置>
 * 
 * @author
 * @data: 2016-8-30 下午2:41:03
 * @version: V1.0
 */
public class SQLConfiguration {
	/**
	 * 对数据库BLOB字段操作的数据
	 */
	public byte[] arrBlob = null;
	
	/**
	 * sql语句
	 */
	public String strSQL = "";
	/**
	 * 设备ID（长度17）
	 */
	public String strDevID = "01234567890123456";
	/**
	 * 设备类型（长度1，默认A）
	 */
	public String strDevType = "A";
	/**
	 * 设备版本（长度4，软件版本，格式0.00-9.99）
	 */
	public String strDevVer = "0.00";
}
