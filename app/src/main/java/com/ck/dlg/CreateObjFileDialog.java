package com.ck.dlg;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hc.u8x_ck.R;

/**
 * @author fei
 * @date on 2018/12/13 0013
 * @describe TODO :
 **/
public class CreateObjFileDialog extends Dialog{

    public CreateObjFileDialog(@NonNull Context context) {
        super(context);
    }

    public CreateObjFileDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CreateObjFileDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_createobjfile);
    }
}
