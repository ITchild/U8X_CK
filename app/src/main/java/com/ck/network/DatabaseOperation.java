/* 
 * @Title:  DatabaseOperation.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2016-8-30 下午2:39:43 
 * @version:  V1.0 
 */
package com.ck.network;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO<-数据库操作>
 * 
 * @author
 * @data: 2016-8-30 下午2:39:43
 * @version: V1.0
 */
public class DatabaseOperation {
	/**
	 * SQL通信协议配置信息
	 */
	private static SQLConfiguration mConfiguration;
	/**
	 * true代表上传，false代表取消上传
	 */
	public static boolean m_bRun = true;

	/**
	 * 完成
	 */
	public static final int Finish = 0;
	/**
	 * 错误
	 */
	public static final int Err = 1;
	/**
	 * 进度
	 */
	public static final int Loading = 2;

	/**
	 * 服务器返回NULL 没有数据
	 */
	public static final int NULL = 3;
	public static OnNetWorkListener listener;
	/**
	 * 错误信息
	 */
	public static String mStrErrInfo;

	
	/**
	 * 
	 * {功能}<-执行SQL语句（增，删，改）>
	 * 
	 * @throw
	 * @return int Err,Finish,NULL
	 */
	public static void execSQL(SQLConfiguration con, OnNetWorkListener netWorkListener) {
		if (netWorkListener != null)
			listener = netWorkListener;

		mConfiguration = con; // socket协议信息
		m_bRun = true;

		int nReturn = 0;
		int nErrNum = 0;
		while (nErrNum < 3 && m_bRun) {
			nReturn = execSqlVisit(); // 访问服务器
			switch (nReturn) {
			case Finish: // 网络访问成功
				m_bRun = false;
				break;
			case Err: // 网络访问异常,或读取数据格式不正确.
				nErrNum++;
				break;
			case NULL:
				m_bRun = false;
				break;
			}
		}

		if (listener != null) {
			if (nErrNum == 3) {
				listener.onFailure(mStrErrInfo);// 网络访问失败监听
			} else {
				listener.onSuccess(); // 网络访问成功监听
			}
		}
	}

	static byte[] m_ArrRead = new byte[1024 * 1024];

	/**
	 * 
	 * {功能}<-执行SQL语句，访问服务器，并获取结果>
	 * 
	 * @throw
	 * @return int
	 */
	private static int execSqlVisit() {
		mStrErrInfo = "";
		SocketUtils m_SocketUtils = null;
		try {
			m_SocketUtils = new SocketUtils(Info.IP, Info.Port);
			byte[] date = initExecSQLSend(); // 初始化socket协议信息

			byte[] arr;
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
			}

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
			if (strResult.equals("HELLO")) { // SQL操作成功
				return NULL;
			} else { // SQL操作失败
				mStrErrInfo = Info.getErrInfo(strResult);
				return Err;
			}

		} catch (Exception e) {
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
	private static byte[] initExecSQLSend() {
		byte[] arr = null;
		try {
			String str = "HC" + "A" + "SQL_CMD" + "XXXX" + mConfiguration.strDevID + mConfiguration.strDevType + mConfiguration.strDevVer + "X" + "XX" + mConfiguration.strSQL;
			byte[] head = str.getBytes("gbk");
			if (mConfiguration.arrBlob == null) { // 判断sql是否含有bolb字段，若有数据跟在头协议后面
				arr = head;
			} else {
				arr = new byte[head.length + mConfiguration.arrBlob.length];
				System.arraycopy(head, 0, arr, 0, head.length);
				System.arraycopy(mConfiguration.arrBlob, 0, arr, head.length, mConfiguration.arrBlob.length);
			}

			byte[] len = ByteUtil.getBytes(arr.length);
			arr[10] = len[3]; // 总长度
			arr[11] = len[2];
			arr[12] = len[1];
			arr[13] = len[0];
			byte[] nSqlLen = ByteUtil.getBytes((short) mConfiguration.strSQL.getBytes("gbk").length);
			arr[37] = nSqlLen[1]; // SQL语句长度
			arr[38] = nSqlLen[0];

			arr[36] = 0;
			int nSum = 0;
			for (int i = 0; i < arr.length; i++) { // CRC初始化
				nSum += (arr[i] & 0xff);
			}
			arr[36] = (byte) ((nSum + arr.length) / 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}

	/*************************************************************************************/

	/**
	 * 
	 * {功能}<-查询云数据库操作>
	 * 
	 * @throw
	 * @return int Err,Finish,NULL
	 */
	public static int querySQL(SQLConfiguration con, OnNetWorkListener netWorkListener) {
		if (netWorkListener != null)
			listener = netWorkListener;
		mConfiguration = con; // socket协议信息
		m_bRun = true;
		int nReturn = 0;
		int nErrNum = 0;
		while (nErrNum < 3 && m_bRun) {

			nReturn = querySqlVisit(); // 访问服务器

			switch (nReturn) {
			case Finish:
				m_bRun = false; // 网络访问成功
				break;
			case Err: // 网络访问异常,或读取数据格式不正确.
				nErrNum++;
				break;
			case NULL:
				m_bRun = false;
				break;
			}
		}

		if (listener != null) {
			if (nErrNum == 3) {
				listener.onFailure(mStrErrInfo);
			} else {
				listener.onSuccess();
			}
		}
		return nReturn;
	}

	public static List<QueryResult> mQueryResults;

	private static int querySqlVisit() {
		mStrErrInfo = "";
		mQueryResults = new ArrayList<QueryResult>();
		SocketUtils m_SocketUtils = null;
		try {
			m_SocketUtils = new SocketUtils(Info.IP, Info.Port);
			byte[] date = initQuerySQLSend(); // 初始化soket协议
			m_SocketUtils.writeData(date); // 发送协议
			Thread.sleep(50);

			int nReadAllLenth = 0;
			int nErrNum = 0;
			int nHeadNidx = 0;
			boolean bIsFinish = false;
			byte[] arr = new byte[4];
			while (nErrNum != 400 && m_bRun) { // 读取返回结果
				int nAvailable = m_SocketUtils.m_InputStream.available();
				if (nAvailable != 0) {
					nErrNum = 0;
					nReadAllLenth += m_SocketUtils.readResult(m_ArrRead, nReadAllLenth, nAvailable);
					if (nReadAllLenth == 5 && new String(m_ArrRead, 0, 5).equals("NULL ")) {
						break;
					}
					while (nHeadNidx < nReadAllLenth) {
						int nAllLenth = 0;
						for (int i = nHeadNidx; i < nReadAllLenth; i++) {
							if (m_ArrRead[i] == 'H' && m_ArrRead[i + 1] == 'C') {
								arr[3] = m_ArrRead[i + 2];
								arr[2] = m_ArrRead[i + 3];
								arr[1] = m_ArrRead[i + 4];
								arr[0] = m_ArrRead[i + 5];
								nAllLenth = ByteUtil.getInt(arr);
								break;
							}
						}
						if (nHeadNidx + nAllLenth < nReadAllLenth) {
							nHeadNidx += nAllLenth;
						} else {
							break;
						}
						if (nAllLenth == 0 || nReadAllLenth == 0)
							break;
					}

					if (m_ArrRead[nHeadNidx + 6] == m_ArrRead[nHeadNidx + 8] && m_ArrRead[nHeadNidx + 7] == m_ArrRead[nHeadNidx + 9]) {
						bIsFinish = true;
						break;
					}
					if (bIsFinish) {
						break;
					}
				} else {
					nErrNum++;

					Thread.sleep(50);
				}
			}
			m_SocketUtils.writeData("HELLO".getBytes());
			m_SocketUtils.closeSocket();
			if (nReadAllLenth == 0) { // 为0异常
				mStrErrInfo = Info.mRead0;
				return Err;
			}
			if (nReadAllLenth == 5 && new String(m_ArrRead, 0, 5).equals("NULL ")) { // NULL
																						// 没有查询到数据
				return NULL;// 没有查询到数据
			}
			int nidx = 0;
			byte[] arrSinglePag = null; // 单包数据
			byte[] arrDataLenth = new byte[4]; // 单包数据长度
			while (nidx < nReadAllLenth && m_bRun) {
				if (nidx == nReadAllLenth - 1)
					break;
				if (m_ArrRead[nidx] == 'H' && m_ArrRead[nidx + 1] == 'C') {
					arrDataLenth[3] = m_ArrRead[nidx + 2];							 // 获取单包数据包长度
					arrDataLenth[2] = m_ArrRead[nidx + 3];
					arrDataLenth[1] = m_ArrRead[nidx + 4];
					arrDataLenth[0] = m_ArrRead[nidx + 5];
					int nDataLenth = ByteUtil.getInt(arrDataLenth);
					arrSinglePag = new byte[nDataLenth];							 // 初始化 单包数据包
					System.arraycopy(m_ArrRead, nidx, arrSinglePag, 0, nDataLenth);  // 单包数据包负值

					boolean bCheckReadResult = checkReadResult(arrSinglePag, nDataLenth); // 检验单包数据格式是否正确
					if (!bCheckReadResult) {
						nidx++;
						Log.i("main", "下载错误");
						continue; // 下载错误，跳出本次数据包，寻找下一数据包
					}
					QueryResult queryResult = new QueryResult(); // 单包数据包查询结果(表中一行)
					nidx += nDataLenth;
					int nFieldNum = arrSinglePag[11]; // 字段数
					byte[] arrFieldLenth = new byte[4];
					int nSum = 0;
					for (int i = 0; i < nFieldNum; i++) {
						arrFieldLenth[3] = arrSinglePag[12 + i * 4]; // 根据字段数，获取相应字段的长度
						arrFieldLenth[2] = arrSinglePag[13 + i * 4];
						arrFieldLenth[1] = arrSinglePag[14 + i * 4];
						arrFieldLenth[0] = arrSinglePag[15 + i * 4];
						int nFieldLenth = ByteUtil.getInt(arrFieldLenth);

						byte[] content = new byte[nFieldLenth]; // 初始字段内容
						System.arraycopy(arrSinglePag, 12 + nFieldNum * 4 + nSum, content, 0, nFieldLenth);
						nSum += nFieldLenth;

						queryResult.mResult.add(content);
					}

					mQueryResults.add(queryResult);

				} else
					nidx++;
			}
			Thread.sleep(400); // 曲线图显示数据时间
			return Finish;
		} catch (Exception e) {
			m_SocketUtils.closeSocket();
			e.printStackTrace();
			return Err;
		}
	}

	/**
	 * {功能}<-检查服务器返回数据是否正确>
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

		// 判断长度
		byte[] arr = new byte[4];
		arr[3] = bArrResult[2];
		arr[2] = bArrResult[3];
		arr[1] = bArrResult[4];
		arr[0] = bArrResult[5];
		int nAllLenth = ByteUtil.getInt(arr);
		if (nAllLenth != nDataLenth) {
			return false;
		}
		// 判断CRC
		byte nCRC = bArrResult[10];
		bArrResult[10] = 0;
		int nSum = 0;
		for (int i = 0; i < nDataLenth; i++) {
			nSum += (bArrResult[i] & 0xff);
		}
		if ((byte) ((nSum + nDataLenth) / 2) != nCRC) {
			return false;
		}
		return true;
	}

	private static byte[] initQuerySQLSend() {
		byte[] arr = null;
		try {
			String str = "HC" + "A" + "SQL_SEL" + "XXXX" + mConfiguration.strDevID + mConfiguration.strDevType + mConfiguration.strDevVer + "X" + mConfiguration.strSQL;
			arr = str.getBytes("gbk");

			byte[] len = ByteUtil.getBytes(arr.length);
			arr[10] = len[3];
			arr[11] = len[2];
			arr[12] = len[1];
			arr[13] = len[0];

			arr[36] = 0;
			int nSum = 0;
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
