package com.huaqin.wirelessfiletransfer.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;
import org.xerrard.util.Util;

import android.util.Log;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;

public class SendStream extends SafeRunnable {
    Socket socket;
    InputStream inputStream;
    String filename;
    long filelength = 0;
    int files = 1;
    IfWiFiP2pConnectionEventsListener listener;
    LogOutputinterface log;

    public SafeThread thread;

    public SendStream(Socket socket, InputStream inputStream, String filename,long filelength,
            int files, IfWiFiP2pConnectionEventsListener listener,
            LogOutputinterface log) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.filename = filename;
        this.filelength =  filelength;
        this.files = files;
        this.listener = listener;
        this.log = log;
        thread = new SafeThread(this);
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

    public void send() throws Throwable{
        try {
            synchronized (socket) {
                OutputStream outputStream = socket.getOutputStream();
                copyFile(inputStream, outputStream);
                listener.onSendFile(filename, filelength);
            }
        }
        catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            listener.onException(e);
        }
    }

    public boolean copyFile(InputStream inputStream, OutputStream out)
            throws Throwable {
        int len = 0;
        long writtensize = 0;
        long filesize = 0;
        byte buf[] = new byte[1024];
        byte[] filenamebytes = filename.getBytes();
        int nowpercent = 1;
        int nowSendPercent = 0;
        filesize = inputStream.available();
       filesize = filelength;

        Util.long2Byte(buf, Const.SEND_STREAM);
        Util.long2Byte(buf, filelength, 8);
        Util.long2Byte(buf, files, 16);
        Util.long2Byte(buf, filenamebytes.length, 24);
        for (int i = 32; i < 32 + filenamebytes.length; i++)
            buf[i] = filenamebytes[i - 32];
        out.write(buf);
        long time = System.currentTimeMillis();
        while ((len = inputStream.read(buf)) != -1) {
            out.write(buf, 0, len);
            writtensize = writtensize + len;
            nowpercent = (int) ((writtensize / (float) filesize) * 100);
            if (nowSendPercent != nowpercent) {
                log.debug(
                        ExceptionUtil.currentMethodName()
                                + "writtensize = %d  len = %d   size =%d   nowpercent=%d",
                        writtensize, len, filesize, nowpercent);
                nowSendPercent = nowpercent;
                listener.onSendFilePart(filename,filesize,nowpercent);
            }
        }
        listener.onSendFilePart(filename,filesize,100);
        Log.e("wangjiatime", " " + (System.currentTimeMillis() - time));
        inputStream.close();
        out.flush();
        return true;
    }

    @Override
    public void safeRun() throws Throwable {
        log.debug(ExceptionUtil.currentMethodName());
        send();

    }
}

