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

public class ServerThread extends SafeRunnable {
    private Socket socket = null;
    private int role;
    private IfWiFiP2pConnectionEventsListener listener;
    LogOutputinterface log;
    WifiP2pDevice thisDevice;
    Context ctx;

    public ServerThread(Socket socket, int role,
            IfWiFiP2pConnectionEventsListener listener, LogOutputinterface log,
            WifiP2pDevice thisDevice, Context ctx) {
        this.socket = socket;
        this.role = role;
        this.log = log;
        this.listener = listener;
        this.thisDevice = thisDevice;
        this.ctx = ctx;
        new SafeThread(this).start();
    }

    @Override
    public void safeRun() {
        log.debug(ExceptionUtil.currentMethodName());
        if (role == Const.ROLE_SEND) {
            new SendRoleThread(socket, listener, log, thisDevice, ctx);
        }
        else {
            new ReceiveRoleThread(socket, listener, log, ctx);
        }

    }

}

