package org.xerrard.util.log;

import java.io.File;
import java.io.FileWriter;

import org.xerrard.util.DateUtil;
import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.MyException;

import android.content.Context;
import android.os.Environment;

public class FileLogger extends AbsWOTPLogOutput {

    protected FileWriter fw = null;
    protected ToastLogger toastLogger = null;

    public FileLogger(Context context) {
        super(context);

        try {
            /*
             * File logFile = new File(String.format("%s/%s", Environment
             * .getExternalStorageDirectory().getAbsolutePath() +
             * "/"+Const.STORE_PATH + "/", "normallog.txt"));
             */

            File logFile = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/" + context.getPackageName() + "/log/", "normallog.txt");

            File dirs = new File(logFile.getParent());
            if (!dirs.exists()) {
                dirs.mkdirs();
            }

            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            fw = new FileWriter(logFile, false);
            toastLogger = new ToastLogger(context);

        }
        catch (MyException ex) {
            throw ex;
        }
        catch (Throwable e) {
            // TODO Auto-generated catch block
            throw new MyException(e);
        }
    }

    @Override
    public void customLog(String tag, String message, Throwable t) {
        // TODO Auto-generated method stub

        try {
            if (fw != null) {
                String line = String.format("[%s] %s %s %s\r\n",
                        DateUtil.toDefaultFmtString(DateUtil.getNow()), tag,
                        message,
                        t == null ? "" : ExceptionUtil.getExceptionTrace(t));
                fw.write(line);
                fw.flush();
                // toastLogger.customLog(tag, message, t);
            }
        }
        catch (MyException ex) {
            throw ex;
        }
        catch (Throwable e) {
            // TODO Auto-generated catch block
            throw new MyException(e);
        }
    }

}
