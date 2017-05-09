package cn.com.glsx.maven.plugin.addversion.element;

import java.io.Serializable;
import cn.com.glsx.maven.plugin.addversion.annotation.Element;

/**
 * @Title: LinkTag.java
 * @Description:
 * @author Alvin.zengqi  
 * @date 2017年4月17日 下午8:30:29
 * @version V1.0  
 * @Company: Didihu.com.cn
 * @Copyright Copyright (c) 2015
 */
@SuppressWarnings("serial")
@Element("link")
public class LinkTag implements Serializable{
	
	@Element("rel")
	private String rel;
	
	@Element("type")
	private String type;
	
	@Element("href")
	private String href;

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	@Override
	public String toString() {
		return "LinkTag [rel=" + rel + ", type=" + type + ", href=" + href + "]";
	}
	
}
