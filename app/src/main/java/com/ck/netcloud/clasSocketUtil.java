/* 
 * @Title:  SocketUtils.java 
 * @Copyright:  XXX Co., Ltd. Copyright YYYY-YYYY,  All rights reserved 
 * @Description:  TODO<请描述此文件是做什么的> 
 * @author:  
 * @data:  2015-3-13 下午4:58:06 
 * @version:  V1.0 
 */
package com.ck.netcloud;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * TODO<进行网络连接服务的类>
 * 
 * @author
 * @data: 2015-3-13 下午4:58:06
 * @version: V1.0
 */
public class clasSocketUtil {
	public static Socket m_Socket;
	public InputStream m_InputStream = null;
	public OutputStream m_OutputStream = null;
	public boolean m_bFlagSocketState = false;
	public String m_strIP;
	public int m_iPort;

	public clasSocketUtil(final String ip, final int port) {
//		new Thread() {
//			public void run() {
				try {
					m_strIP = ip;
					m_iPort = port;

					m_Socket = new Socket(ip, port);
					m_bFlagSocketState = true;
					if (m_OutputStream == null && m_Socket != null) {
						m_OutputStream = m_Socket.getOutputStream();
					}
					if (m_InputStream == null && m_Socket != null) {
						m_InputStream = m_Socket.getInputStream();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					m_bFlagSocketState = false;
					e.printStackTrace();
				}
//			}
//		}.start();

	}

	public void writeData(byte[] b) {
		try {
			if (m_OutputStream == null && m_Socket != null) {
				m_OutputStream = m_Socket.getOutputStream();
			}
			m_OutputStream.write(b);
			m_OutputStream.flush();
			m_bFlagSocketState = true;
		} catch (IOException e) {
			m_bFlagSocketState = false;
		}
	}

	public boolean isColse() {
		return m_Socket.isClosed();
	}

	public int readResult(byte[] readResult,int offset,int length) {
		int nLength = 0;
		try {
			if (m_InputStream == null && m_Socket != null) {
				m_InputStream = m_Socket.getInputStream();
			}
			nLength = 0;
			nLength = m_InputStream.read(readResult, offset, length);
			m_bFlagSocketState = true;
			if (nLength > 0) {
				return nLength;
			}

		} catch (IOException e) {
			Log.i("main", "接收异常");
			e.printStackTrace();
			m_bFlagSocketState = false;
		}
		return -1;
	}

	public void closeStream() {
		try {
			if (m_OutputStream != null) {
				m_Socket.shutdownOutput();
				m_OutputStream = null;
			}
			if (m_InputStream != null) {
				m_Socket.shutdownInput();
				m_InputStream = null;
			}
		} catch (IOException e) {
			m_bFlagSocketState = false;
			e.printStackTrace();
		}

	}

	public void closeSocket() {
		if (m_Socket != null) {
			try {
				if (m_OutputStream != null) {
					m_OutputStream.close();
					m_OutputStream = null;
				}
				if (m_InputStream != null) {
					m_InputStream.close();
					m_InputStream = null;
				}
				m_Socket.close();
				m_bFlagSocketState = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				m_bFlagSocketState = false;
				e.printStackTrace();
			}
		}
	}

	public void reconnect() {
		closeSocket();
		m_Socket = null;
		try {
			m_bFlagSocketState = true;
			m_Socket = new Socket(m_strIP, m_iPort);
		} catch (Exception e) {
			m_bFlagSocketState = false;
		}
	}
}
