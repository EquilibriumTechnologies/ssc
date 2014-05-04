package com.eqt.ssc.model.aws;

import java.util.ArrayList;
import java.util.List;

public class AllBucketsInDetail {

	public List<BucketWrapper> buckets = new ArrayList<BucketWrapper>();

	public List<BucketWrapper> getBuckets() {
		return buckets;
	}

	public void setBuckets(List<BucketWrapper> buckets) {
		this.buckets = buckets;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((buckets == null) ? 0 : buckets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AllBucketsInDetail other = (AllBucketsInDetail) obj;
		if (buckets == null) {
			if (other.buckets != null)
				return false;
		} else if (!buckets.equals(other.buckets))
			return false;
		return true;
	}
}
