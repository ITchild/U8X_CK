package com.ck.dlg;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;

import com.ck.base.U8BaseDialog;
import com.hc.u8x_ck.R;

/**
 * @author fei
 * @date on 2018/12/21 0021
 * @describe TODO :
 **/
public class ChoiceSaveTypeDialog extends U8BaseDialog {

    private CheckBox choiceSave_Draw_cb;
    private CheckBox choiceSave_NoDraw_cb;
    private Button choiceSave_OK_bt;
    private Button choiceSave_Cancel_bt;

    public ChoiceSaveTypeDialog(@NonNull Context context) {
        super(context);
    }

    public ChoiceSaveTypeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public ChoiceSaveTypeDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected int setLayout() {
        return R.layout.dialog_choicesavetype;
    }

    @Override
    protected void actionBeforSetContentView(Bundle savedInstanceState) {
        // 隐去标题栏（应用程序的名字）
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐去状态栏部分(电池等图标和一切修饰部分)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setCanceledOnTouchOutside(false);
        super.actionBeforSetContentView(savedInstanceState);
    }

    @Override
    protected void initView() {
        choiceSave_Draw_cb = (CheckBox) findView(R.id.choiceSave_Draw_cb);
        choiceSave_Draw_cb.setChecked(true);
        choiceSave_NoDraw_cb = (CheckBox)findView(R.id.choiceSave_NoDraw_cb);
        choiceSave_NoDraw_cb.setChecked(true);
        choiceSave_OK_bt = (Button)findView(R.id.choiceSave_OK_bt);
        choiceSave_Cancel_bt = (Button) findView(R.id.choiceSave_Cancel_bt);
    }

    @Override
    protected void initListener() {
        super.initListener();
        choiceSave_OK_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int type = -1;
                if(choiceSave_Draw_cb.isChecked() && !choiceSave_NoDraw_cb.isChecked()){
                    type = 1; //直接使用
                }else if(!choiceSave_Draw_cb.isChecked() && choiceSave_NoDraw_cb.isChecked()){
                    type = 2;//导入到电脑
                }else if(choiceSave_Draw_cb.isChecked() && choiceSave_NoDraw_cb.isChecked()){
                    type = 3;//两种都有
                }
                if(null != mOnChoiceSaveClick){  //type -1:未选择  1：直接使用的图片 2：导入到电脑的图片 3：两种都有
                    mOnChoiceSaveClick.isOk(true,type);
                }
            }
        });
        choiceSave_Cancel_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null != mOnChoiceSaveClick){
                    mOnChoiceSaveClick.isOk(false,-1);
                }
            }
        });
    }

    public interface OnChoiceSaveClick{
        void isOk(boolean isOk,int type);
    }
    private OnChoiceSaveClick mOnChoiceSaveClick;
    public void setOnChoiceSaveClick(OnChoiceSaveClick mOnChoiceSaveClick){
        this.mOnChoiceSaveClick = mOnChoiceSaveClick;
    }

}
