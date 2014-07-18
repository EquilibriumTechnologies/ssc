package com.eqt.ssc.collector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.DescribeInstanceHealthRequest;
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersResult;
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription;
import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.model.aws.LoadBalancersHealth;
import com.eqt.ssc.state.StateEngine;
import com.eqt.ssc.util.Props;

public class ELBCollector extends APICollector {
	Log LOG = LogFactory.getLog(ELBCollector.class);
	AmazonElasticLoadBalancing elb = null;

	public ELBCollector(StateEngine state) {
		super(state);
	}

	private void init(Token token) {
		elb = new AmazonElasticLoadBalancingClient(token.getCredentials());
		if(Props.getProp("ssc.ec2.region.override") != null)
			elb.setRegion(RegionUtils.getRegion(Props.getProp("ssc.ec2.region.override")));

	}
	
	@Override
	public SSCAccountStatus collect(Token token) {
		init(token);
		
		compareObjects(elb.describeLoadBalancerPolicies(),"elb.describeLoadBalancerPolicies", token.getAccountId());
		
		DescribeLoadBalancersResult balancers = elb.describeLoadBalancers();
		compareJson(balancers,"elb.describeLoadBalancers", token.getAccountId());
		
		//TODO: change this to hold DescribeInstanceHealthResult
		LoadBalancersHealth wrapper = new LoadBalancersHealth();
		
		for(LoadBalancerDescription desc : balancers.getLoadBalancerDescriptions()) {
			wrapper.addBalancer(elb.describeInstanceHealth(new DescribeInstanceHealthRequest(desc.getLoadBalancerName())));
		}
		
		compareJson(wrapper, "elb.describeInstanceHealth", token.getAccountId());

		return new SSCAccountStatus(token,stateChanges);
	}

	@Override
	protected String getCustomIntervalProperty() {
		return "ssc.account.check.interval.elb.seconds";
	}

}
