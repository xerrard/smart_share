package org.xerrard.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class DeclareObjectBean {

	
	public static  DeclareObjectBean packageADeclareObject(Object o) {
		return new DeclareObjectBean(o);
	}
	
	private Object value;

	private String clazz;
	 

	private String componentType;
	private List<String> genericElemType;
	
	public String getComponentType() {
		return componentType;
	}

	public List<String> getGenericElemType() {
		return genericElemType;
	}

	public DeclareObjectBean() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("rawtypes")
	private void  changeClass() {
		if (value != null) {
			Class<?> cls = value.getClass();
			clazz = cls.getName();
			
			if (cls.isArray()) {
				componentType = cls.getComponentType().getName();
			} else if (Iterable.class.isInstance(value)){
				Iterable i = (Iterable)value;
				Iterator it = i.iterator();
				if (it.hasNext()) {
					genericElemType = new ArrayList<String>();
					genericElemType.add((it.next().getClass().getName()));
				}
			}
		}
	}

	public DeclareObjectBean(Object object) {
		super();
		this.value = object;
		changeClass();
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object object) {
		this.value = object;
		changeClass();
	}

	public String getClazz() {
		return clazz;
	}
	


}
