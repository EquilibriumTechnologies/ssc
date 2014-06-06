package com.eqt.ssc.model;

public class SSCRecord {
	public SSCKey key;
	public Object value;
	
	public SSCRecord() {}
	
	public SSCRecord(SSCKey key, Object value) {
		this.key = key;
		this.value = value;
	}
}
