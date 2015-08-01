package com.huaqin.wirelessfiletransfer.listen._01_standard;


public interface IfWifiP2pListener {
	String WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION = "onException";

	public abstract void onException(Throwable t);
}
