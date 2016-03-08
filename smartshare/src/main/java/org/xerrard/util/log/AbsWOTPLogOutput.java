package org.xerrard.util.log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.xerrard.util.ExceptionUtil;
import org.xerrard.util.log.ILogOutput;


import android.content.Context;
import android.util.Log;

public abstract class AbsWOTPLogOutput implements ILogOutput {
	
	protected Context context;
	public AbsWOTPLogOutput(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	
	public abstract void customLog(String tag, String message, Throwable t) ;

	private long bitset =
			ILogOutput.LOG_LEVEL.ERROR.level()
			| LOG_LEVEL.DEBUG.level() 
			| LOG_LEVEL.INFOR.level()
			| LOG_LEVEL.WARN.level();
	
	/**
	 * 不输出 commonLog 自己产生的log，否则陷入死递归
	 */
	public void commonLog(String levelName, String tag, Throwable t, String format, Object...args) {
		
		String stackTrace = ExceptionUtil.getExceptionTrace(t);
		
		try {
			
			String DONT_SAVE_OWNER_LOG = this.getClass().getName() + "." + "commonLog";
			if (stackTrace != null && stackTrace.contains(DONT_SAVE_OWNER_LOG)) {
				throw new Exception("Dead recusive....");
			}
			
			String message = String.format(format, args);
			Method m = Log.class.getMethod(levelName, String.class, String.class, Throwable.class);
			m.invoke(Log.class, tag, message, t);
			
			if (isLogOn(LOG_LEVEL.parse(levelName))) {
				customLog(tag, message, t);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("AbsWOTPLogOutput", "Fata error", e);
		}
	}

	public void err(String format, Object...args) {
	
		String sender =  ExceptionUtil.dumapStackTrace(ExceptionUtil.getElement(new Exception(), 1));
		commonLog("e", sender, null, format, args);
	}
	
	public void err(Throwable t, String format, Object...args) {
		String sender =  ExceptionUtil.dumapStackTrace(ExceptionUtil.getElement(new Exception(), 1));
		commonLog("e", sender, t, format, args);
	}
	
	public void debug(String format, Object...args) {
		String sender =  ExceptionUtil.dumapStackTrace(ExceptionUtil.getElement(new Exception(), 1));
		commonLog("d", sender, null, format, args);
	}
	
	public void warn(String format, Object...args) {
		String sender =  ExceptionUtil.dumapStackTrace(ExceptionUtil.getElement(new Exception(), 1));
		commonLog("w", sender, null, format, args);
	}
	
	public void info(String format, Object...args) {
		String sender =  ExceptionUtil.dumapStackTrace(ExceptionUtil.getElement(new Exception(), 1));
		commonLog("i", sender, null, format, args);
	}

	@Override
	public void setLogOn(LOG_LEVEL level, boolean value) {
		// TODO Auto-generated method stub
		if (level != null) {
			bitset = value? (bitset | level.level()) : (bitset & ~level.level());
		}
	}

	@Override
	public boolean isLogOn(LOG_LEVEL level) {
		// TODO Auto-generated method stub
		return level == null? false : (bitset & level.level()) != 0;  
	}

	@Override
	public Set<LOG_LEVEL> logOnSet() {
		// TODO Auto-generated method stub
		 Set<LOG_LEVEL> ret = new HashSet<ILogOutput.LOG_LEVEL>();
		 for (LOG_LEVEL l : LOG_LEVEL.values()) {
			 if (isLogOn(l)) {
				 ret.add(l);
			 }
		 }
		return ret;
	}

}
