package com.eqt.ssc.whiteboard;

/**
 * sometimes i forget how classes work
 *
 */
public class ClassesLearn {

	public static class A {
		
		public String getName() {
			return this.getClass().getSimpleName();
		}
	}
	
	public static class B extends A {
		
	}
	
	public static void main(String[] args) {
		
		B b = new B();
		System.out.println(b.getName());
		
	}
}
