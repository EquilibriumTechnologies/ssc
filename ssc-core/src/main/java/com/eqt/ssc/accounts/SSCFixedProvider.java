package com.eqt.ssc.accounts;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.eqt.ssc.model.SSCAccount;

/**
 * Simple class to hold the creds loaded. Will attempt to load a BasicAWSCredentials
 * if a key and secret exist within the SSCAccount.
 */
public class SSCFixedProvider implements AWSCredentialsProvider {
	
	private AWSCredentials creds;
	public SSCAccount account;
	
	public SSCFixedProvider(SSCAccount account) {
		this.account = account;
		if(account.getAccessKey() != null && account.getSecretKey() != null)
			this.creds = new BasicAWSCredentials(account.getAccessKey(),account.getSecretKey());
	}

	public AWSCredentials getCredentials() {
		return creds;
	}

	public void refresh() {
		//no-op
	}
}
