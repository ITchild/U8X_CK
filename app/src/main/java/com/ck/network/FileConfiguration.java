/* 
 * @Title:  Configuration.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-8-29 下午4:30:05 
 * @version:  V1.0 
 */
package com.ck.network;

/**
 * TODO<-操作文件协议配置>
 * 
 * @author
 * @data: 2016-8-29 下午4:30:05
 * @version: V1.0
 */
public class FileConfiguration {
	/**
	 * 本地下载/上传文件路线
	 */
	public String strClientFilePath = "";
	/**
	 * 服务器下载/上传文件路线
	 */
	public String strServerPath = "";
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
