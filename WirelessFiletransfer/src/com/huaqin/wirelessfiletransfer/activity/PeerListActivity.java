package com.huaqin.wirelessfiletransfer.activity;

import org.xerrard.util.UrlUtil;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.huaqin.wirelessfiletransfer.MainApplication;
import com.huaqin.wirelessfiletransfer.R;
import com.huaqin.wirelessfiletransfer.listen._02_base.NoDoubleClickListener;
import com.huaqin.wirelessfiletransfer.log._01_standard.LogOutputinterface;
import com.huaqin.wirelessfiletransfer.model.Const;
import com.huaqin.wirelessfiletransfer.model.WiFiP2pServicePeer;
import com.huaqin.wirelessfiletransfer.model.WifiP2pSendFileInfo;
import com.huaqin.wirelessfiletransfer.service.MainService;
import com.huaqin.wirelessfiletransfer.ui.PeerListView;
import com.huaqin.wirelessfiletransfer.ui.RotateImageView;

public class PeerListActivity extends Activity {

    public MainApplication app;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    // WFWiFiP2pManager mWFWiFiP2pManager;
    PeerListView mPeerListView;
    RotateImageView mRotateView;
    ImageView mBtnSearch;
    public LogOutputinterface log;
    public static final int MIN_CLICK_DELAY_TIME = 20000;

    class ListUpdateBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Const.ACTION_UPDATE_DISPLAY_LIST)) {
                mPeerListView.invalidate();
            }
            else if (action.equals(Const.ACTION_UPDATE_STOP_SCAN_STATUS)) {
                app.searchStatus = Const.SEARCHSTATUS_STOP;
                mRotateView.stopRotate();
                if (mRotateView.getVisibility() == View.VISIBLE) {
                    mRotateView.setVisibility(View.GONE);
                }
                mBtnSearch.setVisibility(View.GONE);
            }
            else if (action.equals(Const.ACTION_UPDATE_START_SCAN_STATUS)) {

                // ToastUtil.showMessage(PeerListActivity.this,
                // getString(R.string.search),
                // Toast.LENGTH_LONG);
                Toast.makeText(PeerListActivity.this,
                        getString(R.string.search), Toast.LENGTH_SHORT).show();
                PeerListView.currentTouchIndex = -1;

                app.searchStatus = Const.SEARCHSTATUS_START;
                if (mRotateView.getVisibility() == View.GONE) {
                    mRotateView.setVisibility(View.VISIBLE);
                }
                mRotateView.startRotate();
                mBtnSearch.setVisibility(View.VISIBLE);

            }
            else if (action.equals(Const.ACTION_DEVICE_DISCONNECTED)) {
                Toast.makeText(PeerListActivity.this,
                        getString(R.string.connectfail), Toast.LENGTH_LONG)
                        .show();
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindActionBarView();
        app = (MainApplication) getApplication();
        log = app.log;
        if (!app.mServiceSetup) {
            startService(new Intent(this, MainService.class));
        }

        setContentView(R.layout.peerlist);
        mPeerListView = (PeerListView) findViewById(R.id.peerlist);
        mRotateView = (RotateImageView) findViewById(R.id.rotate_search);
        mBtnSearch = (ImageView) findViewById(R.id.btn_search);

        app = (MainApplication) getApplication();
        receiver = new ListUpdateBroadcastReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(Const.ACTION_UPDATE_DISPLAY_LIST);
        intentFilter.addAction(Const.ACTION_UPDATE_STOP_SCAN_STATUS);
        intentFilter.addAction(Const.ACTION_UPDATE_START_SCAN_STATUS);
        intentFilter.addAction(Const.ACTION_DEVICE_DISCONNECTED);
        registerReceiver(receiver, intentFilter);

        if (!app.urilist.isEmpty()) {
            app.urilist.clear();
            
        }
        if (Intent.ACTION_SEND_MULTIPLE == getIntent().getAction()) {
            app.urilist = getIntent().getParcelableArrayListExtra(
                    Intent.EXTRA_STREAM);

        }
        else {
            Uri uri = getIntent().getExtras()
                    .getParcelable(Intent.EXTRA_STREAM);
            CharSequence extra_text = getIntent().getCharSequenceExtra(
                    Intent.EXTRA_TEXT);
            if (extra_text != null) {
                final Uri fileUri = UrlUtil.creatFileForSharedContent(this,
                        extra_text);
                app.urilist.add(fileUri);
            }
            else {
                app.urilist.add(uri);
            }
        }

        for (Uri uri : app.urilist) {// 确保没有发送空文件
            String uriString = uri.toString();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mimetype = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
                    .getFileExtensionFromUrl(uriString));

            WifiP2pSendFileInfo wifiP2pSendFileInfo = WifiP2pSendFileInfo
                    .generateFileInfo(this, uri, mimetype);

            if ((wifiP2pSendFileInfo.mLength) == 0) {
                app.urilist.remove(uri);
            }

        }

        mPeerListView.setPeerList(app.mServicePeerList, app.statusMap);

        mBtnSearch.setOnClickListener(new NoDoubleClickListener(this,
                MIN_CLICK_DELAY_TIME) {
            @Override
            public void onNoDoubleClick(View v) {
                sendBroadcast(new Intent(Const.ACTION_RESTART_SCAN)); // 正常按显示seaching
                // ToastUtil.showMessage(PeerListActivity.this,
                // R.string.search_again, Toast.LENGTH_SHORT);
            }

            @Override
            public void onDoubleClick(View v) { // 10s内按第二次，显示limit
                // ToastUtil.showMessage(PeerListActivity.this,
                // getString(R.string.click_search_limit), Toast.LENGTH_LONG);
                Toast.makeText(context,
                        context.getText(R.string.click_search_limit),
                        Toast.LENGTH_SHORT).show();
            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (app.searchStatus == Const.SEARCHSTATUS_STOP) {
            mRotateView.stopRotate();
            if (mRotateView.getVisibility() == View.VISIBLE) {
                mRotateView.setVisibility(View.GONE);
            }
            mBtnSearch.setVisibility(View.GONE);
        }
        else if (app.searchStatus == Const.SEARCHSTATUS_START) {
            app.searchStatus = Const.SEARCHSTATUS_START;
            if (mRotateView.getVisibility() == View.GONE) {
                mRotateView.setVisibility(View.VISIBLE);
            }
            mRotateView.startRotate();
            mBtnSearch.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        clearStatusMap();
    }

    /**
     * <p>
     * Description:退出界面，清空
     * <p>
     * 
     * @date:2015年4月13日
     */
    private void clearStatusMap() {
        synchronized (app.mServicePeerList) {
            for (WiFiP2pServicePeer peer : app.mServicePeerList) {
                app.statusMap.put(peer, Const.NORMAL_STATUS);
            }
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent); // 获取参数
        if (!app.urilist.isEmpty()) {
            app.urilist.clear();
        }
        if (Intent.ACTION_SEND_MULTIPLE == intent.getAction()) {
            app.urilist = intent.getParcelableArrayListExtra(
                    Intent.EXTRA_STREAM);
        }
        else {

            
            Uri uri = intent.getExtras()
                    .getParcelable(Intent.EXTRA_STREAM);
            CharSequence extra_text = intent.getCharSequenceExtra(
                    Intent.EXTRA_TEXT);
            if (extra_text != null) {
                final Uri fileUri = UrlUtil.creatFileForSharedContent(this,
                        extra_text);
                app.urilist.add(fileUri);
            }
            else {
                app.urilist.add(uri);
            }
            

        }

        for (Uri uri : app.urilist) { // 确保没有发送空文件
            String uriString = uri.toString();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mimetype = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap
                    .getFileExtensionFromUrl(uriString));

            WifiP2pSendFileInfo wifiP2pSendFileInfo = WifiP2pSendFileInfo
                    .generateFileInfo(this, uri, mimetype);

            if ((wifiP2pSendFileInfo.mLength) == 0) {
                app.urilist.remove(uri);
            }

        }
    }

    /**
     * <p>
     * Description:向service发送广播，通知他连接device
     * <p>
     * 
     * @date:2015年5月13日
     * @param position
     */
    public void connectDevice(int position) {
        if (app.mServicePeerList.get(position).device.status == WifiP2pDevice.AVAILABLE) {
            Intent intent = new Intent(Const.ACTION_CONNECT_DEVICE);
            intent.putExtra(Const.ACTION_CONNECT_DEVICE_POSITION, position);
            sendBroadcast(intent);
        }
    }

    /**
     * <p>
     * Description:actionbar设置
     * <p>
     * 
     * @date:2014年9月25日
     */
    private void bindActionBarView() {
        ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(false);
            bar.setTitle(R.string.app_name);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
