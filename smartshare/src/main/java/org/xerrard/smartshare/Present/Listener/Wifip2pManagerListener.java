package org.xerrard.smartshare.Present.Listener;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;

import java.util.List;
import java.util.Map;

/**
 * 类描述：
 * 创建人：xuqiang
 * 创建时间：16-3-8 下午4:28
 * 修改人：xuqiang
 * 修改时间：16-3-8 下午4:28
 * 修改备注：
 */
public class Wifip2pManagerListener extends AbsWifip2pManagerListener {
    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

    }

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {

    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {

    }

    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {

    }

    @Override
    public void onException(Throwable t) {

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {

    }

    @Override
    public void onServiceAvailable(int protocolType, byte[] responseData, WifiP2pDevice srcDevice) {

    }

    @Override
    public void onUpnpServiceAvailable(List<String> uniqueServiceNames, WifiP2pDevice srcDevice) {

    }
}
