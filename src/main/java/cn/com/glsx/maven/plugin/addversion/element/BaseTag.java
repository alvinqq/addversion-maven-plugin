package cn.com.glsx.maven.plugin.addversion.element;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alvin.zengqi  
 * @version V1.0  
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
