package com.huaqin.wirelessfiletransfer.listen._01_standard;
import java.util.Set;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import com.huaqin.wirelessfiletransfer.model.WiFiP2pServicePeer;


public interface IfWiFiP2pManagerEventsListener {
/**
 * <p>
 * Description:manager���쳣����
 * <p>
 * @date:2015��4��4��
 * @param context
 * @param t
 */
    public void onException(Throwable t);
/**
 * <p>
 * Description:manager��⵽servicedeviceset�����仯
 * <p>
 * @date:2015��4��4��
 * @param servicedeviceset
 */
    public void onDisplayListUpdate(Set<WiFiP2pServicePeer> servicedeviceset);
    /**
     * <p>
     * Description:wifip2p���ӳɹ�
     * <p>
     * @date:2015��4��4��
     * @param info
     */
    public void onWifip2pConnected(WifiP2pInfo info,WifiP2pDevice thisDevice);
    
    /**
     * <p>
     * Description:discovery stop
     * <p>
     * @date:2015��4��4��
     */
    public void onWifiP2pDiscoveryStop();
    
    /**
     * <p>
     * Description:wifip2p disconnected
     * <p>
     * @date:2015��4��4��
     */
    public void onWifiP2pDisconnected();
    

}
