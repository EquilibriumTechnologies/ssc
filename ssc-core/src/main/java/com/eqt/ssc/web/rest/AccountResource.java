package com.eqt.ssc.web.rest;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.eqt.ssc.accounts.AccountManager;
import com.eqt.ssc.accounts.AccountManagerFactory;
import com.eqt.ssc.model.SSCAccount;
import com.eqt.ssc.model.Token;

@Path("/")
public class AccountResource {

	//our 'dao'
	AccountManager man = AccountManagerFactory.getInstance();
	
	public static class ManagedAccounts {
		public List<String> accounts = new ArrayList<String>();
	}
	
	@GET
	@Path("managedAccounts")
	@Produces(MediaType.APPLICATION_JSON)
	public ManagedAccounts getManagedAccounts() {
		ManagedAccounts m = new ManagedAccounts();
		List<Token> accounts = man.getAccounts();
		for(Token t : accounts)
			m.accounts.add(t.getAccountId());
		return m;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("addAccount")
	public Response putAccount(SSCAccount account) {
		if(account != null) {
			try {
				man.addAccount(account);
			} catch (UnsupportedOperationException e) {
				return Response.status(Status.NOT_ACCEPTABLE).build();
			}
			return Response.ok().build();
		}
		return Response.status(Status.BAD_REQUEST).build();
	}

}
