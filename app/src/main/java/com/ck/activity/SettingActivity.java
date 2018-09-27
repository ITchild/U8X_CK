package com.ck.activity;

import android.view.View;

import com.hc.u8x_ck.R;

public class SettingActivity extends TitleBaseActivity {
    @Override
    protected int setLayout() {
        return R.layout.ac_setting;
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initListener() {
        super.initListener();
        findViewById(R.id.setting_back_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
