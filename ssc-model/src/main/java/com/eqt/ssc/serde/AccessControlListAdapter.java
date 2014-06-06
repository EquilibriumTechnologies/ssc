package com.eqt.ssc.serde;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.Grant;
import com.amazonaws.services.s3.model.Owner;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

/**
 * Stupid hashset in the ACL class comes out in different orders when left to
 * its own accord.
 * 
 * @author gman
 */
public class AccessControlListAdapter implements JsonSerializer<AccessControlList>,JsonDeserializer<AccessControlList> {

	public JsonElement serialize(AccessControlList acl, Type type, JsonSerializationContext context) {
		ArrayList<Grant> grants = new ArrayList<Grant>(acl.getGrants());
		Collections.sort(grants, new GrantComparator());

		final JsonObject wrapper = new JsonObject();
		wrapper.add("grants", context.serialize(grants));
		wrapper.add("owner", context.serialize(acl.getOwner()));

		return wrapper;
	}

	@SuppressWarnings("rawtypes")
	public AccessControlList deserialize(JsonElement elem, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject obj = (JsonObject)elem;
		
		List<Grant> grants = context.deserialize(obj.getAsJsonArray("grants"), new TypeToken<ArrayList>() {}.getType());
		Collections.sort(grants, new GrantComparator());
		AccessControlList acl = new AccessControlList();
		for(Grant g : grants)
			acl.grantPermission(g.getGrantee(), g.getPermission());
		
		Owner owner = context.deserialize(obj.getAsJsonObject("owner"), new TypeToken<Owner>() {}.getType());

		acl.setOwner(owner);
		return acl;
	}
	
	public class GrantComparator implements Comparator<Grant> {

		public int compare(Grant o1, Grant o2) {
			if (o1.getGrantee() != null && o2.getGrantee() == null)
				return -1;
			else if (o1.getGrantee() == null && o2.getGrantee() != null)
				return 1;

			if (o1.getPermission() != null && o2.getPermission() == null)
				return -1;
			else if (o1.getPermission() == null && o2.getPermission() != null)
				return 1;

			if (o1.getGrantee() == null && o2.getGrantee() == null) {
				if (o1.getPermission() == null && o2.getPermission() == null)
					return 0;
				else
					return o1.getPermission().compareTo(o2.getPermission());
			}

			// easy case
			if (o1.getGrantee().equals(o2.getGrantee()))
				if (o1.getPermission().equals(o2.getPermission()))
					return 0;

			// if identifiers match, compare on perms
			if (o1.getGrantee().getIdentifier().equals(o2.getGrantee().getIdentifier()))
				return o1.getPermission().compareTo(o2.getPermission());
			else
				return o1.getGrantee().getIdentifier().compareTo(o2.getGrantee().getIdentifier());
		}
		
	}
	
}
