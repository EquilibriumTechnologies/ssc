package com.eqt.ssc.model;

import com.eqt.ssc.model.Token;

/**
 * Status object that reports out what happened during a scrape of an account.
 * 
 */
public class SSCAccountStatus {
	Token token;
	public int changes = 0;
	public long timeSinceLastStart;
	public long totalCaptureTimeMS;
	public boolean success;

	public SSCAccountStatus(Token token) {
		this.token = token;
	}

	@Override
	public String toString() {
		return (success ? "sucessfull " : "failed") + " capture of " + changes + " changes in "
				+ (totalCaptureTimeMS / 1000.0d) + " " + token;
	}
}
