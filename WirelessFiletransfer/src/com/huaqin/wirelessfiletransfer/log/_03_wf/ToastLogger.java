package com.huaqin.wirelessfiletransfer.log._03_wf;

import org.xerrard.util.BindWindow;
import org.xerrard.util.ExceptionUtil;

import com.huaqin.wirelessfiletransfer.log._02_base.AbsWOTPLogOutput;

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
                        + (t == null ? "" : ExceptionUtil.getExcetpionTrace(t)));
            }
        }
        catch (Throwable ex) {
            System.err.println("can not output log.");
            ex.printStackTrace();
        }
    }
};
