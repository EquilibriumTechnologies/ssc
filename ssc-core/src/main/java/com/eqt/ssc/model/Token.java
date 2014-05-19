package com.eqt.ssc.model;

import java.util.concurrent.TimeUnit;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

public class Token extends SSCAccount {
//	String accountId;
	AWSCredentialsProvider creds;
	long lastUsed;
	long lastRefreshed;
	long checkInterval = 0;
//	SSCAccount account;
	
	public Token(String accountId, AWSCredentialsProvider creds, long checkInterval) {
		this.accountId = accountId;
		this.creds = creds;
		this.lastRefreshed = System.currentTimeMillis();
		this.lastUsed = 0;
		this.checkInterval = checkInterval;
	}
	
	public long use() {
		this.lastUsed = System.currentTimeMillis();
		return this.lastUsed;
	}
	
	/**
	 * tells you if this token is ready to be collected again, checked
	 * last time it was called use() on vs the time interval it is meant
	 * to check on.
	 * @return true if more time than checkInterval has passed since it last ran.
	 */
	public boolean intervalElapsed() {
		if(lastUsed == 0)
			return true;
		long toGo = System.currentTimeMillis() - lastUsed;
		if(toGo < checkInterval)
			return false;
		return true;
	}
	
	public String getAccountId() {
		return accountId;
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
		return "account: " + accountId + " lastRefreshed: " + lastRefreshed + " last used: " + lastUsed + 
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
