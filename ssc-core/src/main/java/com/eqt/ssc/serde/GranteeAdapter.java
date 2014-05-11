package com.eqt.ssc.serde;

import java.lang.reflect.Type;

import com.amazonaws.services.s3.model.Grantee;
import com.google.gson.InstanceCreator;

public class GranteeAdapter  implements InstanceCreator<Grantee> {

	public Grantee createInstance(Type type) {
		System.out.println("booom boom boom");
		return null;
	}
}
