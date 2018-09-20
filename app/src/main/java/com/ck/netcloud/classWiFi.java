 
package com.ck.netcloud;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class classWiFi {
	Context mContext;
    
    public int m_iWifiStatus;
    public WifiManager m_WifiManager;
    WifiConfiguration m_CurWifiConf;
    
    public classWiFi(Context context) {
    	mContext = context;    	    	
		m_WifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}
	
	//--------------------------------------------------------------------------------------------------
	public boolean OpenWifi()  //打开wifi , 2秒钟未打开，则退出，错误信号
	{   
		if(!m_WifiManager.isWifiEnabled()) {
			m_WifiManager.setWifiEnabled(true); 
			
			for(int i=0;i<40;i++){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(m_WifiManager.isWifiEnabled()){
					return true;
				}
			}
			return false;			
        }	    
	    return true; 	    
	}
   
	public void CloseWifi()
	{   
		if(m_WifiManager.isWifiEnabled()) {
			m_WifiManager.setWifiEnabled(false);
		}
	}
	
	//获取附近可用 WIFI 名称，是异步操作， wifi打开后，进行扫描，扫描完成后，会发生一个Intent：
	//WifiManager.SCAN_RESULTS_AVAILABLE_ACTION, 获取此Intent 后，再进行 GetWifiValidNameList
	//为了快速得到 扫描结果， 监听WifiManager.WIFI_STATE_ENABLED消息，然后进行 StartScan。
	public void StartScan()
	{
        if(!m_WifiManager.isWifiEnabled()) {
        	m_WifiManager.setWifiEnabled(true); 
        	try {
                Thread.sleep(200);   //等待 一会300
            } catch (InterruptedException e) {
                e.printStackTrace();
            } 
        }
        else{
        	m_WifiManager.startScan();
        }		
	}
	
	//获取 wifi信号有效的列表名称 
	public List<String> GetWifiValidNameList(){
    	String strbuf;
    	List<String> strListValidWifiName = new ArrayList<String>();
    	strListValidWifiName.clear();
        
        List<ScanResult> mScanResults = m_WifiManager.getScanResults();
        if(mScanResults.size() > 0){
        	for (ScanResult scanResult : mScanResults) {
            	  strbuf = scanResult.SSID;
            	  if(!strbuf.isEmpty()){  //获取不 空 的名字
            		  strListValidWifiName.add(strbuf);   
            	  }
            }
        }    	
    	return strListValidWifiName;
    }
	
	//获取已经连接过的 wifi名称 (包括没有信号的wifi名)
    public List<String> GetWifiConnetedNameList(){
    	OpenWifi();
    	int idlyCnt = 0;
        while ((idlyCnt < 100) &(m_WifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING)) {
            try {       
                Thread.sleep(100);// 为了避免程序一直while循环，让它睡个100毫秒检测……
            } 
            catch (InterruptedException ie) {
            }
        }
    	
    	String strbuf;
    	List<String> strListConnetedWifiName = new ArrayList<String>();
    	strListConnetedWifiName.clear();
           	
    	List<WifiConfiguration> existConfigs = m_WifiManager.getConfiguredNetworks(); //得到配置好的网络连接
    	if (existConfigs != null && existConfigs.size() != 0) {
            for (WifiConfiguration existCfg : existConfigs) {
          	  strbuf = existCfg.SSID;
          	  int ilenth = strbuf.length();        	  
          	  String strName = strbuf.substring(1, ilenth-1);
          	strListConnetedWifiName.add(strName);       	  
            }
        }
    	return strListConnetedWifiName;
    }
    
	public boolean AkConnetWifi(String wifiName, String Password)
	{	
		if(false ==OpenWifi()){
			return false;
	    }	        
		// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
		// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
		int idlyCnt = 0;
		while ((idlyCnt < 100) &(m_WifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING)) {
		    try {		
				Thread.sleep(100);// 为了避免程序一直while循环，让它睡个100毫秒检测……
			} 
			catch (InterruptedException ie) {
			}
		}
		
		m_CurWifiConf = new WifiConfiguration();
		
		m_CurWifiConf.allowedAuthAlgorithms.clear();  
		m_CurWifiConf.allowedGroupCiphers.clear();  
		m_CurWifiConf.allowedKeyManagement.clear();  
		m_CurWifiConf.allowedPairwiseCiphers.clear();  
		m_CurWifiConf.allowedProtocols.clear();  
		m_CurWifiConf.SSID = "\"" + wifiName + "\"";   //网络名称 二头需要加 ""   
	       
	    if(Password.isEmpty()){  //没有密码
	    	m_CurWifiConf.wepKeys[0] = "";  
	    	m_CurWifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
	    	m_CurWifiConf.wepTxKeyIndex = 0;  
	    	Log.i("main", "没有密码");
	    }
	    else{  // WPA 密码        	
	    	
	    	m_CurWifiConf.preSharedKey = "\"" + Password + "\"";
	    	m_CurWifiConf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	    	m_CurWifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	    	m_CurWifiConf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	    	m_CurWifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	    	// 打开下面语句，则不能自动重联
	    	//m_CurWifiConf.allowedProtocols.set(WifiConfiguration.Protocol.WPA); 
	    	m_CurWifiConf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	    	m_CurWifiConf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	    	m_CurWifiConf.status = WifiConfiguration.Status.ENABLED;
	    }   	
	    
	    int curID = m_WifiManager.addNetwork(m_CurWifiConf); 	 
	    
	    String strbuf,strname;
	    int ilenth;    	
	    List<WifiConfiguration> listwifiCfgs = m_WifiManager.getConfiguredNetworks();
	    for(WifiConfiguration wificfg : listwifiCfgs ) {
	    	strbuf = wificfg.SSID;
	    	ilenth = strbuf.length();        	  
	    	strname = strbuf.substring(1, ilenth-1);      	    
	      	if(strname.equalsIgnoreCase(wifiName)){
	      		m_WifiManager.disconnect();
	      		m_WifiManager.enableNetwork(wificfg.networkId, true);
	      		m_WifiManager.reconnect();   
	   	        return true;
	        }
	    }
	    
	    if(curID >= 0){
	      //若旧列表没有 wifi 名字， 则直连
	        m_WifiManager.disconnect();
	        m_WifiManager.enableNetwork(curID, true);
	        m_WifiManager.reconnect();  
	        return true;   
	    }    
	    return false;        
	}	

	// 断开 当前网络
	public void disCurConnectWifi() {	
		WifiInfo wifiinfo = m_WifiManager.getConnectionInfo();
		int netId = wifiinfo.getNetworkId();
		if(netId > 0){
			m_WifiManager.disableNetwork(netId);
			m_WifiManager.disconnect();	
		}		
	}
	public void ConnectWifibyID(int netID) 
	{	
		OpenWifi();	
		int idlyCnt = 0;
        while ((idlyCnt < 100) &(m_WifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING)) {
            try {       
                Thread.sleep(100);// 为了避免程序一直while循环，让它睡个100毫秒检测……
            } 
            catch (InterruptedException ie) {
            }
        }
		
		if(netID > 0){
			m_WifiManager.disconnect();			
      		m_WifiManager.enableNetwork(netID, true);
      		m_WifiManager.reconnect();   
		}		
	}
	    
	public int IsWifiConnectNameOK(String WifiName)
	{
		WifiInfo wifiinfo = m_WifiManager.getConnectionInfo();
		
		if(wifiinfo.getNetworkId() < 0){ //当前连接 无效
			return -1;
		}
		
		String strbuf = wifiinfo.getSSID();
    	int ilenth = strbuf.length();        	  
    	String strname = strbuf.substring(1, ilenth-1);
    	
    	if(strname.isEmpty()){
    		return -1;
    	}
    	
      	if(strname.equalsIgnoreCase(WifiName)){
      		return 1;
        }
      	else{
      		return 0;
      	}		
	}
	
	public int GetCurWifiNetId() //获取当前 wifi 的ID, 小于零，无效wifi
	{
		WifiInfo wifiinfo = m_WifiManager.getConnectionInfo();
		int netid = wifiinfo.getNetworkId();
		return netid; 
	}
	
}


