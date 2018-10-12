package com.ck.activity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ck.base.TitleBaseActivity;
import com.hc.u8x_ck.R;

public class SettingActivity extends TitleBaseActivity {
    private RadioGroup setting_tab_rg;
    private LinearLayout setting_parm_ll;
    private LinearLayout setting_time_ll;
    private LinearLayout setting_check_ll;

    private SeekBar settingPar_light_sb;//背光亮度
    private TextView settingPar_light_tv;//背光亮度数值显示

    @Override
    protected int initLayout() {
        return R.layout.ac_setting;
    }

    @Override
    protected boolean isBackshow() {
        return false;
    }

    @Override
    protected void initView() {
        super.initView();
        setting_tab_rg = findViewById(R.id.setting_tab_rg);
        setting_parm_ll = findViewById(R.id.setting_parm_ll);
        setting_time_ll = findViewById(R.id.setting_time_ll);
        setting_check_ll = findViewById(R.id.setting_check_ll);

        settingPar_light_sb = findViewById(R.id.settingPar_light_sb);
        settingPar_light_tv = findViewById(R.id.settingPar_light_tv);
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
        setting_tab_rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.setting_parm_rb :
                        setView(1);
                        break;
                    case R.id.setting_time_rb :
                        setView(2);
                        break;
                    case R.id.setting_check_rb :
                        setView(3);
                        break;
                }
            }
        });

        settingPar_light_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                settingPar_light_tv.setText(i+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * 根据选中tab改变View
     * @param viewFlag 1：参数设置  2： 时间设置  3 ： 检定设置
     */
    private void setView(int viewFlag){
        switch (viewFlag){
            case 1 :
                setting_parm_ll.setVisibility(View.VISIBLE);
                setting_time_ll.setVisibility(View.GONE);
                setting_check_ll.setVisibility(View.GONE);
                break;
            case 2 :
                setting_parm_ll.setVisibility(View.GONE);
                setting_time_ll.setVisibility(View.VISIBLE);
                setting_check_ll.setVisibility(View.GONE);
                break;
            case 3 :
                setting_parm_ll.setVisibility(View.GONE);
                setting_time_ll.setVisibility(View.GONE);
                setting_check_ll.setVisibility(View.VISIBLE);
                break;
        }
    }

}
