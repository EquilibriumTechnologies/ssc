package com.eqt.ssc.serde;

import java.io.File;
import java.io.Reader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Grantee;
import com.eqt.ssc.model.SSCKey;
import com.eqt.ssc.model.SSCRecord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * TODO: singleton this thing.
 * @author gman
 *
 */
public class RecordBuilder {

	private static String apiVersion;
	private static Gson gson;
	private static JsonParser parser = new JsonParser();
	
	private static final Log LOG = LogFactory.getLog(RecordBuilder.class);
	
	static {
		GsonBuilder builder = new GsonBuilder();
//		builder.registerTypeAdapter(CanonicalGrantee.class, new GranteeInstanceCreator());
//		builder.registerTypeAdapter(EmailAddressGrantee.class, new GranteeInstanceCreator());
//		builder.registerTypeAdapter(GroupGrantee.class, new GranteeInstanceCreator());
//		builder.registerTypeAdapter(Grantee.class, new InterfaceAdapter<Grantee>());
//		builder.registerTypeAdapter(CanonicalGrantee.class, new CanonicalGranteeAdapter());
		builder.registerTypeAdapter(AccessControlList.class, new AccessControlListAdapter());
		builder.registerTypeAdapter(Grantee.class, new GranteeAdapter());
		builder.registerTypeAdapter(Grant.class, new GrantAdapter());
		builder.registerTypeHierarchyAdapter(SSCRecord.class, new Deserializer());
		gson = builder.create();

		String[] parts = System.getProperty("java.class.path").split(File.pathSeparator);
		FileSystem fs = FileSystems.getDefault();
		String separator = fs.getSeparator();
		
		for(String part : parts) {
			int lastSlash = part.lastIndexOf(separator);
			if(part.substring(lastSlash+1).toLowerCase().startsWith("aws-java")) {
				RecordBuilder.apiVersion = part.substring(lastSlash+1,part.lastIndexOf("."));
				LOG.info("AWS JAVA API verison found: " + RecordBuilder.apiVersion);
			}
		}
	}
	
	private RecordBuilder() {}
		
	public static SSCRecord createRecord(Object obj, String methodName, String acctId) {
		return createRecord(obj,methodName,acctId,System.currentTimeMillis());
	}
	
	public static SSCRecord createRecord(Object obj, String methodName, String acctId, long requestTime) {
		SSCKey k = new SSCKey(obj.getClass().getName(), methodName, acctId, RecordBuilder.apiVersion,requestTime);
		SSCRecord rec = new SSCRecord(k,obj);
		return rec;
	}
	
	public static SSCRecord read(String json) {
		SSCRecord in = gson.fromJson(json, SSCRecord.class);
		return in;
	}
	
	public static SSCRecord read(Reader json) {
		SSCRecord in = gson.fromJson(json, SSCRecord.class);
		return in;
	}
	
	public static String serialize(Object record) {
		return gson.toJson(record);
	}
	
	public static JsonElement treeObject(Object record) {
		return gson.toJsonTree(record);
	}
	
	/**
	 * rips the value portion of a serialized version of an SSCRecord and returns
	 * it as found, not converted back to an object.
	 * TODO: check for actually being an SSCrecord first.
	 * @param record
	 * @return
	 */
	public static String valueAsJson(String record) {
		JsonElement jsonElement = parser.parse(record);
		return jsonElement.getAsJsonObject().getAsJsonObject("value").toString();
	}
	
	/**
	 * if gson's internal equals test works right this could be awesome.
	 * Assumes the user was smart enough to actually pass in the same json types
	 * ie: object, object or array,array.  Object,array would be bad.
	 * @param json1
	 * @param json2
	 * @return
	 */
	public static boolean equality(String json1, String json2) {
		JsonElement jElem1 = parser.parse(json1);
		JsonElement jElem2 = parser.parse(json2);
		
		if(jElem1.isJsonObject())
			return jElem1.getAsJsonObject().equals(jElem2.getAsJsonObject());
		if(jElem1.isJsonArray())
			return jElem1.getAsJsonArray().equals(jElem2.getAsJsonArray());
		if(jElem1.isJsonPrimitive())
			return jElem1.getAsJsonPrimitive().equals(jElem2.getAsJsonPrimitive());
		else
			return jElem1.getAsJsonNull().equals(jElem2.getAsJsonNull());
//		ObjectMapper mapper = new ObjectMapper();
//		try {
//			System.out.println(mapper.readTree(json1));
//			System.out.println(mapper.readTree(json2));
//			return mapper.readTree(json1).equals(mapper.readTree(json2));
//		} catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return false;
	}
	
	
	public static void main(String[] args) {
		String json1 = "{'arr':['yes','no']}";
		String json2 = "{'arr':['no','yes']}";
		System.out.println(RecordBuilder.equality(json1.replaceAll("'", "\""), json2.replaceAll("'", "\"")));
		
		json1 = "{'a':{'a1':'a2'},'b':{'b1':'b2'}}";
		json2 = "{'b':{'b1':'b2'},'a':{'a1':'a2'}}";
		System.out.println(RecordBuilder.equality(json1.replaceAll("'", "\""), json2.replaceAll("'", "\"")));

		json1 = "{'arr':[{'a1':'a2'},{'b1':'b2'}]}";
		json2 = "{'arr':[{'b1':'b2'},{'a1':'a2'}]}";
		System.out.println(RecordBuilder.equality(json1.replaceAll("'", "\""), json2.replaceAll("'", "\"")));
		
	}
}
