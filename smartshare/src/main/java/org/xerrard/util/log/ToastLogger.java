package org.xerrard.util.log;

import org.xerrard.util.BindWindow;
import org.xerrard.util.ExceptionUtil;


import android.content.Context;

public class ToastLogger extends AbsWOTPLogOutput {

    public ToastLogger(Context context) {
        // TODO Auto-generated constructor stub
        super(context);
    }

    @Override
    public void customLog(String tag, String message, Throwable t) {

        try {
            BindWindow bw = (BindWindow) context;
            if (bw != null) {
                bw.invokeUIDelegate("displayToast", message + "\r\n"
                        + (t == null ? "" : ExceptionUtil.getExceptionTrace(t)));
            }
        }
        catch (Throwable ex) {
            System.err.println("can not output log.");
            ex.printStackTrace();
        }
    }
};
