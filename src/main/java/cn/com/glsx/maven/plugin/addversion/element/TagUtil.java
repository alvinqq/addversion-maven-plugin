package cn.com.glsx.maven.plugin.addversion.element;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
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
					if(attributes != null && attributes.size() > 0){
						Iterator<Attribute> attrs = attributes.iterator();
						while(attrs.hasNext()){
							Attribute attr = attrs.next();
							ReflectUtil.invoke(t, "set", attr.getKey(), attr.getValue());
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
					String attrValue = ReflectUtil.invoke(String.class, bean, "get", attr);
					if(!StringUtil.isBlank(attrValue)){
						element.attr(attr, URLEncoder.encode(attrValue, "UTF-8"));
					}
				}
			}
			return URLDecoder.decode(element.toString(), "UTF-8");
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
}
