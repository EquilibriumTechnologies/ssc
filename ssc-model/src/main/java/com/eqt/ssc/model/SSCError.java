package com.eqt.ssc.model;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * error class to log issues with collecting on an account.
 * 
 */
public class SSCError {
	
	public static final String SSCERROR = "ssc.error";

	long timeOfError;
	String collector;
	String accountId;
	String message;
	String exception;

	public SSCError() {}
	
	public SSCError(Throwable t, String collector, String accountId) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		t.printStackTrace(writer);
		this.exception = sw.toString();
		this.message = t.getMessage();
		this.timeOfError = System.currentTimeMillis();
		this.collector = collector;
		this.accountId = accountId;
	}
	
	public SSCError(String exception) {
		
	}
	
	public long getTimeOfError() {
		return timeOfError;
	}

	public void setTimeOfError(long timeOfError) {
		this.timeOfError = timeOfError;
	}

	public String getCollector() {
		return collector;
	}

	public void setCollector(String collector) {
		this.collector = collector;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getException() {
		return exception;
	}

	public void setException(String exception) {
		this.exception = exception;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		result = prime * result + ((collector == null) ? 0 : collector.hashCode());
		result = prime * result + ((exception == null) ? 0 : exception.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + (int) (timeOfError ^ (timeOfError >>> 32));
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
		SSCError other = (SSCError) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		if (collector == null) {
			if (other.collector != null)
				return false;
		} else if (!collector.equals(other.collector))
			return false;
		if (exception == null) {
			if (other.exception != null)
				return false;
		} else if (!exception.equals(other.exception))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (timeOfError != other.timeOfError)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SSCError [timeOfError=" + timeOfError + ", collector=" + collector + ", accountId=" + accountId
				+ ", message=" + message + ", exception=" + exception + "]";
	}
}
