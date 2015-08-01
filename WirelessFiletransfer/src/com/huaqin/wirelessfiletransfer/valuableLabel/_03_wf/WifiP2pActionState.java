package com.huaqin.wirelessfiletransfer.valuableLabel._03_wf;

import com.huaqin.wirelessfiletransfer.valuableLabel._01_standard.BaseValuableLabel;

import android.net.wifi.p2p.WifiP2pManager;

/**
 * 
 * P2P_UNSUPPORTED, ERROR or BUSY

 * 
 * @author yinshengge
 *
 */
public enum WifiP2pActionState implements BaseValuableLabel {
	P2P_UNSUPPORTED(WifiP2pManager.P2P_UNSUPPORTED, "P2p Unsupported"),
	ERROR(WifiP2pManager.ERROR, "Error"),
	BUSY(WifiP2pManager.BUSY, "Busy"),
	NO_SERVICE_REQUESTS(WifiP2pManager.NO_SERVICE_REQUESTS, "No service requests"),
	UNKNOWN(UNDEFINED_VALUE, "UNKNOWN")
	;
	
	
	private int value;
	private String displayString;

	private WifiP2pActionState(int value, String displayString) {
		this.value = value;
		this.displayString = displayString;
	}

	@Override
	public int getValue() {
		return value;
	}

	@Override
	public String getDisplayString() {
		return displayString;
	}
	
	public static BaseValuableLabel parse(int v) {
		BaseValuableLabel ret = UNKNOWN;
		
		for (WifiP2pActionState ds : WifiP2pActionState.values()) {
			if (ds.value == v) {
				ret = ds;
				break;
			}
		}
		
		
		return ret;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getDisplayString();
	}
}