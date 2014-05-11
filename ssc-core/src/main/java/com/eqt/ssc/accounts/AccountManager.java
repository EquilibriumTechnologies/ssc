package com.eqt.ssc.accounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.eqt.ssc.util.Props;

/**
 * Watches a given resource to maintain a list of accounts
 * to collect.
 * TODO: this one is prop driven, will turn into abstract class and extend
 * 	with a possible future one being Kafka backed.
 */
public abstract class AccountManager implements Runnable {
	
	protected static final Log LOG = LogFactory.getLog(AccountManager.class);
	protected AtomicBoolean update = new AtomicBoolean(true);

	public static class Token {
		String accountId;
		AWSCredentialsProvider creds;
		long lastUsed;
		long lastRefreshed;
		long checkInterval = 0;
		
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
	
	//TODO: probably need to synchronize this thing
	protected List<Token> tokens = Collections.synchronizedList(new ArrayList<Token>());
	protected int currToken = -1;
	protected int checkInterval = 60*1000;
	
	public AccountManager() {
		this.checkInterval = Integer.parseInt(Props.getProp("ssc.account.check.interval.seconds","60"))*1000;
		LOG.info("Account Checking interval set to: " + checkInterval + "ms");
	}
	
	/**
	 * Responsible for returning the next token for monitoring. This can
	 * be a blocking call if there is no work to do just yet.
	 * Call is also responsible for refreshing any AWSCreds that are getting close to expiring.
	 * @return
	 */
//	public abstract Token getNextAccount();
	
	public boolean update() {
		return update.get();
	}
	
	public List<Token> getAccounts() {
		update.set(false);
		List<Token> result = null;
		synchronized(tokens) {
			result = new ArrayList<Token>(tokens);
		}
		return result;
	}
}
