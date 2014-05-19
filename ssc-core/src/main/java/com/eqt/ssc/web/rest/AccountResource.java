package com.eqt.ssc.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.eqt.ssc.accounts.AccountManager;
import com.eqt.ssc.accounts.AccountManagerFactory;
import com.eqt.ssc.model.SSCAccount;

@Path("/")
public class AccountResource {

	//our 'dao'
	AccountManager man = AccountManagerFactory.getInstance();
	
	@PUT
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
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
