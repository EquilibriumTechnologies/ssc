package com.eqt.ssc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.eqt.ssc.accounts.AccountManager;
import com.eqt.ssc.accounts.AccountManager.Token;
import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.process.AccountProcessor;
import com.eqt.ssc.state.StateEngine;
import com.eqt.ssc.util.Props;

public class SimpleStateCollector {

	private static Log LOG = LogFactory.getLog(SimpleStateCollector.class);

	private AWSCredentialsProvider provider;
	private AccountManager aMan;
	StateEngine state;
	// Curator bits
	private CuratorFramework client;

	/**
	 * will pull a provider class name from properties. Probably will extend one
	 * of the AWS ones to populate it and send it through this thing so it can
	 * instantiate with a default constructor.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	public SimpleStateCollector() throws IOException, ClassNotFoundException, InstantiationException,
			IllegalAccessException {

		// load our AccountManager
		String acctClass = Props.getProp("ssc.account.manager.class.name",
				"com.eqt.ssc.accounts.SameCredAccountManager");
		Class<? extends AccountManager> amClazz = (Class<? extends AccountManager>) Class.forName(acctClass);
		this.aMan = amClazz.newInstance();
		
		//TODO: probably want to hold onto this thread?
		Thread t = new Thread(this.aMan);
		t.start();
		
		// setup our provider class for working with.
		String provClass = Props.getProp("ssc.provider.class.name",
				"com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider");

		Class<? extends AWSCredentialsProvider> clazz = (Class<? extends AWSCredentialsProvider>) Class
				.forName(provClass);
		this.provider = clazz.newInstance();

		state = new StateEngine(new AmazonS3Client(this.provider));

		// setup zk connectivity
		String zkConnectString = Props.getProp("ssc.dist.zookeeper.connect.string");
		if (zkConnectString == null || "".equals(zkConnectString))
			throw new IllegalStateException("must set ssc.dist.zookeeper.connect.string to use this class");

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.newClient(zkConnectString, retryPolicy);
		client.start();

		// TODO: dunno if this works yet
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				LOG.info("closing client");
				client.close();
				CloseableUtils.closeQuietly(client);
			}
		});

		LOG.info("ready");
	}

	public void run() {

		// loop over every known account
		// call each section of AWS API.
		// check to see if state is newer.
		// if so, update record on S3 and local.

		ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1000, 1, TimeUnit.MINUTES,
				new ArrayBlockingQueue<Runnable>(10));
		Map<Token, Future<SSCAccountStatus>> tasks = new HashMap<Token, Future<SSCAccountStatus>>();

		// get initial list
		List<Token> accounts = aMan.getAccounts();
		LOG.debug("accounts: " + accounts.size());

		while (true) {

			// update?
			if (aMan.update()) {
				LOG.info("UPDATED ACCOUNT LIST RECEIVED!");
				accounts = aMan.getAccounts();
			}

			// start any new ones.
			for (Token t : accounts) {
				// new one?
				if (!tasks.containsKey(t)) {
					// check if its time to run
					if (t.intervalElapsed()) {
						LOG.debug("spawning thread to check account: " + t.getAccountId());
						// giddy up
						AccountProcessor proc = new AccountProcessor(t, state);
						Future<SSCAccountStatus> future = executor.submit(proc);
						tasks.put(t, future);
					} else {
						LOG.debug("not time to run account yet: " + t.getAccountId());
					}
				} else
					LOG.debug("task already known about");
			}

			// cleanup any old ones.
			List<Token> killList = new ArrayList<Token>();
			for (Token t : tasks.keySet()) {
				Future<SSCAccountStatus> future = tasks.get(t);
				// if its done, remove and cleanup.
				if (future.isDone()) {
					try {
						SSCAccountStatus sscAccountStatus = future.get();
						LOG.info(sscAccountStatus);
					} catch (InterruptedException | ExecutionException e) {
						//eating
					} finally {
						// prep to remove from the map
						killList.add(t);
						LOG.info("task completed: " + t.getAccountId());
					}
				} else if (future.isCancelled()) {
					LOG.warn("A task was cancelled, that shouldnt have happened....");
					tasks.remove(t);
				} else {
					LOG.debug("Task still running " + t.getAccountId());
				}
			}

			//actually remove the objects now
			if(killList.size() > 0) {
				LOG.debug("removing " + killList.size() + " tasks");
				for(Token t : killList)
					tasks.remove(t);
			}
			
			// now we insert a tiny wait
			try {
				LOG.debug("sleep");
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Token account = aMan.getNextAccount();
			// //TODO: lame
			// if(account == null) {
			// LOG.debug("nothing to do");
			// continue;
			// }
			// LOG.debug("working on account: " + account);
			//
			// AccountProcessor proc = new AccountProcessor(account, state);
			// try {
			// proc.call();
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }

		}
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, IOException {
		SimpleStateCollector ssc = new SimpleStateCollector();

		ssc.run();
	}

}
