package com.huaqin.wirelessfiletransfer.network;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;

public class Client extends SafeRunnable {
    private Socket socket;
    private InetSocketAddress inetAddress;
    private IfWiFiP2pConnectionEventsListener listener;
    private int role;
    private Socket connectedSocket;
    LogOutputinterface log;
    WifiP2pDevice thisDevice;
    private Context ctx;

    public Client(InetSocketAddress inetAddress,
            IfWiFiP2pConnectionEventsListener listener, int role,
            LogOutputinterface log, WifiP2pDevice thisDevice, Context ctx) {
        this.inetAddress = inetAddress;
        this.listener = listener;
        this.role = role;
        this.log = log;
        this.ctx = ctx;
        this.thisDevice = thisDevice;
        socket = new Socket();
        new SafeThread(this).start();
    }

    public Socket getConnectedSocket() {
        return connectedSocket;
    }

    @Override
    public void safeRun() {
        log.debug(ExceptionUtil.currentMethodName());
        try {
            socket.connect(inetAddress, 50000);
            connectedSocket = socket;
            listener.onNetworkConnected();
            if (role == Const.ROLE_SEND) {
                new SendRoleThread(socket, listener, log, thisDevice, ctx);
            }
            else {
                new ReceiveRoleThread(socket, listener, log, ctx);

            }
        }
        catch (Throwable e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            listener.onException(e);
        }

    }

}
