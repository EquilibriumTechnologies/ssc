package com.eqt.ssc.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.model.aws.LoadBalancersHealth;
import com.eqt.ssc.state.StateEngine;

public class ELBCollector extends APICollector {
	Log LOG = LogFactory.getLog(ELBCollector.class);
	AmazonElasticLoadBalancing elb = null;

	public ELBCollector(Token token, StateEngine state) {
		super(token, state);
		elb = new AmazonElasticLoadBalancingClient(getCreds());
	}

	@Override
	public int collect() {
		compareObjects(elb.describeLoadBalancerPolicies(),"elb.describeLoadBalancerPolicies");
		
		DescribeLoadBalancersResult balancers = elb.describeLoadBalancers();
		compareObjects(balancers,"elb.describeLoadBalancers");
		
		//TODO: change this to hold DescribeInstanceHealthResult
		LoadBalancersHealth wrapper = new LoadBalancersHealth();
		
		for(LoadBalancerDescription desc : balancers.getLoadBalancerDescriptions()) {
			wrapper.addBalancer(elb.describeInstanceHealth(new DescribeInstanceHealthRequest(desc.getLoadBalancerName())));
		}
		
		compareJson("wrapper", "elb.describeInstanceHealth");

		return stateChanges;
	}

	@Override
	protected String getCustomIntervalProperty() {
		return "ssc.account.check.interval.elb.seconds";
	}

}
