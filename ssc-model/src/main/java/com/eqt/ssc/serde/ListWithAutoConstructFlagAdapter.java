package com.eqt.ssc.serde;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.amazonaws.internal.ListWithAutoConstructFlag;
import com.amazonaws.services.s3.model.AccessControlList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * AWS has this list that they use 
 * @author gman
 *
 */
public class ListWithAutoConstructFlagAdapter implements JsonSerializer<ListWithAutoConstructFlag<? extends Object>> {

	@Override
	public JsonElement serialize(ListWithAutoConstructFlag<? extends Object> autoList, Type type,
			JsonSerializationContext context) {
		List<JsonElement> elements = new ArrayList<JsonElement>();
		for(Object o : autoList) {
			elements.add(context.serialize(o));
		}
		Collections.sort(elements,new Comparator<JsonElement>() {

			@Override
			public int compare(JsonElement arg0, JsonElement arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}
			
		});
		
		JsonArray arr = new JsonArray();
		for(JsonElement e : elements)
			arr.add(e);
		return arr;
	}
}
