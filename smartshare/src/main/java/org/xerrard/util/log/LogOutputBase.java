package org.xerrard.util.log;

import android.content.Context;


public class LogOutputBase extends AbsWOTPLogOutput {

	public LogOutputBase(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void customLog(String tag, String message, Throwable t) {
		// TODO Auto-generated method stub
		System.out.println(message);
		if (t != null) {
			t.printStackTrace(System.err);
		}
	} 

}
