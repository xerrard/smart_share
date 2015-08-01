package com.huaqin.wirelessfiletransfer.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;

public class Server extends SafeRunnable {
    public ServerSocket serverSocket;
    public IfWiFiP2pConnectionEventsListener listener;
    public int role;
    private Socket connectedSocket;
    LogOutputinterface log;
    WifiP2pDevice thisDevice;
    private Context ctx;

    public Server(IfWiFiP2pConnectionEventsListener listener, int role,
            LogOutputinterface log, WifiP2pDevice thisDevice, Context ctx) {
        this.listener = listener;
        this.role = role;
        this.log = log;
        this.thisDevice = thisDevice;
        this.ctx = ctx;
        try {
            serverSocket = new ServerSocket(Const.SERVER_PORT);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            listener.onException(e);
        }
        new SafeThread(this).start();
    }

    public Socket getConnectedSocket() {
        return connectedSocket;
    }

    @Override
    public void safeRun() {
        log.debug(ExceptionUtil.currentMethodName());
        try {
            Socket socket = serverSocket.accept();
            connectedSocket = socket;
            listener.onNetworkConnected();
            // ClientSocketList.add(socket);
            new ServerThread(socket, role, listener, log, thisDevice, ctx);
        }
        catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            listener.onException(e);
        }

    }

}

