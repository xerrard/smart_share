package com.huaqin.wirelessfiletransfer.network;

import java.net.Socket;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;

import android.content.Context;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;

public class ReceiveRoleThread extends SafeRunnable {
    private Socket socket;
    private IfWiFiP2pConnectionEventsListener listener;
    LogOutputinterface log;
    private Context ctx;

    public ReceiveRoleThread(Socket socket,
            IfWiFiP2pConnectionEventsListener listener, LogOutputinterface log,
            Context ctx) {
        this.socket = socket;
        this.listener = listener;
        this.log = log;
        this.ctx = ctx;
        new SafeThread(this).start();
    }

    @Override
    public void safeRun() {
        log.debug(ExceptionUtil.currentMethodName());
        new Receive(socket, listener, log,ctx);

    }

}

