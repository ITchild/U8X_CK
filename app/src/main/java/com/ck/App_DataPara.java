package com.ck;
/*
                           _ooOoo_
                          o8888888o
                          88" . "88
                          (| -_- |)
                          O\  =  /O
                       ____/`---'\____
                     .'  \\|     |//  `.
                    /  \\|||  :  |||//  \
                   /  _||||| -:- |||||-  \
                   |   | \\\  -  /// |   |
                   | \_|  ''\---/''  |   |
                   \  .-\__  `-`  ___/-. /
                 ___`. .'  /--.--\  `. . __
              ."" '<  `.___\_<|>_/___.'  >'"".
             | | :  `- \`.;`\ _ /`;.`/ - ` : | |
             \  \ `-.   \_ __\ /__ _/   .-` /  /
        ======`-.____`-.___\_____/___.-`____.-'======
                           `=---='
        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                 佛祖保佑       永无BUG
*/

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.ck.base.BaseApplication;
import com.ck.db.DBService;
import com.ck.dimenUtil.RudenessScreenHelper;
import com.ck.info.ClasFileProjectInfo;
import com.ck.netcloud.ClasSysPara;
import com.hc.u8x_ck.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class App_DataPara extends BaseApplication {
    private static App_DataPara app;
    /**
     * 屏幕密度
     */
    public int nTheme = R.style.AppTheme_Black;
    public ClasSysPara sysPara = new ClasSysPara();           //系统参数
    /**
     * 播放音乐标记位
     */
    public boolean m_bPlayMusic;
    /**
     * 数据库
     */
    public DBService m_DbService;
    private List<Activity> acList;
    private IntentFilter mFilter;
    private String m_strESDir = null;
    /**
     * U盘的广播的监听
     */
    private BroadcastReceiver mHandleMsg = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String pathString = intent.getData().getPath();

            if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {// 插入设备
                m_strESDir = pathString;
                Log.i("fei", pathString + "        " + intent.getDataString());
                Toast.makeText(context, "U盘已插入", Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
                m_strESDir = null;
            }
        }
    };
    private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

    public static App_DataPara getApp() {
        return app;
    }

    /**
     * 文件相关的数据的全局列表
     */
    public List<ClasFileProjectInfo> proData = new ArrayList<>();

    //设计图标注的宽度
    public int designWidth = 800;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //屏幕适配的代码
        new RudenessScreenHelper(this, designWidth).activate();
        m_DbService = DBService.getInstence(this);
        RegistDiskReceiver();
        initPro();
        initRegisterTime();
    }

    public void addAcToList(Activity activity) {
        if (null == acList) {
            acList = new ArrayList<>();
        }
        acList.remove(activity);
        acList.add(activity);
    }

    public void removeAcFromList(Activity activity) {
        if (null == acList || acList.size() <= 0) {
            return;
        }
        acList.remove(activity);
    }

    public void finishAll() {
        if (null == acList) {
            return;
        }
        Log.i("fei", acList.size() + "");
        for (Activity activity : acList) {
            activity.finish();
        }
    }

    public void finishOtherAll(Activity ac){
        if (null == acList || null == ac) {
            return;
        }
        Log.i("fei", acList.size() + "");
        for (Activity activity : acList) {
            if(ac != activity) {
                activity.finish();
            }
        }
        acList.clear();
        acList.add(ac);
    }

    /**
     * 崩溃文件的文件位置
     *
     * @return
     */
    @Override
    protected String setErrorLogPath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/" + getPackageName() + "/错误日志";
    }

    public void initRegisterTime() {
        SharedPreferences sp = getSharedPreferences("RegisterTime", Context.MODE_PRIVATE);
        String str = sp.getString("time", "");
        if (str.equals("")) {
            str = new SimpleDateFormat("yyMMddHHmmssSSS").format(new Date());
            str = str.substring(0, str.length() - 1);
            Editor edit = sp.edit();
            edit.putString("time", str);
            edit.commit();
        }
        sysPara.strDevRegistSN = "149" + str;
    }

    public void initPro() {

    }

    // U盘路径
    public String GetExternalStorageDirectory() {
        return m_strESDir;
//        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    private void RegistDiskReceiver() {
        if (mFilter == null) {
            mFilter = new IntentFilter();
            mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            mFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            mFilter.addDataScheme("file");
            registerReceiver(mHandleMsg, mFilter);
        }
    }

    public void UnRegistDiskReceiver() {
        if (mFilter != null) {
            unregisterReceiver(mHandleMsg);
        }
    }

    public String GetDigitalPile(String strData) {
        String strName = ""; // 汉字部分
        String strDigital = ""; // 数字部分
        int nDigital = 1; // 数字部分
        for (int i = 0; i < strData.length(); i++) {
            if (Character.isDigit(strData.charAt(i))) {
                strDigital += String.valueOf(strData.charAt(i));
            } else {
                strName += strData.charAt(i);
            }
        }

        if (!strDigital.equals("")) {
            nDigital = Integer.parseInt(strDigital) + 1;
        }
        return strName + nDigital;
    }

    /**
     * {功能}<请描述这个方法是干什么的>
     *
     * @return void
     * @throw nType:1-波形，2-字体,3-背景颜色
     */
    public int getZhuTiColor(int nType) {
        switch (nTheme) {
            case R.style.AppTheme_White: // 主题-白色
                switch (nType) {
                    case 1: // 波形
                        return Color.BLACK;
                    case 2:// 字体颜色
                        return Color.BLACK;
                    case 3:
                        return Color.WHITE;
                }
                break;
            case R.style.AppTheme_Black: // 主题-黑色
                switch (nType) {
                    case 1: // 波形
                        return Color.WHITE;
                    case 2:// 字体颜色
                        return Color.WHITE;
                    case 3:
                        return Color.BLACK;
                }
                break;
        }
        return 0;
    }

    public WindowManager.LayoutParams getMywmParams() {
        return wmParams;
    }

}
