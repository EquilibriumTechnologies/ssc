package com.eqt.ssc.serde;

import java.lang.reflect.Type;

import com.eqt.ssc.model.SSCKey;
import com.eqt.ssc.model.SSCRecord;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Deserializer implements JsonDeserializer<SSCRecord> {
	private static Gson gson = new Gson();

	public SSCRecord deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();

		SSCRecord rec = new SSCRecord();
		rec.key = gson.fromJson(jsonObject.getAsJsonObject("key"),SSCKey.class);
		String objName = rec.key.objName;
		try {
//			System.out.println(objName);
			Class<?> payload = Class.forName(objName);
			rec.value = gson.fromJson(jsonObject.getAsJsonObject("value"), payload);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("could not locate class: " + objName);
		}
		return rec;
	}
	
	
//	    public B deserialize(JsonElement json, Type typeOfT, 
//	                         JsonDeserializationContext context)
//	                 throws JsonParseException {
//	        JsonObject jsonObject = json.getAsJsonObject();
//
//	        B b = new B();
//	        b.stringField = jsonObject.get("stringField").getAsString();
//	        b.jsonField = jsonObject.getAsJsonObject("jsonField"); 
//
//	        return b;
//	    }
//	}
}
