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
import com.eqt.ssc.model.Token;
import com.eqt.ssc.state.StateEngine;

public class EC2Collector extends APICollector {
	
	Log LOG = LogFactory.getLog(EC2Collector.class);

	private AmazonEC2 ec2;
	
	public EC2Collector(Token token, StateEngine state) {
		super(token, state);
		this.ec2 = new AmazonEC2Client(getCreds());
	}

	
	
	@Override
	public int collect() {
		
		//TODO: each aws call can throw RuntimeException, need to catch that.
	
		compareObjects(ec2.describeAddresses(), "ec2.describeAddresses");

		compareObjects(ec2.describeAvailabilityZones(), "ec2.describeAvailabilityZones");
		compareObjects(ec2.describeBundleTasks(), "ec2.describeBundleTasks");
		compareObjects(ec2.describeConversionTasks(), "ec2.describeConversionTasks");
		compareObjects(ec2.describeCustomerGateways(), "ec2.describeCustomerGateways");
		compareObjects(ec2.describeDhcpOptions(), "ec2.describeDhcpOptions");
		compareObjects(ec2.describeExportTasks(), "ec2.describeExportTasks");
		
		
		//appears to allow accessing of individual values found in ec2.describeImages().getImages()
//		compareObjects(ec2.describeImageAttribute(), "ec2.describeImageAttribute");

		//limit to images built by this account.
		DescribeImagesRequest imageRequest = new DescribeImagesRequest();
		List<String> accounts = new ArrayList<String>();
		accounts.add(getAccountId());
		imageRequest.setOwners(accounts);
		compareObjects(ec2.describeImages(imageRequest), "ec2.describeImages");
		
		//appears to allow accessing of individual values found in ec2.describeInstances()
		//compareObjects(ec2.describeInstanceAttribute(), "ec2.describeInstanceAttribute");

		compareObjects(ec2.describeInstanceStatus(), "ec2.describeInstanceStatus");
		compareObjects(ec2.describeInstances(), "ec2.describeInstances");
		compareObjects(ec2.describeInternetGateways(), "ec2.describeInternetGateways");
		compareObjects(ec2.describeKeyPairs(), "ec2.describeKeyPairs");
		
		//DEPRECATED
//		compareObjects(ec2.describeLicenses(), "ec2.describeLicenses");
		
		
		compareObjects(ec2.describeNetworkAcls(), "ec2.describeNetworkAcls");
		
		//appears to allow accessing of individual values found in ec2.describeNetworkInterfaces()
		//compareObjects(ec2.describeNetworkInterfaceAttribute(), "ec2.describeNetworkInterfaceAttribute");

		compareObjects(ec2.describeNetworkInterfaces(), "ec2.describeNetworkInterfaces");
		compareObjects(ec2.describePlacementGroups(), "ec2.describePlacementGroups");
		compareObjects(ec2.describeRegions(), "ec2.describeRegions");
		DescribeReservedInstancesResult describeReservedInstances = ec2.describeReservedInstances();
		compareJson(describeReservedInstances, "ec2.describeReservedInstances");
		
		//not pertinent to state capture..
//		compareObjects(ec2.describeReservedInstancesOfferings(), "ec2.describeReservedInstancesOfferings");
		
		compareObjects(ec2.describeRouteTables(), "ec2.describeRouteTables");
		compareObjects(ec2.describeSecurityGroups(), "ec2.describeSecurityGroups");
		
		//appears to allow accessing of individual values found in ec2.describeSnapshots()
		//compareObjects(ec2.describeSnapshotAttribute(), "ec2.describeSnapshotAttribute");
		
		DescribeSnapshotsRequest snapRequest = new DescribeSnapshotsRequest();
		snapRequest.setOwnerIds(accounts);
		compareObjects(ec2.describeSnapshots(snapRequest), "ec2.describeSnapshots");
		
		//creates error: Status Code: 400, AWS Service: AmazonEC2, 
		//AWS Request ID: 22534b6a-9091-4566-b8c7-c10e0dabfe58, AWS Error Code: InvalidSpotDatafeed.NotFound,
		//AWS Error Message: Spot datafeed subscription does not exist.
//		compareObjects(ec2.describeSpotDatafeedSubscription(), "ec2.describeSpotDatafeedSubscription");
		compareObjects(ec2.describeSpotInstanceRequests(), "ec2.describeSpotInstanceRequests");
		
		//not useful to log.
//		compareObjects(ec2.describeSpotPriceHistory(), "ec2.describeSpotPriceHistory");
		
		compareObjects(ec2.describeSubnets(), "ec2.describeSubnets");
		compareObjects(ec2.describeTags(), "ec2.describeTags");
		
		//appears to allow accessing of individual values found in ec2.describeVolumes()
		//compareObjects(ec2.describeVolumeAttribute(), "ec2.describeVolumeAttribute");
		compareObjects(ec2.describeVolumeStatus(), "ec2.describeVolumeStatus");
		compareJson(ec2.describeVolumes(), "ec2.describeVolumes");
		

		//unknown call
//		compareObjects(ec2.describeVpcPeeringConnection(), "ec2.describeVpcPeeringConnection");
		
		compareObjects(ec2.describeVpcs(), "ec2.describeVpcs");
		compareObjects(ec2.describeVpnConnections(), "ec2.describeVpnConnections");
		compareObjects(ec2.describeVpnGateways(), "ec2.describeVpnGateways");
		
		return stateChanges;
	}

	@Override
	protected String getCustomIntervalProperty() {
		return "ssc.account.check.interval.ec2.seconds";
	}
}
