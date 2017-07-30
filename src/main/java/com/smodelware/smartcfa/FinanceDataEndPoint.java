package com.smodelware.smartcfa;

import com.smodelware.smartcfa.vo.UserTest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

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
    public Response getCompanyNames(final String nameInitials)
    {
        log.info("article:"+nameInitials);
        System.out.println("article:"+nameInitials);


        UserTest userTest = new UserTest();
        Response rs = Response.ok(userTest) .build();
        return rs;
    }

}
