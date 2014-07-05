package com.eqt.ssc.collector;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeReservedInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.state.StateEngine;

public class EC2Collector extends APICollector {
	
	Log LOG = LogFactory.getLog(EC2Collector.class);

	private AmazonEC2 ec2;
	
	public EC2Collector(Token token, StateEngine state) {
		super(state);
	}

	private void init(Token token) {
		this.ec2 = new AmazonEC2Client(token.getCredentials());
	}
	
	
	@Override
	public SSCAccountStatus collect(Token token) {
		init(token);
		
		//TODO: each aws call can throw RuntimeException, need to catch that.
	
		compareObjects(ec2.describeAddresses(), "ec2.describeAddresses",token.getAccountId());

		compareObjects(ec2.describeAvailabilityZones(), "ec2.describeAvailabilityZones",token.getAccountId());
		compareObjects(ec2.describeBundleTasks(), "ec2.describeBundleTasks",token.getAccountId());
		compareObjects(ec2.describeConversionTasks(), "ec2.describeConversionTasks",token.getAccountId());
		compareObjects(ec2.describeCustomerGateways(), "ec2.describeCustomerGateways",token.getAccountId());
		compareObjects(ec2.describeDhcpOptions(), "ec2.describeDhcpOptions",token.getAccountId());
		compareObjects(ec2.describeExportTasks(), "ec2.describeExportTasks",token.getAccountId());
		
		
		//appears to allow accessing of individual values found in ec2.describeImages().getImages()
//		compareObjects(ec2.describeImageAttribute(), "ec2.describeImageAttribute");

		//limit to images built by this account.
		DescribeImagesRequest imageRequest = new DescribeImagesRequest();
		List<String> accounts = new ArrayList<String>();
		accounts.add(token.getAccountId());
		imageRequest.setOwners(accounts);
		compareObjects(ec2.describeImages(imageRequest), "ec2.describeImages",token.getAccountId());
		
		//appears to allow accessing of individual values found in ec2.describeInstances()
		//compareObjects(ec2.describeInstanceAttribute(), "ec2.describeInstanceAttribute");

		compareObjects(ec2.describeInstanceStatus(), "ec2.describeInstanceStatus",token.getAccountId());
		compareObjects(ec2.describeInstances(), "ec2.describeInstances",token.getAccountId());
		compareObjects(ec2.describeInternetGateways(), "ec2.describeInternetGateways",token.getAccountId());
		compareObjects(ec2.describeKeyPairs(), "ec2.describeKeyPairs",token.getAccountId());
		
		//DEPRECATED
//		compareObjects(ec2.describeLicenses(), "ec2.describeLicenses");
		
		
		compareObjects(ec2.describeNetworkAcls(), "ec2.describeNetworkAcls",token.getAccountId());
		
		//appears to allow accessing of individual values found in ec2.describeNetworkInterfaces()
		//compareObjects(ec2.describeNetworkInterfaceAttribute(), "ec2.describeNetworkInterfaceAttribute");

		compareObjects(ec2.describeNetworkInterfaces(), "ec2.describeNetworkInterfaces",token.getAccountId());
		compareObjects(ec2.describePlacementGroups(), "ec2.describePlacementGroups",token.getAccountId());
		compareObjects(ec2.describeRegions(), "ec2.describeRegions",token.getAccountId());
		DescribeReservedInstancesResult describeReservedInstances = ec2.describeReservedInstances();
		compareJson(describeReservedInstances, "ec2.describeReservedInstances",token.getAccountId());
		
		//not pertinent to state capture..
//		compareObjects(ec2.describeReservedInstancesOfferings(), "ec2.describeReservedInstancesOfferings");
		
		compareObjects(ec2.describeRouteTables(), "ec2.describeRouteTables",token.getAccountId());
		compareObjects(ec2.describeSecurityGroups(), "ec2.describeSecurityGroups",token.getAccountId());
		
		//appears to allow accessing of individual values found in ec2.describeSnapshots()
		//compareObjects(ec2.describeSnapshotAttribute(), "ec2.describeSnapshotAttribute");
		
		DescribeSnapshotsRequest snapRequest = new DescribeSnapshotsRequest();
		snapRequest.setOwnerIds(accounts);
		compareObjects(ec2.describeSnapshots(snapRequest), "ec2.describeSnapshots",token.getAccountId());
		
		//creates error: Status Code: 400, AWS Service: AmazonEC2, 
		//AWS Request ID: 22534b6a-9091-4566-b8c7-c10e0dabfe58, AWS Error Code: InvalidSpotDatafeed.NotFound,
		//AWS Error Message: Spot datafeed subscription does not exist.
//		compareObjects(ec2.describeSpotDatafeedSubscription(), "ec2.describeSpotDatafeedSubscription");
		compareObjects(ec2.describeSpotInstanceRequests(), "ec2.describeSpotInstanceRequests",token.getAccountId());
		
		//not useful to log.
//		compareObjects(ec2.describeSpotPriceHistory(), "ec2.describeSpotPriceHistory");
		
		compareObjects(ec2.describeSubnets(), "ec2.describeSubnets",token.getAccountId());
		compareObjects(ec2.describeTags(), "ec2.describeTags",token.getAccountId());
		
		//appears to allow accessing of individual values found in ec2.describeVolumes()
		//compareObjects(ec2.describeVolumeAttribute(), "ec2.describeVolumeAttribute");
		compareObjects(ec2.describeVolumeStatus(), "ec2.describeVolumeStatus",token.getAccountId());
		compareJson(ec2.describeVolumes(), "ec2.describeVolumes",token.getAccountId());
		

		//unknown call
//		compareObjects(ec2.describeVpcPeeringConnection(), "ec2.describeVpcPeeringConnection");
		
		compareObjects(ec2.describeVpcs(), "ec2.describeVpcs",token.getAccountId());
		compareObjects(ec2.describeVpnConnections(), "ec2.describeVpnConnections",token.getAccountId());
		compareObjects(ec2.describeVpnGateways(), "ec2.describeVpnGateways",token.getAccountId());
		
		return new SSCAccountStatus(token,stateChanges);
	}

	@Override
	protected String getCustomIntervalProperty() {
		return "ssc.account.check.interval.ec2.seconds";
	}
}
