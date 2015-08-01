package com.huaqin.wirelessfiletransfer.network;

import java.net.Socket;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;

public class SendRoleThread extends SafeRunnable {
    private Socket socket;
    private IfWiFiP2pConnectionEventsListener listener;
    LogOutputinterface log;
    WifiP2pDevice thisDevice;
    private Context ctx;

    public SendRoleThread(Socket socket,
            IfWiFiP2pConnectionEventsListener listener, LogOutputinterface log,
            WifiP2pDevice thisDevice, Context ctx) {
        this.socket = socket;
        this.listener = listener;
        this.log = log;
        this.ctx = ctx;
        this.thisDevice = thisDevice;
        new SafeThread(this).start();

    }

    @Override
    public void safeRun() {
        log.debug(ExceptionUtil.currentMethodName());
        new Receive(socket, listener, log, ctx); //先做好接收的准备，再发送请求
        new SendRequest(socket, Const.SEND_REQUEST, listener, log, thisDevice);

    }

}
