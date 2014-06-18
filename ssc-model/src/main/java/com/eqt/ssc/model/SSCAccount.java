package com.eqt.ssc.model;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Wraps up all the info we could want on an account for monitoring purposes.
 * The only one we must have is accountId, all the others are optional.
 *
 */
public class SSCAccount {

	protected String accountId;
	protected String accessKey;
	protected String secretKey;
	protected String s3BucketName;
	protected String s3Path;
	protected String ctBucketName;
	protected String ctPath;
	
	//for a given key, say s3, or ct, or ssc, can store the last update.
	protected Map<String, Long> updateTimestampMap;
	
	protected Map<String,String> attributes;
	
	/**
	 * here for serialization
	 */
	public SSCAccount() {}
	
	public SSCAccount(String accountId) {
		this.accountId = accountId;
	}
	
	public SSCAccount(String accountId, String accessKey, String secretKey) {
		this.accountId = accountId;
		this.accessKey = accessKey;
		this.secretKey = secretKey;
	}

	public long lastUpdate(String item) {
		if(updateTimestampMap == null)
			updateTimestampMap = new HashMap<String, Long>();
		if(updateTimestampMap.get(item) == null)
			updateTimestampMap.put(item, 0l);
		return updateTimestampMap.get(item);
	}
	
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getS3BucketName() {
		return s3BucketName;
	}

	public void setS3BucketName(String s3BucketName) {
		this.s3BucketName = s3BucketName;
	}

	public String getS3Path() {
		return s3Path;
	}

	public void setS3Path(String s3Path) {
		this.s3Path = s3Path;
	}

	public String getCtBucketName() {
		return ctBucketName;
	}

	public void setCtBucketName(String ctBucketName) {
		this.ctBucketName = ctBucketName;
	}

	public String getCtPath() {
		return ctPath;
	}

	public void setCtPath(String ctPath) {
		this.ctPath = ctPath;
	}

	public Map<String, Long> getUpdateTimestampMap() {
		if(updateTimestampMap == null)
			updateTimestampMap = new HashMap<String, Long>();
		return updateTimestampMap;
	}

	public void setUpdateTimestampMap(Map<String, Long> updateTimestampMap) {
		this.updateTimestampMap = updateTimestampMap;
	}

	public Map<String, String> getAttributes() {
		if(attributes == null)
			attributes = new TreeMap<String,String>();
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(String key, String value) {
		if(attributes == null)
			attributes = new TreeMap<String,String>();
		this.attributes.put(key, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSCAccount other = (SSCAccount) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		return true;
	}
	
	public String toString() {
		return accountId + " " + accessKey + " " + updateTimestampMap.toString() + " " + attributes.toString();
	}
}
