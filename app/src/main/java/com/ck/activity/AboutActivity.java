package com.ck.activity;

import android.widget.TextView;

import com.ck.base.TitleBaseActivity;
import com.ck.utils.FileUtil;
import com.ck.utils.PackageUtil;
import com.hc.u8x_ck.R;

public class AboutActivity extends TitleBaseActivity {
    private TextView about_freeSpace_tv;
    private TextView about_softVision_tv;
    private TextView about_sysVision_tv;
    private TextView about_hardVision_tv;

    @Override
    protected int initLayout() {
        return R.layout.ac_about;
    }

    @Override
    protected void initView() {
        super.initView();
        about_freeSpace_tv = findView(R.id.about_freeSpace_tv);
        about_softVision_tv = findView(R.id.about_softVision_tv);
        about_sysVision_tv = findView(R.id.about_sysVision_tv);
        about_hardVision_tv = findView(R.id.about_hardVision_tv);
    }

    @Override
    protected void initData() {
        super.initData();
        about_freeSpace_tv.setText(FileUtil.getFreeSpaceperSend());
        about_softVision_tv.setText(PackageUtil.getVersion(this));
        about_sysVision_tv.setText("Android "+PackageUtil.getSystemVersion());
        about_hardVision_tv.setText("V 1.2.0");
        baseTitle_title_tv.setText("关于仪器");
    }

    @Override
    protected void initListener() {
        super.initListener();
//        findViewById(R.id.about_back_bt).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
    }
}
