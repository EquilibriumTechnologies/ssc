package com.eqt.ssc.model.aws;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketLoggingConfiguration;
import com.amazonaws.services.s3.model.BucketNotificationConfiguration;
import com.amazonaws.services.s3.model.BucketPolicy;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.eqt.ssc.serde.RecordBuilder;

/**
 * neat little object to wrap all the information we want to have on a bucket
 * into a single object for storage.
 * 
 * @author gman
 * 
 */
public class BucketWrapper implements Comparable<BucketWrapper> {
	private Bucket bucket;
	private AccessControlList bucketAcl;
	private String bucketLocation;
	private BucketLoggingConfiguration bucketLoggingConfiguration;
	private BucketNotificationConfiguration bucketNotificationConfiguration;
	private BucketPolicy bucketPolicy;

	// doesnt exist
	// "s3:GetBucketRequestPayment",
	private BucketVersioningConfiguration bucketVersioningConfiguration;
	private BucketWebsiteConfiguration bucketWebsiteConfiguration;
	private BucketLifecycleConfiguration bucketLifecycleConfiguration;

	public Bucket getBucket() {
		return bucket;
	}

	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}

	public AccessControlList getBucketAcl() {
		return bucketAcl;
	}

	public void setBucketAcl(AccessControlList bucketAcl) {
		this.bucketAcl = bucketAcl;
	}

	public String getBucketLocation() {
		return bucketLocation;
	}

	public void setBucketLocation(String bucketLocation) {
		this.bucketLocation = bucketLocation;
	}

	public BucketLoggingConfiguration getBucketLoggingConfiguration() {
		return bucketLoggingConfiguration;
	}

	public void setBucketLoggingConfiguration(BucketLoggingConfiguration bucketLoggingConfiguration) {
		this.bucketLoggingConfiguration = bucketLoggingConfiguration;
	}

	public BucketNotificationConfiguration getBucketNotificationConfiguration() {
		return bucketNotificationConfiguration;
	}

	public void setBucketNotificationConfiguration(BucketNotificationConfiguration bucketNotificationConfiguration) {
		this.bucketNotificationConfiguration = bucketNotificationConfiguration;
	}

	public BucketPolicy getBucketPolicy() {
		return bucketPolicy;
	}

	public void setBucketPolicy(BucketPolicy bucketPolicy) {
		this.bucketPolicy = bucketPolicy;
	}

	public BucketVersioningConfiguration getBucketVersioningConfiguration() {
		return bucketVersioningConfiguration;
	}

	public void setBucketVersioningConfiguration(BucketVersioningConfiguration bucketVersioningConfiguration) {
		this.bucketVersioningConfiguration = bucketVersioningConfiguration;
	}

	public BucketWebsiteConfiguration getBucketWebsiteConfiguration() {
		return bucketWebsiteConfiguration;
	}

	public void setBucketWebsiteConfiguration(BucketWebsiteConfiguration bucketWebsiteConfiguration) {
		this.bucketWebsiteConfiguration = bucketWebsiteConfiguration;
	}

	public BucketLifecycleConfiguration getBucketLifecycleConfiguration() {
		return bucketLifecycleConfiguration;
	}

	public void setBucketLifecycleConfiguration(BucketLifecycleConfiguration bucketLifecycleConfiguration) {
		this.bucketLifecycleConfiguration = bucketLifecycleConfiguration;
	}

	/**
	 * Note: dont trust this yet, think recordbuilder has issues.
	 */
	@Override
	public boolean equals(Object o) {

		if (o == null)
			return false;
		if (!(o instanceof BucketWrapper))
			return false;

		BucketWrapper b = (BucketWrapper) o;

		String b1 = RecordBuilder.serialize(this);
		String b2 = RecordBuilder.serialize(b);
		return RecordBuilder.equality(b1, b2);
	}

	public int compareTo(BucketWrapper b) {
		if (b == null)
			return 1;

		// only comparing on bucket name
		return this.bucket.getName().compareTo(b.bucket.getName());
	}
}
