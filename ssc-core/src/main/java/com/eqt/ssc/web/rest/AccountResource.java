package com.eqt.ssc.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eqt.ssc.accounts.AccountManager;
import com.eqt.ssc.accounts.AccountManagerFactory;
import com.eqt.ssc.model.SSCAccount;
import com.eqt.ssc.model.Token;

@Path("/")
public class AccountResource {
	
	Log LOG = LogFactory.getLog(AccountResource.class);

	//our 'dao'
	AccountManager man = AccountManagerFactory.getInstance();
	
	public static class ManagedAccounts {
		public List<String> accounts = new ArrayList<String>();
	}
	
	@GET
	@Path("allAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public ManagedAccounts getAllAccounts() {
		LOG.info("getAllAccounts called");
		ManagedAccounts m = new ManagedAccounts();
		List<SSCAccount> accounts = man.getKnownAccounts();
		for(SSCAccount a : accounts)
			m.accounts.add(a.getAccountId());
		return m;
	}
	
	@GET
	@Path("managedAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public ManagedAccounts getManagedAccounts() {
		LOG.info("getManagedAccounts called");
		ManagedAccounts m = new ManagedAccounts();
		List<Token> accounts = man.getAccounts();
		for(Token t : accounts)
			m.accounts.add(t.getAccountId());
		return m;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("addAccount")
	public Response putAccount(SSCAccount account) {
		if(account != null) {
			try {
				LOG.info("ADD account request: " + account);
				man.addAccount(account);
			} catch (UnsupportedOperationException e) {
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
			return Response.ok().build();
		}
		return Response.status(Status.BAD_REQUEST).build();
	}

}
