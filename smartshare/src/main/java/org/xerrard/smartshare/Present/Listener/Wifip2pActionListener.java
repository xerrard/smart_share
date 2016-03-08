package org.xerrard.smartshare.Present.Listener;

import org.xerrard.smartshare.Present.Wifip2pSolutionProvider;
import org.xerrard.util.log.ILogOutput;

/**
 * 类描述：
 * 创建人：xuqiang
 * 创建时间：16-3-8 下午4:55
 * 修改人：xuqiang
 * 修改时间：16-3-8 下午4:55
 * 修改备注：
 */
public class Wifip2pActionListener extends AbsWifip2pActionListener {
    private ILogOutput log;


    public Wifip2pActionListener(Wifip2pSolutionProvider owner, String actionName, ILogOutput log) {
        super(owner, actionName);
        this.log = log;
    }

    @Override
    public void onFailure(String actionName, int reason) {
        if (log != null) {

            String msg = String.format("%s fail. reason=%s", actionName,
                    reason);
            log.warn(msg);
        }
    }

    @Override
    public void onSuccess(String actionName) {
        if (log != null) {
            log.debug("%s success", actionName);
        }
    }

    @Override
    public void onException(Throwable t) {
        if (log != null) {
            String msg = String.format("%s fail.", actionName);

            log.err(msg);
        }
    }
}
