package com.ck.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ck.base.RxBus;
import com.ck.bean.RxBusMsgBean;
import com.ck.utils.Catition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author fei
 * @date on 2018/10/16 0016
 * @describe TODO :
 **/
public class SocketService extends Service {

    private static final String TAG = "BackService";
    /** 主机IP地址  */
    private static String HOST = "192.168.3.195";
    /** 端口号  */
    public static final int PORT =8234;
    private long sendTime = 0L;
    private Socket socket;
    private ReadThread mReadThread;
    private InputStream is;//输入流
    private int count;//读取的字节长度
    @Override
    public void onCreate() {
        super.onCreate();
        new InitSocketThread().start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean sendMsg(String msg) {
        if (null == socket) {
            return false;
        }
        try {
            if (!socket.isClosed() && !socket.isOutputShutdown()) {
                OutputStream os = socket.getOutputStream();
                os.write(msg.getBytes());
                os.flush();
                sendTime = System.currentTimeMillis();// 每次发送成功数据，就改一下最后成功发送的时间，节省心跳间隔时间
                Log.i(TAG, "发送成功的时间：" + sendTime+"  内容-->"+msg);
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG,"send-->"+e.getMessage());
            return false;
        }
        return true;
    }

    // 初始化socket
    private void initSocket() throws UnknownHostException, IOException {
        socket = new Socket(HOST, PORT);
        socket.setSoTimeout(13000);//?
        if (socket.isConnected()){//连接成功
                mReadThread = new ReadThread(socket);
                mReadThread.start();
        }
    }

    // 释放socket
    private void releaseLastSocket(Socket mSocket) {
        try {
            if (null != mSocket) {
                if (!mSocket.isClosed()) {
                    is.close();
                    mSocket.close();
                }
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class InitSocketThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                initSocket();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.i(TAG,"socket-->"+e.getMessage());
                Log.i(TAG,"连接失败");
            } catch (IOException e) {
                e.printStackTrace();
                Log.i(TAG,"socket-->"+e.getMessage());
            }
        }
    }

    public class ReadThread extends Thread {
        private Socket rSocket;
        private boolean isStart = true;

        public ReadThread(Socket socket) {
            rSocket=socket;
        }

        public void release() {
            isStart = false;
            releaseLastSocket(rSocket);
        }

        @SuppressLint("NewApi")
        @Override
        public void run() {
            super.run();
            String line="";
            if (null != rSocket) {
                while (isStart&&!rSocket.isClosed()&&!rSocket.isInputShutdown()){
                    try {
                        is=rSocket.getInputStream();
                        count=is.available();
                        byte[] data=new byte[count];
                        is.read(data);
                        line=new String(data);
                        if (null != line && !line.trim().equals("")){
                            Log.i("Socket",line);
                            RxBusMsgBean busMsgBean = new RxBusMsgBean();
                            busMsgBean.setWhat(Catition.Key.KEY);
                            busMsgBean.setMsg(line);
                            RxBus.getInstance().post(busMsgBean);
                        }else {
                        }

                    } catch (IOException e) {
                        Log.i(TAG,"Read-->"+e.getClass().getName());
                        e.printStackTrace();
                        continue;
                    }
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
        mReadThread.release();
        releaseLastSocket(socket);
    }
}
