package com.eqt.ssc.util;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.eqt.ssc.accounts.SSCFixedProvider;
import com.eqt.ssc.accounts.ZookeeperMultiAccountManager;

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
			AWSCredentialsProvider provider = new SSCFixedProvider(args[1], args[2]);

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
		
		//add account if it doesnt exist.
		if(client.checkExists().forPath(_AM_ACCOUNTS_PATH+"/"+id) == null) {
			//TODO: encryption of keys.
			String payload = AWSUtils.packAWSCredentials(args[1],args[2],id);
			
			byte[] bytes = payload.getBytes();
			client.create().forPath(_AM_ACCOUNTS_PATH+"/"+id,bytes);
			System.out.println("account created");
		} else
			System.out.println("account already existed");
		client.close();
	}

}
