import java.io.File;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeVpcsResult;
import com.amazonaws.services.ec2.model.DescribeVpnGatewaysResult;
import com.eqt.ssc.model.SSCKey;
import com.eqt.ssc.model.SSCRecord;
import com.eqt.ssc.serde.Deserializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class WhatCanIGet {

	private String apiVersion;
	static ObjectMapper mapper = new ObjectMapper();
	private Gson gson;
	
	
	public WhatCanIGet() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeHierarchyAdapter(SSCRecord.class, new Deserializer());
		gson = builder.create();

		String[] parts = System.getProperty("java.class.path").split(File.pathSeparator);
		FileSystem fs = FileSystems.getDefault();
		String separator = fs.getSeparator();
		
		for(String part : parts) {
			int lastSlash = part.lastIndexOf(separator);
			if(part.substring(lastSlash+1).toLowerCase().startsWith("aws-java")) {
				this.apiVersion = part.substring(lastSlash+1,part.lastIndexOf("."));
				System.out.println("AWS JAVA API verison found: " + this.apiVersion);
			}
		}
		
	}
	
	public void write(Object obj, String methodName, String acctId) {
		write(obj,methodName,acctId,System.currentTimeMillis());
	}
	
	public void write(Object obj, String methodName, String acctId, long requestTime) {
		SSCKey k = new SSCKey(obj.getClass().getName(), methodName, acctId, this.apiVersion,requestTime);
		SSCRecord rec = new SSCRecord(k,obj);
		String out = gson.toJson(rec);
		System.out.println(out);
		SSCRecord in = gson.fromJson(out, SSCRecord.class);
		System.out.println(gson.toJson(in));
		System.out.println();
		
	}
	
		
	
	/**
	 * @param args
	 * @throws AmazonClientException 
	 * @throws JsonProcessingException 
	 * @throws AmazonServiceException 
	 */
	public static void main(String[] args) throws AmazonServiceException, JsonProcessingException, AmazonClientException {
		
		String json = "{'key':{'objName':'com.amazonaws.services.ec2.model.DescribeVolumesResult','methodName':'ec2.describeVolumes','requestTime':1398713211392,'accountId':'243679723095','apiVersion':'aws-java-sdk-1.6.0'},'value':{'volumes':[{'volumeId':'vol-dfc9cfa9','size':6,'snapshotId':'snap-46dc1211','availabilityZone':'us-east-1b','state':'in-use','createTime':'Apr 13, 2014 9:34:38 AM','attachments':[{'volumeId':'vol-dfc9cfa9','instanceId':'i-f87c73a9','device':'/dev/sda1','state':'attached','attachTime':'Apr 13, 2014 9:34:38 AM','deleteOnTermination':true}],'volumeType':'standard'}]}}";
		JsonParser parser = new JsonParser();
		JsonObject parse = (JsonObject)parser.parse(json);
		JsonObject jsonObject = parse.getAsJsonObject("value");
		System.out.println(jsonObject.toString());
		
		System.exit(1);
		
		
		WhatCanIGet w = new WhatCanIGet();
		
		System.out.println("connecting");
		ClasspathPropertiesFileCredentialsProvider provider = new ClasspathPropertiesFileCredentialsProvider();
		String accessKeyId = provider.getCredentials().getAWSAccessKeyId();
		AmazonEC2 ec2 = new AmazonEC2Client(provider);
		ec2.setRegion(Region.getRegion(Regions.US_EAST_1));
		System.out.println("Connected");

//		DescribeImagesResult imagesDesc = ec2.describeImages();
//		System.out.println("payload received: " + imagesDesc.getImages().size());
//		int i = 0;
//		long now = System.currentTimeMillis();
//		for(Image img : imagesDesc.getImages()) {
////			img.gett
//			write(img,"ec2.describeImages().getImages()",accessKeyId,now);
//			if(i++ == 10)
//				break;
//		}
		DescribeVpcsResult describeVpcs = ec2.describeVpcs();
		DescribeVpnGatewaysResult describeVpnGateways = ec2.describeVpnGateways();
		if(describeVpnGateways == null)
			System.out.println("null");
		else 
			System.out.println("not null");
		
		System.exit(1);
		
		w.write(ec2.describeAccountAttributes(),"ec2.describeAccountAttributes()",accessKeyId);
		w.write(ec2.describeInstances(),"ec2.describeInstances()",accessKeyId);
		w.write(ec2.describeNetworkAcls(),"ec2.describeNetworkAcls()",accessKeyId);
		w.write(ec2.describeInternetGateways(),"ec2.describeInternetGateways()",accessKeyId);
		w.write(ec2.describeAvailabilityZones(),"ec2.describeAvailabilityZones()",accessKeyId);
		System.out.println("done");

	}

}
