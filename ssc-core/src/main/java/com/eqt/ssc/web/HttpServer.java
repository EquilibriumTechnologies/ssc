package com.eqt.ssc.web;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

import com.eqt.ssc.util.Props;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * creates and sets up a jetty server for jersey rest services.
 * @author gman
 */
public class HttpServer implements Closeable {

	Server server;
	
	public HttpServer() throws Exception {
		server = new Server(Props.getPropInt("ssc.web.port", "8080"));

		ServletHolder sh = new ServletHolder(SpringServlet.class);

		sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");
		//package where all rest api's live
		sh.setInitParameter("com.sun.jersey.config.property.packages", "com.eqt.ssc.web.rest");
		sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

		// root your context in the creation of it.
		ServletContextHandler context = new ServletContextHandler(server, "/api",
				ServletContextHandler.SESSIONS);
		context.addEventListener(new ContextLoaderListener());
		context.setInitParameter("contextConfigLocation", "classpath*:**/sscContext.xml");
		// * apparently needs to be here to work.
		context.addServlet(sh, "/*");
		server.setHandler(context);
		
		server.start();
	}

	@Override
	public void close() throws IOException {
		if(server != null)
			try {
				server.stop();
			} catch (Exception e) {
				//yummy
			}
	}
}
