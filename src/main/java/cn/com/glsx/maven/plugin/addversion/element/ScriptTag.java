package cn.com.glsx.maven.plugin.addversion.element;

import java.io.Serializable;
import cn.com.glsx.maven.plugin.addversion.annotation.Element;

/**
 * @author Alvin.zengqi  
 * @version V1.0  
 */
@SuppressWarnings("serial")
@Element("script")
public class ScriptTag extends BaseTag implements Serializable{
	
	@Element("id")
	private String id;
	
	@Element("type")
	private String type;
	
	@Element("src")
	private String src;
	
	@Element("charset")
	private String charset;
	
	@Element("async")
	private String async;
	
	@Element("title")
	private String title;

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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAsync() {
		return async;
	}

	public void setAsync(String async) {
		this.async = async;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "ScriptTag [id=" + id + ", type=" + type + ", src=" + src + ", charset=" + charset + ", async=" + async + ", title=" + title + ", customAttr=" + getCustomAttr() + "]";
	}

}
