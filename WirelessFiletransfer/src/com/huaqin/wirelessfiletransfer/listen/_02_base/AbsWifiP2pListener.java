package com.huaqin.wirelessfiletransfer.listen._02_base;

import com.huaqin.wirelessfiletransfer.listen._01_standard.IfWifiP2pListener;
import com.huaqin.wirelessfiletransfer.wifip2p.WifiP2pSolutionProvider;


public abstract class AbsWifiP2pListener implements IfWifiP2pListener {

	protected WifiP2pSolutionProvider owner;
	
	public AbsWifiP2pListener(WifiP2pSolutionProvider owner) {
		// TODO Auto-generated constructor stub
		this.owner = owner;
	}
}
