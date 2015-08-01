package com.huaqin.wirelessfiletransfer.valuableLabel._03_wf;

import com.huaqin.wirelessfiletransfer.valuableLabel._01_standard.BaseValuableLabel;

import android.content.Intent;
import android.net.wifi.WifiManager;

public enum WifiState implements BaseValuableLabel {
	DISABLED(WifiManager.WIFI_STATE_DISABLED, "Disabled"),
	DISABLING(WifiManager.WIFI_STATE_DISABLING, "Disabling"),
	ENABLED(WifiManager.WIFI_STATE_ENABLED, "Enabled"),
	ENABLING(WifiManager.WIFI_STATE_ENABLING, "Enabling"),
	UNKNOWN(WifiManager.WIFI_STATE_UNKNOWN, "Unknown");
	
	private int value;
	private String displayString;

	private WifiState(int value, String displayString) {
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
		
		for (WifiState ds : WifiState.values()) {
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

	public static BaseValuableLabel retriveFromIntent(Intent i) {
		 return i == null? UNKNOWN :
				parse(i.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED));
	}	
}