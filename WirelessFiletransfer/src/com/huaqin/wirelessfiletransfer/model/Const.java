package com.huaqin.wirelessfiletransfer.model;

public class Const {
    public static final int SEND_REQUEST = 10;
    public static final int FILE_ACCEPT_CONFIRM = 11;
    public static final int FILE_REFUSE_CONFIRM = 12;
    public static final int SEND_STREAM = 13;
    public static final int SEND_ACK = 14;
    public static final int SEND_WHOLE_ACK = 15;
    public static final String START_WFSERVICE = "android.intent.action.startWFService";
    public static final int SERVER_PORT = 8988;
    public static final int ROLE_SEND = 2;
    public static final int ROLE_RECEIVER = 1;
    public static final String ACTION_UPDATE_DISPLAY_LIST = "android.intent.action.update.displaylist";
    public static final String ACTION_UPDATE_STOP_SCAN_STATUS = "android.intent.action.update.stopscanstatus";
    public static final String ACTION_UPDATE_START_SCAN_STATUS = "android.intent.action.update.startscanstatus";
    
    public static final String ACTION_SERVCIE_STOPED = "android.intent.action.service.stoped";
    public static final String ACTION_SERVCIE_STARTED = "android.intent.action.service.started";
     
    public static final String ACTION_RESTART_SCAN = "android.intent.action.restartscan";
    public static final String ACTION_DISCONNECT_DEVICE = "android.intent.action.disconnectpeer";
    public static final String ACTION_DEVICE_DISCONNECTED = "android.intent.action.disconnected";
    public static final String ACTION_CONNECT_DEVICE = "android.intent.action.connectpeer";
    public static final String ACTION_CONNECT_DEVICE_POSITION = "connect_position";
    
    public static final String ACTION_UPDATE_HISTORY = "android.intent.action.update.history";

    public static final String ACTION_TRANSFERING = "android.intent.action.transfering";
    public static final int SEARCHSTATUS_START = 0;
    public static final int SEARCHSTATUS_STOP = 1;
    
    public static final int SCAN_FREQUENCY = 180000; // 每三分重新搜索一次
    public static final int SCAN_FREQUENCY_MAX_COUNT = 3; // 每两分重新搜索一次
    public static final int INVITE_LIMIT_TIME = 15000;
    
    public static final String SEND_STATUS = "(sent)";
    public static final String REJECT_STATUS = "(rejected)";
    public static final String NORMAL_STATUS = "";

    public static final int NOTIFICATION_SEND = 1;

    public static final int NOTIFICATION_RECEIVE = 2;
    public static final int NOTIFICATION_SERVICE_START = 3;
    public static final int NOTIFICATION_TRANSFERING = 4;
    
    public static final int NOTIFICATION_RECEIVE_BASE = 3;

    public static final String SOCKET_EXCEPTION_ECONNRESET = "sendto failed: ECONNRESET (Connection reset by peer) ";
    public static final String SOCKET_EXCEPTION_EPIPE = "sendto failed: EPIPE (Broken pipe) ";
    public static final String IO_EXCEPTION_EPIPE = "write failed: ENOSPC (No space left on device)";
 
    public static final int DIRECTION_OUTBOUND = 0;
    public static final int DIRECTION_INBOUND = 1;


    public static int RECEIVE_THREAD_STATUS_RUNNING = 1;
    public static int RECEIVE_THREAD_STATUS_SUSPEND = 0;
    public static int RECEIVE_THREAD_STATUS_STOP = 2;
    
    public static final String STORE_PATH = "WirelessFileTransfer";
    
}

