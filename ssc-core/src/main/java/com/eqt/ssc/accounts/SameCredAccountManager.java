package com.eqt.ssc.accounts;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.eqt.ssc.model.SSCAccount;
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

		Token t = new Token(accountId,provider);
		tokens.add(t);
		LOG.debug("account loaded.");
	}
	
	public void run() {
	}
	
	@Override
	public void addAccount(SSCAccount account) {
		throw new UnsupportedOperationException("does not supprt this");
	}

	@Override
	public List<SSCAccount> getKnownAccounts() {
		List<SSCAccount> accounts = new ArrayList<SSCAccount>();
		if(tokens.size() == 1)
			accounts.add((SSCAccount)tokens.get(0));
		return accounts;
	}

}
