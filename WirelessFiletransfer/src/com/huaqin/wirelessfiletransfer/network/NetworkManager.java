package com.huaqin.wirelessfiletransfer.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.FileSizeUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;

import android.content.Context;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.webkit.MimeTypeMap;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;
import com.huaqin.wirelessfiletransfer.model.WifiP2pSendFileInfo;

public class NetworkManager implements IfWiFiP2pConnectionEventsListener {
    public static NetworkManager instance;
    private Context ctx;
    private LogOutputinterface log;
    protected Socket connectedSocket;
    protected Server server;
    protected Client client;
    protected boolean isowner;
    protected String senderName;
    protected String receiverName;
    protected Receive receiveThread;
    protected List<Uri> urilist = null;
    private boolean receive_ack = false;
    IfWiFiP2pConnectionEventsListener mEventListener;

        private NetworkManager(Context ctx, LogOutputinterface log,
            List<Uri> urilist) {
        this.ctx = ctx;
        this.log = log;
        this.urilist = urilist;
    }

    public static NetworkManager getSingletonInstance(Context ctx,
            LogOutputinterface log, List<Uri> urilist) {
        if (instance == null) {
            instance = new NetworkManager(ctx, log, urilist);
        }
        return instance;
    }

    public void setEventListener(
            IfWiFiP2pConnectionEventsListener mEventListener) {
        this.mEventListener = mEventListener;

    }

    @Override
    public void onException(Throwable t) {
        // TODO Auto-generated method stub

    }

    /**
     * \
     * <p>
     * Description:连接并且发送request
     * <p>
     * 连接成功，会返回onNetworkConnected()
     * <p>
     * 发送request完毕，会返回onSendRequeset（Const.SEND_REQUEST）
     * 
     * @date:2015年4月3日
     * @param info
     */
    public void connectNetworkAndSendRequest(WifiP2pInfo info,
            WifiP2pDevice thisDevice, int role, List<Uri> urilist) {
        log.debug(
                ExceptionUtil.currentMethodName()
                        + "info.groupFormed = %b   info.isGroupOwner = %b   thisDevice = %s    app.role = %d    \r\n",
                info.groupFormed, info.isGroupOwner, thisDevice, role);
        if (info.groupFormed) {
            if (info.isGroupOwner) {
                server = new Server(this, role, log, thisDevice, ctx);
            }
            else {
                client = new Client(new InetSocketAddress(
                        info.groupOwnerAddress, Const.SERVER_PORT), this, role,
                        log, thisDevice, ctx);
            }
        }
        this.isowner = info.isGroupOwner;
        this.urilist = urilist;

    }

    /**
     * 连接成功 网络socket连通后，得到当前正在连接着的socket
     */
    @Override
    public void onNetworkConnected() {
        if (isowner) {
            connectedSocket = server.getConnectedSocket();
        }
        else {
            connectedSocket = client.getConnectedSocket();
        }
        log.debug(ExceptionUtil.currentMethodName()
                + "isowner = %b    connectedSocket = %s", isowner,
                connectedSocket != null ? connectedSocket.getInetAddress()
                        .toString() : "null");
        mEventListener.onNetworkConnected();
    }

    /**
     * 发送Request完毕，此处reqcode有两种可能 1.Const.SEND_REQUEST SEND_REQUEST已经发送 默认
     * 2.Const.FILE_ACCEPT_CONFIRM 已经发送 3.Const.FILE_REFUSE_CONFIRM 已经发送
     * 4.Const.SEND_ACK ack已经发送
     */
    @Override
    public void onSendRequeset(int reqcode) {
        log.debug(ExceptionUtil.currentMethodName());
        if (reqcode == Const.SEND_ACK) {
            onSendAck();
        }
        else if (reqcode == Const.FILE_ACCEPT_CONFIRM) {
            onSendAcceptConfirm();
        }
        else if (reqcode == Const.FILE_REFUSE_CONFIRM) {
            onSendRefuseConfirm();
        }
        else if (reqcode == Const.SEND_WHOLE_ACK) {
            onSendWholeAck();
        }
        mEventListener.onSendRequeset(reqcode);
    }

    @Override
    public void onReceiveRequest(String senderName, SafeRunnable receiveThread) {
        log.debug(ExceptionUtil.currentMethodName() + "SenderName = %s\r\n",
                senderName);
        this.receiveThread = (Receive) receiveThread;
        mEventListener.onReceiveRequest(senderName, receiveThread);
    }

    /**
     * <p>
     * Description:拒绝Const.SEND_REQUEST请求，发送Const.FILE_REFUSE_CONFIRM，表示拒绝
     * ，返回onSendRefuseConfirm
     * <p>
     * 
     * @date:2015年4月3日
     */
    public void refuseFileTransfer() {
        log.debug(ExceptionUtil.currentMethodName());
        receiveThread.runningstate = Const.RECEIVE_THREAD_STATUS_STOP;
        new SendRequest(connectedSocket, Const.FILE_REFUSE_CONFIRM, this, log,
                null);
    }

    @Override
    public void onSendRefuseConfirm() {
        log.debug(ExceptionUtil.currentMethodName());
        mEventListener.onSendRefuseConfirm();
    }

    @Override
    public void onReceiveRefuseConfirm() {
        log.debug(ExceptionUtil.currentMethodName());
        mEventListener.onReceiveRefuseConfirm();
    }

    /**
     * <p>
     * Description:接收Const.SEND_REQUEST请求，发送Const.FILE_ACCEPT_CONFIRM，表示接受
     * <p>
     * 
     * @date:2015年4月3日
     */
    public void acceptFileTransfer() {
        receiveThread.runningstate = Const.RECEIVE_THREAD_STATUS_RUNNING;
        new SendRequest(connectedSocket, Const.FILE_ACCEPT_CONFIRM, this, log,
                null);

    }

    @Override
    public void onSendAcceptConfirm() {
        log.debug(ExceptionUtil.currentMethodName());
        mEventListener.onSendAcceptConfirm();
    }

    @Override
    public void onReceiveAcceptConfirm() {
        log.debug(ExceptionUtil.currentMethodName());
        sendFiles();
        mEventListener.onReceiveAcceptConfirm();
    }

    /**
     * <p>
     * Description:发送文件
     * <p>
     * 
     * @date:2015年4月3日
     */
    protected void sendFiles() {
        log.debug(ExceptionUtil.currentMethodName());

        new SafeThread(new SafeRunnable() {

            @Override
            public void safeRun() throws Throwable {

                for (int i = 0; i < urilist.size(); i++) {
                    InputStream is = null;
                    String file_name = null;
                    String uriString = urilist.get(i).toString();
                    MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                    String mimetype = mimeTypeMap
                            .getMimeTypeFromExtension(MimeTypeMap
                                    .getFileExtensionFromUrl(uriString));

                    WifiP2pSendFileInfo wifiP2pSendFileInfo = WifiP2pSendFileInfo
                            .generateFileInfo(ctx, urilist.get(i), mimetype);
                    is = wifiP2pSendFileInfo.mInputStream;
                    file_name = wifiP2pSendFileInfo.mFileName;

                    receive_ack = false;
                    new SendStream(connectedSocket, is, file_name,
                            wifiP2pSendFileInfo.mLength, urilist.size(),
                            NetworkManager.this, log);
                    log.debug(ExceptionUtil.currentMethodName()
                            + "SendStream start i = %d app.urilist.size()=%d",
                            i, urilist.size());

                    while (!receive_ack) {
                        try {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            onException(e);
                        }// 一直等到ack才进行下一个传输
                    }

                }
                onSendFiles(); // 全部发送完毕
            }
        }).start();
    }

    /**
     * 发送文件片
     */
    @Override
    public void onSendFilePart(String filename, long filesize, int nowpercent) {
        log.debug(ExceptionUtil.currentMethodName() + "nowpercent is %d"
                + "filename = %s\r\n", nowpercent, filename);
        mEventListener.onSendFilePart(filename, filesize, nowpercent);
    }

    /**
     * 发送完一个文件完毕
     */
    @Override
    public void onSendFile(String filename, long filesize) {
        log.debug(ExceptionUtil.currentMethodName());
        mEventListener.onSendFile(filename, filesize);
    }

    /**
     * 发送完所有文件
     */
    @Override
    public void onSendFiles() {
        log.debug(ExceptionUtil.currentMethodName());
        mEventListener.onSendFiles();
    }

    /**
     * 收到文件碎片
     */
    @Override
    public void onReceiveFilePart(String filename, long filesize,
            int nowpercent) {
        log.debug(ExceptionUtil.currentMethodName() + " file = %s "
                + "filesize = %s" + " nowpercent = %d", filename, nowpercent,
                FileSizeUtil.FormetFileSize( filesize));
        mEventListener.onReceiveFilePart(filename, filesize, nowpercent);
    }

    /**
     * 收到单个文件
     */
    @Override
    public void onReveiveFile(File file) {
        log.debug(ExceptionUtil.currentMethodName() + " file= %s ",
                file.getName());
        sendAck();
        mEventListener.onReveiveFile(file);
    }

    /**
     * 收到所有文件
     */
    @Override
    public void onReveiveFiles(File file) {
        log.debug(ExceptionUtil.currentMethodName());
        sendWholeAck();
        mEventListener.onReveiveFiles(file);
    }

    /**
     * <p>
     * Description:发送ack，告知发送端，文件已经收到
     * <p>
     * 
     * @date:2015年4月4日
     */
    protected void sendAck() {
        log.debug(ExceptionUtil.currentMethodName());
        new SendRequest(connectedSocket, Const.SEND_ACK, this, log, null);
    }

    @Override
    public void onSendAck() {
        log.debug(ExceptionUtil.currentMethodName());
        mEventListener.onSendAck();
    }

    /**
     * 收到Ack，已确认接收端收到文件，关闭Socket,关闭
     */
    @Override
    public void onReceiveAck() {
        log.debug(ExceptionUtil.currentMethodName());
        receive_ack = true;
        mEventListener.onReceiveAck();
    }

    /**
     * <p>
     * Description:发送ack，告知发送端，文件已经收到
     * <p>
     * 
     * @date:2015年4月4日
     */
    protected void sendWholeAck() {
        log.debug(ExceptionUtil.currentMethodName());
        new SendRequest(connectedSocket, Const.SEND_WHOLE_ACK, this, log, null);
    }

    /**
     * 已发送ack 发送完后，将当前的角色置为接收方
     */
    @Override
    public void onSendWholeAck() {
        // TODO Auto-generated method stub
        log.debug(ExceptionUtil.currentMethodName());
        mEventListener.onSendWholeAck();
    }

    /**
     * 收到Ack，已确认接收端收到文件，关闭Socket,关闭
     */
    @Override
    public void onReceiveWholeAck() {
        log.debug(ExceptionUtil.currentMethodName());
        receive_ack = true;
        mEventListener.onReceiveWholeAck();
    }

    /**
     * <p>
     * Description:关闭socket。要注意两点：
     * 1.由于无法判断当前service中跑的是server还算client，所以要进行判断并且将所有的socket都关闭
     * 2.socket关闭前，务必将流关闭，要不然会出现EPIPE（Broken pipe）错误
     * <p>
     * 
     * @date:2015年4月3日
     */
    public void closeSocket() {
        log.debug(ExceptionUtil.currentMethodName());
        try {
            if (connectedSocket != null && !connectedSocket.isClosed()) {
                // connectedSocket.getOutputStream().close();
                // connectedSocket.getInputStream().close();
                // //receive线程关闭时已经将inputstream已经close
                connectedSocket.close();
            }
            if (server != null && server.serverSocket != null
                    && !server.serverSocket.isClosed()) {
                server.serverSocket.close();
            }
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            onException(e);
        }
    }

}

