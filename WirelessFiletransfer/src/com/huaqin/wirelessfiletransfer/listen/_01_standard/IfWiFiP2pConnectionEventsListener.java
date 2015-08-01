package com.huaqin.wirelessfiletransfer.listen._01_standard;

import java.io.File;

import org.xerrard.util.SafeThread.SafeRunnable;

public interface IfWiFiP2pConnectionEventsListener extends IfWifiP2pListener {

    /**
     * <p>
     * Description:网络socket连接成功
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onNetworkConnected();

    /**
     * <p>
     * Description:发送Request完毕
     * <p>
     * 
     * @date:2015年4月4日
     * @param reqcode
     */
    public void onSendRequeset(int reqcode);

    /**
     * <p>
     * Description:收到SEND_REQUEST
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onReceiveRequest(String SenderName,SafeRunnable receiveThread);

    /**
     * <p>
     * Description:发送Const.FILE_ACCEPT_CONFIRM完毕
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onSendAcceptConfirm();

    /**
     * <p>
     * Description:接收到Const.FILE_ACCEPT_CONFIRM
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onReceiveAcceptConfirm();

    /**
     * <p>
     * Description:发送Const.FILE_REFUSE_CONFIR完毕
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onSendRefuseConfirm();

    /**
     * <p>
     * Description:接收到Const.FILE_REFUSE_CONFIRM
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onReceiveRefuseConfirm();

    /**
     * <p>
     * Description:发送单个文件完毕
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onSendFile(String filename,long filesize);
    
    /**
     * <p>
     * Description:发送文件完毕
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onSendFiles();

    /**
     * <p>
     * Description:发送到nowpercent%的文件碎片
     * <p>
     * 
     * @date:2015年4月4日
     * @param nowpercent
     */
    public void onSendFilePart(String filename, long filesize,int nowpercent);

    /**
     * <p>
     * Description:file这个文件接收到nowpercent%的文件碎片
     * <p>
     * 
     * @date:2015年4月4日
     * @param file
     * @param nowpercent
     */
    public void onReceiveFilePart(String filename,long filesize, int nowpercent);


    /**
     * <p>
     * Description:file这个文件接收完毕
     * <p>
     * 
     * @date:2015年4月4日
     * @param file
     */
    public void onReveiveFile(File file);

    /**
     * <p>
     * Description:最后一个文件接收完毕
     * <p>
     * 
     * @date:2015年4月4日
     * @param file
     */
    public void onReveiveFiles(File file);

    /**
     * <p>
     * Description:已发送ack
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onSendAck();

    /**
     * <p>
     * Description:收到ack
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onReceiveAck();

    /**
     * <p>
     * Description:已发送Whole_ack
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onSendWholeAck();

    /**
     * <p>
     * Description:收到Whole_ack
     * <p>
     * 
     * @date:2015年4月4日
     */
    public void onReceiveWholeAck();

}

