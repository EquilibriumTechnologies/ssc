package com.eqt.ssc.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.eqt.ssc.accounts.SSCFixedProvider;
import com.eqt.ssc.accounts.ZookeeperMultiAccountManager;
import com.eqt.ssc.model.SSCAccount;

/**
 * manual way to push some creds into ZK.
 * @author gman
 */

public class ZKAccountHelper extends ZookeeperMultiAccountManager {

	public ZKAccountHelper() throws Exception {
		super();
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("parameters: zkStr AccountAccessKey AccountSecretKey [accountid]");
			System.exit(1);
		}

		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		CuratorFramework client = CuratorFrameworkFactory.newClient(args[0], retryPolicy);
		client.start();

		// make dirs
		if(client.checkExists().forPath(_AM_ACCOUNTS_PATH) == null)
			client.create().creatingParentsIfNeeded().forPath(_AM_ACCOUNTS_PATH);

		String id = null;
		
		// validate account:
		try {
			SSCAccount account = new SSCAccount();
			account.setAccessKey(args[1]);
			account.setSecretKey(args[2]);
			AWSCredentialsProvider provider = new SSCFixedProvider(account);

			if (args.length == 4)
				id = args[3];
			else
				id = AWSUtils.getAccountId(provider);
			
			if(id == null || "".equals(id))
				throw new IllegalStateException("tried and tried and could not get the account id");
			else
				System.out.println("account ID: " + id);

			AmazonEC2 ec2 = new AmazonEC2Client(provider);
			ec2.describeAddresses();
		} catch (Throwable t) {
			System.out.println("total fail trying to validate account, giving up.");
			t.printStackTrace();
			System.exit(1);
		}
		
		//TODO: encryption of keys.
//		String payload = AWSUtils.packAWSCredentials(args[1],args[2],id);
		String payload = AWSUtils.serialize(new SSCAccount(id, args[1], args[2]));
		System.out.println("payload: " + payload);
		byte[] bytes = payload.getBytes();
		String path = ZKPaths.makePath(_AM_ACCOUNTS_PATH, id);

		//add account if it doesnt exist.
		if(client.checkExists().forPath(path) == null) {
			client.create().forPath(path,bytes);
			System.out.println("account created");
		} else {
			System.out.println("account already existed, updating");
			client.setData().forPath(path,bytes);
		}
		client.close();
	}

}
