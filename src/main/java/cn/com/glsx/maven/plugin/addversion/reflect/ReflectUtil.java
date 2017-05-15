package cn.com.glsx.maven.plugin.addversion.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cn.com.glsx.maven.plugin.addversion.annotation.Element;

/**
 * @author Alvin.zengqi  
 * @version V1.0  
 */
public class ReflectUtil {
	
	public static String getTagName(Class<?> clazz){
		Element element = clazz.getAnnotation(Element.class);
		return element.value();
	}
	
	public static List<String> getAttrName(Class<?> clazz){
		List<String> elements = new ArrayList<String>();
		Field[] fields = clazz.getDeclaredFields();
		if(fields != null && fields.length > 0){
			for(Field f:fields){
				Element element = f.getAnnotation(Element.class);
				elements.add(element.value());
			}
		}
		return elements;
	}
	
	public static Object invoke(Object obj, String methodName, Object... args) {
		Class<?>[] parameterTypes = null;
		if (args != null && args.length > 0) {
			parameterTypes = new Class[args.length];
			for (int i=0; i< args.length; i++) {
				parameterTypes[i] = getObjectType(args[i]);
			}
		}
		try {
			Method method = obj.getClass().getMethod(methodName, parameterTypes);
			if (method != null) { 
				return method.invoke(obj, args); 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static <T> T invoke(Class<T> clazz, Object obj, String methodName, Object... args){
		Object t = invoke(obj, methodName, args);
		if(t != null){
			return clazz.cast(t);
		}
		return null;
	}
	
	public static String methodName(String getOrSet, String filedName){
		String methodName = getOrSet + filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
		return methodName;
	}
	
	private static Class<?> getObjectType(Object obj){
		if (obj instanceof Integer) {
			return Integer.class;
		} else if (obj instanceof String) {
			return String.class;
		} else if (obj instanceof Double) {
			return Double.class;
		} else if (obj instanceof Float) {
			return Float.class;
		} else if (obj instanceof Long) {
			return Long.class;
		} else if (obj instanceof Boolean) {
			return Boolean.class;
		} else if (obj instanceof Date) { 
			return Date.class; 
		} else if (obj instanceof Short) {
			return Short.class;
		}
		return Object.class;
	}
}
