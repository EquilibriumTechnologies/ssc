package com.eqt.ssc.accounts;

import com.eqt.ssc.util.Props;
/**
 * Lets us pull a singleton of the property driven AccountManager.
 * @author gman
 *
 */
public class AccountManagerFactory {
	//we self bootstrap ourselves, this initializing our accountManager instance.
	@SuppressWarnings("unused")
	private static final AccountManagerFactory factory = new AccountManagerFactory();
	private static volatile AccountManager man;

	@SuppressWarnings("unchecked")
	private AccountManagerFactory() {
		String acctClass = Props.getProp("ssc.account.manager.class.name",
				"com.eqt.ssc.accounts.SameCredAccountManager");
		try {
			// load our AccountManager
			Class<? extends AccountManager> amClazz = (Class<? extends AccountManager>) Class.forName(acctClass);
			man = amClazz.newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("could not load class: " + acctClass, e);
		}
	}

	public static AccountManager getInstance() {
		return man;
	}

}
