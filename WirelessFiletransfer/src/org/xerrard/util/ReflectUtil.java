package org.xerrard.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReflectUtil {
	public static class Invoker {
		private Method m;
		private Object receiver;
		private Object[] args;
		
		public Invoker(String methodName, String className, Object receiver, Class<?>[] argsType, Object[] args) {
			this(getMethodWithNoError(className, methodName, argsType), receiver, args);
		}
		
		
		public Invoker(Method m, Object receiver, Object[] args) {
			super();
			this.m = m;
			this.receiver = receiver;
			this.args = args;
		}
		 
		public void invok() throws Throwable {
			// TODO Auto-generated method stub
			if (m != null && receiver != null) {
				m.invoke(receiver, args);
			}
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return String.format("%s (%s) ", receiver.getClass().getName(), m.toString()); 
		}
		
		public String header() {
			return String.format("%s.%s", receiver.getClass().getName() , m.getName());
		}
	}




	public static Class<?> getClassWithNoError(String className) {
		Class<?> ret  = null;
		try {
			ret = Class.forName(className);
		} catch (Exception ex) {
			// keep empty!
		}
		return ret;
	}


	public static Method getMethodWithNoError(String className, String methodName, Class<?>[] argsType) {
		Method ret  = null;
		try {
			Class<?> cls = getClassWithNoError(className);
			if (cls != null) {
				ret = cls.getMethod(methodName, argsType);
			}
			
		} catch (Exception ex) {
			// keep empty!
		}
		return ret;
	}

	public static Method getMethodWithNoError(Class<?> cls, String methodName, Class<?>[] argsType) {
		return cls == null? null : getMethodWithNoError(cls.getName(), methodName, argsType);
		
	}

	  
	public static DeclareObjectBean packageADeclareObject(Object o) {
		return DeclareObjectBean.packageADeclareObject(o);
	}
	
	static Class<?>[] BasicType  = new Class<?>[] {
		Character.class,
		Boolean.class,
		Integer.class,
		Long.class,
		Short.class,
		Byte.class,
		String.class,
		Double.class,
		Float.class
		
	};
	
	private static HashMap<Class<?>, Class<?>> BoxTypeMap = null;
	
	static  {
		BoxTypeMap = new HashMap<Class<?>, Class<?>>();
		BoxTypeMap.put(char.class, Character.class);
		BoxTypeMap.put(boolean.class, Boolean.class);
		BoxTypeMap.put(int.class, Integer.class);
		BoxTypeMap.put(long.class, Long.class);
		BoxTypeMap.put(short.class, Short.class);
		BoxTypeMap.put(byte.class, Byte.class);
		BoxTypeMap.put(double.class, Double.class);
		BoxTypeMap.put(float.class, Float.class);
		
	}
	
	public static Class<?> getBoxType(Class<?> cls) {
		Class<?> ret = null;
		if (cls != null && cls.isPrimitive()) {
			ret  = BoxTypeMap.get(cls);
		}
		return ret;
	}
	
	public static boolean isBasicType(Class<?> cls) {
		boolean ret = false;
		if (cls != null) {
			if (cls.isPrimitive()) {
				ret = true;
			} else {
				for (Class<?> item : BasicType) {
					if (cls.equals(item)) {
						ret = true;
						break;
					}
				}
			}
		}
		return ret;
	}
	
	public static Field getDeclaredFieldContainSuperClass(Class<?> cls, String name) {
		Field ret = null;
		if (cls != null && !Object.class.equals(cls)) {
			try {ret = cls.getDeclaredField(name);} catch (NoSuchFieldException e) {}
			if (ret == null) {
				ret = getDeclaredFieldContainSuperClass(cls.getSuperclass(), name);
			}
		}
		return ret;
	}




	public static List<Method> getMethods(Class<?> cls, String methodName) {
		List<Method> ret =  null;
		// TODO Auto-generated method stub
		
		if (cls != null && methodName != null) {
			ret = new ArrayList<Method>();
			for (Method m : cls.getMethods()) {
				if (m.getName().equals(methodName)) {
					ret.add(m);
				}
			}
		}
			
		return ret ;
	}


	public static Field getFieldWithoutError(Class<?> cls, String attrib) {
		// TODO Auto-generated method stub
		Field f = null;
		
		try {
			if (cls != null) {
				try {f = cls.getField(attrib);} catch (Throwable t){}
				if (f == null) {
					try {f = cls.getDeclaredField(attrib);} catch (Throwable t){}
				}
				if (f == null) {
					f = getFieldWithoutError(cls.getSuperclass(), attrib);
				}
			}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
		}
		
		return f;
	}

}

