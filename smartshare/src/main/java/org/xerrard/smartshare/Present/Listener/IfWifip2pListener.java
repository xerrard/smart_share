package org.xerrard.smartshare.Present.Listener;


/**
 * 所有的listener都必须实现的功能，即异常处理
 */
public interface IfWifip2pListener {
	String WIFI_P2P_ACTION_LISTENER_METHOD_ONEXCEPTION = "onException";

	void onException(Throwable t);
}
