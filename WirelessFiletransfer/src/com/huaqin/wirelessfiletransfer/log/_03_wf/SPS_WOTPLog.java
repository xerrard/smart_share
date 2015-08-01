package com.huaqin.wirelessfiletransfer.log._03_wf;

import org.xerrard.util.DateUtil;
import org.xerrard.util.ExceptionUtil;

import com.huaqin.wirelessfiletransfer.log._02_base.AbsWOTPLogOutput;

import android.content.Context;
import android.util.Log;

public class SPS_WOTPLog extends AbsWOTPLogOutput {

    public SPS_WOTPLog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void customLog(String tag, String message, Throwable t) {

        String line = String.format("[%s] %s %s %s\r\n",
                DateUtil.toDefaultFmtString(DateUtil.getNow()), tag, message,
                t == null ? "" : ExceptionUtil.getExcetpionTrace(t));

        Log.i("xerrard", line);
    }

}

