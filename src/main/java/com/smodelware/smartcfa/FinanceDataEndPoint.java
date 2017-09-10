package com.smodelware.smartcfa;

import com.smodelware.smartcfa.vo.UserTest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Created by varun on 7/29/2017.
 */

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class FinanceDataEndPoint {

    private static final Logger log = Logger.getLogger(Catalog.class.getName());


    @GET
    @Path("/finance/getCompanyNames")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCompanyNames(@QueryParam("nameInitials")final String nameInitials)
    {
        log.info("article:"+nameInitials);
        /*System.out.println("article:"+nameInitials);


        UserTest userTest = new UserTest();*/
        Response rs = Response.ok("1") .build();
        return rs;
    }

}
