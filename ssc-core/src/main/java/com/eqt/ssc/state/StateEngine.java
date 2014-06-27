package com.eqt.ssc.state;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.eqt.ssc.model.SSCKey;
import com.eqt.ssc.model.SSCRecord;
import com.eqt.ssc.serde.RecordBuilder;
import com.eqt.ssc.util.Props;

/**
 * This is the shim that will wrap whatever I need to track as much as I can
 * in memory and local storage
 * 
 * @author gman
 *
 */
public class StateEngine {
	protected String bucketName;
	protected String regionName;
//	protected Bucket bucket;
	protected boolean bucketCreated = false;
	protected AmazonS3 s3;
	protected static final DateTimeFormatter formatter = DateTimeFormat.forPattern("/YYYY/MM/dd/YYYY-MM-dd-HH-mm-ss-SSS");
	
	protected static final Log LOG = LogFactory.getLog(StateEngine.class);
	
	//map to hold the last known values
	protected Map<String, String> valueMap = new HashMap<String,String>();
	
	public StateEngine(AmazonS3 s3) {
		this.s3 = s3;
		this.bucketName = Props.getProp("ssc.s3.bucket.name");
		this.regionName = Props.getProp("ssc.s3.bucket.region");
		
		LOG.info("S3 SSCLOG Bucket: " + bucketName + " in region: " + regionName);
		
		//check for existence of bucket to write to
		if(!s3.doesBucketExist(bucketName)) {
			LOG.info("no bucket, creating");
			Region region = Region.valueOf(Props.getProp("ssc.s3.bucket.region"));
			Bucket bucket = null;
			try {
				bucket = s3.createBucket(bucketName, region);
				//TODO: this should probably set logging into this bucket?????
			} catch(AmazonS3Exception e) {
				throw new IllegalStateException("bucket doesnt exist and could not create it: " + bucketName, e);
			}
			if(bucket != null) {
				LOG.info("bucket created, NOTE THERE IS NO LOGGING POLICY SETUP!");
			} else
				throw new IllegalStateException("bucket doesnt exist and could not create it: " + bucketName);
		}
		LOG.info("ready");
	}
	
	/**
	 * Writes the given record out.
	 * @param record is a record that has new state compared to its last instance.
	 */
	public void writeNewRecord(SSCRecord record) {
		String fileName = buildNewFileName(record.key);
		String json = RecordBuilder.serialize(record);
		LOG.debug("creating file: " + fileName);
		LOG.debug("json: " + json);
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			
			//we are loading data zipped into S3, every byte costs after all!
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			
			//used to force encoding
			Writer writer = new OutputStreamWriter(gzip,"UTF-8");
			writer.write(json);
			writer.flush();
			writer.close();

			ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
			LOG.info(fileName + " compressed size: " + out.size() + "/" + json.getBytes().length);
			
			//setting metadata so that we can view and access the object in S3 correctly.
			ObjectMetadata meta = new ObjectMetadata();
			meta.setContentLength(out.size());
			meta.setContentType("application/json");
			meta.setContentEncoding("gzip");
			S3Object s3Obj = new S3Object();
			s3Obj.setObjectContent(in);
			s3.putObject(new PutObjectRequest(bucketName, fileName, in, meta));
			s3Obj.close();
			
			//store into our lookup map for next time.
			valueMap.put(mapKey(record.key), json);
			
		} catch (IOException e) {
			//TODO: at least try another time or two...
			throw new IllegalStateException("I cant write, I cant zip, only thing I can do is complain.");
		}
	}
	

	/**
	 * This is used for moving bulk file downloads into the logging S3 bucket. It still requires 
	 * an account as it will still log underneath an accounts area.
	 * @param account  account to associate file with
	 * @param key object name to give file
	 * @param file file to upload.
	 */
	public void writeFile(SSCKey record, String key, File file) {
		//TODO: retry a few times?
		s3.putObject(key, key, file);
	}
	
	public String getLastKnown(SSCKey newRecord) {
		String last = valueMap.get(mapKey(newRecord));
		//hitup s3
		if(last == null) {
//			LOG.debug("not in local cache, fetching from S3");
			last = getFromFS(newRecord);
			if(last != null)
				valueMap.put(mapKey(newRecord), last);
		}
		return last;
	}
	
	//returns a unique key to check in the map for
	protected String mapKey(SSCKey key) {
		return key.accountId + key.methodName;
	}
	
	/**
	 * When local copy does not have any record, hit up FileSystem
	 * to see if its there.
	 * TODO: make it switch between S3 and a given mount point?
	 * @param newRecord the key for your new record to find a previous record for.
	 * @return
	 */
	protected String getFromFS(SSCKey newRecord) {
		//storage format is: SSCLogs/AccountID/region/call/year/month/day/timestamp
		//we add another '/' to make sure we dont collide with a path like blabla when we just want bla.
		String basePath = buildBasePath(newRecord) + "/";
		LOG.debug("basePath for retrieval: " + basePath);
		ObjectListing objects = s3.listObjects(bucketName, basePath);
		
		//walk to the last one
		while(objects.isTruncated()) {
			LOG.debug("still looping path: " + objects.getNextMarker());
			objects = s3.listNextBatchOfObjects(objects);
		}
		
		//we now have the last listing, get objects.
		List<S3ObjectSummary> objectSummaries = objects.getObjectSummaries();
		//null check, was nothing there to begin with.
		if(objectSummaries == null || objectSummaries.size() == 0)
			return null;
		
		//actually had an object, get the last one
		S3ObjectSummary objectSummary = objectSummaries.get(objectSummaries.size()-1);
		LOG.debug("retrieving file: " + objectSummary.getKey());
		S3Object object = s3.getObject(bucketName, objectSummary.getKey());
		S3ObjectInputStream inputStream = object.getObjectContent();
		String json = "";
		try {
			GZIPInputStream in = new GZIPInputStream(inputStream);
			//used to force encoding
			BufferedReader reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line = null;
			while((line = reader.readLine()) != null) {
				json += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				LOG.info("troubles closing file handle for: " + objectSummary.getKey());
			}
		}
		return json;
	}
	
	/**
	 * builds the base path to pull from.
	 * @param key
	 * @return
	 */
	protected String buildBasePath(SSCKey key) {
		String path = "SSCLogs/" + key.accountId + "/" + regionName + "/" + key.methodName;
		return path;
	}
	
	protected String buildNewFileName(SSCKey key) {
		String path = buildBasePath(key);
		path += formatter.print(key.requestTime);
		path+=".json.gz";
		return path;
	}
	
	
}
