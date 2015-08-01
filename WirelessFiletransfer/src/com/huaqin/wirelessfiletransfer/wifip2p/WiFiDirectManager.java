package com.huaqin.wirelessfiletransfer.wifip2p;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.IpAddrUtils;
import org.xerrard.util.ObjectLowCopy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pManagerEventsListener;
import com.huaqin.wirelessfiletransfer.listen._02_base.AbsWifiP2pActionListener;
import com.huaqin.wirelessfiletransfer.listen._02_base.AbsWifiP2pBroadcastListener;
import com.huaqin.wirelessfiletransfer.listen._02_base.AbsWifiP2pManagerListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;
import com.huaqin.wirelessfiletransfer.model.WiFiP2pServicePeer;
import com.huaqin.wirelessfiletransfer.valuableLabel._01_standard.BaseValuableLabel;
import com.huaqin.wirelessfiletransfer.valuableLabel._03_wf.WifiP2pDeviceState;
import com.huaqin.wirelessfiletransfer.valuableLabel._03_wf.WifiP2pDiscoveryState;

public class WiFiDirectManager {
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_NAME = "xerrard";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    protected String serviceName = SERVICE_NAME;
    protected String serviceType = SERVICE_REG_TYPE;
    protected String dnsDescription = TXTRECORD_PROP_AVAILABLE;

    protected WifiP2pSolutionProvider p2pSolution = null;
    public static WiFiDirectManager instance;
    public LogOutputinterface log;
    public Context ctx;
    protected WifiP2pManageListener mWifiP2pManageListener;
    protected WifiP2pBroadcastListener mWifiP2pBroadcastListener;
    protected IfWiFiP2pManagerEventsListener mEventListener;
    public Set<WiFiP2pServicePeer> mServicePeerSet = null;
    protected static final String[] P2PDEVICE_LOWCOPY_ATTRIBS = new String[] {
            "deviceAddress", "deviceName", "status" };
    
    protected Timer timer;
    private int reStartServiceCount = 0;
    
    private WifiP2pDevice thisDevice;

    
    
    
    public WifiP2pDevice getThisDevice() {
        return this.thisDevice;
    }

    private WifiP2pDevice targetDevice;

    public WifiP2pDevice getTargetDevice() {
        return this.targetDevice;
    }

    private WiFiDirectManager(Context ctx) {
        this.ctx = ctx;

    }

    public void initWiFiP2pManager() {
        if (p2pSolution == null) {
            p2pSolution = new WifiP2pSolutionProvider(ctx, log);
            mWifiP2pBroadcastListener = new WifiP2pBroadcastListener();
            mWifiP2pManageListener = new WifiP2pManageListener();
            p2pSolution.setManageListener(mWifiP2pManageListener);
            p2pSolution.setBoradcastListener(mWifiP2pBroadcastListener);

        }
    }


    public void setEventListener(IfWiFiP2pManagerEventsListener ctx) {
        mEventListener = ctx;
    }

    public void addtoServiceList(WiFiP2pServicePeer srcService) {
        // TODO Auto-generated method stub
        // 有效的 member 被搜索到
        if (mServicePeerSet == null) {
            mServicePeerSet = new HashSet<WiFiP2pServicePeer>();
        }
        synchronized (mServicePeerSet) {
            mServicePeerSet.add(srcService);
        }

    }

    public static WiFiDirectManager getSingletonInstance(Context ctx) {
        if (instance == null) {
            instance = new WiFiDirectManager(ctx);
        }
        return instance;
    }

    /**
     * <p>
     * Description:把Device打出Log
     * <p>
     * 
     * @date:2015年3月12日
     * @param d
     * @return
     */
    private String dumpDevice(WifiP2pDevice d) {
        String ret = "Null";
        if (d != null) {
            ret = String
                    .format("Device Details: address:%s\r\nname:%s\r\nstatus:%s\r\ngroupOwner:%s\r\ndsp:%s\r\nkey:%s\r\npbc:%s",
                            d.deviceAddress, d.deviceName,
                            WifiP2pDeviceState.parse(d.status),
                            d.isGroupOwner(), d.wpsDisplaySupported(),
                            d.wpsKeypadSupported(), d.wpsPbcSupported());
        }
        return ret;
    }

    /**
     * <p>
     * Description:把deviceList打出log来
     * <p>
     * 
     * @date:2015年3月12日
     * @param deviceList
     */
    private void dumpPeersList(Collection<WifiP2pDevice> deviceList) {
        // TODO Auto-generated method stub
        log.debug(ExceptionUtil.currentMethodName());
        if (deviceList != null) {
            for (WifiP2pDevice d : deviceList) {
                log.debug(dumpDevice(d));
            }
        }
    }

    private void refreshServicePeerList(WifiP2pDeviceList peers) {
        log.debug(ExceptionUtil.currentMethodName());
        if (mServicePeerSet != null) {

            synchronized (mServicePeerSet) {
                Set<WiFiP2pServicePeer> lostSet = new HashSet<WiFiP2pServicePeer>();
                for (WiFiP2pServicePeer servicepeer : mServicePeerSet) {
                    WifiP2pDevice oldD = servicepeer.device;
                    WifiP2pDevice newD = peers.get(oldD.deviceAddress);

                    // peers list 找不到则删除
                    // TODO： ????
                    if (newD == null) {
                        lostSet.add(servicepeer);
                    }
                    else {
                        updateDevice(oldD, newD);
                    }
                }
                mServicePeerSet.removeAll(lostSet);
            }

            log.debug("refreshServicePeerList mServicePeerSet.size is %d ",
                    mServicePeerSet.size());
        }
    }

    /**
     * old 在前 new 在后
     * 
     * @param member
     * @param newDevice
     */
    public void updateDevice(WifiP2pDevice oldDevice, WifiP2pDevice newDevice) {
        log.debug("update member. %s\r\n=>%s\r\n", dumpDevice(oldDevice),
                dumpDevice(newDevice));

        // from 在前 to 在后
        ObjectLowCopy.copy(newDevice, oldDevice, P2PDEVICE_LOWCOPY_ATTRIBS);
    }

    public void disCoverServicePeers() {

        if (thisDevice != null) {
            if (thisDevice.status != WifiP2pDevice.AVAILABLE) {
                return;
            }
        }
        
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        p2pSolution.startSearchService(serviceName, serviceType,
                dnsDescription, null);

        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (reStartServiceCount > Const.SCAN_FREQUENCY_MAX_COUNT) {
                    reStartServiceCount = 0;
                    timer.cancel();
                    ((Service)ctx).stopSelf(); //关闭Service
                }
                else {
                    p2pSolution.startSearchService(serviceName, serviceType,
                            dnsDescription, null);
                    reStartServiceCount++;
                }
            }
        }, Const.SCAN_FREQUENCY, Const.SCAN_FREQUENCY); // 延时60000ms后执行，60000ms执行一次
        
        ctx.sendBroadcast(new Intent(Const.ACTION_UPDATE_START_SCAN_STATUS)); // 通知系统当前已经在搜索了
    }

    public void connectdevice(WifiP2pDevice device, AbsWifiP2pActionListener l) {

        this.targetDevice = device;
        try {
            p2pSolution.connectP2p(device, l);
        }
        catch (Throwable t) {
            mEventListener
                    .onException(new RuntimeException("connectdevice", t));
        }

        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                if (targetDevice.status == WifiP2pDevice.INVITED) {
                    disInviteCurrentDevice(null);
                }
            }
        }, Const.INVITE_LIMIT_TIME);// 15000ms如果还是invite状态，则取消连接

    }

    public void disInviteCurrentDevice(AbsWifiP2pActionListener l) {
        ctx.sendBroadcast(new Intent(Const.ACTION_DEVICE_DISCONNECTED));
        try {
            p2pSolution.disinviteP2p(l);
        }
        catch (Throwable t) {
            mEventListener.onException(new RuntimeException(
                    "disInviteCurrentDevice", t));
        }
    }

    public void disConnectCurrentDevice(AbsWifiP2pActionListener l) {
        try {
            p2pSolution.disconnectP2p(l);
        }
        catch (Throwable t) {
            mEventListener.onException(new RuntimeException("disConnectdevice",
                    t));
        }

    }

    class WifiP2pManageListener extends AbsWifiP2pManagerListener {

        /***
         * 本机连接状态更新
         */
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            if (info.groupOwnerAddress != null) {
                log.debug(
                        "%s-\r\nGroupOwner:%s\r\nGroupFormed:%s\r\nGroupOwnerAddress:%s\r\nGroupHostAddress:%s\r\n",
                        ExceptionUtil.currentMethodName(), info.isGroupOwner,
                        info.groupFormed, IpAddrUtils
                                .getDottedDecimalIP(info.groupOwnerAddress
                                        .getAddress()), info.groupOwnerAddress
                                .getHostAddress());
            }

            if (timer != null) {
                timer.cancel(); // 一旦请求连接，就取消timer，搜索不能再进行
            }
            mEventListener.onWifip2pConnected(info, thisDevice);
            
            
        }

        /**
         * The requested peer list is available the callback of
         * p2psolution.requestPeersList.每次更新PeerList，会走这里
         */
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            // TODO Auto-generated method stub
            log.debug("%s-\r\nPeers Count = %d", ExceptionUtil
                    .currentMethodName(), peers == null ? 0 : peers
                    .getDeviceList().size());

            // dumpPeersList(peers.getDeviceList());

            refreshServicePeerList(peers);
            if (mServicePeerSet != null) {
                mEventListener.onDisplayListUpdate(mServicePeerSet);
            }
        }

        @Override
        public void onChannelDisconnected() {
            log.debug(ExceptionUtil.currentMethodName());

        }

        /**
         * The requested Bonjour service response is available.
         * 
         * This function is invoked when the device with the specified Bonjour
         * registration type returned the instance name. 每一次搜索到servicedevice时
         */
        @Override
        public void onDnsSdServiceAvailable(String instanceName,
                String registrationType, WifiP2pDevice srcDevice) {
            log.debug("%s-\r\nDeviceName = %s-\r\n InstanceName = %s ",
                    ExceptionUtil.currentMethodName(), srcDevice.deviceName,
                    instanceName);
            // A service has been discovered. Is this our app?

            if (instanceName.equalsIgnoreCase(serviceName)) {

                // update the UI and add the item the discovered
                // device.

                WiFiP2pServicePeer service = new WiFiP2pServicePeer();
                service.device = srcDevice;
                service.instanceName = instanceName;
                service.serviceRegistrationType = registrationType;
                addtoServiceList(service);
                if (mServicePeerSet != null) {
                    mEventListener.onDisplayListUpdate(mServicePeerSet);
                }
            }

        }

        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomainName,
                Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceAvailable(int protocolType, byte[] responseData,
                WifiP2pDevice srcDevice) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onUpnpServiceAvailable(List<String> uniqueServiceNames,
                WifiP2pDevice srcDevice) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onException(Throwable t) {
            // TODO Auto-generated method stub

        }
    }

    class WifiP2pBroadcastListener extends AbsWifiP2pBroadcastListener {

        @Override
        public void onException(Throwable t) {
            log.err(ExceptionUtil.currentMethodName() + "-\r\n"
                    + ExceptionUtil.getExcetpionTrace(t));

        }

        /**
         * Broadcast intent action indicating that the state of Wi-Fi p2p
         * connectivity has changed
         */
        @Override
        public void onWifiP2pConnectionChanged(WifiP2pSolutionProvider owner,
                Context context, Intent intent, NetworkInfo networkInfo) {
            if (networkInfo != null) {
                log.debug("%s-\r\nAvailable=%s Connected=%s \r\ndetails:%s",
                        ExceptionUtil.currentMethodName(),
                        networkInfo.isAvailable(), networkInfo.isConnected(),
                        networkInfo.toString());

            }

            if (networkInfo.isConnected()) {
                p2pSolution.requestConnectionInfo();
            }
            else {
                mEventListener.onWifiP2pDisconnected();
            }

        }

        /**
         * Broadcast intent action indicating that this device details have
         * changed.
         */
        @Override
        public void onWifiP2pThisDeviceChanged(WifiP2pSolutionProvider owner,
                Context context, Intent intent, WifiP2pDevice device) {
            log.debug("%s-\r\n%s ", ExceptionUtil.currentMethodName(),
                    dumpDevice(device));
            thisDevice = device;
        }

        /**
         * Broadcast intent action indicating that the available peer list has
         * changed. This can be sent as a result of peers being found, lost or
         * updated.
         */
        @Override
        public void onWifiP2pPeersChanged(WifiP2pSolutionProvider owner,
                Context context, Intent intent) {
            log.debug(ExceptionUtil.currentMethodName());
            p2pSolution.requestPeersList();
        }

        /**
         * Broadcast intent action indicating that peer discovery has either
         * started or stopped. One extra EXTRA_DISCOVERY_STATE indicates whether
         * discovery has started or stopped.
         * 
         * Note that discovery will be stopped during a connection setup. If the
         * application tries to re-initiate discovery during this time, it can
         * fail.
         * 
         * service的discovery start/stop，不会收到此 广播。如果修改wifi名称，会discovery
         * stop，然后会收到此广播
         */
        @Override
        public void onWifiP2pDiscoveryChanged(WifiP2pSolutionProvider owner,
                Context context, Intent intent, BaseValuableLabel state) {
            log.debug("%s-\r\nstate=%s", ExceptionUtil.currentMethodName(),
                    state.getDisplayString());

            if (WifiP2pDiscoveryState.STOPPED.equals(state)) {
                mEventListener.onWifiP2pDiscoveryStop();
            }

        }

        /**
         * Broadcast intent action to indicate whether Wi-Fi p2p is enabled or
         * disabled
         */
        @Override
        public void onWifiP2pStateChanged(WifiP2pSolutionProvider owner,
                Context context, Intent intent, BaseValuableLabel state) {
            log.debug("%s-\r\nstate=%s", ExceptionUtil.currentMethodName(),
                    state.toString());

        }

        @Override
        public void onWifiP2pBroadcastReceive(WifiP2pSolutionProvider owner,
                Context context, Intent intent, String action) {
            log.debug("%s-\r\n%s", ExceptionUtil.currentMethodName(), action);

        }

        /**
         * Broadcast intent action indicating that Wi-Fi has been enabled,
         * disabled, enabling, disabling, or unknown. One extra provides this
         * state as an int. Another extra provides the previous state, if
         * available.
         */
        @Override
        public void onWifiStateChanged(WifiP2pSolutionProvider owner,
                Context context, Intent intent, BaseValuableLabel state) {
            // TODO Auto-generated method stub

        }
    }

    class WifiP2pActionListener extends AbsWifiP2pActionListener {

        public WifiP2pActionListener(
                com.huaqin.wirelessfiletransfer.wifip2p.WifiP2pSolutionProvider owner,
                String actionName) {
            super(owner, actionName);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onException(Throwable t) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onFailure(String actionName, int reason) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSuccess(String actionName) {
            // TODO Auto-generated method stub

        }
    }
}

