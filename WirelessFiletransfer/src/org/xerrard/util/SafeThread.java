package org.xerrard.util;

import java.lang.reflect.Field;

public class SafeThread extends Thread {


	private static UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
		
		@Override
		public void uncaughtException(Thread arg0, Throwable arg1) {
			// TODO Auto-generated method stub
			System.out.println(arg0.getName() + " Occurred uncaught exception ");
			arg1.printStackTrace();
		}
	};
	
	public static final void registerCommonExceptionHandler(UncaughtExceptionHandler h) {
		if (h != null) {
			handler = h;
		}
	}
	
	public static final void setCurrentThreadExceptionHandler(UncaughtExceptionHandler h) {
		Thread.currentThread().setUncaughtExceptionHandler(h);
	}
	

	public interface ResurrectionNotify {

		void resurrection(SafeThread resurrectedThread);
		ResurrectionNotify resNDefault = new ResurrectionNotify() {
			
			@Override
			public void resurrection(SafeThread resurrectedThread) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	
	private ResurrectionNotify resN;
	public void registerResurrection(ResurrectionNotify n) {
		resN = n;
	}
	
	public void registerResurrection() {
		registerResurrection(ResurrectionNotify.resNDefault);
	}
	
	public abstract static class SafeRunnable implements Runnable {

		private static final String PREFIX_RESURRECTION = "*RESURRECTION*";
		
		public abstract void safeRun() throws Throwable;
		
		@Override
		public final void run() {
			// TODO Auto-generated method stub
			
			try {
				if (Thread.currentThread().getUncaughtExceptionHandler() == null) {
					setCurrentThreadExceptionHandler(handler);
				}
				
				safeRun();
				
			} catch (Throwable e) {
				SafeThread t = (SafeThread)Thread.currentThread();
				 UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
				if (h != null) {
					h.uncaughtException(t, e);
				}
				
				if (t.resN != null) {
					SafeThread newClone = new SafeThread();
					
					Field[] attrs = new Field[] {
					ReflectUtil.getFieldWithoutError(Thread.class, "target"),
					ReflectUtil.getFieldWithoutError(Thread.class, "priority"),
					ReflectUtil.getFieldWithoutError(Thread.class, "name"),
					ReflectUtil.getFieldWithoutError(Thread.class, "daemon"),
					ReflectUtil.getFieldWithoutError(Thread.class, "uncaughtExceptionHandler"),
					ReflectUtil.getFieldWithoutError(SafeThread.class, "resN")};
					
					ObjectLowCopy.copy(t, newClone, attrs);
					
					if (!newClone.getName().startsWith(PREFIX_RESURRECTION)) {
						newClone.setName(SafeRunnable.PREFIX_RESURRECTION + newClone.getName());
					}
					newClone.start();
					t.resN.resurrection(newClone);
				}
			}
		}
	}
	
	
	
	public SafeThread() {
		super();
		// TODO Auto-generated constructor stub
	}

	public SafeThread(Runnable runnable, String threadName) {
		super(runnable, threadName);
		entry = runnable;
		// TODO Auto-generated constructor stub
	}

	public SafeThread(Runnable runnable) {
		super(runnable);
		entry = runnable;
		// TODO Auto-generated constructor stub
	}

	public SafeThread(String threadName) {
		super(threadName);
		// TODO Auto-generated constructor stub
	}

	public SafeThread(ThreadGroup group, Runnable runnable, String threadName,
			long stackSize) {
		super(group, runnable, threadName, stackSize);
		entry = runnable;
		// TODO Auto-generated constructor stub
	}

	public SafeThread(ThreadGroup group, Runnable runnable, String threadName) {
		super(group, runnable, threadName);
		entry = runnable;
		// TODO Auto-generated constructor stub
	}

	public SafeThread(ThreadGroup group, Runnable runnable) {
		super(group, runnable);
		entry = runnable;
		// TODO Auto-generated constructor stub
	}

	public SafeThread(ThreadGroup group, String threadName) {
		super(group, threadName);
		// TODO Auto-generated constructor stub
	}
	
	private Runnable entry;
	public Runnable getTaskEntry () {
		return entry;
	}

}
