package com.eqt.ssc.accounts;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.util.AWSUtils;

/**
 * Really for testing purposes or for monitoring the account that SSC
 * is running in. It will look for the AwsCredentials.properties on the
 * classpath and load it.
 * @author gman
 *
 */
public class SameCredAccountManager extends AccountManager {
	
	public SameCredAccountManager() {
		super();
		ClasspathPropertiesFileCredentialsProvider provider = new ClasspathPropertiesFileCredentialsProvider();
		String accountId = AWSUtils.getAccountId(provider);
		
		if(accountId == null) 
			throw new IllegalStateException("cannot derive local account, tried lots of things, the easiest to go fix is to grant access to iam.getUser()");

		tokens.add(new Token(accountId, provider,checkInterval));
		LOG.debug("account loaded.");
	}
	
//	public Token getNextAccount() {
	public void run() {
		//does nothing since the account was loaded at startup.
		
//		if(tokens.size() == 0)
//			return;
//		//walked to the end, reset
//		if(currToken == tokens.size())
//			currToken = -1;
//		
//		//increment token
//		currToken += 1;
//
//		if(currToken < tokens.size()) {
//			Token t = tokens.get(currToken);
//			//if too soon, skip over
//			if(System.currentTimeMillis() - t.lastUsed < checkInterval) {
//				//TODO: HACKING!
//				try {
//					Thread.sleep(checkInterval/4);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				return;
//			}
//			
//			t.lastUsed = System.currentTimeMillis();
//			//refresh?
//			if(t.lastUsed - t.lastRefreshed > TimeUnit.MINUTES.toMillis(5)) {
//				LOG.debug("refreshing creds for: " + t.accountId);
//				t.creds.refresh();
//				t.lastRefreshed = t.lastUsed;
//			}
//			LOG.debug("account selected for state capture: " + t);
//			return t;
//		}
//		return null;
	}

}
