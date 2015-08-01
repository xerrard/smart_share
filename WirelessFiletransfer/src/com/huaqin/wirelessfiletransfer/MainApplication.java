package com.huaqin.wirelessfiletransfer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xerrard.util.CrashHandler;

import android.app.Application;
import android.app.Notification.Builder;
import android.net.Uri;

import com.huaqin.wirelessfiletransfer.activity.WirelessTransferHistory.WirelessTransferHistoryItem;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.log._03_wf.FileLogger;
import com.huaqin.wirelessfiletransfer.model.WiFiP2pServicePeer;

public class MainApplication extends Application {
    public List<WiFiP2pServicePeer> mServicePeerList;
    public Uri uri;
    public ArrayList<Uri> urilist;
    public InputStream is;
    public int role;
    public int ConnectedId = 0;
    public Map<WiFiP2pServicePeer, String> statusMap;
    public boolean mServiceSetup = false;
    public List<WirelessTransferHistoryItem> mFileTransferReceiveHistorylist;
    public List<WirelessTransferHistoryItem> mFileTransferSendHistorylist;
    public List<Builder> mNotifacionList;
    public int searchStatus;
    public LogOutputinterface log;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        mServicePeerList = new ArrayList<WiFiP2pServicePeer>();
        statusMap = new HashMap<WiFiP2pServicePeer, String>();
        urilist = new ArrayList<Uri>();
        mFileTransferReceiveHistorylist = new ArrayList<WirelessTransferHistoryItem>();
        mFileTransferSendHistorylist = new ArrayList<WirelessTransferHistoryItem>();

        mNotifacionList = new ArrayList<Builder>();

        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler
        crashHandler.init(getApplicationContext());

         log = new FileLogger(this);
        //log = new SPS_WOTPLog(this);
    }

}

