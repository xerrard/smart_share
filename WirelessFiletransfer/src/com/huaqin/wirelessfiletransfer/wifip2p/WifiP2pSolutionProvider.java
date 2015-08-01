package com.huaqin.wirelessfiletransfer.wifip2p;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.xerrard.util.ArgsUtil;
import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.StringUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWifiP2pListener;
import com.huaqin.wirelessfiletransfer.listen._02_base.AbsWifiP2pActionListener;
import com.huaqin.wirelessfiletransfer.listen._02_base.AbsWifiP2pBroadcastListener;
import com.huaqin.wirelessfiletransfer.listen._02_base.AbsWifiP2pListener;
import com.huaqin.wirelessfiletransfer.listen._02_base.AbsWifiP2pManagerListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.valuableLabel._03_wf.WifiP2pDeviceState;
import com.huaqin.wirelessfiletransfer.valuableLabel._03_wf.WifiP2pDiscoveryState;
import com.huaqin.wirelessfiletransfer.valuableLabel._03_wf.WifiState;
import com.huaqin.wirelessfiletransfer.wifip2p.WiFiDirectManager.WifiP2pManageListener;

/**
 * 
 * @ClassName:WiFiP2pSolution
 * @Description:本类用于wifip2p service的搜索和连接 搜索service的流程： 1.注册service
 *                          2.设置service监听 3.搜索service(步骤：新建一个service
 *                          requeset添加到wifip2pmanager中，然后启动搜索)
 * 
 *                          注意：所有的节点必须完成搜索流程，这样此节点才能被其他节点搜索到
 * @author:xerrard
 * @date:2015年3月5日
 */
public class WifiP2pSolutionProvider extends BroadcastReceiver {
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";
    public static final int STATE_STRING = 0;
    public static final int STATE_LIST = 1;
    protected static final String TAG = "WiFiP2pSolution";
    private WifiP2pManager manager;
    private Channel channel;
    private LogOutputinterface log = null;
    private AbsWifiP2pManagerListener ml;
    private AbsWifiP2pBroadcastListener bl;
    private IntentFilter mWifiActionFilterForBroadcast;
    private WifiP2pServiceInfo mWifiP2pServiceInfo;
    private WifiP2pServiceRequest mWifiP2pServiceRequest;


    public WifiP2pSolutionProvider(Context ctx, LogOutputinterface log) {

        // TODO Auto-generated constructor stub
        this.log = log;
        try {
            ArgsUtil.assertNotNull(ctx);
            setBroadcastReceiverIntent(ctx);
            manager = (WifiP2pManager) ctx
                    .getSystemService(Context.WIFI_P2P_SERVICE);

            ArgsUtil.assertNotNull(manager,
                    "Can not load WifiP2pManager service.");

            channel = manager.initialize(ctx, ctx.getMainLooper(),
                    new ChannelListener() {

                        @Override
                        public void onChannelDisconnected() {
                            // TODO Auto-generated method stub
                            dispatchMessageToListener(
                                    ml,
                                    WifiP2pManageListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONCHANNELDISCONNECTED);
                        }
                    });
            disconnectP2p(null);

        }
        catch (RuntimeException ex) {
            throw ex;
        }
        catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void setManageListener(AbsWifiP2pManagerListener l) {
        this.ml = l;
    }

    public void setBoradcastListener(AbsWifiP2pBroadcastListener l) {
        this.bl = l;
    }

    private AbsWifiP2pActionListener getListener(AbsWifiP2pActionListener l,
            String actionName) {
        return l == null ? new DefaultActionListener(this, log, actionName) : l;
    }

    private void setBroadcastReceiverIntent(Context ctx) {
        try {
            ctx.unregisterReceiver(this);
        }
        catch (Throwable t) {
            log.warn("un register error.", t);
        }
        try {

            if (mWifiActionFilterForBroadcast == null) {
                // TODO Auto-generated method stub
                mWifiActionFilterForBroadcast = new IntentFilter();
                mWifiActionFilterForBroadcast
                        .addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
                mWifiActionFilterForBroadcast
                        .addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
                mWifiActionFilterForBroadcast
                        .addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
                mWifiActionFilterForBroadcast
                        .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
                mWifiActionFilterForBroadcast
                        .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
                mWifiActionFilterForBroadcast
                        .addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            }

            ctx.registerReceiver(this, mWifiActionFilterForBroadcast);
        }
        catch (Throwable t) {
            dispatchMessageToListener(
                    ml,
                    AbsWifiP2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("onReceive broadcast error.", t));
        }

    }



    public void startSearchService(String serviceName, String serviceType,
            String dnsDescription, AbsWifiP2pActionListener l) {
        log.debug(ExceptionUtil.currentMethodName() + "-\r\n");
        manager.stopPeerDiscovery(channel, getListener(l, "stopPeerDiscovery")); // 为了解决重新搜索不到的问题，先stop搜索，再start搜索

        Map<String, String> record = new HashMap<String, String>();
        record.put(dnsDescription, "visible");
        
        try {
            ArgsUtil.assertNotNull(manager,
                    "P2P Message still not established.");

            manager.clearLocalServices(channel,               //clearService
                    getListener(l, "clearLocalServices"));
            manager.clearServiceRequests(channel,
                    getListener(l, "clearServiceRequests"));  //clear ServiceRequrest
            mWifiP2pServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                    serviceName, serviceType, record);
            manager.addLocalService(channel, mWifiP2pServiceInfo,   //add Service
                    getListener(l, "addLocalService"));
            
            manager.setDnsSdResponseListeners(channel, ml, ml);
            mWifiP2pServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
            manager.addServiceRequest(channel, mWifiP2pServiceRequest, //add ServiceRequest
                    getListener(l, "addServiceRequest"));

            manager.discoverServices(channel,                    //discoverServices
                    getListener(l, "discoverServices"));
        }
        catch (Throwable t) {
            dispatchMessageToListener(
                    ml,
                    AbsWifiP2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("startSearchService error", t));
        }
    }


    /**
     * <p>
     * Description:连接service
     * <p>
     * 
     * @date:2015年3月5日
     * @param service
     */
    public void connectP2p(WifiP2pDevice device, AbsWifiP2pActionListener l) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (mWifiP2pServiceRequest != null) {
            manager.removeServiceRequest(channel, mWifiP2pServiceRequest,
                    getListener(l, "removeServiceRequest"));
        }

        manager.connect(channel, config, getListener(l, "connect"));

    }

    public void disinviteP2p(AbsWifiP2pActionListener l) {
        manager.cancelConnect(channel, getListener(l, "cancelConnect"));

    }

    public void disconnectP2p(AbsWifiP2pActionListener l) {
        manager.removeGroup(channel, getListener(l, "disconnectP2p"));
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            String action = intent.getAction();

            dispatchMessageToListener(
                    bl,
                    AbsWifiP2pBroadcastListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PBROADCASTRECEIVE,
                    this, context, intent, action);

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
                    .equals(action)) {

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                dispatchMessageToListener(
                        bl,
                        AbsWifiP2pBroadcastListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PCONNECTIONCHANGED,
                        this, context, intent, networkInfo);

            }
            else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
                    .equals(action)) {

                dispatchMessageToListener(
                        bl,
                        AbsWifiP2pBroadcastListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PPEERSCHANGED,
                        this, context, intent);

            }
            else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION
                    .equals(action)) {

                /********************** discovery state changed. **************************/
                dispatchMessageToListener(
                        bl,
                        AbsWifiP2pBroadcastListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PDISCOVERYCHANGED,
                        this, context, intent,
                        WifiP2pDiscoveryState.retriveFromIntent(intent));

            }
            else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION
                    .equals(action)) {
                /********************** wifi p2p state changed. **************************/
                dispatchMessageToListener(
                        bl,
                        AbsWifiP2pBroadcastListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PSTATECHANGED,
                        this, context, intent,
                        WifiP2pDeviceState.retriveFromIntent(intent)

                );

            }
            else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                /********************** wifi state changed. **************************/
                dispatchMessageToListener(
                        bl,
                        AbsWifiP2pBroadcastListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFISTATECHANGED,
                        this, context, intent,
                        WifiState.retriveFromIntent(intent));
            }
            else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
                    .equals(action)) {
                dispatchMessageToListener(
                        bl,
                        AbsWifiP2pBroadcastListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PTHISDEVICECHANGED,
                        this,
                        context,
                        intent,
                        (WifiP2pDevice) intent
                                .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            }
        }
        catch (Throwable t) {
            dispatchMessageToListener(
                    ml,
                    AbsWifiP2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("onReceive broadcast error.", t));
        }
    }

    public void requestConnectionInfo() {
        try {
            // TODO Auto-generated method stub
            if (manager != null && channel != null) {
                manager.requestConnectionInfo(channel,
                        new ConnectionInfoListener() {

                            @Override
                            public void onConnectionInfoAvailable(
                                    WifiP2pInfo info) {
                                dispatchMessageToListener(
                                        ml,
                                        WifiP2pManageListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONCONNECTIONINFOAVAILABLE,
                                        info);
                            }
                        });
            }
        }
        catch (Throwable t) {
            dispatchMessageToListener(
                    ml,
                    AbsWifiP2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("requestConnectionInfo error.", t));
        }
    }

    public void requestPeersList() {
        // TODO Auto-generated method stub

        if (manager != null) {
            manager.requestPeers(channel, new PeerListListener() {

                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    // TODO Auto-generated method stub
                    dispatchMessageToListener(
                            ml,
                            WifiP2pManageListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONPEERSAVAILABLE,
                            peers);
                }
            });
        }

    }

    class DefaultActionListener extends AbsWifiP2pActionListener {

        private LogOutputinterface log;

        public DefaultActionListener(WifiP2pSolutionProvider owner,
                LogOutputinterface log, String actionName) {
            super(owner, actionName);
            this.log = log;
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onException(Throwable t) {
            // TODO Auto-generated method stub
            if (log != null) {
                String msg = String.format("%s fail.", actionName);

                log.err(msg);

                dispatchMessageToListener(
                        ml,
                        IfWifiP2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                        new RuntimeException(msg, t));
            }
        }

        @Override
        public void onFailure(String actionName, int reason) {
            // TODO Auto-generated method stub
            if (log != null) {

                String msg = String.format("%s fail. reason=%s", actionName,
                        reason);
                log.warn(msg);
            }
        }

        @Override
        public void onSuccess(String actionName) {
            // TODO Auto-generated method stub
            if (log != null) {
                log.debug("%s success", actionName);
            }
        }

    }

    public void dispatchMessageToListener(IfWifiP2pListener listener,
            String methodName, Object... args) {

        if (StringUtil.isNotNullAndEmpy(methodName) && listener != null) {
            try {
                Method m = null;
                Method[] methods = listener.getClass().getMethods();
                for (Method each : methods) {
                    if (each.getName().equals(methodName)) {
                        m = each;
                        break;
                    }
                }
                if (m != null) {
                    m.invoke(listener, args);
                }
            }
            catch (RuntimeException ex) {
                throw ex;
            }
            catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }
}

