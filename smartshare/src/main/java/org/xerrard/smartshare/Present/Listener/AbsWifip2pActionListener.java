package org.xerrard.smartshare.Present.Listener;

import android.net.wifi.p2p.WifiP2pManager.ActionListener;

import org.xerrard.smartshare.Present.Wifip2pSolutionProvider;

/**
 * wifip2p的每一种操作都是异步操作，而操作是否成功就是看这个actionlistener
 */
public abstract class AbsWifip2pActionListener implements ActionListener{

    protected String actionName;
    protected Wifip2pSolutionProvider owner;

    public AbsWifip2pActionListener(Wifip2pSolutionProvider owner,
            String actionName) {
        // TODO Auto-generated constructor stub
        this.owner = owner;
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
