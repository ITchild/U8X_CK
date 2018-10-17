package com.ck.main;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.ck.App_DataPara;
import com.ck.collect.Ui_Collect;
import com.ck.collect.Ui_FileSelete;
import com.ck.dlg.DLG_Alert;
import com.ck.netcloud.ui_net_soft_update;
import com.ck.utils.BroadcastAction;
import com.hc.u8x_ck.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    public TextView m_tvYIQIDY;
    // 声明一个数组，用来存储所有需要动态申请的权限
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,};
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();
    boolean bIsReload = false;
    BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastAction.Reload)) {
                bIsReload = true;
            }

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    State state = networkInfo.getState();
                    boolean isConnected = state == State.CONNECTED;
                    // true连接到WIFI false未连接或者正在连接
                    if (isConnected) {
                    } else {
                    }
                }
            }
        }
    };
    App_DataPara mApp;
    IntentFilter m_Filter;
    private boolean isNotShowAgin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ui_main);
        mApp = (App_DataPara) getApplication();
        m_Filter = new IntentFilter();
        m_Filter.addAction(BroadcastAction.Reload);
        registerReceiver(mWifiReceiver, m_Filter);
        TextView versionTV = (TextView) findViewById(R.id.soft_version);
        versionTV.setText("软件版本号:" + getVersion());

        bIsReload = false;
        m_tvYIQIDY = (TextView) findViewById(R.id.yiqiDY);

    }

    @Override
    protected void onResume() {
        if (bIsReload) {
            bIsReload = false;
            reload();
        }
        super.onResume();
        checkPermission();
    }

    private AlertDialog permissionDialog;
    private void checkPermission() {
        //判断哪些权限未授予以便必要的时候重新申请
        mPermissionList.clear();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);
            }
        }
         //判断存储委授予权限的集合是否为空
        if (null != mPermissionList && mPermissionList.size() > 0) {
            // 后续操作...
            if(null == permissionDialog) {
                permissionDialog = new AlertDialog.Builder(this).create();
                permissionDialog.setCancelable(false);
                permissionDialog.setTitle("提示");
                permissionDialog.setMessage("为了软件的正常运行\n请您同意软件的使用权限");
                permissionDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                });
                permissionDialog.setButton(AlertDialog.BUTTON_POSITIVE, "确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if (isNotShowAgin || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            getAppDetailSettingIntent();
                        } else {
                            getPermission();
                        }
                    }
                });
            }
            if(!permissionDialog.isShowing()) {
                permissionDialog.show();
            }
        } else {//未授予的权限为空，表示都授予了

        }
    }


    /**
     *  以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面(6.0系统测试可用)
      */
    private void getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
    }

    /**
     * 适配6.0以上，获取动态权限
     */
    private void getPermission() {
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    /**
     * {功能}<获取手机软件版本号>
     *
     * @return String 版本号
     * @throw
     */
    public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return "V" + version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {功能}<"基桩检测"按钮的点击事件>
     *
     * @return void
     * @throw
     */
    public void startCollect(View View) {
        Intent intent = new Intent(this, Ui_Collect.class);
        startActivity(intent);

    }

    /**
     * {功能}<"数据浏览"按钮的点击事件>
     *
     * @return void
     * @throw
     */
    public void startBrowse(View View) {
        startActivity(new Intent(this, Ui_FileSelete.class));
    }

    /**
     * {功能}<"参数设置"按钮的点击事件>
     *
     * @return void
     * @throw
     */
    public void ParaSet(View View) {
        Intent intent = new Intent(this, ui_net_soft_update.class);
        startActivity(intent);
    }

    /**
     * {功能}<"退出"按钮的点击事件>
     *
     * @return void
     * @throw
     */
    public void ExitProgram(View View) {
        Exitdialog();
    }

    /**
     * {功能}<退出对话框>
     *
     * @return void
     * @throw
     */
    protected void Exitdialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(" 提示信息");
        dialog.setMessage("确定要退出吗？");
        dialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                App_DataPara app_DataPara = (App_DataPara) getApplication();
                app_DataPara.UnRegistDiskReceiver();
                MainActivity.this.finish();
                System.exit(0);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // 这里添加点击确定后的逻辑
            }
        });
        dialog.create().show();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mWifiReceiver);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onReturn(null);
        }
        return false;
    }

    public void onReturn(View View) {
        final DLG_Alert alert = new DLG_Alert(this, "提示信息", "确定要退出吗？");
        alert.show();
        alert.setBtnOKOnClick(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                App_DataPara app_DataPara = (App_DataPara) getApplication();
                app_DataPara.UnRegistDiskReceiver();
                MainActivity.this.finish();
                System.exit(0);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                isNotShowAgin = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = ActivityCompat.
                                shouldShowRequestPermissionRationale(this, permissions[i]);
                        if (showRequestPermission) {
                            // 后续操作...
                        } else { //拒绝再次询问
                            isNotShowAgin = true;
                        }
                    }
                }
                checkPermission();
                // 授权结束后的后续操作...
                break;
            default:
                break;
        }
    }

    public void ThreadSleep(long nTime) {
        try {
            Thread.sleep(nTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
