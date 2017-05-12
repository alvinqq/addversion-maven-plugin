package cn.com.glsx.maven.plugin.addversion.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
	
	public static Method getMethod(Class<?> clazz, String getOrSet , String fieldName){
		try {
			if("set".equals(getOrSet)){
				return clazz.getMethod(methodName(getOrSet, fieldName), String.class);
			}else if("get".equals(getOrSet)){
				return clazz.getMethod(methodName(getOrSet, fieldName));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Object invoke(Object obj, String getOrSet, String fieldName, Object... args){
		Method method = getMethod(obj.getClass(), getOrSet, fieldName);
		if(method != null){
			try {
				return method.invoke(obj, args);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static <T> T invoke(Class<T> clazz, Object obj, String getOrSet, String fieldName, Object... args){
		Object t = invoke(obj, getOrSet, fieldName, args);
		if(t != null){
			return clazz.cast(t);
		}
		return null;
	}
	
	private static String methodName(String getOrSet, String filedName){
		String methodName = getOrSet + filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
		return methodName;
	}
}
