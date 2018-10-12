package com.ck.dlg;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.hc.u8x_ck.R;

public class ShowPicDialog extends Dialog {

    private ImageView showPic_back_iv ,showPic_show_iv;

    public ShowPicDialog(@NonNull Context context) {
        super(context);
    }

    public ShowPicDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ShowPicDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow() ;
        WindowManager m = window.getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = window.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.9); // 改变的是dialog框在屏幕中的位置而不是大小
        window.setAttributes(p);
        window.setBackgroundDrawableResource(android.R.color.transparent);// 去掉dialog的默认背景

        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_showpic);
        initView();
        initListener();
    }

    private void initView(){
        showPic_back_iv = findViewById(R.id.showPic_back_iv);
        showPic_show_iv = findViewById(R.id.showPic_show_iv);
    }

    private void initListener(){
        showPic_back_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void showPicBmp(Bitmap bmp){
        showPic_show_iv.setImageBitmap(bmp);
    }
}
