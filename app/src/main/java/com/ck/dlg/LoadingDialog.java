package com.ck.dlg;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import com.ck.base.U8BaseDialog;
import com.hc.u8x_ck.R;

/**
 * @author fei
 * @date on 2018/12/21 0021
 * @describe TODO :
 **/
public class LoadingDialog extends U8BaseDialog {
    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
    @Override
    protected int setLayout() {
        return R.layout.dialog_loading;
    }

    @Override
    protected void actionBeforSetContentView(Bundle savedInstanceState) {
        // 隐去标题栏（应用程序的名字）
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawableResource(android.R.drawable.screen_background_light_transparent);
        // 隐去状态栏部分(电池等图标和一切修饰部分)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setCanceledOnTouchOutside(false);
        super.actionBeforSetContentView(savedInstanceState);
    }

    @Override
    protected void initView() {

    }
}
