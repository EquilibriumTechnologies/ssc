package com.eqt.ssc.serde;

import java.lang.reflect.Type;

import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.amazonaws.services.s3.model.EmailAddressGrantee;
import com.amazonaws.services.s3.model.Grantee;
import com.google.gson.InstanceCreator;

public class GranteeInstanceCreator implements InstanceCreator<Grantee> {

	public Grantee createInstance(Type type) {
		System.out.println("INSTANCE TYPE 1 " + type.toString());
		return null;
	}
}
