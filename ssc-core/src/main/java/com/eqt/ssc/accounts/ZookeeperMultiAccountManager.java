package com.eqt.ssc.accounts;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.eqt.ssc.model.SSCAccount;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.util.AWSUtils;
import com.eqt.ssc.util.Props;

/**
 * This class uses zk to support mutiple accounts to log status against, as well
 * as distributing the work accross multiple instances by being aware of how
 * many other managers are running.
 * 
 * @author gman
 * 
 */
public class ZookeeperMultiAccountManager extends AccountManager {
	private static final Log LOG = LogFactory.getLog(ZookeeperMultiAccountManager.class);

	// providerWrapper
	boolean wrap = false;
	Class<? extends AWSCredentialsProvider> wrapperClass = null;
	Constructor<?> wrapCon = null;

	// Curator bits
	private CuratorFramework client;
	private PathChildrenCache accountCache = null;
	private PathChildrenCache managerCache = null;

	// whoami
	String managerName = null;
	int totalManagers = 0;

	protected static final String _AM_BASE_PATH = "/ssc_am";
	private static final String _AM_DISCOVERY_PATH = _AM_BASE_PATH + "/discovery";
	protected static final String _AM_ACCOUNTS_PATH = _AM_BASE_PATH + "/accounts";

	@SuppressWarnings("unchecked")
	public ZookeeperMultiAccountManager() throws Exception {
		super();
		// see if we will be wrapping.
		if (Props.getProp("ssc.account.manager.provider") != null) {
			wrap = true;
			wrapperClass = (Class<? extends AWSCredentialsProvider>) Class.forName(Props
					.getProp("ssc.account.manager.provider"));
			wrapCon = wrapperClass.getConstructor(AWSCredentialsProvider.class);
			LOG.debug("constructor loaded: " + wrapCon.getName());
		} else {
			LOG.debug("no wrapper defined");
		}

		String zkConnectString = Props.getProp("ssc.dist.zookeeper.connect.string");
		if (zkConnectString == null || "".equals(zkConnectString))
			throw new IllegalStateException("must set ssc.dist.zookeeper.connect.string to use this class");

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.newClient(zkConnectString, retryPolicy);
		client.start();

		// make sure directories are present.
		if (client.checkExists().forPath(_AM_DISCOVERY_PATH) == null)
			client.create().creatingParentsIfNeeded().forPath(_AM_DISCOVERY_PATH);
		if (client.checkExists().forPath(_AM_ACCOUNTS_PATH) == null)
			client.create().creatingParentsIfNeeded().forPath(_AM_ACCOUNTS_PATH);

		// connect ephemerally to zk
		managerName = ZKPaths.getNodeFromPath(client.create().creatingParentsIfNeeded()
				.withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(ZKPaths.makePath(_AM_DISCOVERY_PATH, "0")));

		LOG.debug("manager name set to: " + managerName);

		// lets setup our account cache.
		accountCache = new PathChildrenCache(client, _AM_ACCOUNTS_PATH, true);
		accountCache.start(StartMode.BUILD_INITIAL_CACHE);

		// lets setup manager cache
		managerCache = new PathChildrenCache(client, _AM_DISCOVERY_PATH, true);
		managerCache.start(StartMode.BUILD_INITIAL_CACHE);

		// shutdown hooks
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				LOG.info("closing client");
				// CloseableUtils.closeQuietly(serviceDiscovery);
				CloseableUtils.closeQuietly(accountCache);
				CloseableUtils.closeQuietly(managerCache);
				CloseableUtils.closeQuietly(client);
			}
		});
	}

	public void run() {

		// TODO: add in a shutdown flag or something
		while (true) {

			// check on how many other managers are running.

			List<ChildData> managers = managerCache.getCurrentData();
			int totalManagers = managers.size();
			int pos = 0;

			// find our pos in the list, this will be offset calc.
			for (ChildData c : managers) {
				if (managerName.equals(ZKPaths.getNodeFromPath(c.getPath())))
					break;
				else
					pos++;
			}

			// get latest accountList
			List<ChildData> accounts = accountCache.getCurrentData();

			// used to keep track of ones we are meant to have, if not in here,
			// kill.
			List<String> newAccountList = new ArrayList<String>();

			// loop over all the accounts.
			int count = 0;
			for (ChildData c : accounts) {
				// this is one of ours. Take it.
				if (count == pos) {
					String acctStr = new String(c.getData());
					SSCAccount account = AWSUtils.deserialize(acctStr);
					String accountId = account.getAccountId();
					// add to our list of keepers.
					newAccountList.add(accountId);

					// did we already have it?
					boolean has = false;
					synchronized (tokens) {
						for (Token t : tokens)
							if (t.getAccountId().equals(accountId)) {
								has = true;
								break;
							}
					}

					// nope, lets set it up then.
					if (!has) {
						//TODO: rework this, maybe make Token a provider?
						AWSCredentialsProvider provider = new SSCFixedProvider(account.getAccessKey(),
								account.getSecretKey());
						if (wrap) {
							try {
								provider = (AWSCredentialsProvider) wrapCon.newInstance(provider);
								LOG.debug("wrapped provider");

							} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
								LOG.error("could not create class: " + wrapperClass.getName(), e);
								throw new IllegalStateException(e);
							} catch (InvocationTargetException e) {
								LOG.error(
										"could not invoke powerfull enough spell for class: " + wrapperClass.getName(),
										e);
								throw new IllegalStateException(e);
							}
						}

						Token t = new Token(account,provider);
						
						synchronized (tokens) {
							tokens.add(t);
						}
						update.set(true);
						LOG.info("added new account: " + accountId);
					}
				}
				count++;

				// gone too far check
				if (count == totalManagers) {
					count = 0;
				}
			}

			// no we square up the accounts we should have vs all the ones in
			// our list and remove those that are no longer ours to maintain.
			List<Token> killList = new ArrayList<Token>();
			synchronized (tokens) {
				for (Token t : tokens) {
					if (!newAccountList.contains(t.getAccountId()))
						killList.add(t);
				}

				if(killList.size() > 0) {
					for (Token t : killList) {
						LOG.debug("removing Token: " + t.getAccountId());
						tokens.remove(t);
					}
					update.set(true);
				}
			}

			// should be good to go!
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// eat it
			}

		}
	}
	
	@Override
	public void addAccount(SSCAccount account) {
		String payload = AWSUtils.serialize(account);
		LOG.debug("account being added: " + payload);
		byte[] bytes = payload.getBytes();
		String path = ZKPaths.makePath(_AM_ACCOUNTS_PATH, account.getAccountId());

		//add account if it doesnt exist.
		try {
			if(client.checkExists().forPath(path) == null) {
				client.create().forPath(path,bytes);
				LOG.debug("account created");
			} else {
				LOG.debug("account already existed, updating");
				client.setData().forPath(path,bytes);
			}
		} catch (Exception e) {
			throw new IllegalStateException("well couldnt do that", e);
		}
	}
}
