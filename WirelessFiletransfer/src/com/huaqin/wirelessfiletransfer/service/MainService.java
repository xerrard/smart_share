package com.huaqin.wirelessfiletransfer.service;

import java.io.File;
import java.util.Set;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.FileSizeUtil;
import org.xerrard.util.SafeThread.SafeRunnable;
import org.xerrard.util.VarArgsUtil;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.huaqin.wirelessfiletransfer.R;
import com.huaqin.wirelessfiletransfer.activity.WirelessTransferHistory;
import com.huaqin.wirelessfiletransfer.model.Const;
import com.huaqin.wirelessfiletransfer.model.WiFiP2pServicePeer;

public class MainService extends WFService {

    private AlertDialog.Builder alertDialogbuild;
    private AlertDialog alertDialog;
    private AlertDialog.Builder exceptionDialogbuild;
    private AlertDialog exceptionDialog;

    private ProgressDialog pLoadingDialog;
    private ProgressDialog pSendDialog;
    private ProgressDialog pReceiveDialog;
    private NotificationManager mNotificationManager = null;
    private Notification mSendNotification = null;
    private Notification mReceiveNotification = null;
    private Notification.Builder mSendNotificationBuild;
    private Notification.Builder mReceiveNotificationBuild;
    private Notification.Builder mStartServiceNotificationBuild;
    private Notification mStartServiceNotification;
    private Notification.Builder mTransferingNotificationBuild;
    private Notification mTransferingNotification; 
    
    BroadcastReceiver receiver;
    private boolean isheadprogress = true;
    
    
    class MainServiceBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Const.ACTION_RESTART_SCAN)) { 
                Toast.makeText(context, getString(R.string.search), Toast.LENGTH_SHORT).show();
                discoverService();
                
            }
            else if (action.equals(Const.ACTION_DISCONNECT_DEVICE)) {

            }
            else if (action.equals(Const.ACTION_CONNECT_DEVICE)) {
                int position = intent.getIntExtra(
                        Const.ACTION_CONNECT_DEVICE_POSITION, 0);
                connectDevice(app.mServicePeerList.get(position).device);
            }
            else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)||action.equals(Intent.ACTION_MEDIA_UNMOUNTED)){
                sendBroadcast(new Intent(Const.ACTION_SERVCIE_STOPED));
                stopSelf(); //热插拔关闭
            }
            else if (action.equals(Const.ACTION_TRANSFERING)) { 
                if(pSendDialog.getProgress()>0){
                    pSendDialog.show();
                }else if (pReceiveDialog.getProgress()>0){
                    pReceiveDialog.show();
                }
            }

            
            
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLoadingProgressDialog();
        initAlertdialog();
        initSendProgressDialog();
        initReceiveProgressDialog();
        initNotification();
        initExceptionDialog();
        receiver = new MainServiceBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Const.ACTION_RESTART_SCAN);
        intentFilter.addAction(Const.ACTION_DISCONNECT_DEVICE);
        intentFilter.addAction(Const.ACTION_CONNECT_DEVICE);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Const.ACTION_TRANSFERING);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(Const.NOTIFICATION_SERVICE_START, mStartServiceNotification);
        sendBroadcast(new Intent(Const.ACTION_SERVCIE_STARTED));
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent(Const.ACTION_SERVCIE_STOPED));
        stopForeground(true);
        unregisterReceiver(receiver);
        mNotificationManager.cancelAll();
        System.exit(0);
    }

    private void initExceptionDialog() {
        exceptionDialogbuild = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exceptiontitle))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exceptionDialog.dismiss();
                    }
                });

        exceptionDialog = exceptionDialogbuild.create();
        exceptionDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        exceptionDialog.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
    }

    @Override
    public void displayException(String str) {
        super.displayException(str);

        if (pSendDialog != null && pSendDialog.isShowing()) {
            pSendDialog.dismiss();
        }
        if (pReceiveDialog != null && pReceiveDialog.isShowing()) {
            pReceiveDialog.dismiss();
        }

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        if (pLoadingDialog != null && pLoadingDialog.isShowing()) {
            pLoadingDialog.dismiss();
        }

        exceptionDialog.setMessage(str);
        exceptionDialog.show();

        isheadprogress = true; //初始化progress
    }

    private void initSendProgressDialog() {
        pSendDialog = new ProgressDialog(this, R.style.CustomDialog);
        pSendDialog.setTitle(getString(R.string.sending_files));
        pSendDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pSendDialog.setIndeterminate(false);
        pSendDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        pSendDialog.setOnCancelListener(new OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface dialog) {
                
                mNotificationManager.notify(Const.NOTIFICATION_TRANSFERING,
                        mTransferingNotification);
            }
        });
    }

    private void initReceiveProgressDialog() {
        pReceiveDialog = new ProgressDialog(this, R.style.CustomDialog);
        pReceiveDialog.setTitle(getString(R.string.receiving_files));
        pReceiveDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pReceiveDialog.setIndeterminate(false);
        pReceiveDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        pReceiveDialog.setOnCancelListener(new OnCancelListener() {
            
            @Override
            public void onCancel(DialogInterface dialog) {
                
                mNotificationManager.notify(Const.NOTIFICATION_TRANSFERING,
                        mTransferingNotification);
            }
        });
    }

    private void initLoadingProgressDialog() {
        pLoadingDialog = new ProgressDialog(this, R.style.CustomDialog);
        pLoadingDialog.setTitle(getString(R.string.loading));
        pLoadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //pLoadingDialog.setIndeterminate(false);
        pLoadingDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    private void initNotification() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mSendNotificationBuild = new Notification.Builder(this)
                .setContentTitle(getString(R.string.sending_files))
                .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                .setTicker(getString(R.string.sending_files));

        mReceiveNotificationBuild = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(getString(R.string.receiving_files))
                .setTicker(getString(R.string.receiving_files));

        mStartServiceNotificationBuild = new Notification.Builder(this)
                .setContentTitle(getString(R.string.service_running))
                .setContentText(getString(R.string.restart_service))
                .setSmallIcon(R.drawable.ic_small_icon)
                .setTicker(getString(R.string.service_running));


        
        if (!app.mNotifacionList.isEmpty()) {
            app.mNotifacionList.clear();
        }
        app.mNotifacionList.add(mSendNotificationBuild);
        app.mNotifacionList.add(mReceiveNotificationBuild);
        
        mTransferingNotificationBuild = new Notification.Builder(this)
        .setContentTitle(getString(R.string.transfering))
        .setContentText(getString(R.string.transfering_process))
        .setTicker(getString(R.string.transfering))
        .setSmallIcon(R.drawable.ic_small_icon);
        setNotifiacitonIntent();
        
        mTransferingNotification = mTransferingNotificationBuild.build();

        
        mStartServiceNotification = mStartServiceNotificationBuild.build();
        mNotificationManager.notify(Const.NOTIFICATION_SERVICE_START,
                mStartServiceNotification);
    }

    /**
     * <p>
     * Description:点击notification进入fileexploree,待实现
     * <p>
     * 
     * @date:2015年3月30日
     */
    private void setNotifiacitonIntent() {
        Intent receiveintent = new Intent(this, WirelessTransferHistory.class);
        receiveintent.putExtra("direction", Const.DIRECTION_INBOUND);
        receiveintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent receivePendingIntent = PendingIntent.getActivity(this,
                Const.NOTIFICATION_RECEIVE, receiveintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mReceiveNotificationBuild.setContentIntent(receivePendingIntent);

        Intent sendintent = new Intent(this, WirelessTransferHistory.class);
        sendintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        sendintent.putExtra("direction", Const.DIRECTION_OUTBOUND);
        PendingIntent sendPendingIntent = PendingIntent.getActivity(this,
                Const.NOTIFICATION_SEND, sendintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mSendNotificationBuild.setContentIntent(sendPendingIntent);

        Intent startintent = new Intent(Const.ACTION_RESTART_SCAN);
        PendingIntent startPendingIntent = PendingIntent.getBroadcast(this,
                Const.NOTIFICATION_SERVICE_START, startintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mStartServiceNotificationBuild.setContentIntent(startPendingIntent);

        Intent transferintent = new Intent(Const.ACTION_TRANSFERING);
        PendingIntent transferPendingIntent = PendingIntent.getBroadcast(this,
                Const.NOTIFICATION_TRANSFERING, transferintent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mTransferingNotificationBuild.setContentIntent(transferPendingIntent);
        
    }

    private void initAlertdialog() {

        alertDialogbuild = new AlertDialog.Builder(this, R.style.CustomDialog)
                .setTitle(getString(R.string.incoming_file_confirm_content))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(
                        getString(R.string.incoming_file_confirm_ok),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                pLoadingDialog.show();
                                mWFNetworkManager.acceptFileTransfer();
                            }
                        })
                .setNegativeButton(
                        getString(R.string.incoming_file_confirm_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                log.debug(ExceptionUtil.currentMethodName() + "NegativeButton");
                                mWFNetworkManager.refuseFileTransfer();
                            }
                        })
                 .setCancelable(false);
        /*
                .setOnCancelListener(new DialogInterface.OnCancelListener() { // 点击返回键和cancel功能一致

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                log.debug(ExceptionUtil.currentMethodName() + "Cancel");
                                refuseFileTransfer();
                            }
                        });
*/ //去掉cancel 
    }

    public void alertShow(Object... args) {
        String senderName = VarArgsUtil.getArgument(0, String.class, args);
        log.debug(ExceptionUtil.currentMethodName() + "senderName=%s\r\n",
                senderName);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.incoming_dialog, null);
        ((TextView) view.findViewById(R.id.from_content)).setText(senderName);
        alertDialogbuild.setView(view);
        alertDialog = alertDialogbuild.create();
        alertDialog.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.setCanceledOnTouchOutside(false); // 点击外面区域不会让dialog消失
        alertDialog.show();
    }

    @Override
    public void onWifip2pConnected(WifiP2pInfo info, WifiP2pDevice thisDevice) {
        super.onWifip2pConnected(info, thisDevice);
        Intent intent = new Intent(Const.ACTION_UPDATE_STOP_SCAN_STATUS);
        sendBroadcast(intent);
    }

    @Override
    public void onDisplayListUpdate(Set<WiFiP2pServicePeer> serviceDeviceSet) {
        super.onDisplayListUpdate(serviceDeviceSet);
        synchronized (app.mServicePeerList) {
            app.mServicePeerList.clear();
            if (!serviceDeviceSet.isEmpty()) {
                app.mServicePeerList.addAll(serviceDeviceSet);
            }
        }

        Intent intent = new Intent(Const.ACTION_UPDATE_DISPLAY_LIST);
        sendBroadcast(intent);
    }

    /**
     * 收到SEND_REQUEST，显示是否接受文件的dialog
     */
    @Override
    public void onReceiveRequest(String senderName, SafeRunnable receiveThread) {
        super.onReceiveRequest(senderName, receiveThread);
        invokeUIDelegate("alertShow", senderName);
    }

    /**
     * 接收到Const.FILE_REFUSE_CONFIRM，本机告知用户对方不愿意接收文件
     */
    @Override
    public void onReceiveRefuseConfirm() {
        invokeUIDelegate("notifyReceverRefused");
        super.onReceiveRefuseConfirm();
    }

    
    /**
     * 发送文件碎片，显示当前的发送进度
     */
    @Override
    public void onSendFilePart(String filename, long filesize, int nowpercent) {
        super.onSendFilePart(filename, filesize, nowpercent);
        invokeUIDelegate("showSendProgress", filename,
                FileSizeUtil.FormetFileSize(filesize),
                Integer.valueOf(nowpercent));
        if (nowpercent <= 1) {
            invokeUIDelegate("notifySending", 0);
        }
    }

    @Override
    public void onSendFile(String filename, long filesize) {
        super.onSendFile(filename, filesize);
        invokeUIDelegate("notifySending",
                app.mFileTransferSendHistorylist.size());
        sendBroadcast(new Intent(Const.ACTION_UPDATE_HISTORY)); // history更新
    }

    @Override
    public void onSendFiles() {
        super.onSendFiles();
        invokeUIDelegate("notifySent", app.mFileTransferSendHistorylist.size());
        sendBroadcast(new Intent(Const.ACTION_UPDATE_HISTORY)); // history更新

    }

    /**
     * <p>
     * Description:每个文件发送的通知
     * <p>
     * @date:2015年6月11日
     * @param args
     */
    public void notifySending(Object... args) { 
        log.debug(ExceptionUtil.currentMethodName());
        int files = VarArgsUtil.getArgument(0, Integer.class, args).intValue();
        mSendNotificationBuild.setContentTitle(
                getString(R.string.sending_files)).setTicker(
                getString(R.string.sending_files));

        String content = getString(R.string.noti_caption, files);
        if (files > 0) {
            mSendNotificationBuild.setContentText(content);
        }
        mSendNotification = mSendNotificationBuild.build();
        mNotificationManager.notify(Const.NOTIFICATION_SEND, mSendNotification);
    }

    public void notifySent(Object... args) {
        log.debug(ExceptionUtil.currentMethodName());
        int files = VarArgsUtil.getArgument(0, Integer.class, args).intValue();
        mSendNotificationBuild.setContentTitle(getString(R.string.sent_files))
                .setTicker(getString(R.string.sent_files));
        String content = getString(R.string.noti_caption, files);
        if (files > 0) {
            mSendNotificationBuild.setContentText(content);
        }
        mSendNotification = mSendNotificationBuild.build();
        mNotificationManager.notify(Const.NOTIFICATION_SEND, mSendNotification);
        

    }

    /**
     * 收到文件碎片，显示当前的接收进度
     */
    @Override
    public void onReceiveFilePart(String filename, long filesize, int nowpercent) {
        super.onReceiveFilePart(filename, filesize, nowpercent);
        invokeUIDelegate("showReceiveProgress", filename,
                FileSizeUtil.FormetFileSize(filesize),
                Integer.valueOf(nowpercent));
        if (nowpercent <= 1) {
            invokeUIDelegate("notifyReceiving", 0);
        }

    }

    /**
     * 收到单个文件
     */
    @Override
    public void onReveiveFile(File file) {

        super.onReveiveFile(file);
        invokeUIDelegate("notifyReceiving",
                app.mFileTransferReceiveHistorylist.size());
        sendBroadcast(new Intent(Const.ACTION_UPDATE_HISTORY)); // history更新
    }

    /**
     * 收到所有文件，更新notification
     */
    @Override
    public void onReveiveFiles(File file) {
        super.onReveiveFiles(file);
        invokeUIDelegate("notifyReceived",
                app.mFileTransferReceiveHistorylist.size());
        sendBroadcast(new Intent(Const.ACTION_UPDATE_HISTORY)); // history更新
    }

    /**
     * <p>
     * Description:文件接收的通知
     * <p>
     * 
     * @date:2015年4月4日
     * @param args
     */
    public void notifyReceiving(Object... args) {
        log.debug(ExceptionUtil.currentMethodName());
        int files = VarArgsUtil.getArgument(0, Integer.class, args).intValue();
        mReceiveNotificationBuild.setContentTitle(
                getString(R.string.receiving_files)).setTicker(
                getString(R.string.receiving_files));
        String content = getString(R.string.noti_caption, files);
        if (files > 0) {
            mReceiveNotificationBuild.setContentText(content);
        }
        mReceiveNotification = mReceiveNotificationBuild.build();
        mNotificationManager.notify(Const.NOTIFICATION_RECEIVE,
                mReceiveNotification);
    }

    /**
     * <p>
     * Description:文件已接收
     * <p>
     * 
     * @date:2015年4月4日
     * @param args
     */
    public void notifyReceived(Object... args) {
        log.debug(ExceptionUtil.currentMethodName());
        int files = VarArgsUtil.getArgument(0, Integer.class, args).intValue();
        mReceiveNotificationBuild.setContentTitle(
                getString(R.string.received_files)).setTicker(
                getString(R.string.received_files));
        String content = getString(R.string.noti_caption, files);
        if (files > 0) {
            mReceiveNotificationBuild.setContentText(content);
        }
        mReceiveNotification = mReceiveNotificationBuild.build();
        mNotificationManager.notify(Const.NOTIFICATION_RECEIVE,
                mReceiveNotification);
    }

    /**
     * <p>
     * Description:显示当前的发送进度
     * <p>
     * 
     * @date:2015年3月25日
     * @param args
     * @throws InterruptedException
     */
    public void showSendProgress(Object... args) throws InterruptedException {
        String filename = VarArgsUtil.getArgument(0, String.class, args);
        String filesize = VarArgsUtil.getArgument(1, String.class, args);
        int nowpercent = VarArgsUtil.getArgument(2, Integer.class, args)
                .intValue();
        log.debug(ExceptionUtil.currentMethodName() + "nowpercent is %d",
                nowpercent);
        
        pSendDialog.setMessage(filename + "    " + filesize);
        
        if(isheadprogress){
            if (!pSendDialog.isShowing()) {
                pSendDialog.show();
            }
            isheadprogress = false;
        }
        
        pSendDialog.setProgress(nowpercent);
        
        if (nowpercent >= 100) {
            pSendDialog.dismiss();
            mNotificationManager.cancel(Const.NOTIFICATION_TRANSFERING);
            isheadprogress = true;
        }
    }

    /**
     * <p>
     * Description:显示当前的接收进度
     * <p>
     * 
     * @date:2015年3月25日
     * @param args
     * @throws InterruptedException
     */
    public void showReceiveProgress(Object... args) throws InterruptedException {
        if (pLoadingDialog != null && pLoadingDialog.isShowing()) {
            pLoadingDialog.dismiss();
        }

        String filename = VarArgsUtil.getArgument(0, String.class, args);
        String filesize = VarArgsUtil.getArgument(1, String.class, args);
        int nowpercent = VarArgsUtil.getArgument(2, Integer.class, args)
                .intValue();
        log.debug(ExceptionUtil.currentMethodName() + "nowpercent is %d",
                nowpercent);
        pReceiveDialog.setMessage(filename + "    " + filesize);
        
        if(isheadprogress){
            if (!pReceiveDialog.isShowing()) {
                pReceiveDialog.show();
            }
            isheadprogress = false;
        }
        pReceiveDialog.setProgress(nowpercent);
        
        if (nowpercent >= 100) {
            pReceiveDialog.dismiss();
            mNotificationManager.cancel(Const.NOTIFICATION_TRANSFERING);
            isheadprogress = true;
        }
    }

    /**
     * <p>
     * Description:告知本机用户对方机不愿意接收文件
     * <p>
     * 
     * @date:2015年5月27日
     */
    public void notifyReceverRefused(Object... args) {
        Toast.makeText(this, getString(R.string.notice_refused, receiverName),
                Toast.LENGTH_LONG).show();
    }

}

