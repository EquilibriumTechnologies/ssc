package com.eqt.ssc.serde;


import org.junit.Test;

import com.amazonaws.internal.ListWithAutoConstructFlag;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static org.junit.Assert.*;

public class ListWithAutoConstructFlagAdapterTest {

	@Test
	public void testSer() {
		ListWithAutoConstructFlag<String> list1 = new ListWithAutoConstructFlag<String>();
		list1.add("foo");
		list1.add("bar");
		ListWithAutoConstructFlag<String> list2 = new ListWithAutoConstructFlag<String>();
		list2.add("bar");
		list2.add("foo");

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ListWithAutoConstructFlag.class,new ListWithAutoConstructFlagAdapter());
		Gson gson = builder.create();
//		System.out.println(gson.toJson(list1));
//		System.out.println(gson.toJson(list2));
		assertEquals(gson.toJson(list1), gson.toJson(list2));
	}

	
	@Test
	public void testNestedSer() {
		ListWithAutoConstructFlag<ListWithAutoConstructFlag<String>> d1 = new ListWithAutoConstructFlag<ListWithAutoConstructFlag<String>>();
		
		ListWithAutoConstructFlag<String> d11 = new ListWithAutoConstructFlag<String>();
		d11.add("foo1");
		d11.add("bar1");
		ListWithAutoConstructFlag<String> d12 = new ListWithAutoConstructFlag<String>();
		d12.add("bar2");
		d12.add("foo2");
		d1.add(d11);
		d1.add(d12);
		
		ListWithAutoConstructFlag<ListWithAutoConstructFlag<String>> d2 = new ListWithAutoConstructFlag<ListWithAutoConstructFlag<String>>();
		
		ListWithAutoConstructFlag<String> d21 = new ListWithAutoConstructFlag<String>();
		d21.add("foo2");
		d21.add("bar2");
		ListWithAutoConstructFlag<String> d22 = new ListWithAutoConstructFlag<String>();
		d22.add("bar1");
		d22.add("foo1");
		d2.add(d21);
		d2.add(d22);
		
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(ListWithAutoConstructFlag.class,new ListWithAutoConstructFlagAdapter());
		Gson gson = builder.create();
//		System.out.println(gson.toJson(d1));
//		System.out.println(gson.toJson(d2));
		assertEquals(gson.toJson(d1), gson.toJson(d2));

	}
	
	
}
