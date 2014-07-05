package com.eqt.ssc.model;

import com.eqt.ssc.model.Token;

/**
 * Status object that reports out what happened during a scrape of an account.
 * 
 */
public class SSCAccountStatus {
	public Token token;
	public int changes = 0;
	public long timeSinceLastStart;
	public long totalCaptureTimeMS;
	public boolean success;

	public SSCAccountStatus(Token token) {
		this.token = token;
	}

	public SSCAccountStatus(Token token, int changes) {
		this.token = token;
		this.changes = changes;
	}
	
	@Override
	public String toString() {
		return (success ? "sucessfull" : "failed") + " capture of " + changes + " changes in "
				+ (totalCaptureTimeMS / 1000.0d) + " " + token;
	}
}
