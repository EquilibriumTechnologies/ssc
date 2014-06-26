package com.eqt.ssc.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

/***
 * utility that will take an s3 bucket and mirror it locally.
 * 
 * @author gman
 * 
 */
public class S3LogDownloader {
	Log LOG = LogFactory.getLog(S3LogDownloader.class);

	AmazonS3 s3;
	String localPath;
	String bucketName;
	String bucketPrefix;

	public S3LogDownloader(String localPath, String bucketName, String bucketPrefix) {

		// add on extra slash
		if (!localPath.endsWith("/"))
			localPath += "/";

		// check local dir, make if it does not exist
		File localDir = new File(localPath);
		if (!localDir.exists())
			localDir.mkdirs();

		if (!localDir.isDirectory()) {
			System.out.println("Local Dir is not a dir: " + localPath);
			System.exit(1);
		}
		this.localPath = localPath;
		this.bucketName = bucketName;
		this.bucketPrefix = bucketPrefix;

		s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
	}

	OutputStream os = null;
	String outputStreamKey = null;
	String lastObjectName = "";

	public OutputStream getOS(String key) throws IOException {
		if(key != null)
			if (key.contains("/"))
				key = key.substring(key.lastIndexOf("/") + 1);
		if (os == null) {
			// create one with the path given
			String path = localPath + key + ".gz";

			// create a local file:
			File check = new File(path);
			File parentFile = check.getParentFile();
			if (!parentFile.exists())
				parentFile.mkdirs();
			outputStreamKey = key;
			// os = new FileOutputStream(path);
			os = new GZIPOutputStream(new FileOutputStream(path), 8192, true);
		}
		lastObjectName = key;
		return os;
	}

	public int collect() {

		long totalBytes = 0;
		int totalFiles = 0;
		long start = System.currentTimeMillis();


		LOG.debug("listing s3logs for bucket: " + bucketName + " with prefix: " + bucketPrefix);

		// get the first object listing
		// ObjectListing listObjects = s3.listObjects(bucketName, bucketPrefix);
		ListObjectsRequest req = new ListObjectsRequest();
		req.setBucketName(bucketName);
		req.setPrefix(bucketPrefix);
		req.setMarker(bucketPrefix + "2014-06-24-14-24-23-BD60F43E244705D6");
		ObjectListing listObjects = s3.listObjects(req);

		do {
			for (S3ObjectSummary objectSummary : listObjects.getObjectSummaries()) {
				S3Object object = s3.getObject(bucketName, objectSummary.getKey());
				S3ObjectInputStream inputStream = object.getObjectContent();

				// plain txt files
				if ("text/plain".equals(object.getObjectMetadata().getContentType())) {
					try {

						OutputStream outputStream = getOS(object.getKey());

						IOUtils.copy(inputStream, outputStream);
						lastObjectName = object.getKey();
						totalFiles++;
						totalBytes += object.getObjectMetadata().getContentLength();
						LOG.info("wrote file: " + object.getKey());
					} catch (IOException e) {
						// TODO: need to log error event
						LOG.error("errors writing to files", e);
						throw new IllegalStateException("files are too hard", e);
					} finally {
						IOUtils.closeQuietly(inputStream);
					}
				} else {
					System.out
							.println("unhandled content encoding: " + object.getObjectMetadata().getContentEncoding());
				}
			}

			listObjects = s3.listNextBatchOfObjects(listObjects);
		} while (listObjects.isTruncated());

		// all done writing file out, close OS, then push to s3.
		try {
			LOG.info("finished writting file: " + outputStreamKey);
			OutputStream os2 = getOS(null);
			os2.flush();
			os2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long now = System.currentTimeMillis();
		System.out.println(totalFiles + " files downloaded:  " + (totalBytes / 1000.0 / 1000.0) + " mb downloaded in "
				+ ((now - start) / 1000) + " seconds.");

		return totalFiles;
	}

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("USAGE: localPath bucketname <bucketPrefix>");
			System.exit(1);
		}

		String localPath = args[0];
		String bucketName = args[1];
		String bucketPrefix = "";

		if (args.length == 3)
			bucketPrefix = args[2];

		S3LogDownloader downloader = new S3LogDownloader(localPath, bucketName, bucketPrefix);
		downloader.collect();
	}
}
