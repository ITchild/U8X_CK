package com.ck.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.hc.u8x_ck.R;

public abstract class TitleBaseActivity extends U8BaseAc {

    private TextClock baseTitle_clock_tc;
    protected TextView baseTitle_title_tv;
    private ImageView baseTitle_power_iv;
    private TextView baseTitle_power_tv;

    protected void initView(){
        baseTitle_clock_tc = findView(R.id.baseTitle_clock_tc);
        baseTitle_title_tv = findView(R.id.baseTitle_title_tv);
        baseTitle_power_iv = findView(R.id.baseTitle_power_iv);
        baseTitle_power_tv = findView(R.id.baseTitle_power_tv);
    }
    protected void initListener(){

    }
    protected  void  initData (){

    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);//电量检测
//        filter.addAction(Intent.ACTION_SCREEN_OFF); //息屏检测
        this.registerReceiver(this.mBatteryReceiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(this.mBatteryReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
            Log.i("fei",action);
            if(Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                String msg = "";
                int voltage = arg1.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
                msg = msg + "电压：" + voltage / 1000 + "." + voltage % 1000 + "V\n";

                int temperature = arg1.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                msg = msg + "温度：" + temperature / 10 + "." + temperature % 10 + "℃\n";

                int level = arg1.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = arg1.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int levelPercent = (int) (((float) level / scale) * 100);
                msg = msg + "电量：" + levelPercent + "%\n";

                int status = arg1.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                String strStatus = "未知状态";
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        strStatus = "充电中……";
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        strStatus = "放电中……";
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        strStatus = "未充电";
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        strStatus = "充电完成";
                        break;
                }
                msg = msg + "状态：" + strStatus + "\n";

                int health = arg1.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN);
                String strHealth = "未知 :(";
                switch (health) {
                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        strHealth = "好 :)";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        strHealth = "过热！";
                        break;
                    case BatteryManager.BATTERY_HEALTH_DEAD: // 未充电时就会显示此状态，这是什么鬼？
                        strHealth = "良好";
                        break;
                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        strHealth = "电压过高！";
                        break;
                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        strHealth = "未知 :(";
                        break;
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        strHealth = "过冷！";
                        break;
                }
                msg = msg + "健康状况：" + strHealth + "\n";

                String technology = arg1.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
                msg = msg + "电池技术：" + technology;
                Log.i("fei", msg);
                baseTitle_power_tv.setText(levelPercent+"%");
            }
//            else if (Intent.ACTION_SCREEN_OFF.equals(action)) { // 锁屏
//                screenOff();//调用息屏的方法
//            }
        }
    };

//    /**
//     * 息屏的方法
//     */
//    protected void screenOff(){
//
//    }

}
