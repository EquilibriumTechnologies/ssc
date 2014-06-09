package com.eqt.ssc.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
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
public class S3Downloader {

	public static void main(String[] args) {

		if (args.length < 2) {
			System.out.println("USAGE: localPath bucketname <bucketPrefix>");
			System.exit(1);
		}

		String localPath = args[0];
		String bucketName = args[1];
		String bucketPrefix = "";

		//add on extra slash
		if(!localPath.endsWith("/"))
			localPath += "/";
		
		if (args.length == 3)
			bucketPrefix = args[2];
		
		//check local dir, make if it does not exist
		File localDir = new File(localPath);
		if(!localDir.exists())
			localDir.mkdirs();
		
		if(!localDir.isDirectory()) {
			System.out.println("Local Dir is not a dir: " + localPath);
			System.exit(1);
		}

		long totalBytes = 0;
		long start = System.currentTimeMillis();
		
		AmazonS3 s3 = new AmazonS3Client(
				new ClasspathPropertiesFileCredentialsProvider());

		ObjectListing listObjects = s3.listObjects(bucketName, bucketPrefix);
		do {
			for (S3ObjectSummary objectSummary : listObjects.getObjectSummaries()) {
				S3Object object = s3.getObject(bucketName, objectSummary.getKey());
				S3ObjectInputStream inputStream = object.getObjectContent();
				if("gzip".equals(object.getObjectMetadata().getContentEncoding())) {
					InputStream in = null;
					try {
						
						totalBytes += object.getObjectMetadata().getContentLength();

						in = new GZIPInputStream(inputStream);
						//write this sucker out
						String path = localPath + object.getKey();
						//have to take the gz off since this is not downloading compressed!
						if(path.endsWith(".gz"))
							path = path.substring(0,path.length() - 3);
						System.out.print("Writing file: " + path);
						File check = new File(path);
						File parentFile = check.getParentFile();
						if(!parentFile.exists())
							parentFile.mkdirs();
						
						FileOutputStream out = new FileOutputStream(path);
						IOUtils.copy(in, out);
						System.out.println(" written.");
						
					} catch (IOException e) {
						System.out.println("crap");
						e.printStackTrace();
						throw new IllegalStateException("files are too hard",e);
					} finally {
						IOUtils.closeQuietly(in);
					}
				} else {
					System.out.println("unhandled content encoding: " + object.getObjectMetadata().getContentEncoding());
				}
			}

			listObjects = s3.listNextBatchOfObjects(listObjects);
		} while (listObjects.isTruncated());
		
		long now = System.currentTimeMillis();
		System.out.println((totalBytes / 1000.0/ 1000.0) + " mb downloaded in " + ((now -start)/1000) + " seconds.");

	}
}
