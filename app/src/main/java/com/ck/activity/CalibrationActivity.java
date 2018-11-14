package com.ck.activity;

import android.content.Intent;
import android.view.View;
import android.widget.Button;

import com.ck.activity_key.KeyCollectActivity;
import com.ck.base.TitleBaseActivity;
import com.hc.u8x_ck.R;

/**
 * @author fei
 * @date on 2018/11/2 0002
 * @describe TODO :
 **/
public class CalibrationActivity extends TitleBaseActivity {
    private Button text_entryDemo;

    @Override
    protected int initLayout() {
        return R.layout.ac_calibration;
    }

    @Override
    protected void initView() {
        super.initView();
        text_entryDemo = (Button) findView(R.id.text_entryDemo);
    }

    @Override
    protected void initData() {
        super.initData();
        baseTitle_title_tv.setText("仪器标定");
    }

    @Override
    protected void initListener() {
        super.initListener();
        text_entryDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CalibrationActivity.this, KeyCollectActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

}
