package com.ck.netcloud;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.hc.u8x_ck.R;


public class dlg_wifi_name_pswd extends Dialog {
    private Context mContext;
          
    public dlg_wifi_name_pswd(Context context, int theme) {
        super(context, theme);        
        mContext=context;
    }
    public dlg_wifi_name_pswd(Context context) {
        super(context);
        mContext=context;
    }
    
    protected void onCreate(Bundle savedInstanceState) {
      //隐去标题栏（应用程序的名字）  
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐去状态栏部分(电池等图标和一切修饰部分)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.ui_net_dlg_wifi_name_pswd);
        super.onCreate(savedInstanceState);    
            
    };        
   

    

}
