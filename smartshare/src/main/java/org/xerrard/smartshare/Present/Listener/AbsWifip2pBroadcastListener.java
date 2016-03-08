package org.xerrard.smartshare.Present.Listener;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;

import org.xerrard.smartshare.Present.State.IBaseState;
import org.xerrard.smartshare.Present.Wifip2pSolutionProvider;


/**
 * 用于wifip2p Broadcast receiver
 */
public abstract class AbsWifip2pBroadcastListener implements IfWifip2pListener {


    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PCONNECTIONCHANGED = "onWifiP2pConnectionChanged";

    /**Broadcast intent action indicating that the state of Wi-Fi p2p
     * connectivity has changed
     * @param owner
     * @param context
     * @param intent
     * @param networkInfo
     */
    public abstract void onWifiP2pConnectionChanged(
              Wifip2pSolutionProvider owner
            , Context context
            , Intent intent
            , NetworkInfo networkInfo) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PTHISDEVICECHANGED = "onWifiP2pThisDeviceChanged";

    /**Broadcast intent action indicating that this device details have
     * changed.
     * @param owner
     * @param context
     * @param intent
     * @param device
     */
    public abstract void onWifiP2pThisDeviceChanged(
            Wifip2pSolutionProvider owner
            , Context context
            , Intent intent
            , WifiP2pDevice device) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PPEERSCHANGED = "onWifiP2pPeersChanged";

    /**Broadcast intent action indicating that the available peer list has
     * changed. This can be sent as a result of peers being found, lost or
     * updated.
     * @param owner
     * @param context
     * @param intent
     */
    public abstract void onWifiP2pPeersChanged(
            Wifip2pSolutionProvider owner
            , Context context
            , Intent intent) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PDISCOVERYCHANGED = "onWifiP2pDiscoveryChanged";

    /**Broadcast intent action indicating that peer discovery has either
     * started or stopped. One extra EXTRA_DISCOVERY_STATE indicates whether
     * discovery has started or stopped.
     *
     * Note that discovery will be stopped during a connection setup. If the
     * application tries to re-initiate discovery during this time, it can
     * fail.
     *
     * service的discovery start/stop，不会收到此 广播。如果修改wifi名称，会discovery
     * stop，然后会收到此广播
     * @param owner
     * @param context
     * @param intent
     * @param state
     */
    public abstract void onWifiP2pDiscoveryChanged(
            Wifip2pSolutionProvider owner
            , Context context
            , Intent intent
            , IBaseState state) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PSTATECHANGED = "onWifiP2pStateChanged";

    /**Broadcast intent action to indicate whether Wi-Fi p2p is enabled or
     * disabled
     * @param owner
     * @param context
     * @param intent
     * @param state
     */
    public abstract void onWifiP2pStateChanged(
            Wifip2pSolutionProvider owner
            , Context context
            , Intent intent
            , IBaseState state) ;
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PBROADCASTRECEIVE = "onWifiP2pBroadcastReceive";
    public abstract void onWifiP2pBroadcastReceive(
            Wifip2pSolutionProvider owner
            , Context context
            , Intent intent
            , String action);
    
    public static final String WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFISTATECHANGED = "onWifiStateChanged";

    /**Broadcast intent action indicating that Wi-Fi has been enabled,
     * disabled, enabling, disabling, or unknown. One extra provides this
     * state as an int. Another extra provides the previous state, if
     * available.
     * @param owner
     * @param context
     * @param intent
     * @param state
     */
    public abstract void onWifiStateChanged(
            Wifip2pSolutionProvider owner
            , Context context
            , Intent intent
            , IBaseState state) ;
    
}
