package com.eqt.ssc.collector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.model.aws.AllBucketsInDetail;
import com.eqt.ssc.model.aws.BucketWrapper;
import com.eqt.ssc.state.StateEngine;

public class S3Collector extends APICollector {
	private Log LOG = LogFactory.getLog(S3Collector.class);
	private AmazonS3 s3;

	public S3Collector(StateEngine state) {
		super(state);
	}

	private void init(Token token) {
		s3 = new AmazonS3Client(token.getCredentials());
	}
	
	@Override
	public SSCAccountStatus collect(Token token) {
		init(token);
		
		//grab all the buckets
		List<Bucket> buckets = s3.listBuckets();
		LOG.debug(buckets.size() + " buckets found");
//		compareObjects(buckets, "s3.listBuckets");
		
		List<BucketWrapper> wrappers = new ArrayList<BucketWrapper>();
		for(Bucket b : buckets) {
			BucketWrapper wrapper = new BucketWrapper();
			wrapper.setBucket(b);
			
			wrapper.setBucketAcl(s3.getBucketAcl(b.getName()));
			wrapper.setBucketLocation(s3.getBucketLocation(b.getName()));
			wrapper.setBucketLoggingConfiguration(s3.getBucketLoggingConfiguration(b.getName()));
			wrapper.setBucketNotificationConfiguration(s3.getBucketNotificationConfiguration(b.getName()));
			wrapper.setBucketPolicy(s3.getBucketPolicy(b.getName()));
			
			//doesnt exist
			//"s3:GetBucketRequestPayment",
			wrapper.setBucketVersioningConfiguration(s3.getBucketVersioningConfiguration(b.getName()));
			wrapper.setBucketWebsiteConfiguration(s3.getBucketWebsiteConfiguration(b.getName()));
			wrapper.setBucketLifecycleConfiguration(s3.getBucketLifecycleConfiguration(b.getName()));
			
			//TODO: do we want to drive into each objects ACL? I think this is fine
			//to leave up to the s3 logs.
			wrappers.add(wrapper);
		}
		
		Collections.sort(wrappers);
		
		AllBucketsInDetail detail = new AllBucketsInDetail();
		detail.setBuckets(wrappers);
		//compare all the buckets
		compareJson(detail, "ssc.allBucketsDetail", token.getAccountId());

		return new SSCAccountStatus(token, stateChanges);
	}

	@Override
	protected String getCustomIntervalProperty() {
		return "ssc.account.check.interval.s3.seconds";
	}
}
