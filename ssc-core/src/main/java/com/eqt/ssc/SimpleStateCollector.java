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
import org.apache.curator.utils.CloseableUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.eqt.ssc.accounts.AccountManager;
import com.eqt.ssc.accounts.AccountManagerFactory;
import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.process.AccountProcessor;
import com.eqt.ssc.state.StateEngine;
import com.eqt.ssc.util.Props;
import com.eqt.ssc.web.HttpServer;

public class SimpleStateCollector {

	private static Log LOG = LogFactory.getLog(SimpleStateCollector.class);

	private AWSCredentialsProvider provider;
	private AccountManager aMan;
	StateEngine state;
	
	boolean shutDownHookRegistered = false;
	
	//threadpool for workers
	ThreadPoolExecutor executor;
	
	//web server
	HttpServer webServer;
	
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

		Thread t = null;
		try {
			this.aMan = AccountManagerFactory.getInstance();
			
			//TODO: probably want to hold onto this thread?
			t = new Thread(this.aMan);
			t.start();
			
			webServer = new HttpServer();
			
			// setup our provider class for working with.
			//--Russ: I don't like the use of the generic term "provider". Should be something like "credentialsprovider"
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
	
			//setup thread pool
			executor = new ThreadPoolExecutor(50, 1000, 1, TimeUnit.MINUTES,
					new ArrayBlockingQueue<Runnable>(10));
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					LOG.info("closing client");
					CloseableUtils.closeQuietly(webServer);
					if(executor != null)
						executor.shutdown();
					try {
						//give executor a chance to die.
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//dont really care.
					} finally {
						if(executor != null && !executor.isShutdown())
							executor.shutdownNow();
					}
				}
			});
			shutDownHookRegistered = true;
	
		} catch(Throwable e) {
			LOG.error("Failure to get everything started, force quitting",e);
			if(!shutDownHookRegistered) {
				LOG.info("closing client");
				CloseableUtils.closeQuietly(webServer);
				if(executor != null)
					executor.shutdownNow();
				
				if(t != null)
					t.interrupt();
			}
			System.exit(1);
		}
		
		LOG.info("ready");
	}

	public void run() {
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
					//lets kick it off
					LOG.debug("spawning thread to check account: " + t.getAccountId());
					// giddy up
					//TODO: could pool these and reuse.
					AccountProcessor proc = new AccountProcessor(t, state);
					Future<SSCAccountStatus> future = executor.submit(proc);
					tasks.put(t, future);
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
						//TODO: create error log to write to.
						LOG.warn("throwing up working on an account " + t.getAccountId(), e);
					} finally {
						// prep to remove from the map
						killList.add(t);
						LOG.info("task completed: " + t.getAccountId());
					}
				} else if (future.isCancelled()) {
					LOG.warn("A task was cancelled, that shouldnt have happened....");
					killList.add(t);
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
				//Shouldn't have been interrupted... better exit
				System.exit(2);
			}
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
