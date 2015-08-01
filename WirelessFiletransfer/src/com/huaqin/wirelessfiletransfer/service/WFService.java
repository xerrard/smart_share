package com.huaqin.wirelessfiletransfer.service;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Set;

import org.xerrard.util.BindService;
import org.xerrard.util.DateUtil;
import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.FileSizeUtil;
import org.xerrard.util.SafeThread.SafeRunnable;
import org.xerrard.util.VarArgsUtil;
import org.xerrard.util.WifiUtil;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.IBinder;
import android.widget.Toast;

import com.huaqin.wirelessfiletransfer.MainApplication;
import com.huaqin.wirelessfiletransfer.R;
import com.huaqin.wirelessfiletransfer.activity.WirelessTransferHistory.WirelessTransferHistoryItem;
import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pManagerEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;
import com.huaqin.wirelessfiletransfer.model.WiFiP2pServicePeer;
import com.huaqin.wirelessfiletransfer.network.NetworkManager;
import com.huaqin.wirelessfiletransfer.network.Receive;
import com.huaqin.wirelessfiletransfer.wifip2p.WiFiDirectManager;

public class WFService extends BindService implements
        IfWiFiP2pManagerEventsListener, IfWiFiP2pConnectionEventsListener {
    protected LogOutputinterface log;
    protected MainApplication app;
    protected static WiFiDirectManager mWFWiFiP2pManager = null;
    protected static NetworkManager mWFNetworkManager = null;
    protected ArrayList<File> mReceiveFileList;
    protected String senderName;
    protected String receiverName;


    @Override
    public void onCreate() {
        super.onCreate();
        app = (MainApplication) getApplication();
        log = app.log;
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!WifiUtil.isWifiEnabled(this)) {
            WifiUtil.enableWifi(this); // 确保wifi打开
        }
        app.mServiceSetup = true;
        mReceiveFileList = new ArrayList<File>();
        initWifiP2pManaer();
        initNetWorkManger();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        app.mServiceSetup = false;
        Toast.makeText(this, "filetransfer sevice is stoped", Toast.LENGTH_LONG)
                .show();
        super.onDestroy();
    }

    private void initWifiP2pManaer() {
        mWFWiFiP2pManager = WiFiDirectManager.getSingletonInstance(this);
        mWFWiFiP2pManager.log = log;
        mWFWiFiP2pManager.initWiFiP2pManager();
        mWFWiFiP2pManager.setEventListener(this);
    }

    private void initNetWorkManger(){
        mWFNetworkManager = NetworkManager.getSingletonInstance(this,log,app.urilist);
        mWFNetworkManager.setEventListener(this);
    }
    
    /**
     * <p>
     * Description:开启搜索 1.重置role 2.搜索 3.启动timer，每隔SCAN_FREQUENCY
     * 搜索一次。两种情况timer关闭：1.重启本搜索 2.连接成功
     * <p>
     * 
     * @date:2015年4月4日
     */
    protected void discoverService() {
        WifiP2pDevice thisdevice = mWFWiFiP2pManager.getThisDevice();
        if (thisdevice != null) {
            if (thisdevice.status != WifiP2pDevice.AVAILABLE) {
                return;
            }
        }

        app.role = Const.ROLE_RECEIVER; // 每次连接状态发生变化，重新搜索的时候，重置下role
        log.debug(ExceptionUtil.currentMethodName() + "   app.role = %d",
                app.role);

        mWFWiFiP2pManager.disCoverServicePeers();


    }


    
    /**
     * <p>
     * Description:主动连接peer，发送端会调用
     * <p>
     * @date:2015年5月13日
     * @param device
     */
    protected void connectDevice(WifiP2pDevice device){

        app.role = Const.ROLE_SEND;
        mWFWiFiP2pManager.connectdevice(device, null);

    }
    
    /**
     * WIFI P2P连接成功
     */
    @Override
    public void onWifip2pConnected(WifiP2pInfo info, WifiP2pDevice thisDevice) {
        log.debug(ExceptionUtil.currentMethodName());
        WifiP2pDevice receiverdevice = mWFWiFiP2pManager.getTargetDevice();
        if (receiverdevice != null) {
            receiverName = receiverdevice.deviceName;
        }
        mWFNetworkManager.connectNetworkAndSendRequest(info, thisDevice, app.role, app.urilist);
    }

    /**
     * discovery stop，暂时不搜索
     */
    @Override
    public void onWifiP2pDiscoveryStop() {
        // discoverService();
    }

    /**
     * wifip2p disconnected,释放上次的socket 开启搜索
     */
    @Override
    public void onWifiP2pDisconnected() {
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            onException(e);
            e.printStackTrace();
        }
        mWFNetworkManager.closeSocket();
        discoverService();
    }
    
    @Override
    public void onNetworkConnected() {
    }


    @Override
    public void onSendRequeset(int reqcode) {
    }

    @Override
    public void onReceiveRequest(String senderName, SafeRunnable receiveThread) {
        this.senderName = senderName;
    }

    @Override
    public void onSendRefuseConfirm() {
    }

    @Override
    public void onReceiveRefuseConfirm() {
        app.statusMap.put(app.mServicePeerList.get(app.ConnectedId),
                Const.REJECT_STATUS);
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            onException(e);
        }
        disConnect();
    }
    

    @Override
    public void onSendAcceptConfirm() {
    }
    
    @Override
    public void onReceiveAcceptConfirm() {
    }

    @Override
    public void onSendFilePart(String filename, long filesize, int nowpercent) {
    }

    @Override
    public void onSendFile(String filename, long filesize) {
        app.mFileTransferSendHistorylist.add(new WirelessTransferHistoryItem(
                filename, receiverName, app.role, DateUtil
                        .getNowDateTimeString(this), FileSizeUtil
                        .FormetFileSize(filesize)));
    }

    @Override
    public void onSendFiles() {
    }

    @Override
    public void onReceiveFilePart(String filename, long filesize, int nowpercent) {
    }
    


    @Override
    public void onReveiveFile(File file) {
        mReceiveFileList.add(file);
        app.mFileTransferReceiveHistorylist
                .add(new WirelessTransferHistoryItem(file, senderName,
                        app.role, DateUtil.getNowDateTimeString(this),
                        FileSizeUtil.getAutoFileOrFilesSize(file)));
    }

    @Override
    public void onReveiveFiles(File file) {
        mReceiveFileList.add(file);
        app.mFileTransferReceiveHistorylist
                .add(new WirelessTransferHistoryItem(file, senderName,
                        app.role, DateUtil.getNowDateTimeString(this),
                        FileSizeUtil.getAutoFileOrFilesSize(file)));

    }

    @Override
    public void onSendAck() {

    }

    @Override
    public void onReceiveAck() {

    }

    @Override
    public void onSendWholeAck() {
    }

    @Override
    public void onReceiveWholeAck() {
        app.statusMap.put(app.mServicePeerList.get(app.ConnectedId),
                Const.SEND_STATUS);
        try {
            Thread.sleep(500);
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            onException(e);
        }
        disConnect();
    }

    /**
     * <p>
     * Description:关闭连接，分两部分 1.关闭socket连接 2.断开wifip2p连接
     * <p>
     * 
     * @date:2015年4月3日
     */
    protected void disConnect() {
        log.debug(ExceptionUtil.currentMethodName());

        mWFWiFiP2pManager.disConnectCurrentDevice(null);
    }




    public void onException(Context context, Throwable t) {
        // TODO Auto-generated method stub
        try {
            invokeUIDelegate("onExceptionVg", this, t);
            if (log != null) {
                log.err(t, "Occurred exception.");
            }
        }
        catch (Throwable ex) {
            // 此时出现错误
            ex.printStackTrace();
        }
        finally {
            // this.finish();
        }

    }

    protected void onExceptionOccured(Context ctx, Throwable t) {
        // TODO Auto-generated method stub
        if (t instanceof ConnectException) {
            displayException(getString(R.string.connectexception));
        }
        else if (t instanceof SocketException) {
            /*
             * if (t.getMessage().equals(Const.SOCKET_EXCEPTION_ECONNRESET)) {
             * displayException(getString(R.string.socketexception_econnresst));
             * } else if (t.getMessage().equals(Const.SOCKET_EXCEPTION_EPIPE)) {
             * displayException(getString(R.string.exceptiontitle)); }
             */
            displayException(getString(R.string.socketexception) + "");
        }
        else if(t instanceof IOException){
            if(t.getMessage().equals(Const.IO_EXCEPTION_EPIPE)){
                log.err("displayException IO_EXCEPTION_EPIPE succes");
                displayException(getString(R.string.nospaceexception));
            }
            //displayException(getString(R.string.nospaceexception));
            //log.err("Exception:t.getMessage() = " + t.getMessage()  + " end");
        }
        else {
            displayException(getString(R.string.tranferexception) + "");
        }
        //log.err("Exception:" + ExceptionUtil.getExcetpionTrace(t));

        disConnect();
        // stopSelf(); // 出现问题，关闭service
    }

    public final void onExceptionVg(Object... objects) {
        onExceptionOccured(VarArgsUtil.getArgument(0, Context.class, objects),
                VarArgsUtil.getArgument(1, Throwable.class, objects));
    }

    public void displayToast(Object... objects) {
        String msg = VarArgsUtil.getArgument(0, String.class, objects);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void displayException(String str) {
        log.debug(ExceptionUtil.currentMethodName() + " str = %s", str);
    }

    @Override
    public void onDisplayListUpdate(Set<WiFiP2pServicePeer> serviceDeviceSet) {
        log.debug(ExceptionUtil.currentMethodName()
                + "serviceDeviceSet.size is %d", serviceDeviceSet.size());

    }

    @Override
    public void onException(Throwable t) {
        Receive.isstoped = true;
        onException(this, t);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        log.debug(ExceptionUtil.currentMethodName() + "thread is = ",
                thread.getName());
        if (log != null) {
            log.err(ex, "Occurred exception.");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }



}

