package com.huaqin.wirelessfiletransfer.ui;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.huaqin.wirelessfiletransfer.R;
import com.huaqin.wirelessfiletransfer.activity.PeerListActivity;
import com.huaqin.wirelessfiletransfer.model.WiFiP2pServicePeer;

public class PeerListView extends View {

    private int[] color = new int[] { Color.RED, Color.YELLOW, Color.BLUE,
            Color.GREEN, Color.CYAN, Color.MAGENTA };
    static final String SEND = "SEND";
    static final String REJECTED = "REJECTED";
    static final String INITIAL_STATUS = "";
    private static final int TEXT_SIZE = 20;

    private PeerListActivity mPeerListActivity;
    private List<WiFiP2pServicePeer> mServicePeerList;
    private Map<WiFiP2pServicePeer, String> statusMap;
    private Paint imagePaint;
    private Paint pointPaint;
    private Paint textPaint;
    public static int currentTouchIndex = -1;

    public PeerListView(Context context) {
        super(context);

        mPeerListActivity = (PeerListActivity) context;
        initCanvas();
    }

    public PeerListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPeerListActivity = (PeerListActivity) context;
        initCanvas();
    }

    public PeerListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPeerListActivity = (PeerListActivity) context;
        initCanvas();
    }

    public void setPeerList(List<WiFiP2pServicePeer> mServicePeerList,
            Map<WiFiP2pServicePeer, String> statusMap) {
        this.mServicePeerList = mServicePeerList;
        this.statusMap = statusMap;
    }

    protected void initCanvas() {
        imagePaint = new Paint();
        imagePaint.setStrokeWidth(2);
        imagePaint.setStyle(Paint.Style.STROKE);
        pointPaint = new Paint();
        pointPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setTextSize(TEXT_SIZE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (PeerListBackgroundView.isStarted) { // 保证背景先画，画完背景，list才能获得算出坐标
            synchronized (mServicePeerList) {
                if (mServicePeerList != null) {
                    for (int i = 0; i < mServicePeerList.size(); i++) {
                        /**
                         * 画小圆 画小点
                         */
                        imagePaint.setColor(color[i]);
                        pointPaint.setColor(color[i]);
                        if (currentTouchIndex == i) {
                            canvas.drawCircle(
                                    PeerListBackgroundView.circlex[i],
                                    PeerListBackgroundView.circley[i],
                                    PeerListBackgroundView.largeradius,
                                    imagePaint);
                            canvas.drawCircle(
                                    PeerListBackgroundView.circlex[i],
                                    PeerListBackgroundView.circley[i],
                                    PeerListBackgroundView.largeradius / 4,
                                    pointPaint);
                        }
                        else {

                            canvas.drawCircle(
                                    PeerListBackgroundView.circlex[i],
                                    PeerListBackgroundView.circley[i],
                                    PeerListBackgroundView.smallradius,
                                    imagePaint);

                            canvas.drawCircle(
                                    PeerListBackgroundView.circlex[i],
                                    PeerListBackgroundView.circley[i],
                                    PeerListBackgroundView.smallradius / 4,
                                    pointPaint);
                        }

                        /**
                         * 画devicename
                         */

                        if (mServicePeerList.get(i).device.status == WifiP2pDevice.AVAILABLE) {
                            textPaint.setColor(Color.WHITE);
                        }
                        else {
                            textPaint.setColor(Color.GRAY);
                        }
                        String name = mServicePeerList.get(i).device.deviceName;
                        if (name == null || name.equals("")) {
                            name = mServicePeerList.get(i).device.deviceAddress;
                        }
                        int textleft = PeerListBackgroundView.circlex[i]
                                - (int) textPaint.measureText(name) / 2;
                        canvas.drawText(name, textleft,
                                PeerListBackgroundView.circley[i] + 2
                                        * TEXT_SIZE, textPaint);

                        /**
                         * 画当前的状态，sent refuse connecting
                         */
                        String nowstatus = statusMap.get(mServicePeerList
                                .get(i));
                        if (currentTouchIndex == i) { // 正在连接的显示connecting
                            nowstatus = mPeerListActivity.getResources()
                                    .getString(R.string.connecting);
                        }
                        if (nowstatus != null) { // sent/refused/connecting
                            textleft = PeerListBackgroundView.circlex[i]
                                    - (int) textPaint.measureText(nowstatus) / 2;
                            canvas.drawText(nowstatus, textleft,
                                    PeerListBackgroundView.circley[i] + 4
                                            * TEXT_SIZE, textPaint);
                        }
                    }
                }
            }
        }
    }

    /**
     * \
     * <p>
     * Description:判断点击的点是不是第i个device
     * <p>
     * 
     * @date:2015年5月15日
     * @param x
     * @param y
     * @param i
     * @return
     */
    private boolean inTouchArea(float x, float y, int i) {
        return Math.pow(x - PeerListBackgroundView.circlex[i], 2)
                + Math.pow(y - PeerListBackgroundView.circley[i], 2) < Math
                    .pow(PeerListBackgroundView.smallradius * 8, 2);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            synchronized (mServicePeerList) {
                for (int i = 0; i < mServicePeerList.size(); i++) {
                    if (inTouchArea(x, y, i)) {
                        currentTouchIndex = i;
                        mPeerListActivity.connectDevice(i);
                        mPeerListActivity.app.ConnectedId = i;
                        break;
                    }
                }
            }
        }

        return super.onTouchEvent(event);

    }
}

