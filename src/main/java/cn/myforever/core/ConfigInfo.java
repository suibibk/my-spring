package cn.myforever.core;

import java.util.List;

/**
 * 配置文件信息，配置文件中的信息读取出来放到这里
 * @author forever
 *
 */
public class ConfigInfo {
	private String tagName;
	private String id;//map中的key,根据此key来找对象
	private String clazz;//类全名
	private List<String> propertys;//属性
	
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public List<String> getPropertys() {
		return propertys;
	}
	public void setPropertys(List<String> propertys) {
		this.propertys = propertys;
	}
	@Override
	public String toString() {
		return "ConfigInfo [tagName=" + tagName + ", id=" + id + ", clazz=" + clazz + ", propertys=" + propertys + "]";
	}
	
}
