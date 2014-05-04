package com.eqt.ssc.serde;

import java.lang.reflect.Type;

import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Grantee;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.Permission;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GrantAdapter implements JsonSerializer<Grant>, JsonDeserializer<Grant> {
	
	//TODO: use this
	static final String _GRANTEE_JSON_NAME = "granteeTest"; //"granteeWrapper";

	public JsonElement serialize(Grant grant, Type type, JsonSerializationContext context) {
		final JsonObject wrapper = new JsonObject();
		final JsonObject grantee = new JsonObject();
		if(grant.getGrantee() != null) {
//			System.out.println("GRANT.grantee: " + grant.getGrantee().getClass().getName());
			grantee.addProperty("type", grant.getGrantee().getClass().getName());
			grantee.add("data", context.serialize(grant.getGrantee()));
			wrapper.add(_GRANTEE_JSON_NAME, grantee);
		}
		wrapper.add("permission", context.serialize(grant.getPermission()));
		return wrapper;
	}

	public Grant deserialize(JsonElement elem, Type type, JsonDeserializationContext context) throws JsonParseException {
		System.out.println("deserialize");
		final JsonObject wrapper = (JsonObject) elem;

		Grantee g = null;

		if(wrapper.has(_GRANTEE_JSON_NAME)) {
			final JsonObject grantee = wrapper.get(_GRANTEE_JSON_NAME).getAsJsonObject();
			final JsonElement typeName = grantee.get("type");
			final JsonElement data = grantee.get("data");
			
//			System.out.println("grantee type: " + typeName);
			
			if("com.amazonaws.services.s3.model.GroupGrantee".equals(typeName)) {
				//this is an enum, grab data
				g = GroupGrantee.valueOf(data.getAsString());
			} else if("com.amazonaws.services.s3.model.CanonicalGrantee".equals(typeName)) {
				g = context.deserialize(data, typeForName(typeName));
			} else if("com.amazonaws.services.s3.model.EmailAddressGrantee".equals(typeName)) {
				g = context.deserialize(data, typeForName(typeName));
			} else {
				throw new JsonParseException("i dont know what you gave me, : " + typeName);
			}		}

		
		Permission p = context.deserialize(wrapper.get("permission"),Permission.class);
		return new Grant(g, p);
	}
	
	
	private Type typeForName(final JsonElement typeElem) {
		try {
			return Class.forName(typeElem.getAsString());
		} catch (ClassNotFoundException e) {
			throw new JsonParseException(e);
		}
	}
}
