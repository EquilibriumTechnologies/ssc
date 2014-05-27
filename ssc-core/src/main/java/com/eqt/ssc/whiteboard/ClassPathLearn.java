package com.eqt.ssc.whiteboard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * *SIGH* i can never remember how to load something on the classpath.
 * 
 * So the trick is if its a folder then it must not end with a /, and the file resource must start with a /
 * @author gman
 *
 */
public class ClassPathLearn {

	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Enter Path to find file: ");
		
		String line = "";
		
		if(args.length > 0)
			line = args[0];
		
		do {
			InputStream stream = System.class.getResourceAsStream(line);
			if(stream == null) {
				System.out.println("FileNotFound at: " + line);
			} else {
				BufferedReader r = new BufferedReader(new InputStreamReader(stream));
				System.out.println("First line of file: " + r.readLine());
			}
			
			if(stream != null)
				stream.close();
			
			System.out.println("Enter Path to find file: ");
		} while((line = br.readLine()) != "q") ;
		
	}

}
