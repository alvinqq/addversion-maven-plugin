package cn.com.glsx.maven.plugin.addversion.element;

import java.io.Serializable;
import cn.com.glsx.maven.plugin.addversion.annotation.Element;

/**
 * @author Alvin.zengqi  
 * @version V1.0  
 */
@SuppressWarnings("serial")
@Element("script")
public class ScriptTag implements Serializable{
	
	@Element("type")
	private String type;
	
	@Element("src")
	private String src;
	
	@Element("charset")
	private String charset;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	@Override
	public String toString() {
		return "ScriptTag [type=" + type + ", src=" + src + ", charset=" + charset + "]";
	}

}
