package com.eqt.ssc.process;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonServiceException;
import com.eqt.ssc.accounts.AccountManager;
import com.eqt.ssc.accounts.AccountManagerFactory;
import com.eqt.ssc.collector.APICollector;
import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.model.SSCError;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.state.StateEngine;
import com.eqt.ssc.util.Props;

public class AccountProcessor extends APICollector implements Callable<SSCAccountStatus> {
	private static final Log LOG = LogFactory.getLog(AccountProcessor.class);

	private List<APICollector> collectors = new ArrayList<APICollector>();
	private Token token;
	protected AccountManager aman = null;

	/**
	 * calls the props class for ssc.process.api.collectors which will be a
	 * comma separated list of collectors to run.
	 */
	@SuppressWarnings("unchecked")
	public AccountProcessor(Token token, StateEngine state) {
		super(state);
		this.token = token;

		String prop = Props.getProp("ssc.process.api.collectors");
		if (prop == null || "".equals(prop))
			throw new IllegalStateException("ssc.process.api.collectors is not set");

		String[] collectors = prop.split(",");
		for (String c : collectors) {
			try {
				Class<? extends APICollector> clazz = (Class<? extends APICollector>) Class.forName(c);
				Constructor<?> cons = clazz.getConstructor(StateEngine.class);
				APICollector newInstance = (APICollector) cons.newInstance(state);
				this.collectors.add(newInstance);
//				LOG.info("added instance: " + clazz.getName());

			} catch (ClassNotFoundException e) {
				throw new IllegalStateException("could not find class", e);
			} catch (InstantiationException e) {
				throw new IllegalStateException("could not instantiate class", e);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("could not access class", e);
			} catch (NoSuchMethodException e) {
				throw new IllegalStateException("could not find constructor", e);
			} catch (SecurityException e) {
				throw new IllegalStateException("could not get through security", e);
			} catch (IllegalArgumentException e) {
				throw new IllegalStateException("could not get the right arguments", e);
			} catch (InvocationTargetException e) {
				throw new IllegalStateException("could not get the invocation target", e);
			}
		}
		
		aman = AccountManagerFactory.getInstance();
	}

	@Override
	public SSCAccountStatus collect(Token token) {
		throw new UnsupportedOperationException("not intended for use as an APICollector, this runs them.");
	}

	
	public SSCAccountStatus call() throws Exception {
		long start = System.currentTimeMillis();
		boolean ran = false;
		int total = 0;
		// TODO: handle exceptions and rerun up to a number of times.
		for (APICollector collector : collectors) {
			try {
				long curr = System.currentTimeMillis();
				if(token.lastUpdate(collector.getCollectorName()) + collector.getIntervalTime() < curr) {
					token.use(collector.getCollectorName());
					SSCAccountStatus status = collector.collect(token);
					//take the token, may have had changes
					token = status.token;
					total+= status.changes;
					ran = true;
				} else {
					LOG.debug("too soon for: " + collector.getCollectorName() + " " + collector.getIntervalTime());
				}
			} catch (AmazonServiceException e) {
				//typically access denied, this is a big deal.
				LOG.error("unable to interact with " + collector.getCollectorName() + " for account: " + token.getAccountId(), e);
				SSCError error = new SSCError(e,collector.getCollectorName(),token.getAccountId());
				compareObjects(error, SSCError.SSCERROR, token.getAccountId());
			} catch(Throwable t) {
				LOG.error("boom, ungood, cannot run collector: " + collector.getCollectorName() + " for account: " + token.getAccountId(),t);
				SSCError error = new SSCError(t,collector.getCollectorName(),token.getAccountId());
				compareObjects(error, SSCError.SSCERROR, token.getAccountId());
				throw new ExecutionException(t);
			}
		}
		long now = System.currentTimeMillis();
		
		//send an update back out to the AM for recording.
		if(ran) {
			LOG.info("calling addAccount");
			aman.addAccount(token);
		}
		
		//TODO: more granular metrics reporting
		SSCAccountStatus status = new SSCAccountStatus(token);
		status.changes = total;
		status.success = true;
		status.timeSinceLastStart = start;
		status.totalCaptureTimeMS = now - start;
		
		return status;
	}

	@Override
	protected String getCustomIntervalProperty() {
		// TODO Auto-generated method stub
		return null;
	}
}
