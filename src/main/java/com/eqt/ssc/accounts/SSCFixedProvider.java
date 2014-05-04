package com.eqt.ssc.accounts;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

/**
 * Simple class to hold the creds loaded.
 */
public class SSCFixedProvider implements AWSCredentialsProvider {
	
	private AWSCredentials creds;
	
	public SSCFixedProvider(String key, String secret) {
		this.creds = new BasicAWSCredentials(key, secret);
	}

	public AWSCredentials getCredentials() {
		return creds;
	}

	public void refresh() {
		//no-op
	}
}
