package com.eqt.ssc.whiteboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class HttpJettyServer {

	public static final Log LOG = LogFactory.getLog(HttpJettyServer.class);

	@Path("/")
	public static class Res {

		static Map<String, Payload> map = new HashMap<String, Payload>();

		@GET
		@Path("payloads")
		@Produces(MediaType.APPLICATION_JSON)
		public Payloads getPayloads() {
			LOG.info("getPayloads");

//			ArrayList<Payload> list = new ArrayList<Payload>(map.values());
//			LOG.info("list: " + list.size());
//			GenericEntity<List<Payload>> entity = new GenericEntity<List<Payload>>(list) {
//			};
//			return Response.ok(entity).build();
			Payloads p = new Payloads();
			p.setPayloads(new ArrayList<Payload>(map.values()));
			 return p;
		}

		
		@GET
		@Path("payload/{id}")
		@Produces(MediaType.APPLICATION_JSON)
		public Payload getPayload(@PathParam("id") String id) {
			LOG.info("getPayload");
			if (id != null) {
				LOG.info("getPayload ID: " + id + " " + map.get(id));
				return map.get(id);
			}
			return null;
		}

		@Path("payload")
		@Consumes(MediaType.APPLICATION_JSON)
		@POST
		public Response putPayload(Payload p) {
			LOG.info("putPayloads");
			if (p != null) {
				LOG.info("putPayloads: " + p);
				map.put(p.name, p);
				return Response.ok().build();
			}
			LOG.info("putPayloads: no data");
			return Response.status(Status.NO_CONTENT).build();
		}

	}

	public static void main(String[] args) throws Exception {

		Server server = new Server(8182);

		ServletHolder sh = new ServletHolder(ServletContainer.class);
		sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass",
				"com.sun.jersey.api.core.PackagesResourceConfig");
		sh.setInitParameter("com.sun.jersey.config.property.packages", "com.eqt.ssc.whiteboard");// Set
																									// the
																									// package
																									// where
																									// the
																									// services
																									// reside
		sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

		//root your context in the creation of it.
		ServletContextHandler context = new ServletContextHandler(server, "/api/firstSteps", ServletContextHandler.SESSIONS);
		//* apparently needs to be here to work.
		context.addServlet(sh, "/*");
		server.setHandler(context);

		//multiple contexts can be added this way.
//		ContextHandlerCollection contexts = new ContextHandlerCollection();
//        contexts.setHandlers(new Handler[] { context0, webapp });
//        server.setHandler(contexts);
		
		// WebAppContext bb = new WebAppContext();
		// bb.setServer(server);
		// bb.setContextPath("/");
		// bb.setWar("src/main/webapp");
		// server.setHandler(bb);

		try {
			System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
			server.start();
			while (System.in.available() == 0) {
				Thread.sleep(5000);
			}
			server.stop();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}
}
