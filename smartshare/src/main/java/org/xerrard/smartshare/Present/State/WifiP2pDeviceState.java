package org.xerrard.smartshare.Present.State;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;

public enum WifiP2pDeviceState implements IBaseState {
	AVAILABLE(WifiP2pDevice.AVAILABLE, "Available"),
	INVITED(WifiP2pDevice.INVITED, "Invited"),
	CONNECTED(WifiP2pDevice.CONNECTED, "Connected"),
	FAILED(WifiP2pDevice.FAILED, "Failed"),
	UNAVAILABLE(WifiP2pDevice. UNAVAILABLE, "Unavailable"),
	UNKNOWN(UNDEFINED_VALUE, "Unknown");
	
	private int value;
	private String displayString;

	private WifiP2pDeviceState(int value, String displayString) {
		this.value = value;
		this.displayString = displayString;
	}

	public int getValue() {
		return value;
	}

	public String getDisplayString() {
		return displayString;
	}
	
	public static IBaseState parse(int v) {
		IBaseState ret = UNKNOWN;
		
		for (WifiP2pDeviceState ds : WifiP2pDeviceState.values()) {
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
	
	public static IBaseState retriveFromIntent(Intent i) {
		 return i == null? UNKNOWN :
				parse(i.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED));
	}
}