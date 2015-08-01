package com.huaqin.wirelessfiletransfer.network;

import java.io.OutputStream;
import java.net.Socket;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;
import org.xerrard.util.Util;

import android.net.wifi.p2p.WifiP2pDevice;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;

//发送请求
public class SendRequest extends SafeRunnable {
    Socket socket;
    int reqcode = 0;
    IfWiFiP2pConnectionEventsListener listener;
    LogOutputinterface log;
    WifiP2pDevice thisDevice;

    public SendRequest(Socket socket, int reqcode,
            IfWiFiP2pConnectionEventsListener listener, LogOutputinterface log,
            WifiP2pDevice thisDevice) {
        this.socket = socket;
        this.reqcode = reqcode;
        this.listener = listener;
        this.log = log;
        this.thisDevice = thisDevice;
        SafeThread thread = new SafeThread(this);
        thread.start();
        try {
            thread.join(); // 必须当前的SendStream线程跑完，再继续下面的SendStream线程
        }
        catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            listener.onException(e);
        }
    }

    public void send() {
        log.debug(ExceptionUtil.currentMethodName());
        try {
            synchronized (socket) {
                OutputStream outputStream = socket.getOutputStream();
                byte[] buf;
                if (reqcode == Const.SEND_REQUEST) {
                    String thisname = thisDevice.deviceName;
                    if (thisname == null || thisname.equals("")) {
                        thisname = thisDevice.deviceAddress;
                    }
                    byte[] thisnamebytes = thisname.getBytes();
                    buf = new byte[1024];
                    Util.long2Byte(buf, reqcode);
                    Util.long2Byte(buf, thisnamebytes.length, 8);
                    for (int i = 16; i < 16 + thisnamebytes.length; i++)
                        buf[i] = thisnamebytes[i - 16];
                }
                else {
                    buf = new byte[8];
                    Util.long2Byte(buf, reqcode);
                }
                outputStream.write(buf);
                outputStream.flush();
            }
        }
        catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            listener.onException(e);
        }
    }

    @Override
    public void safeRun() {
        log.debug(ExceptionUtil.currentMethodName());
        send();
        listener.onSendRequeset(reqcode);
    }
}

