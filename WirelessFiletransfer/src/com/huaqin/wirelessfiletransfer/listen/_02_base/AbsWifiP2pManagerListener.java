package com.huaqin.wirelessfiletransfer.listen._02_base;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWifiP2pListener;

import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.UpnpServiceResponseListener;

public abstract class AbsWifiP2pManagerListener implements IfWifiP2pListener,
        ConnectionInfoListener, PeerListListener, ChannelListener,
        DnsSdServiceResponseListener, DnsSdTxtRecordListener,
        GroupInfoListener, ServiceResponseListener, UpnpServiceResponseListener {

    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONUPNPSERVICEAVAILABLE = "onUpnpServiceAvailable";
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONSERVICEAVAILABLE = "onServiceAvailable";
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONGROUPINFOAVAILABLE = "onGroupInfoAvailable";
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONDNSSDTXTRECORDAVAILABLE = "onDnsSdTxtRecordAvailable";
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONDNSSDSERVICEAVAILABLE = "onDnsSdServiceAvailable";
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONCHANNELDISCONNECTED = "onChannelDisconnected";
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONPEERSAVAILABLE = "onPeersAvailable";
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONCONNECTIONINFOAVAILABLE = "onConnectionInfoAvailable";
    // public static final String
    // WIFI_P2P_ACTION_LISTENER_METHOD_ONCONNECTIONLOST = "onConnectionLost";

    // public abstract void onConnectionLost();

}
