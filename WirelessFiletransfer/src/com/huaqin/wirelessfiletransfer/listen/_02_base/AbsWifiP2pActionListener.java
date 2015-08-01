package com.huaqin.wirelessfiletransfer.listen._02_base;

import com.huaqin.wirelessfiletransfer.wifip2p.WifiP2pSolutionProvider;

import android.net.wifi.p2p.WifiP2pManager.ActionListener;

public abstract class AbsWifiP2pActionListener extends AbsWifiP2pListener
        implements ActionListener {

    protected String actionName;

    public AbsWifiP2pActionListener(WifiP2pSolutionProvider owner,
            String actionName) {
        // TODO Auto-generated constructor stub
        super(owner);
        this.actionName = actionName;
    }

    @Override
    final public void onFailure(int reason) {
        // TODO Auto-generated method stub
        onFailure(actionName, reason);
    }

    @Override
    final public void onSuccess() {
        // TODO Auto-generated method stub
        onSuccess(actionName);
    }

    public abstract void onFailure(String actionName, int reason);

    public abstract void onSuccess(String actionName);
}
