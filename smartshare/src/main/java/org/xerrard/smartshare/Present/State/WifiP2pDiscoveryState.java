package org.xerrard.smartshare.Present.State;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;

public enum WifiP2pDiscoveryState implements IBaseState {
	STARTED(WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED, "Discovery started"),
	STOPPED(WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED, "Discovery stoped"),
	UNKNOWN(UNDEFINED_VALUE, "Unknown");

	private int value;
	private String displayString;

	private WifiP2pDiscoveryState(int value, String displayString) {
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
	
	public static IBaseState parse(int v) {
		IBaseState ret = UNKNOWN;
		
		for (WifiP2pDiscoveryState ds : WifiP2pDiscoveryState.values()) {
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
				parse(i.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED));
	}
}