package com.huaqin.wirelessfiletransfer.listen._02_base;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWifiP2pListener;
import com.huaqin.wirelessfiletransfer.valuableLabel._01_standard.BaseValuableLabel;
import com.huaqin.wirelessfiletransfer.wifip2p.WifiP2pSolutionProvider;


public abstract class AbsWifiP2pBroadcastListener implements IfWifiP2pListener {


    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PCONNECTIONCHANGED = "onWifiP2pConnectionChanged";
    public abstract void onWifiP2pConnectionChanged(
              WifiP2pSolutionProvider owner 
            , Context context
            , Intent intent
            , NetworkInfo networkInfo) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PTHISDEVICECHANGED = "onWifiP2pThisDeviceChanged";
    public abstract void onWifiP2pThisDeviceChanged(
              WifiP2pSolutionProvider owner 
            , Context context
            , Intent intent
            , WifiP2pDevice device) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PPEERSCHANGED = "onWifiP2pPeersChanged";
    public abstract void onWifiP2pPeersChanged(
              WifiP2pSolutionProvider owner 
            , Context context
            , Intent intent) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PDISCOVERYCHANGED = "onWifiP2pDiscoveryChanged";
    public abstract void onWifiP2pDiscoveryChanged(
              WifiP2pSolutionProvider owner 
            , Context context
            , Intent intent
            , BaseValuableLabel state) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PSTATECHANGED = "onWifiP2pStateChanged";
    public abstract void onWifiP2pStateChanged(
              WifiP2pSolutionProvider owner 
            , Context context
            , Intent intent
            , BaseValuableLabel state) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PBROADCASTRECEIVE = "onWifiP2pBroadcastReceive";
    public abstract void onWifiP2pBroadcastReceive(
              WifiP2pSolutionProvider owner 
            , Context context
            , Intent intent
            , String action);
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFISTATECHANGED = "onWifiStateChanged";
    public abstract void onWifiStateChanged(
              WifiP2pSolutionProvider owner 
            , Context context
            , Intent intent
            , BaseValuableLabel state) ;
    
}
