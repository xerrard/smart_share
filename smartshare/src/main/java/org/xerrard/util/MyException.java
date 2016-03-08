package org.xerrard.util;

public class MyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5541412920953885052L;

	public MyException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MyException(RuntimeException ex) {
		super(ex.getMessage(), ex.getCause());
	}

	public MyException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public MyException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public MyException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	public MyException(MyException ex) {
		super(ex.getMessage(), ex.getCause());
	}
}
