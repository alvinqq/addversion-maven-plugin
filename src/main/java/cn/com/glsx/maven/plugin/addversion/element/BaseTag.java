package cn.com.glsx.maven.plugin.addversion.element;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Title: BaseTag.java
 * @Description:
 * @author Alvin.zengqi  
 * @date 2017年5月15日 下午3:19:56
 * @version V1.0  
 * @Company: Didihu.com.cn
 * @Copyright Copyright (c) 2015
 */
@SuppressWarnings("serial")
public class BaseTag implements Serializable{
	
	private Map<String, String> customAttr = new HashMap<String, String>();
	
	public Map<String, String> getCustomAttr(){
		return customAttr;
	}
	
	public int getCustomAttrSize(){
		return customAttr.size();
	}
	
}
