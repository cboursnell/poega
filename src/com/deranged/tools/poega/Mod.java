package com.deranged.tools.poega;

import java.io.Serializable;

public class Mod implements Serializable {

	private String desc;
	private String name;
	private float value;
	private String prefix;
	private String postfix;
	private int keystoneId; // can be set if you want, but not essential
	
	public Mod(String desc, float value, String prefix, String postfix) {
		this.desc = desc;
		this.value = value;
		this.prefix = prefix;
		this.postfix = postfix;
		this.keystoneId = 0;
		this.name = "";
	}
	
	public Mod(String desc, String name, float value, String prefix, String postfix) {
		this.name = name;
		this.desc = desc;
		this.value = value;
		this.prefix = prefix;
		this.postfix = postfix;
		this.keystoneId = 0;
	}
	
	public Mod dup() {
		return new Mod(desc, name, value, prefix, postfix);
	}
	
	
	public String getDesc() {
		return desc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getValue() {
		return value;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getPostfix() {
		return postfix;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setValue(float value) {
		this.value = value;
	}
	
	public void addValue(float value) {
		this.value += value;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}
	
	public int getKeystoneId() {
		return keystoneId;
	}

	public void setKeystoneId(int keystoneId) {
		this.keystoneId = keystoneId;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (value > 0) {
			sb.append(prefix);
			sb.append(value);
			sb.append(postfix);
		}
		sb.append(desc);
		return sb.toString();
	}

	public String printElements() {
		StringBuilder sb = new StringBuilder();
		sb.append("\""+prefix+"\"");
		sb.append(", ");
		sb.append("\""+value+"\"");
		sb.append(", ");
		sb.append("\""+postfix+"\"");
		sb.append(", ");
		sb.append("\""+desc+"\"");
		return sb.toString();
	}
	
//	public String toStringWithoutHash() {
//		StringBuilder sb = new StringBuilder();
//		for(int i = 0 ; i < list.length; i++) {
//			if(list[i].matches("#")) {
//				//sb.append(prefix+value+postfix+" ");
//			} else {
//				if (i<list.length-1) {
//					sb.append(list[i]+" ");
//				} else {
//					sb.append(list[i]);
//				}
//			}
//		}
//		return sb.toString();
//	}
	
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		for(int i = 0 ; i < list.length; i++) {
//			if(list[i].matches("#")) {
//				sb.append(prefix+value+postfix+" ");
//			} else {
//				if (i<list.length-1) {
//					sb.append(list[i]+" ");
//				} else {
//					sb.append(list[i]);
//				}
//			}
//		}
//		return sb.toString();
//	}

//	public String toStringWithHash() {
//		StringBuilder sb = new StringBuilder();
//		for(int i = 0 ; i < list.length; i++) {
//			if (i<list.length-1) {
//				sb.append(list[i]+" ");
//			} else {
//				sb.append(list[i]);
//			}
//		}
//		return sb.toString();
//	}
}
