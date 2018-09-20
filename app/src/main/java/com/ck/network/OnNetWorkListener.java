
/* 
 * @Title:  OnCurScrollListener.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-2-29 上午9:02:29 
 * @version:  V1.0 
 */ 
package com.ck.network;

/** 
 * TODO<-访问网络结果监听> 
 * @author 
 * @data:  2016-2-29 上午9:02:29 
 * @version:  V1.0 
 */
public interface OnNetWorkListener {
	void onSuccess();
	void onLoading(long total, long current);
	void onFailure(String strException);
}
