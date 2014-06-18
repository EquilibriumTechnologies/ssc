package com.eqt.ssc.accounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eqt.ssc.model.SSCAccount;
import com.eqt.ssc.model.Token;

/**
 * Watches a given resource to maintain a list of accounts
 * to collect.
 * TODO: this one is prop driven, will turn into abstract class and extend
 * 	with a possible future one being Kafka backed.
 */
public abstract class AccountManager implements Runnable {
	
	protected static final Log LOG = LogFactory.getLog(AccountManager.class);
	protected AtomicBoolean update = new AtomicBoolean(true);

	
	//TODO: probably need to synchronize this thing
	protected List<Token> tokens = Collections.synchronizedList(new ArrayList<Token>());
	protected int currToken = -1;
	protected static final int DEFAULT_CHECK_INTERVAL = 60*1000;
	
	public AccountManager() { }
	
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
	
	/**
	 * used to allow adding of accounts into the system from the webservices api's.
	 * also updates an existing account if it is already present, useful for 
	 * adding in statistics that need to be kept up to date.
	 * @param account
	 */
	public abstract void addAccount(SSCAccount account) throws UnsupportedOperationException;
}
