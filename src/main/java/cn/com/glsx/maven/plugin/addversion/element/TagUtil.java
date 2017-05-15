package cn.com.glsx.maven.plugin.addversion.element;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import cn.com.glsx.maven.plugin.addversion.reflect.ReflectUtil;

/**
 * @author Alvin.zengqi  
 * @version V1.0  
 */
public class TagUtil {
	
	public static <T> T tagToBean(String tagStr, Class<T> clazz){
		try {
			Document doc = Jsoup.parse(tagStr);
			String tag = ReflectUtil.getTagName(clazz);
			Elements elements = doc.select(tag);
			if(elements != null && !elements.isEmpty()){
				Element element = elements.first();
				Attributes attributes = element.attributes();
					T t = clazz.newInstance();
					BaseTag baseTag = null;
					if(t instanceof BaseTag){
						baseTag = (BaseTag) t;
					}
					if(attributes != null && attributes.size() > 0){
						Iterator<Attribute> attrs = attributes.iterator();
						List<String> attrName = ReflectUtil.getAttrName(clazz);
						while(attrs.hasNext()){
							Attribute attr = attrs.next();
							if(attrName.contains(attr.getKey())){
								String methodName = ReflectUtil.methodName("set", attr.getKey());
								ReflectUtil.invoke(t, methodName, attr.getValue());
							}else{
								// save custom attr
								baseTag.getCustomAttr().put(attr.getKey(), attr.getValue());
							}
						}
					}
					return t;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String beanToTag(Object bean){
		try{
			String tagName = ReflectUtil.getTagName(bean.getClass());
			Element element = new Element(Tag.valueOf(tagName), "");
			List<String> attrName = ReflectUtil.getAttrName(bean.getClass());
			if(attrName != null && !attrName.isEmpty()){
				for(String attr:attrName){
					String methodName = ReflectUtil.methodName("get", attr);
					String attrValue = ReflectUtil.invoke(String.class, bean, methodName);
					if(!StringUtil.isBlank(attrValue)){
						element.attr(attr, URLEncoder.encode(attrValue, "UTF-8"));
					}
				}
				if(bean instanceof BaseTag){
					BaseTag baseTag = (BaseTag) bean;
					Map<String, String> customAttr = baseTag.getCustomAttr();
					if(customAttr.size() > 0){
						for(Entry<String, String> entry:customAttr.entrySet()){
							element.attr(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
						}
					}
				}
				if(bean instanceof BaseTag){
					BaseTag baseTag = (BaseTag) bean;
					Map<String, String> customAttr = baseTag.getCustomAttr();
					if(customAttr.size() > 0){
						for(Entry<String, String> entry:customAttr.entrySet()){
							element.attr(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
						}
					}
				}
			}
			return URLDecoder.decode(element.toString(), "UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String [] args){
		/*ScriptTag script = new ScriptTag();
		script.setId("test");
		script.setSrc("http://www.baidu.com");
		script.setType("javascript/text");
		script.getCustomAttr().put("data-jquery", "jquery");
		script.getCustomAttr().put("data-java", "java");
		System.out.println(TagUtil.beanToTag(script));*/
		String script = "<script id=\"test\" type=\"javascript/text\" src=\"http://www.baidu.com\" data-jquery=\"jquery\" data-java=\"java\"></script>";
		ScriptTag scriptTag = TagUtil.tagToBean(script, ScriptTag.class);
		System.out.println(scriptTag.toString());
		scriptTag.setAsync("true");
		scriptTag.getCustomAttr().put("data-java", "javascript");
		System.out.println(TagUtil.beanToTag(scriptTag));
	}
	
}
