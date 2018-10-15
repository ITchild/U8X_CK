package com.ck.dlg;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.fei.feilibs_1_0_0.base.dialog.BaseDialog;
import com.hc.u8x_ck.R;

/**
 * @author fei
 * @date on 2018/10/15 0015
 * @describe TODO : 用于只有一个Button提示框的Dialog展示
 **/
public class SigleBtMsgDialog extends BaseDialog {
    private TextView tv_sigleBtMsg_title;
    private TextView tv_sigleBtMsg_msg;
    private Button bt_sigleBtMsg_yes;

    public SigleBtMsgDialog(@NonNull Context context) {
        super(context);
    }

    public SigleBtMsgDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public SigleBtMsgDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 隐去标题栏（应用程序的名字）
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐去状态栏部分(电池等图标和一切修饰部分)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setCanceledOnTouchOutside(false);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setLayout() {
        return R.layout.dlg_siglebtmsg;
    }

    @Override
    protected void initView() {
        tv_sigleBtMsg_title = findView(R.id.tv_sigleBtMsg_title);
        tv_sigleBtMsg_msg = findView(R.id.tv_sigleBtMsg_msg);
        bt_sigleBtMsg_yes = findView(R.id.bt_sigleBtMsg_yes);

        bt_sigleBtMsg_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnBtClickListener){
                    mOnBtClickListener.onBtClick();
                }
            }
        });
    }

    public void setTitleMsg(String title) {
        if (null == tv_sigleBtMsg_title) {
            return;
        }
        tv_sigleBtMsg_title.setText(null == title ? "提示" : title);
    }

    public void setMsg(String msg) {
        if (null == tv_sigleBtMsg_msg) {
            return;
        }
        tv_sigleBtMsg_msg.setText(null == msg ? "" : msg);
    }

    public void setBtTxt(String txt){
        if(null == bt_sigleBtMsg_yes){
            return;
        }
        bt_sigleBtMsg_yes.setText(null == txt ? "确定" : txt);
    }


    public void setOnBtClickListener(OnBtClickListener mOnBtClickListener){
        this.mOnBtClickListener = mOnBtClickListener;
    }
    public interface  OnBtClickListener {
        void onBtClick();
    }
    private OnBtClickListener mOnBtClickListener;
}
