package com.eqt.ssc.model;

public class SSCKey {
	public String objName;
	public String methodName;
	public long requestTime;
	public String accountId;
	public String apiVersion;

	public SSCKey() {
	}

	public SSCKey(String objName, String methodName, String accountId, String apiVersion) {
		this(objName, methodName, accountId, apiVersion, System.currentTimeMillis());
	}

	public SSCKey(String objName, String methodName, String accountId, String apiVersion, long requestTime) {
		this.objName = objName;
		this.methodName = methodName;
		this.accountId = accountId;
		this.requestTime = requestTime;
		this.apiVersion = apiVersion;
	}

	public String toString() {
		return "accountID: " + accountId + " methodName: " + methodName + " objName: " + objName + " api: "
				+ apiVersion + " requestTime: " + requestTime;
	}

}
