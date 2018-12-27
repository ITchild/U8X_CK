
/* 
 * @Title:  ClasFileManage.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-3-18 下午3:19:54 
 * @version:  V1.0 
 */ 
package com.ck.info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/** 
 * TODO<工程文件夹管理器> 
 * @author 
 * @data:  2016-3-18 下午3:19:54 
 * @version:  V1.0 
 */
public class ClasFileProjectInfo implements Serializable{
	/**
	 * 工程文件名
	 */
	public String mFileProjectName;
	/**
	 * 是否选中0未选中，1半选中，2完全选中
	 */
	public int nIsSelect = 0;
	
	/**
	 * 修改日期
	 */
	public String mLastModifiedDate;
	
	/**
	 * 工程种文件信息
	 */
	public List<ClasFileGJInfo> mstrArrFileGJ = new ArrayList<ClasFileGJInfo>();




}
