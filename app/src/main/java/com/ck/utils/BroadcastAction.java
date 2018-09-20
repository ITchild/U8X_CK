
/* 
 * @Title:  BroadcastAction.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-4-1 上午9:15:46 
 * @version:  V1.0 
 */ 
package com.ck.utils;

/** 
 * TODO<请描述这个类是干什么的> 
 * @author 
 * @data:  2016-4-1 上午9:15:46 
 * @version:  V1.0 
 */
public class BroadcastAction {
	/**
	 * 接收并绘制最新采集数据
	 */
	public static String ReceiveData = "Action.ReceiveData";
	/**
	 * ClasSingleGraphView刷新曲线
	 */
	public static String RefreshSingleGraph = "Action.RefreshSingleGraphView";
	/**
	 * ClasMoveGraphView刷新曲线
	 */
	public static String RefreshMoveGraph = "Action.RefreshMoveGraph";
	
	/**
	 * 修改指定曲线的首波位置，刷新曲线
	 */
	public static String UpdateSingleGraph = "Action.UpdateSingleGraph";
	/**
	 * 计深装置未安装，停止采集。
	 */
	public static String StopCollect = "Action.StopCollect";
	/**
	 * 从新加载系统
	 */
	public static String Reload = "Action.reload";
	/**
	 * 仪器管理列表选择
	 */
	public static String ParSelect = "Action.parselect";
	/**
	 * 手动连接无线
	 */
	public static String HandConnectWifi = "Action.HandConnectWifi";
	/**
	 * 断开无线
	 */
	public static String DisconnectWifi = "Action.disconnectwifi";
	/**
	 * 寻找首波
	 */
	public static String FindHead = "Action.findHead";
	/**
	 * 结束寻找首波
	 */
	public static String FinishFindHead = "Action.finishFindHead";
	/**
	 * 点击曲线图单一曲线的当前
	 */
	public static String ClickDepth = "Action.ClickDepth";
	/**
	 * 保存采集文件后。
	 */
	public static String SaveFileFinish = "Action.SaveFileFinish";
	/**
	 * 更新进度条。
	 */
	public static String UpdataProgress = "Action.UpdataProgress";
	/**
	 * 采集零声时。
	 */
	public static String CollectT0 = "Action.CollectT0";
}
