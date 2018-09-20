package com.ck.netcloud;

import java.util.List;

public class ClasSysPara {
    public String strDevNO;
    
    
    public String strDevRegistSN;   //设备组成序列号，  190年月日时分秒毫秒 
    
    public String strUserName;
    
    
    public int iEnableWifiSampleDev;  //使能wifi 连接本地 采集器 设备
        
    public String strNeedUploadDataSize;     //需要上传的总数据大小。
    public String strNeedUploadInfo;             //需要上传的统计信息。
    public List<String> listStrNeedUploadProjectName;  //需要上传的工程名称    
    public String strNetCloudWifiName;		//网络上传服务器WIFI 名称
    public String strNetCloudWifiPswd;		//网络上传WIFI 密码
    
}
