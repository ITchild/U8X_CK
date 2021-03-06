package com.ck.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ck.base.TitleBaseActivity;
import com.ck.utils.DisplayUtil;
import com.ck.utils.PreferenceHelper;
import com.ck.utils.Stringutil;
import com.hc.u8x_ck.R;

public class SettingActivity extends TitleBaseActivity {

    public static final String ACTION_REQUEST_SHUTDOWN="android.intent.action.ACTION_REQUEST_SHUTDOWN";
    public static final String EXTRA_KEY_CONFIRM="android.intent.extra.KEY_CONFIRM";

    private RadioGroup setting_tab_rg;
    private LinearLayout setting_parm_ll;
    private LinearLayout setting_time_ll;
    private LinearLayout setting_check_ll;
    private LinearLayout setting_about_ll;

    private SeekBar settingPar_light_sb;//背光亮度
    private TextView settingPar_light_tv;//背光亮度数值显示
    private CheckBox settingPar_theme_cb;//主题的选项

    private EditText settingPar_pwd_et;

    @Override
    protected int initLayout() {
        return R.layout.ac_setting;
    }

    @Override
    protected void initView() {
        super.initView();
        setting_tab_rg = findView(R.id.setting_tab_rg);
        setting_parm_ll = findView(R.id.setting_parm_ll);
        setting_time_ll = findView(R.id.setting_time_ll);
        setting_check_ll = findView(R.id.setting_check_ll);
        setting_about_ll = findView(R.id.setting_about_ll);

        settingPar_light_sb = findView(R.id.settingPar_light_sb);
        settingPar_light_tv = findView(R.id.settingPar_light_tv);
        settingPar_theme_cb = findView(R.id.settingPar_theme_cb);
        settingPar_pwd_et = findView(R.id.settingPar_pwd_et);
    }

    @Override
    protected void initData() {
        super.initData();
        int theme = PreferenceHelper.getTheme();
        settingPar_theme_cb.setChecked(theme == R.style.AppTheme_White
                ? true : false);
        int light = PreferenceHelper.getScreenLisght();
        settingPar_light_sb.setProgress(light);
        settingPar_light_tv.setText(light+"");
        baseTitle_title_tv.setText("设置");
    }

    @Override
    protected void initListener() {
        super.initListener();
        findViewById(R.id.setting_back_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToHome();
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
                    case R.id.setting_about_rb :
                        setView(4);
                        break;
                }
            }
        });

        settingPar_light_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                settingPar_light_tv.setText(i+"");
                DisplayUtil.setScreenBrightness(i*255/100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PreferenceHelper.setScreenLight(seekBar.getProgress());
            }
        });
        findViewById(R.id.settingtime_date_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                DateUtil.setDate(2018,10,24);
//                DateUtil.setTime(13,34);
//                shutdown();
                reboot();
            }
        });
        settingPar_theme_cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!settingPar_theme_cb.isChecked()){
                    //日间
                    PreferenceHelper.setTheme(R.style.AppTheme_Black);
                    settingPar_theme_cb.setChecked(true);
                }else{
                    //夜间
                    PreferenceHelper.setTheme(R.style.AppTheme_White);
                    settingPar_theme_cb.setChecked(false);
                }
                recreate();
            }
        });
        settingPar_pwd_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String res = charSequence.toString();
                if(Stringutil.isEmpty(res)){
                    return;
                }
                if(res.equals("123456789")){
                    clickHome();//模拟点击Home键
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

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
                setting_about_ll.setVisibility(View.GONE);
                break;
            case 2 :
                setting_parm_ll.setVisibility(View.GONE);
                setting_time_ll.setVisibility(View.VISIBLE);
                setting_check_ll.setVisibility(View.GONE);
                setting_about_ll.setVisibility(View.GONE);
                break;
            case 3 :
                setting_parm_ll.setVisibility(View.GONE);
                setting_time_ll.setVisibility(View.GONE);
                setting_check_ll.setVisibility(View.VISIBLE);
                setting_about_ll.setVisibility(View.GONE);
                break;
            case 4 :
                setting_parm_ll.setVisibility(View.GONE);
                setting_time_ll.setVisibility(View.GONE);
                setting_check_ll.setVisibility(View.GONE);
                setting_about_ll.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 返回Home界面
     */
    private void backToHome(){
        finish();
    }

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     * 若要屏蔽Back键盘,注释该行代码即可
     */
    @Override
    public void onBackPressed() {
        backToHome();
        super.onBackPressed();
    }

    /**
     * 关机
     */
    private void shutdown() {
        Intent intent = new Intent(ACTION_REQUEST_SHUTDOWN);
        intent.putExtra(EXTRA_KEY_CONFIRM, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * 重启
     */
    private void reboot(){
        Intent intent=new Intent(Intent.ACTION_REBOOT);
        intent.putExtra("nowait", 1);
        intent.putExtra("interval", 1);
        intent.putExtra("window", 0);
        sendBroadcast(intent);
    }

    /**
     * 模拟Home键的点击
     */
    private void clickHome(){
        Intent intentw = new Intent(Intent.ACTION_MAIN);
        intentw.addCategory(Intent.CATEGORY_HOME);
        intentw.setClassName("android",
                "com.android.internal.app.ResolverActivity");
        startActivity(intentw);
    }


}
