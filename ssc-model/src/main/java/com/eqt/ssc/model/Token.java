package com.eqt.ssc.model;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * live running version of the SSCAccount, this adds in credentials
 * for aws, and checkIntervals for actually polling an account.
 * @author gman
 *
 */
public class Token extends SSCAccount {

	AWSCredentialsProvider creds;
//	long lastUsed;
	long lastRefreshed;
	
	public Token() { }
	
	public Token(SSCAccount account, AWSCredentialsProvider creds) {
		this.accountId = account.getAccountId();
		this.accessKey = account.getAccessKey();
		this.secretKey = account.getSecretKey();
		this.ctBucketName = account.getCtBucketName();
		this.ctPath = account.getCtPath();
		this.s3BucketName = account.getS3BucketName();
		this.s3Path = account.getS3Path();
		this.updateTimestampMap = new HashMap<String, Long>(account.getUpdateTimestampMap());
		this.creds = creds;
	}
	
	public Token(String accountId, AWSCredentialsProvider creds) {
		this.accountId = accountId;
		this.creds = creds;
		this.lastRefreshed = System.currentTimeMillis();
	}
	
	public void setProvider(AWSCredentialsProvider creds) {
		this.creds = creds;
	}
	
	public long use(String item) {
		this.updateTimestampMap.put(item, System.currentTimeMillis());
		return this.updateTimestampMap.get(item);
	}
	
	public AWSCredentials getCredentials() {
		//5 minute refresh
		if((System.currentTimeMillis() - this.lastRefreshed) > TimeUnit.MINUTES.toMillis(5)) {
			creds.refresh();
			this.lastRefreshed = System.currentTimeMillis();
		}
		return creds.getCredentials();
	}
	
	public String toString() {
		return "account: " + accountId + " lastRefreshed: " + lastRefreshed + 
				" keyId: " + creds.getCredentials().getAWSAccessKeyId();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(!(o instanceof Token))
			return false;
		Token t = (Token)o;
		return t.accountId.equals(this.accountId);
	}

}
