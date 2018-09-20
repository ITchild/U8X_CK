/* 
 * @Title:  HCNetWorkUtils.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-8-29 下午3:37:45 
 * @version:  V1.0 
 */
package com.ck.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * TODO<下载服务器文件>
 * 
 * @author
 * @data: 2016-8-29 下午3:37:45
 * @version: V1.0
 */
public class FileOperation {
	/**
	 * 通信协议配置信息
	 */
	private static FileConfiguration mConfiguration;
	/**
	 * true代表下载，false代表取消下载
	 */
	public static boolean m_bRun = true;
	/**
	 * 完成
	 */
	private static final int Finish = 0;
	/**
	 * 错误
	 */
	private static final int Err = 1;
	/**
	 * 进度
	 */
	private static final int Loading = 2;
	public static OnNetWorkListener listener;
	/**
	 * 错误信息
	 */
	public static String mStrErrInfo;

	/**
	 * 
	 * {功能}<网络下载文件>
	 * 
	 * @throw
	 * @return void
	 */
	public static void downloadFile(FileConfiguration con, OnNetWorkListener netWorkListener) {
		if (netWorkListener != null)
			listener = netWorkListener;
		mConfiguration = con; // socket协议信息
		m_bRun = true;
		Log.i("main", "downloadFile");
		int nReturn = 0;
		int nErrNum = 0;
		while (nErrNum < 3 && m_bRun) {

			nReturn = downloadVisit(); // 访问网络

			switch (nReturn) {
			case Finish:
				m_bRun = false; // 网络访问成功
				break;
			case Err: // 网络访问异常,或读取数据格式不正确.
				nErrNum++;
				Log.i("main", "nErrNum = " + nErrNum);
				break;
			}
		}
		Log.i("main", "结束" + nErrNum);
		if (listener != null) {
			if (nErrNum == 3) {
				listener.onFailure(mStrErrInfo); // 网络访问失败监听
			} else {
				listener.onSuccess(); // 网络访问成功监听
			}
		}
	}

	static byte[] m_ArrRead = new byte[1024 * 1024];
	static byte[] m_ArrReadAll = null;

	/**
	 * 
	 * {功能}<-访问服务器并下载文件>
	 * 
	 * @throw
	 * @return int
	 */
	private static int downloadVisit() {
		mStrErrInfo = "";
		m_ArrReadAll = null;
		SocketUtils m_SocketUtils = null;
		try {
			m_SocketUtils = new SocketUtils(Info.IP, Info.Port);
			// 分隔符索引
			if (!m_SocketUtils.m_bFlagSocketState)
				return Err;
			m_SocketUtils.writeData(initDownloadSend()); // 初始化 socket协议并发送

			int nReadDataLenth = 0;
			int nErrNum = 0;
			int nLenth;
			int nAllLenth = 0;
			while (nErrNum < 400 && m_bRun) { // 循环读取socket返回数据
				int nAvailable = m_SocketUtils.m_InputStream.available();
				if (nAvailable != 0) {
					nErrNum = 0;
					nLenth = m_SocketUtils.readResult(m_ArrRead, 0, nAvailable);
					if (m_ArrReadAll == null && m_ArrRead[0] == 'H' && m_ArrRead[1] == 'C') { // 获取首包数据中整个数据包的长度
						byte[] arr = new byte[4];
						arr[3] = m_ArrRead[2];
						arr[2] = m_ArrRead[3];
						arr[1] = m_ArrRead[4];
						arr[0] = m_ArrRead[5];
						nAllLenth = ByteUtil.getInt(arr);
						m_ArrReadAll = new byte[nAllLenth];
					}
					Log.i("main", "长度 = "+ nAvailable+ new String(m_ArrRead, 0, nAvailable));
					System.arraycopy(m_ArrRead, 0, m_ArrReadAll, nReadDataLenth, nLenth);
					nReadDataLenth += nLenth;

					if (listener != null) // 监听下载进度
						listener.onLoading(nAllLenth, nReadDataLenth);

					if (nReadDataLenth == nAllLenth) { // 读取长度 = 总长度时，退出循环
						break;
					}
				} else {
					nErrNum++;
					Thread.sleep(50);
				}
			}
			
			m_SocketUtils.writeData("HELLO".getBytes());
			m_SocketUtils.closeSocket();

			if (nReadDataLenth == 0) { // 为0读取异常
				mStrErrInfo = Info.mRead0;
				return Err;
			}
			if (nReadDataLenth == 12) { // 读取长度为12时，服务器没有下载文件
				mStrErrInfo = Info.getErrInfo(new String(m_ArrReadAll, 0, 5));
				return Err;// 仪器没有数据
			}

			boolean bCheckReadResult = checkReadResult(m_ArrReadAll, nReadDataLenth); // 校验
																						// 读取数据格式
			if (!bCheckReadResult) { // 下载错误，从新下载
				mStrErrInfo = "文件校验不对";
				return Err;
			}

			FileOutputStream out = new FileOutputStream(mConfiguration.strClientFilePath); // 保存下载文件到指定路径
			out.write(m_ArrReadAll, 7, nReadDataLenth - 7);
			out.flush();
			out.close();
			return Finish;
		} catch (Exception e) {
			if (m_SocketUtils != null)
				m_SocketUtils.closeSocket();
			e.printStackTrace();
			return Err;
		}
	}

	/**
	 * 
	 * {功能}<-初始化socket协议>
	 * 
	 * @throw
	 * @return byte[]
	 */
	private static byte[] initDownloadSend() {
		String str = "HC" + "A" + "DW_LOAD" + "XXXX" + mConfiguration.strDevID + mConfiguration.strDevType + mConfiguration.strDevVer + "X" + mConfiguration.strServerPath;

		byte[] arr = null;
		try {
			arr = str.getBytes("gbk");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] len = ByteUtil.getBytes(arr.length); // 总长度
		arr[10] = len[3];
		arr[11] = len[2];
		arr[12] = len[1];
		arr[13] = len[0];

		arr[36] = 0;
		int nSum = 0; // CRC
		for (int i = 0; i < arr.length; i++) {
			nSum += (arr[i] & 0xff);
		}
		arr[36] = (byte) ((nSum + arr.length) / 2);
		return arr;
	}

	/**
	 * 
	 * {功能}<-检验读取的数据格式>
	 * 
	 * @throw
	 * @return boolean
	 */
	private static boolean checkReadResult(byte[] bArrResult, int nDataLenth) {
		if (nDataLenth <= 0)
			return false;

		// 判断HC
		if (bArrResult[0] != 'H' && bArrResult[1] != 'C') {
			return false;
		}
		byte[] arr = new byte[4];
		arr[3] = bArrResult[2];
		arr[2] = bArrResult[3];
		arr[1] = bArrResult[4];
		arr[0] = bArrResult[5];
		int nFileLenth = ByteUtil.getInt(arr);
		// 判断文件长度
		if (nFileLenth != nDataLenth) {
			return false;
		}
		byte nCRC = bArrResult[6];
		bArrResult[6] = 0;
		int nSum = 0;
		for (int i = 0; i < nDataLenth; i++) {
			nSum += (bArrResult[i] & 0xff);
		}
		// 判断CRC
		if ((byte) ((nSum + nDataLenth) / 2) != nCRC) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * {功能}<-读取本地版本文件，获取版本信息，数组信息[0-新版本号，1-新apk名称,2-版本信息，3-总长度]>
	 * 
	 * @throw
	 * @return String[]
	 */
	public static String[] showUpDateDlg(String strVersionPath) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(strVersionPath), "gbk"));
			String readLine = null;
			StringBuffer buffer = new StringBuffer();
			while ((readLine = reader.readLine()) != null) {
				if (readLine.contains("/")) {
					buffer.append(readLine);
				} else {
					buffer.append(readLine + "、");
				}
			}
			String m_stResult = buffer.toString() + "/" + 0.63;
			String[] split = m_stResult.split("/");
			return split;
		} catch (Exception e) {
			return null;
		}
	}

	/********************************************************************************/

	/**
	 * 
	 * {功能}<-上传文件到服务器>
	 * 
	 * @throw
	 * @return void
	 */
	public static void uploadFile(FileConfiguration con, OnNetWorkListener netWorkListener) {
		if (netWorkListener != null)
			listener = netWorkListener;
		mConfiguration = con; // socket协议信息
		m_bRun = true;
		int nReturn = 0;
		int nErrNum = 0;
		while (nErrNum < 3 && m_bRun) {
			nReturn = uploadVisit(); // 访问网络
			switch (nReturn) {
			case Finish: // 网络访问成功
				m_bRun = false;
				break;
			case Err: // 网络访问异常,或读取数据格式不正确.
				nErrNum++;
				break;
			}
		}
		if (listener != null) {
			if (nErrNum == 3) { // 网络访问失败监听
				listener.onFailure(mStrErrInfo);
			} else { // 网络访问成功监听
				listener.onSuccess();
			}
		}
	}

	/**
	 * 
	 * {功能}<-访问服务器并上传数据>
	 * 
	 * @throw
	 * @return int
	 */
	private static int uploadVisit() {
		mStrErrInfo = "";
		SocketUtils m_SocketUtils = null;
		try {
			m_SocketUtils = new SocketUtils(Info.IP, Info.Port);
			byte[] date = initUploadSend(); // 初始化 socket协议并发送

			byte[] arr;
			int nTotal = date.length / Info.nPacketLenth + 1;
			for (int i = 0; i < date.length;) { // 数据表超过IPAndPort.nPacketLenth
												// 分包发送
				arr = null;
				if (i + Info.nPacketLenth >= date.length) {
					arr = new byte[date.length - i];
					System.arraycopy(date, i, arr, 0, date.length - i);
				} else {
					arr = new byte[Info.nPacketLenth];
					System.arraycopy(date, i, arr, 0, Info.nPacketLenth);
				}
				m_SocketUtils.writeData(arr);
				i += Info.nPacketLenth;
				listener.onLoading(nTotal, i + 1);
			}

			Thread.sleep(1000);
			int nReadDataLenth = 0;
			int nErrNum = 0;
			while (nErrNum != 400 && m_bRun) { // 循环读取返回结果
				int nAvailable = m_SocketUtils.m_InputStream.available();
				if (nAvailable != 0) {
					nErrNum = 0;
					nReadDataLenth = m_SocketUtils.readResult(m_ArrRead, 0, nAvailable);
					break;
				} else {
					nErrNum++;
					Thread.sleep(50);
				}
			}
			m_SocketUtils.writeData("HELLO".getBytes());
			m_SocketUtils.closeSocket();
			if (nReadDataLenth == 0) { // 读取为0异常
				mStrErrInfo = Info.mRead0;
				return Err;
			}
			String strResult = new String(m_ArrRead, 0, 5);
			if (strResult.equalsIgnoreCase("HELLO")) { // SQL操作成功
				return Finish;
			} else { // SQL操作失败
				mStrErrInfo = Info.getErrInfo(strResult);
				return Err;
			}

		} catch (Exception e) {
			if (m_SocketUtils != null)
				m_SocketUtils.closeSocket();
			e.printStackTrace();
			return Err;
		}
	}

	/**
	 * 
	 * {功能}<-初始化socket协议>
	 * 
	 * @throw
	 * @return byte[]
	 */
	private static byte[] initUploadSend() {
		byte[] arr = null;
		try {
			String str = "HC" + "A" + "UP_LOAD" + "XXXX" + mConfiguration.strDevID + mConfiguration.strDevType + mConfiguration.strDevVer + "X" + "XX" + mConfiguration.strServerPath;
			byte[] head = str.getBytes("gbk"); // 头协议

			File uploadFile = new File(mConfiguration.strClientFilePath); // 读取上传文件
			FileInputStream in = new FileInputStream(uploadFile);
			int nFileLenth = in.available();
			byte[] fileArr = new byte[nFileLenth];
			in.read(fileArr);

			arr = new byte[head.length + nFileLenth]; // 合并头协议和上传文件
			System.arraycopy(head, 0, arr, 0, head.length);
			System.arraycopy(fileArr, 0, arr, head.length, fileArr.length);

			byte[] len = ByteUtil.getBytes(arr.length); // 协议总长度
			arr[10] = len[3];
			arr[11] = len[2];
			arr[12] = len[1];
			arr[13] = len[0];

			byte[] nServerPathLen = ByteUtil.getBytes((short) mConfiguration.strServerPath.getBytes("gbk").length);
			arr[37] = nServerPathLen[1]; // 服务器上传路径长度
			arr[38] = nServerPathLen[0];

			arr[36] = 0;
			int nSum = 0; // CRC
			for (int i = 0; i < arr.length; i++) {
				nSum += (arr[i] & 0xff);
			}
			arr[36] = (byte) ((nSum + arr.length) / 2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return arr;
	}

	public static void cancel() {
		m_bRun = false;
		listener = null;
	}
}
