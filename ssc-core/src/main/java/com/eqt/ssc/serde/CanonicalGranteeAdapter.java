package com.eqt.ssc.serde;

import java.lang.reflect.Type;

import com.amazonaws.services.s3.model.CanonicalGrantee;
import com.google.gson.InstanceCreator;

public class CanonicalGranteeAdapter implements InstanceCreator<CanonicalGrantee> {

	public CanonicalGrantee createInstance(Type type) {
		return new CanonicalGrantee("placeholder");
	}
	

}
