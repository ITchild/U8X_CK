package com.ck.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kongqw.serialportlibrary.SerialPortManager;
import com.kongqw.serialportlibrary.listener.OnOpenSerialPortListener;
import com.kongqw.serialportlibrary.listener.OnSerialPortDataListener;

import java.io.File;
import java.util.Arrays;

/**
 * @author fei
 * @date on 2018/11/2 0002
 * @describe TODO :
 **/
public class SerialService extends Service implements OnOpenSerialPortListener {
    String device_name = "/dev/ttyUSB0";
    int Baudrate = 115200;
    private String TAG = SerialService.class.getSimpleName();
    private SerialPortManager mSerialPortManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSerialPortManager = new SerialPortManager();

        // 打开串口
        boolean openSerialPort = mSerialPortManager.setOnOpenSerialPortListener(this)
                .setOnSerialPortDataListener(new OnSerialPortDataListener() {
                    @Override
                    public void onDataReceived(byte[] bytes) {
                        Log.i(TAG, "onDataReceived [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "onDataReceived [ String ]: " + new String(bytes));
                        final byte[] finalBytes = bytes;
                    }

                    @Override
                    public void onDataSent(byte[] bytes) {
                        Log.i(TAG, "onDataSent [ byte[] ]: " + Arrays.toString(bytes));
                        Log.i(TAG, "onDataSent [ String ]: " + new String(bytes));
                        final byte[] finalBytes = bytes;
                    }
                })
                .openSerialPort(new File(device_name), Baudrate);

        Log.i(TAG, "onCreate: openSerialPort = " + openSerialPort);

    }

    @Override
    public void onSuccess(File file) {
        Log.i(TAG,"打开成功");
    }

    @Override
    public void onFail(File file, Status status) {
        switch (status) {
            case NO_READ_WRITE_PERMISSION:
                Log.i(TAG, "没有读写权限");
                break;
            case OPEN_FAIL:
            default:
                Log.i(TAG,"串口打开失败");
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null != mSerialPortManager) {
            mSerialPortManager.closeSerialPort();
        }
    }
}
