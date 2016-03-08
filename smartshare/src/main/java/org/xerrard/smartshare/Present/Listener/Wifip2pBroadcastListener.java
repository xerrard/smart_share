package org.xerrard.smartshare.Present.Listener;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;

import org.xerrard.smartshare.Present.State.IBaseState;
import org.xerrard.smartshare.Present.Wifip2pSolutionProvider;

/**
 * 类描述：
 * 创建人：xuqiang
 * 创建时间：16-3-8 下午4:19
 * 修改人：xuqiang
 * 修改时间：16-3-8 下午4:19
 * 修改备注：
 */
public class Wifip2pBroadcastListener extends AbsWifip2pBroadcastListener {

    @Override
    public void onWifiP2pConnectionChanged(Wifip2pSolutionProvider owner, Context context, Intent intent, NetworkInfo networkInfo) {

    }

    @Override
    public void onWifiP2pThisDeviceChanged(Wifip2pSolutionProvider owner, Context context, Intent intent, WifiP2pDevice device) {

    }

    @Override
    public void onWifiP2pPeersChanged(Wifip2pSolutionProvider owner, Context context, Intent intent) {

    }

    @Override
    public void onWifiP2pDiscoveryChanged(Wifip2pSolutionProvider owner, Context context, Intent intent, IBaseState state) {

    }

    @Override
    public void onWifiP2pStateChanged(Wifip2pSolutionProvider owner, Context context, Intent intent, IBaseState state) {

    }

    @Override
    public void onWifiP2pBroadcastReceive(Wifip2pSolutionProvider owner, Context context, Intent intent, String action) {

    }

    @Override
    public void onWifiStateChanged(Wifip2pSolutionProvider owner, Context context, Intent intent, IBaseState state) {

    }

    @Override
    public void onException(Throwable t) {

    }
}