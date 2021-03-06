package org.xerrard.smartshare.Present;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;

import org.xerrard.smartshare.Model.Const;
import org.xerrard.smartshare.Present.Listener.AbsWifip2pActionListener;
import org.xerrard.smartshare.Present.Listener.AbsWifip2pBroadcastListener;
import org.xerrard.smartshare.Present.Listener.AbsWifip2pManagerListener;
import org.xerrard.smartshare.Present.Listener.IfWifip2pListener;
import org.xerrard.smartshare.Present.Listener.Wifip2pActionListener;
import org.xerrard.smartshare.Present.Listener.Wifip2pBroadcastListener;
import org.xerrard.smartshare.Present.Listener.Wifip2pManagerListener;
import org.xerrard.smartshare.Present.State.WifiP2pDeviceState;
import org.xerrard.smartshare.Present.State.WifiP2pDiscoveryState;
import org.xerrard.smartshare.Present.State.WifiState;
import org.xerrard.util.ArgsUtil;
import org.xerrard.util.StringUtil;
import org.xerrard.util.log.ILogOutput;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 类描述：提供最基础的wifip2p API调用支持，callback在外部
 * 如 actionlistener  broadcastlistener  managerlistener
 * 创建人：xuqiang
 * 创建时间：16-3-8 上午11:26
 * 修改人：xuqiang
 * 修改时间：16-3-8 上午11:26
 * 修改备注：
 */
public class Wifip2pSolutionProvider extends BroadcastReceiver {
    private Context mContext;
    private ILogOutput log;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private AbsWifip2pManagerListener mWifiP2pManagerListener;
    private AbsWifip2pBroadcastListener mWifiP2pBroadcastListener;
    private IntentFilter mWifiActionFilterForBroadcast;


    /**
     * @param context
     * @param log
     * @param ml
     * @param bl
     */
    public Wifip2pSolutionProvider(Context context, ILogOutput log, AbsWifip2pManagerListener ml,
                                   AbsWifip2pBroadcastListener bl, AbsWifip2pActionListener al) {
        this.mContext = context;
        this.log = log;
        this.mWifiP2pBroadcastListener = getBroadcastListener(bl);
        this.mWifiP2pManagerListener = getManagerListener(ml);
    }


    /**
     * 初始化的过程中，先获取WIFIP2P服务，并且注册广播
     */
    public void initWifip2p() {
        try {
            ArgsUtil.assertNotNull(mContext);
            manager = (WifiP2pManager) mContext
                    .getSystemService(Context.WIFI_P2P_SERVICE);
            registerBroadcast(mContext);

            ArgsUtil.assertNotNull(manager,
                    "Can not load WifiP2pManager service.");

            channel = manager.initialize(mContext, mContext.getMainLooper(),
                    new WifiP2pManager.ChannelListener() {

                        @Override
                        public void onChannelDisconnected() {
                            // TODO Auto-generated method stub
                            dispatchMessageToListener(
                                    mWifiP2pManagerListener,
                                    AbsWifip2pManagerListener
                                            .WIFI_P2P_ACTION_LISTENER_METHOD_ONCHANNELDISCONNECTED);
                        }
                    });
            //disconnectP2p(null);

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void discoverService() {
        // 为了解决重新搜索不到的问题，先stop搜索，再start搜索
        manager.stopPeerDiscovery(channel, getActionListener(null, "stopPeerDiscovery"));

        Map<String, String> record = new HashMap<String, String>();
        record.put(Const.TXTRECORD_PROP_AVAILABLE, "visible");
        try {
            ArgsUtil.assertNotNull(manager,
                    "P2P Message still not established.");
            manager.clearLocalServices(channel,
                    getActionListener(null, "clearLocalServices"));
            manager.clearServiceRequests(channel,
                    getActionListener(null, "clearServiceRequests"));
            WifiP2pServiceInfo mWifiP2pServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                    Const.SERVICE_NAME, Const.SERVICE_REG_TYPE, record);
            manager.addLocalService(channel, mWifiP2pServiceInfo,
                    getActionListener(null, "addLocalService"));
            manager.setDnsSdResponseListeners(channel, mWifiP2pManagerListener,
                    mWifiP2pManagerListener);
            manager.addServiceRequest(channel, WifiP2pDnsSdServiceRequest.newInstance(),
                    getActionListener(null, "addServiceRequest"));
            manager.discoverServices(channel,                    //discoverServices
                    getActionListener(null, "discoverServices"));
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pManagerListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("createLocalService error.", t));
        }
    }

    public void connectP2p(WifiP2pDevice device, AbsWifip2pActionListener listener) {
        try {
            ArgsUtil.assertNotNull(manager,
                    "connectP2p P2P Message still not established.");
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.wps.setup = WpsInfo.PBC;
            manager.connect(channel, config, getActionListener(listener, "connect"));
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pManagerListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("connectP2p error.", t));
        }
    }

    public void disinviteP2p(AbsWifip2pActionListener listener) {
        try {
            ArgsUtil.assertNotNull(manager,
                    "disinviteP2p P2P Message still not established.");
            manager.cancelConnect(channel, getActionListener(listener, "cancelConnect"));
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pManagerListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("disinviteP2p error.", t));
        }
    }

    public void disconnectP2p(AbsWifip2pActionListener listener) {
        try {
            ArgsUtil.assertNotNull(manager,
                    "disconnectP2p P2P Message still not established.");
            manager.removeGroup(channel, getActionListener(listener, "removeGroup"));
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pManagerListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("disconnectP2p error.", t));
        }
    }

    public void createGroup(AbsWifip2pActionListener listener) {
        try {
            ArgsUtil.assertNotNull(manager,
                    "createGroup P2P Message still not established.");
            manager.createGroup(channel, getActionListener(listener, "createGroup"));
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pManagerListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("disconnectP2p error.", t));
        }
    }




    private void registerBroadcast(Context ctx) {
        //unRegisterBroadcast(ctx);
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
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pBroadcastListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("register broadcast error.", t));
        }

    }

    private void unRegisterBroadcast(Context ctx) {
        try {
            ctx.unregisterReceiver(this);
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pBroadcastListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("unregister broadcast error ", t));
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            String action = intent.getAction();

            dispatchMessageToListener(
                    mWifiP2pBroadcastListener,
                    AbsWifip2pBroadcastListener
                            .WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PBROADCASTRECEIVE,
                    this, context, intent, action);

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION
                    .equals(action)) {

                NetworkInfo networkInfo = (NetworkInfo) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                dispatchMessageToListener(
                        mWifiP2pBroadcastListener,
                        AbsWifip2pBroadcastListener
                                .WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PCONNECTIONCHANGED,
                        this, context, intent, networkInfo);

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION
                    .equals(action)) {

                dispatchMessageToListener(
                        mWifiP2pBroadcastListener,
                        AbsWifip2pBroadcastListener
                                .WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PPEERSCHANGED,
                        this, context, intent);

            } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION
                    .equals(action)) {

                /********************** discovery state changed. **************************/
                dispatchMessageToListener(
                        mWifiP2pBroadcastListener,
                        AbsWifip2pBroadcastListener
                                .WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PDISCOVERYCHANGED,
                        this, context, intent,
                        WifiP2pDiscoveryState.retriveFromIntent(intent));

            } else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION
                    .equals(action)) {
                /********************** wifi p2p state changed. **************************/
                dispatchMessageToListener(
                        mWifiP2pBroadcastListener,
                        AbsWifip2pBroadcastListener
                                .WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PSTATECHANGED,
                        this, context, intent,
                        WifiP2pDeviceState.retriveFromIntent(intent)

                );

            } else if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                /********************** wifi state changed. **************************/
                dispatchMessageToListener(
                        mWifiP2pBroadcastListener,
                        AbsWifip2pBroadcastListener
                                .WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFISTATECHANGED,
                        this, context, intent,
                        WifiState.retriveFromIntent(intent));
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
                    .equals(action)) {
                dispatchMessageToListener(
                        mWifiP2pBroadcastListener,
                        AbsWifip2pBroadcastListener
                                .WIFI_P2P_ACTION_LISTENER_METHOD_ONWIFIP2PTHISDEVICECHANGED,
                        this,
                        context,
                        intent,
                        (WifiP2pDevice) intent
                                .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
            }
        } catch (Throwable t) {
            dispatchMessageToListener(
                    mWifiP2pBroadcastListener,
                    IfWifip2pListener.WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION,
                    new RuntimeException("onReceive broadcast error.", t));
        }
    }

    /**
     * 将原本 异步callback和broadcastreceiver要处理的事情整合到一起，统一为onXXX的模式
     *
     * @param listener
     * @param methodName
     * @param args
     */
    public void dispatchMessageToListener(IfWifip2pListener listener,
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
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }


    private AbsWifip2pBroadcastListener getBroadcastListener(AbsWifip2pBroadcastListener l) {
        return l == null ? new Wifip2pBroadcastListener() : l;
    }

    private AbsWifip2pManagerListener getManagerListener(AbsWifip2pManagerListener l) {
        return l == null ? new Wifip2pManagerListener() : l;
    }

    private AbsWifip2pActionListener getActionListener(AbsWifip2pActionListener l,
                                                       String actionName) {
        return l == null ? new Wifip2pActionListener(this, actionName, log) : l;
    }
}
