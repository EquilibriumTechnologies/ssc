package com.eqt.ssc.model.aws;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthResult;

public class LoadBalancersHealth {

	List<DescribeInstanceHealthResult> balancersHealth = new ArrayList<DescribeInstanceHealthResult>();

	public List<DescribeInstanceHealthResult> getBalancers() {
		return balancersHealth;
	}

	public void setBalancers(List<DescribeInstanceHealthResult> balancersHealth) {
		this.balancersHealth = balancersHealth;
	}
	
	public void addBalancer(DescribeInstanceHealthResult balancer) {
		this.balancersHealth.add(balancer);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((balancersHealth == null) ? 0 : balancersHealth.hashCode());
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
		LoadBalancersHealth other = (LoadBalancersHealth) obj;
		if (balancersHealth == null) {
			if (other.balancersHealth != null)
				return false;
		} else if (!balancersHealth.equals(other.balancersHealth))
			return false;
		return true;
	}
}
