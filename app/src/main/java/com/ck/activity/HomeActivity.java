package com.ck.activity;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ck.activity_key.KeyCollectActivity;
import com.ck.activity_key.KeyFileBowerActivity;
import com.ck.adapter.HomeDisAdapter;
import com.ck.base.TitleBaseActivity;
import com.ck.dlg.SigleBtMsgDialog;
import com.ck.service.SocketService;
import com.hc.u8x_ck.R;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends TitleBaseActivity {

    protected HomeDisAdapter mHomeDisAdapter;
    protected List<String> homeDisData;
    protected int num = 3;
    private RecyclerView home_display;
    @Override
    protected int initLayout() {
        return R.layout.ac_home;
    }

    @Override
    protected boolean isBackshow() {
        return false;
    }

    @Override
    protected void initView() {
        super.initView();
        home_display = findView(R.id.home_display);
        Intent intent = new Intent(this, SocketService.class);
        startService(intent);
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
                showMsgDialog();
                break;
            case 3:
                showMsgDialog();
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
        startActivity(new Intent(this, KeyCollectActivity.class));
    }

    /**
     * 跳转到文件管理界面
     */
    private void jumpToFileManger() {
        startActivity(new Intent(this, KeyFileBowerActivity.class));
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

}
