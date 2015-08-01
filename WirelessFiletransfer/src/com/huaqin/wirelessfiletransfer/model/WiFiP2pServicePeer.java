package com.huaqin.wirelessfiletransfer.model;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * A structure to hold service information.
 */
public class WiFiP2pServicePeer {
	public WifiP2pDevice device;
	public String instanceName = null;
	public String serviceRegistrationType = null;
	//public boolean isinvited = false;
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof WiFiP2pServicePeer) {
			WiFiP2pServicePeer p = (WiFiP2pServicePeer) obj;
			if (instanceName.equals(p.instanceName)
					&& device.deviceName.equals(p.device.deviceName)) {
				return true;
			}
		}
		return false;
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.device.deviceName.hashCode();
	}

	public static String getDeviceStatus(int statusCode) {
		switch (statusCode) {
		case WifiP2pDevice.CONNECTED:
			return "Connected";
		case WifiP2pDevice.INVITED:
			return "Invited";
		case WifiP2pDevice.FAILED:
			return "Failed";
		case WifiP2pDevice.AVAILABLE:
			return "Available";
		case WifiP2pDevice.UNAVAILABLE:
			return "Unavailable";
		default:
			return "Unknown";

 

		}
	}
}

