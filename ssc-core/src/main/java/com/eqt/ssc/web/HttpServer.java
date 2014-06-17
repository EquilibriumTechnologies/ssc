package com.eqt.ssc.web;

import java.io.Closeable;
import java.io.IOException;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.web.context.ContextLoaderListener;

import com.eqt.ssc.util.Props;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;

/**
 * creates and sets up a jetty server for jersey rest services.
 * 
 * @author gman
 */
public class HttpServer implements Closeable {

	Server server;

	public HttpServer() throws Exception {
		server = new Server(Props.getPropInt("ssc.web.port"));

		ServletHolder sh = new ServletHolder(SpringServlet.class);

		if (Props.getPropBoolean("ssc.web.ssl")) {
			HttpConfiguration https = new HttpConfiguration();
			https.addCustomizer(new SecureRequestCustomizer());

			SslContextFactory sslContextFactory = new SslContextFactory();
			sslContextFactory.setKeyStorePath(HttpServer.class.getResource(Props.getProp("ssc.web.keystore")).toExternalForm());
			sslContextFactory.setKeyStorePassword(Props.getProp("ssc.web.keystore.password"));
			sslContextFactory.setKeyManagerPassword(Props.getProp("ssc.web.keymanager.password"));

			ServerConnector sslConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory,
					"http/1.1"), new HttpConnectionFactory(https));
			sslConnector.setPort(Props.getPropInt("ssc.web.port"));
			server.setConnectors(new Connector[] { sslConnector });
		}

		sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");
		// package where all rest api's live
		sh.setInitParameter("com.sun.jersey.config.property.packages", "com.eqt.ssc.web.rest");
		sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

		// root your context in the creation of it.
		ServletContextHandler context = new ServletContextHandler(server, "/api", ServletContextHandler.SESSIONS);
		context.addEventListener(new ContextLoaderListener());
		context.setInitParameter("contextConfigLocation", "classpath*:**/sscContext.xml");
		// * apparently needs to be here to work.
		context.addServlet(sh, "/*");
		server.setHandler(context);

		server.start();
	}

	@Override
	public void close() throws IOException {
		if (server != null)
			try {
				server.stop();
			} catch (Exception e) {
				// yummy
			}
	}
}
