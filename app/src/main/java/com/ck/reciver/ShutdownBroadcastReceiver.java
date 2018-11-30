package com.ck.reciver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;

/**
 * @author fei
 * @date on 2018/11/26 0026
 * @describe TODO :
 **/
public class ShutdownBroadcastReceiver  extends BroadcastReceiver {
    private static String TAG = "ShutdownBroadcastReceiver";
    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    @Override
    public void onReceive(Context context, Intent intent) {  //即将关机时，要做的事情
        if (intent.getAction().equals(ACTION_SHUTDOWN)) {
            Log.i(TAG, "ShutdownBroadcastReceiver onReceive(), Do thing!");
            Vibrator vibrator = (Vibrator)context.getSystemService(context.VIBRATOR_SERVICE);
            vibrator.vibrate(5000);
        }
    }
}
