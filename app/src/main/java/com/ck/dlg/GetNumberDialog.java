package com.ck.dlg;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ck.base.U8BaseDialog;
import com.hc.u8x_ck.R;

/**
 * @author fei
 * @date on 2018/11/13 0013
 * @describe TODO :
 **/
public class GetNumberDialog extends U8BaseDialog {

    protected GetNumberDialog(@NonNull Context context) {
        super(context);
    }

    protected GetNumberDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected GetNumberDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected int setLayout() {
        return R.layout.dialog_getnumber;
    }

    @Override
    protected void initView() {

    }
}
