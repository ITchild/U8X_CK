package com.ck.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import com.ck.adapter.HomeDisAdapter;
import com.ck.base.TitleBaseActivity;
import com.ck.dlg.SigleBtMsgDialog;
import com.ck.dlg.TwoBtMsgDialog;
import com.hc.u8x_ck.R;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends TitleBaseActivity {

    protected HomeDisAdapter mHomeDisAdapter;
    protected List<String> homeDisData;
    protected int num = 3; //RecycleView的gridLayout布局中的列数
    private RecyclerView home_display;

    //申请权限的dialog
    private TwoBtMsgDialog permissionDialog;
    private boolean isNotShowAgin = false; //用户是否选择了不在询问
    // 声明一个数组，用来存储所有需要动态申请的权限
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();

    @Override
    protected int initLayout() {
        return R.layout.ac_home;
    }

    @Override
    protected void initView() {
        super.initView();
        home_display = findView(R.id.home_display);
    }

    @Override
    protected void initData() {
        super.initData();
        if (null == homeDisData) {
            homeDisData = new ArrayList<>();
            homeDisData.add(getStr(R.string.str_measureWide));
            homeDisData.add(getStr(R.string.str_flieManger));
            homeDisData.add(getStr(R.string.str_onTimeMeasure));
            homeDisData.add(getStr(R.string.str_instrument));
            homeDisData.add(getStr(R.string.str_setting));
            homeDisData.add(getStr(R.string.str_about));
        }
        home_display.setLayoutManager(new GridLayoutManager(this, num));
        mHomeDisAdapter = new HomeDisAdapter(this, homeDisData);
        home_display.setAdapter(mHomeDisAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mHomeDisAdapter.setOnHomeDisItemClick(new HomeDisAdapter.OnHomeDisItemClick() {
            @Override
            public void onItem(int position) {
                jumpOnAll(position);
            }
        });
    }

    /**
     * 点击后跳转界面的所有方法的入口
     * @param position
     */
    protected void jumpOnAll(int position) {
        switch (position) {
            case 0:
                jumpToCollect();
                break;
            case 1:
                jumpToFileManger();
                break;
            case 2:
                jumpToOnTime();
                break;
            case 3:
                jumpToCalibration();
                break;
            case 4:
                jumpToSetting();
                break;
            case 5:
                jumpToAbout();
                break;
        }
        mHomeDisAdapter.setFocusPosition(position);
    }

    /**
     * 跳转到裂缝检测界面
     */
    private void jumpToCollect() {
        startActivity(new Intent(this, CollectActivity.class));
//        startActivity(new Intent(this, USBCollectActivity.class));
    }

    /**
     * 跳转到文件管理界面
     */
    private void jumpToFileManger() {
        startActivity(new Intent(this, FileBowerActivity.class));
    }
    /**
     * 跳转到定时监测界面
     */
    private void jumpToOnTime() {
        startActivity(new Intent(this, OnTimeCollectActivity.class));
    }

    /**
     * 跳转到标定界面
     */
    private void jumpToCalibration() {
        startActivity(new Intent(this, CalibrationActivity.class));
    }

    private void jumpToSetting() {
        startActivity(new Intent(this, SettingActivity.class));
        finish();
    }

    private void jumpToAbout() {
        startActivity(new Intent(this, AboutActivity.class));
    }

    /**
     * 显示的提示框
     */
    private void showMsgDialog() {
        final SigleBtMsgDialog dialog = new SigleBtMsgDialog(this);
        dialog.show();
        dialog.setTitleMsg(getStr(R.string.str_prompt));
        dialog.setMsg(getStr(R.string.str_noAc_msg));
        dialog.setOnBtClickListener(new SigleBtMsgDialog.OnBtClickListener() {
            @Override
            public void onBtClick() {
                dialog.dismiss();
            }
        });
    }


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
            if (null == permissionDialog) {
                permissionDialog = new TwoBtMsgDialog(HomeActivity.this);
                permissionDialog.setCancelable(false);
                permissionDialog.setOnBtClickListener(new TwoBtMsgDialog.OnBtClickListener() {
                    @Override
                    public void onBtClick(boolean isOk) {
                        if(isOk){
                            permissionDialog.dismiss();
                            if (isNotShowAgin || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                                getAppDetailSettingIntent();
                            } else {
                                getPermission();
                            }
                        }else{
                            permissionDialog.dismiss();
                            finish();
                        }
                    }
                });
            }
            if (!permissionDialog.isShowing()) {
                permissionDialog.show();
                permissionDialog.setTitle("提示");
                permissionDialog.setMsg("为了软件的正常运行\n请您同意软件的使用权限");
                permissionDialog.setBtCancelTxt("退出");
            }
        } else {//未授予的权限为空，表示都授予了

        }
    }
    /**
     * 适配6.0以上，获取动态权限
     */
    private void getPermission() {
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    /**
     * 以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面(6.0系统测试可用)
     */
    private void getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", getPackageName());
        }
        startActivity(localIntent);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {



        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



}
