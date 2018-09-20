/* 
 * @Title:  QueryResult.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-8-31 上午9:46:56 
 * @version:  V1.0 
 */
package com.ck.network;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO<查询数据库操作后，存储结果类。每一个List中存放的byte代表查询数据库时字段的顺序>
 * 
 * @author
 * @data: 2016-8-31 上午9:46:56
 * @version: V1.0
 */
public class QueryResult {
	public List<byte[]> mResult = new ArrayList<byte[]>();
}
