package com.ck.netcloud;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ck.App_DataPara;
import com.ck.utils.PathUtils;
import com.hc.u8x_ck.R;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;


public  class ui_net_upload_file extends Activity {
    App_DataPara AppDatPara;
    
    public static final int NO_GPRS = 0;
    public static final int NO_WIFI = 111;

    private boolean isGPRSConnect = false;
    private boolean isWifiConnect = false;    

    private ProgressBar mProgressBar;
    private TextView mTotalTextView, mPercent;
    
    public classWiFi mclsWifi;
    private IntentFilter netFilter;
    NetReceiver netReceiver;
       
    public static final int HANDLE_SUCESS = 0;
    public static final int HANDLE_LOADING = 1;
    public static final int HANDEL_ERROR = 2;
    
    public int iUploadAllFileCnt = 0;
    public int iUploadCurFileNo = 0;
    
    public String mstrUpdateApkName = "";
    public String mstrUpdateInfoVersion = "1.00";
    public boolean bFindNewVersionFlag = false;
    
    //public boolean bCurDownLoadApkFile = false;  //当前 下载 的是 APK 文件？
    public int  iUploadFileStatus = 0;    
    
    public class NetReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
        	//ui_net_wifi_sel 界面，wifi 选择有效
        	if (intent.getAction().equals("intent.net.wifi.selected")) {	
        		IntentWifiUploadFile();
        	}

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    State state = networkInfo.getState();
                    
                    if (state == State.CONNECTED) {
                        //不是采集器 wifi 名称
                        if( 0  ==  mclsWifi.IsWifiConnectNameOK(AppDatPara.sysPara.strDevNO)){
                            isWifiConnect  = true;  
                            isGPRSConnect  = false;  
                            
                            ((TextView) findViewById(R.id.upload_data_size)).setText("WIFI有效，开始上传...");
                        }
                                            
                    }
                }
            }
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if(networkInfo != null) {
                    State GPRSState = networkInfo.getState();
                    if (GPRSState == State.CONNECTED) {
                        isGPRSConnect = true; 
                        isWifiConnect = false; 
                        ((TextView) findViewById(R.id.upload_data_size)).setText("GPRS有效...");
                    }
                }                
            }
        }
    }
    
    Handler handler = new Handler() {
    public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_SUCESS:    
                iUploadFileStatus = 1;  //接收OK         
                iUploadCurFileNo += 1;
                
                mProgressBar.setProgress(iUploadCurFileNo);                 
                if(iUploadAllFileCnt < 1){
                    iUploadAllFileCnt = 100;
                }
                int iper = 100 * iUploadCurFileNo / iUploadAllFileCnt;
                mPercent.setText(iper + " %");     
                
                if(iUploadCurFileNo == iUploadAllFileCnt){ 
                    ((TextView) findViewById(R.id.upload_data_size)).setText("上传完成! " + AppDatPara.sysPara.strNeedUploadDataSize);
                    
                    ((Button) findViewById(R.id.btn_wifi_upload)).setTextColor(Color.WHITE);
                    ((Button) findViewById(R.id.btn_gprs_upload)).setTextColor(Color.WHITE);
                    ((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.WHITE);
                    ((Button) findViewById(R.id.btn_wifi_upload)).setClickable(true);
                    ((Button) findViewById(R.id.btn_gprs_upload)).setClickable(true);
                    ((Button) findViewById(R.id.btn_cancel)).setClickable(true);
                    
                    //initProgressBar(100);
                }                                         
                break;
                
            case HANDLE_LOADING:
                long total = msg.arg1;
                long current = msg.arg2;
                if (total <= 0 || current <= 0) {
                    return;
                }
                float progress = (float) current / total;
                int num = (int) (progress * 100);
                if (num < mProgressBar.getProgress()){
                    return;
                }                    
                if (num > 100){
                    num = 100;
                }

                float all = (float) (total / 1024) / 1024;
                float cur = (float) (current / 1024) / 1024;
                mPercent.setText(num + " %");
                mTotalTextView.setText((Math.round(cur * 100) / 100.0) + " / " + (Math.round(all * 100) / 100.0) + "M");
                mProgressBar.setProgress(num);
                break;
            case HANDEL_ERROR:                
                ((Button) findViewById(R.id.btn_wifi_upload)).setTextColor(Color.WHITE);
                ((Button) findViewById(R.id.btn_gprs_upload)).setTextColor(Color.WHITE);
                ((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.WHITE);
                ((Button) findViewById(R.id.btn_wifi_upload)).setClickable(true);
                ((Button) findViewById(R.id.btn_gprs_upload)).setClickable(true);
                ((Button) findViewById(R.id.btn_cancel)).setClickable(true);
                ((TextView) findViewById(R.id.upload_data_size)).setText("上传失败！");
                
                iUploadFileStatus = -1;
                mProgressBar.setProgress(0);
                mPercent.setText("0 %");     
                break;
            }
        };
    };
    
	
	protected void onCreate(Bundle savedInstanceState) {
	    
	    AppDatPara = (App_DataPara) getApplicationContext();
	    setFinishOnTouchOutside(false);
        super.onCreate(savedInstanceState);               
        //隐去标题栏（应用程序的名字）  
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐去状态栏部分(电池等图标和一切修饰部分)
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.ui_net_upload_file);		
        
        mProgressBar = (ProgressBar) findViewById(R.id.upload_progressBar);
        mProgressBar.setIndeterminate(false);
        mPercent = (TextView) findViewById(R.id.upload_textPercent);
        
        ((TextView) findViewById(R.id.upload_data_size)).setText(AppDatPara.sysPara.strNeedUploadDataSize);
        ((TextView) findViewById(R.id.upload_pro_pile_cnt)).setText(AppDatPara.sysPara.strNeedUploadInfo);
                        
        AppDatPara.sysPara.iEnableWifiSampleDev = 0;
        
        netReceiver = new NetReceiver();       
        
        netFilter = new IntentFilter();
        netFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        netFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        netFilter.addAction("intent.net.wifi.selected");   
        
        registerReceiver(netReceiver, netFilter);
        
        mclsWifi = new classWiFi(ui_net_upload_file.this);
        
        initProgressBar(100); 
	};
	
	
	public String getVersion() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "-.--";
    }
		
	private void initProgressBar(int iMax) {   
	    mPercent.setText("0 %");
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(iMax);
        mProgressBar.setProgress(0);        
        
    }
	
	public void WifiUpload(View View) {
	    isWifiConnect  = false;  
        isGPRSConnect  = false;  
        
		//AppDatPara.sysPara.iEnableWifiSampleDev = 0;
		//创建 net_wifi_sel界面，通过 sendintent 来传递操作wifi的信息
		Intent intent = new Intent(ui_net_upload_file.this,ui_net_wifi_sel.class);
        startActivity(intent);   
	}    

	public void IntentWifiUploadFile()
	{
		((Button) findViewById(R.id.btn_wifi_upload)).setTextColor(Color.GRAY);
        ((Button) findViewById(R.id.btn_gprs_upload)).setTextColor(Color.GRAY);
        //((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.GRAY);
        ((Button) findViewById(R.id.btn_wifi_upload)).setClickable(false);
        ((Button) findViewById(R.id.btn_gprs_upload)).setClickable(false);
        //((Button) findViewById(R.id.btn_cancel)).setClickable(false);
	    
	    
        ((TextView) findViewById(R.id.upload_data_size)).setTextColor(Color.rgb(0xF6,0xB8,0x00)); //海创黄色
        ((TextView) findViewById(R.id.upload_data_size)).setText("网络连接中...");
	    
	    isGPRSConnect = false; 
        isWifiConnect = false;
       
        UpLoadFileToServiceDB();
	}
	
	public void GprsUpload(View View) {
	    ((Button) findViewById(R.id.btn_wifi_upload)).setTextColor(Color.GRAY);
        ((Button) findViewById(R.id.btn_gprs_upload)).setTextColor(Color.GRAY);
        //((Button) findViewById(R.id.btn_cancel)).setTextColor(Color.GRAY);
        ((Button) findViewById(R.id.btn_wifi_upload)).setClickable(false);
        ((Button) findViewById(R.id.btn_gprs_upload)).setClickable(false);
        //((Button) findViewById(R.id.btn_cancel)).setClickable(false);
	    
	    
        ((TextView) findViewById(R.id.upload_data_size)).setTextColor(Color.rgb(0xF6,0xB8,0x00)); //海创黄色
        ((TextView) findViewById(R.id.upload_data_size)).setText("网络连接中...");
	    
	    isGPRSConnect = false; 
        isWifiConnect = false;
	    
	    final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        
        
        //判断 GPRS 是否打开
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if(networkInfo != null) {
            State GPRSState = networkInfo.getState();
            if (GPRSState == State.CONNECTED) {
                isGPRSConnect = true; 
                isWifiConnect = false;
                ((TextView) findViewById(R.id.upload_data_size)).setText("GPRS有效...");
            }
        }
        
        if(isGPRSConnect == false){
            //使能 GPRS 网络
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Method setMobileDataEnabl;
            try {
                setMobileDataEnabl = cm.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
                setMobileDataEnabl.invoke(cm, true);
            } catch (Exception e) {
                e.printStackTrace();
            }            
        }        
        UpLoadFileToServiceDB();        
   }
		
	
  //上传文件到服务器数据库	
   void UpLoadFileToServiceDB()
   {
       iUploadCurFileNo = 0;  
       iUploadAllFileCnt = 0;
       int iProjCnt = AppDatPara.sysPara.listStrNeedUploadProjectName.size();
       String strName;

       //获取文件夹下面的文件个数
       for(int k=0;k<iProjCnt;k++){  //获取所有文件夹下面的 所有文件，并获取全路径名
           strName =  AppDatPara.sysPara.listStrNeedUploadProjectName.get(k);
           File file = new File(PathUtils.FILE_PATH + File.separator + strName);
           if (file.exists()) { //文件夹存在
               File[] listFiles = file.listFiles();
               for (File f : listFiles) {
            	   strName = f.getName();   
                   iUploadAllFileCnt += 1;                               
               }               
           }
       }
       
       
       initProgressBar(iUploadAllFileCnt);
       
       
       final clasNetConfig netcfg = new clasNetConfig();
       new Thread() {
           public void run() {
                   for(int i=0;i<500;i++){ //等待 GPRS 打开时间是 50秒超时
                       if(isGPRSConnect == true){
                           break;
                       }
                       if(isWifiConnect == true){
                           break;
                       }                       
                       
                       try {
                           Thread.sleep(100);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }                          
                   }
                   
                   if(isGPRSConnect == false) {
                           if(isWifiConnect == false){
                                   handler.sendEmptyMessage(HANDEL_ERROR);
                                   return;
                           }
                   }                                   
                   
                   int iProjectCnt = AppDatPara.sysPara.listStrNeedUploadProjectName.size();
                   String strProjectName;
                   String strFileName;
                   String strFilePath;
                   
                   netcfg.strUserName = AppDatPara.sysPara.strUserName;   
                   netcfg.strDevID = AppDatPara.sysPara.strDevRegistSN;
                   netcfg.strDevVer = getVersion();
                   netcfg.strDevNO = AppDatPara.sysPara.strDevNO;
                   netcfg.strUserName = AppDatPara.sysPara.strUserName;     
                   
                   boolean bFlag = true;
                   for(int i=0;i<iProjectCnt;i++){  //获取所有文件夹下面的 所有文件，并获取全路径名
                       strProjectName =  AppDatPara.sysPara.listStrNeedUploadProjectName.get(i);
                       File file = new File(PathUtils.FILE_PATH + File.separator + strProjectName);
                       if (file.exists()) { //文件夹存在
                           strFilePath = PathUtils.FILE_PATH + File.separator + strProjectName;
                           File[] listFiles = file.listFiles();
                           for (File f : listFiles ) {
                               strFileName = f.getName();      
                               
                                //开始上传文件；；；；                                                                              
                               iUploadFileStatus = 0;
                               String str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                               netcfg.strUploadTime = str;   //上传时间   "2016-11-05 12:25:36";     
                               
                               netcfg.strClientFilePath = strFilePath + File.separator + strFileName;
                               netcfg.strProject = strProjectName;                               
                               netcfg.strPile =strFileName.substring(0, strFileName.indexOf("."));   
                               clasNetOp.UpLoadFile2Db(netcfg, new interfaceNetOpListener() {
                                       public void onSuccess() { 
                                           handler.sendEmptyMessage(HANDLE_SUCESS);
                                       }
                                       public void onLoading(long total, long current) {
                                           //Message Handle_ms =Message.obtain();
                                           //Handle_ms.what = HANDLE_LOADING;
                                           //Handle_ms.arg1 = (int) total;
                                           //Handle_ms.arg2 = (int) current;
                                           //handler.sendMessage(Handle_ms);
                                       }
                                       public void onFailure(String strErr) {
                                           handler.sendEmptyMessage(HANDEL_ERROR);
                                       }
                                });
                               
                               
                               for(int idly=0;idly<250;idly++){  //等待响应 超时25秒
                                   if(iUploadFileStatus == 1){                                       
                                       break;
                                   }   
                                   if(iUploadFileStatus < 0){   
                                       bFlag = false;
                                       i = iProjectCnt + 1;
                                       break;
                                   } 
                                   
                                   try {
                                       Thread.sleep(100);
                                   } catch (InterruptedException e) {
                                       e.printStackTrace();
                                   } 
                               }
                               
                               if(bFlag == false){  //跳出  for (File f : listFiles )  循环
                                   break;
                               }                             
                               
                           }
                       }                       
                   }                   
                                      
                   
           };
       }.start();   
   }
   	

	public void BtnReturn(View View) {
		if (netFilter != null) {
			unregisterReceiver(netReceiver);
		}
		
		AppDatPara.sysPara.iEnableWifiSampleDev = 1;
	    ui_net_upload_file.this.finish(); // 关闭 当前 activity
	}
	
	protected void onDestroy() {   //按对话框以外区域时， 销毁函数
		AppDatPara.sysPara.iEnableWifiSampleDev = 1;
	    //Log.i("ui_browse10_upload_file", "onDestroy");
		unregisterReceiver(netReceiver);
	    super.onDestroy();
	}
}
