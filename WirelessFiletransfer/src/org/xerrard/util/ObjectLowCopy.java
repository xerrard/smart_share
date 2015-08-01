package org.xerrard.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

public class ObjectLowCopy {
	public static void copy(Object from, Object to , Field[] attributes) {
		try {
			if (from != null 
					&& from != null 
					&& from.getClass().isInstance(to) 
					&& attributes != null) {
				
				for (Field attrib : attributes) {
					if (attrib != null && attrib.getDeclaringClass().equals(from.getClass()) ) {
						attrib.setAccessible(true);
						attrib.set(to, attrib.get(from));
					}
				}
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public static void copy(Object from, Object to , String[] attributes) {
		copy(from, to, Arrays.asList(attributes));
	}

	public static void copy(Object from, Object to , Collection<String> attributes) {
		
		try {
			if (from != null && from != null &&  from.getClass().isInstance(to) && attributes != null) {
				Class<?> cls = from.getClass();
				Field[] arr = new Field[attributes.size()];
				int index = 0;
				
				for (String attrib : attributes) {
					arr[index++] = ReflectUtil.getFieldWithoutError(cls, attrib);
				}
				
				copy(from, to, arr);
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
}
