package com.huaqin.wirelessfiletransfer.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.SafeThread;
import org.xerrard.util.SafeThread.SafeRunnable;
import org.xerrard.util.Util;

import android.content.Context;
import android.os.Environment;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWiFiP2pConnectionEventsListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;

public class Receive extends SafeRunnable {

    private Socket socket;
    private IfWiFiP2pConnectionEventsListener listener;
    public static boolean isstoped = false;
    LogOutputinterface log;
    public int runningstate = Const.RECEIVE_THREAD_STATUS_RUNNING;
   
    
    public Receive(Socket socket, IfWiFiP2pConnectionEventsListener listener,
            LogOutputinterface log, Context ctx) {
        this.socket = socket;
        this.listener = listener;
        this.log = log;

        new SafeThread(this).start();
    }

    @Override
    public void safeRun() {
        log.debug(ExceptionUtil.currentMethodName());
        isstoped = false; //置状态
        try {
            InputStream inputStream = socket.getInputStream();
            FileOutputStream outputStream = null;
            File f = null;
            boolean is_request = true;
            int sendDevicenamebytes = 0;
            String sendDevicename;

            long read = 0; // 当前读到的数据长度
            long filesize = 0; // 从reqcode中读到的文件长度
            int filenamebytes = 0; // filename
            String filename = null; // filename
            int files = 0; // 文件个数
            int readfiles = 0; // 当前接收到了第几个文件
            byte[] buffer = new byte[1024];

            int nowpercent = 1;
            int nowReceivePercent = 0;

            while (!isstoped) {

                int len = inputStream.read(buffer);
                if (is_request) {
                    int code = (int) Util.getLong(buffer);
                    // 收到是否接收请求
                    if (code == Const.SEND_REQUEST) {

                        sendDevicenamebytes = (int) Util.getLong(buffer, 8);
                        sendDevicename = Util.Bytes2String(buffer, 16,
                                16 + sendDevicenamebytes - 1);

                        listener.onReceiveRequest(sendDevicename, this); // 告诉UI，准备弹出询问框

                        runningstate = Const.RECEIVE_THREAD_STATUS_SUSPEND;
                        while (runningstate == Const.RECEIVE_THREAD_STATUS_SUSPEND)
                            ; // 如果外部发来信号，接收线程结束
                        if (runningstate == Const.RECEIVE_THREAD_STATUS_STOP) {
                            runningstate = Const.RECEIVE_THREAD_STATUS_RUNNING;
                            break;
                        }
                    }
                    // 收到确认接收
                    else if (code == Const.FILE_ACCEPT_CONFIRM) {
                        listener.onReceiveAcceptConfirm(); // 收到确认信息后，会发送文件

                    }
                    else if (code == Const.FILE_REFUSE_CONFIRM) {
                        listener.onReceiveRefuseConfirm();
                        break;
                    }

                    else if (code == Const.SEND_ACK) {
                        listener.onReceiveAck();
                    }

                    else if (code == Const.SEND_WHOLE_ACK) {
                        listener.onReceiveWholeAck();
                        break; // 收到 FILE_REFUSE_CONFIRM SEND_WHOLE_ACK
                               // ，退出receive进程
                    }
                    else if (code == Const.SEND_STREAM) {

                        is_request = false;

                        filesize = Util.getLong(buffer, 8);
                        files = (int) Util.getLong(buffer, 16);
                        filenamebytes = (int) Util.getLong(buffer, 24);
                        filename = Util.Bytes2String(buffer, 32,
                                32 + filenamebytes - 1);

                        /*
                         * f = new
                         * File(Environment.getExternalStorageDirectory() +"/" +
                         * Const.STORE_PATH + "/" + filename);
                         */
                        f = new File(Environment.getExternalStorageDirectory()
                                .getPath() + "/" + Const.STORE_PATH + "/",
                                filename);

                        int i = 0;
                        while (f.exists()) {

                            /*
                             * f = new File(
                             * Environment.getExternalStorageDirectory() + "/" +
                             * Const.STORE_PATH + "/" + "(" + ++i + ")" +
                             * filename);
                             */
                            f = new File(Environment
                                    .getExternalStorageDirectory().getPath()
                                    + "/" + Const.STORE_PATH + "/", "(" + ++i
                                    + ")" + filename);
                        }
                        File dirs = new File(f.getParent());
                        if (!dirs.exists())
                            dirs.mkdirs();
                        f.createNewFile();
                        outputStream = new FileOutputStream(f);
                    }
                }
                else {
                    read = read + len;

                    nowpercent = (int) (read / filesize * 100);
                    if (nowReceivePercent != nowpercent) {
                        nowReceivePercent = nowpercent;
                        log.debug(
                                ExceptionUtil.currentMethodName()
                                        + "  read = %d  len = %d   filesize =%d   nowpercent=%d",
                                read, len, filesize, nowpercent);
                        listener.onReceiveFilePart(filename, filesize,
                                nowpercent);
                    }

                    outputStream.write(buffer, 0, len);
                    if (read == filesize) {
                        listener.onReceiveFilePart(filename, filesize, 100); // 提示当前文件已经发送到100%
                        nowpercent = 0; // 初始化nowpercent，为下一个文件的接受做准备
                        readfiles++; // 准备接受下个文件
                        is_request = true; // 下个文件stream依然又request包头
                        read = 0; // 初始化read ，为下一个文件的接收做准备
                        filesize = 0; // 初始化filesize ，为下一个文件的接收做准备
                        outputStream.flush();
                        outputStream.close();
                        if (readfiles == files) { // 如果已经发送到最后一个文件
                            files = 0;
                            readfiles = 0; // 初始化files readfiles,为下一个文件的接收做准备
                            listener.onReveiveFiles(f);// 最后一个文件接收的通知
                            break;
                        }
                        listener.onReveiveFile(f);// 前面每个文件接收的通知

                    }
                }
            }
            isstoped = false; //线程关闭前，置状态

        }
        catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            listener.onException(e);
        }

    }

}

