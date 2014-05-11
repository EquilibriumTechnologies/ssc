package com.eqt.ssc.util;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;

public class AWSUtils {

	public static String getAccountId(AWSCredentialsProvider provider) {

		String accountId = null;
		//this is a neat little attempt to deduce the account we just authenticated to.
		//if we have access to IAM.getUser() then sweet, if not the error will tell us.
		try {
			AmazonIdentityManagement iam = new AmazonIdentityManagementClient(provider);
			//arn:aws:service:region:account:resource
			String arn = iam.getUser().getUser().getArn();
			String[] parts = arn.split(":");
			if(parts.length >= 6) {
				accountId = parts[4];
			}
		} catch (AmazonServiceException e) {
		    if (e.getErrorCode().compareTo("AccessDenied") == 0) {
		        String msg = e.getMessage();
		        //example error
		        // User: arn:aws:iam::123456789012:user/division_abc/subdivision_xyz/Bob is not authorized to perform: iam:GetUser on resource: arn:aws:iam::123456789012:user/division_abc/subdivision_xyz/Bob
		        int arnIdx = msg.indexOf("arn:aws");
		        if (arnIdx != -1) {
		            int arnSpace = msg.indexOf(" ", arnIdx);
		            String arn = msg.substring(arnIdx, arnSpace);
			        //round 2!
					String[] parts = arn.split(":");
					if(parts.length >= 6) {
						accountId = parts[4];
					}
		        }
		    }
		}
		return accountId;
	}
	
	public static String packAWSCredentials(String key, String secret, String id) {
		return key + ":" + secret + ":" + id;
	}
	
	public static String getAccountId(String packed) {
		return packed.split(":")[2];
	}
	
	public static String getAccountKey(String packed) {
		return packed.split(":")[0];
	}
	
	public static String getAccountSecret(String packed) {
		return packed.split(":")[1];
	}

}
