package com.eqt.ssc.collector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.eqt.ssc.model.SSCAccountStatus;
import com.eqt.ssc.model.SSCKey;
import com.eqt.ssc.model.Token;
import com.eqt.ssc.state.StateEngine;
import com.eqt.ssc.util.Props;

/**
 * Collector that will grab logs being written by the s3 service across
 * accounts and put them into somewhat better formatting within s3.
 * Assumes that the token has information about what the bucket location
 * and prefix is for polling for s3 logs.
 * 
 * This collector will attempt to condense logs from s3 down into bigger files
 * and then compress them.
 * @author gman
 *
 */
public class S3LogCollector extends APICollector {

	Log LOG = LogFactory.getLog(S3LogCollector.class);
	private AmazonS3 s3;
	
	private static final String S3_LOG_LAST_MARKER = "s3.log.last.marker";
	
	//local file info
	File localFile = null;
	OutputStream os = null;
	//this is the name of the first s3log found
	String outputStreamKey = null;
	
	//this is the name of the last s3 log found
	String lastObjectName = "";
	//base path to write to
	String localPath;
	//bucket to upload to
	String bucketName;
	String bucketPrefix;
	
	//Tuning parameters.
	int maxCache = 1;
	int maxBatch = 2;
	
	public S3LogCollector(StateEngine state) {
		super(state);
		
		//TODO: have a prop to override
		localPath = System.getProperty("java.io.tmpdir");
		
		// add on extra slash
		if (!localPath.endsWith("/"))
			localPath += "/";

		// check local dir, make if it does not exist
		File localDir = new File(localPath);
		if (!localDir.exists())
			localDir.mkdirs();

		if (!localDir.isDirectory()) {
			throw new IllegalStateException("Local Dir is not a dir: " + localPath);
		}
		
		maxCache = Props.getPropInt("ssc.collector.s3logcollector.maxCache");
		maxBatch = Props.getPropInt("ssc.collector.s3logcollector.maxBatch");
	}

	private void init(Token token) {
		s3 = new AmazonS3Client(token.getCredentials());
		s3.setRegion(RegionUtils.getRegion(Props.getProp("ssc.s3.bucket.region")));

		bucketName = token.getS3BucketName();
		bucketPrefix = token.getS3Path();
	}
	
	@Override
	public SSCAccountStatus collect(Token token) {
		//finish init
		init(token);
		
		SSCAccountStatus status = new SSCAccountStatus(token);
		
		long totalBytes = 0;
		int totalFiles = 0;
		long start = System.currentTimeMillis();
		
		//its possible to not have a bucket setup for S3Logging, fire a warn and return.
		if(bucketName == null) {
			LOG.warn("S3Log collecting is enabled but no bucket to pull from is defined for account: " +  token.getAccountId());
			return status;
		}

		LOG.debug("listing s3logs for bucket: " + bucketName + " with prefix: " + bucketPrefix);

		// get the first object listing
		ListObjectsRequest req = new ListObjectsRequest();
		req.setBucketName(bucketName);
		req.setPrefix(bucketPrefix);
		req.setMaxKeys(100);
		
		//lookup to see if we have a previous marker to start at. (basically last object copied)
		//NOTE: marker has to be full path, prefix doesnt apply to it for whatever reason.
		String marker = token.getAttributes().get(S3_LOG_LAST_MARKER);
		LOG.debug("last marker: " + marker);
		if(marker != null)
			if(bucketPrefix.endsWith("/"))
				req.setMarker(bucketPrefix + marker);
			else
				req.setMarker(bucketPrefix + "/" + marker);

		ObjectListing listObjects = s3.listObjects(req);

		do {
			for (S3ObjectSummary objectSummary : listObjects.getObjectSummaries()) {
				S3Object object = s3.getObject(bucketName, objectSummary.getKey());
				S3ObjectInputStream inputStream = object.getObjectContent();

				// plain txt files
				if ("text/plain".equals(object.getObjectMetadata().getContentType())) {
					try {

						OutputStream outputStream = getOS(object.getKey());

						//TODO: this creates a SocketException (connection reset). Trap and retry
						IOUtils.copy(inputStream, outputStream);
						totalFiles++;
						totalBytes += object.getObjectMetadata().getContentLength();
						LOG.info("wrote file: " + object.getKey());
					} catch (IOException e) {
						LOG.error("errors writing to files", e);
						throw new IllegalStateException("files are too hard", e);
					} finally {
						IOUtils.closeQuietly(inputStream);
					}
					
					//TODO: gzip!
				} else {
					LOG.warn("unhandled content encoding: " + object.getObjectMetadata().getContentEncoding());
				}
				
				//batch check
				if(totalFiles >= maxBatch)
					break;
			}

			listObjects = s3.listNextBatchOfObjects(listObjects);
		} while (listObjects.isTruncated() && totalFiles < maxBatch);

		//see if we wrote anything
		if(totalFiles > 0) {
			// all done writing file out, close OS, then push to s3.
			try {
				LOG.info("finished writting file: " + outputStreamKey);
				OutputStream os2 = getOS(null);
				os2.flush();
				os2.close();
				String objName = outputStreamKey + "-" + lastObjectName + ".gz";
				SSCKey key = new SSCKey("s3", "s3logs", token.getAccountId(), "unknown");
				//TODO: retry more than once on error.
				state.writeFile(key, objName, localFile); 
				localFile.delete();
				token.addAttribute(S3_LOG_LAST_MARKER, lastObjectName);
			} catch (IOException e) {
				LOG.error("could not write to S3",e);
				throw new RuntimeException(e);
			}
		}

		long now = System.currentTimeMillis();
		LOG.debug(totalFiles + " files downloaded:  " + (totalBytes / 1000.0 / 1000.0) + " mb downloaded in "
				+ ((now - start) / 1000) + " seconds.");

		status.changes = totalFiles;
		return status;
	}

	@Override
	protected String getCustomIntervalProperty() {
		return "ssc.account.check.interval.s3log.seconds";
	}

	//Terribly done function, does way to much hidden work
	private OutputStream getOS(String key) throws IOException {
		if(key != null) {
			if (key.contains("/"))
				key = key.substring(key.lastIndexOf("/") + 1);
			lastObjectName = key;
		}
		if (os == null) {
			// create one with the path given
			String path = localPath + key + ".gz";

			// create a local file:
			localFile = new File(path);
			File parentFile = localFile.getParentFile();
			if (!parentFile.exists())
				parentFile.mkdirs();
			//if a failed run is still around, this will cleanup
			if(localFile.exists())
				localFile.delete();
			outputStreamKey = key;
			os = new GZIPOutputStream(new FileOutputStream(localFile), 8192, true);
		}
		return os;
	}
}
