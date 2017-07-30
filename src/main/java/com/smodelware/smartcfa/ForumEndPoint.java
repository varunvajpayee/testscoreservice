package com.smodelware.smartcfa;

import com.smodelware.smartcfa.vo.Article;
import com.smodelware.smartcfa.vo.UserTest;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Created by varun on 7/3/2017.
 */

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ForumEndPoint {

    private static final Logger log = Logger.getLogger(Catalog.class.getName());

    @POST
    @Path("/forum/saveTopic")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveTopic(final String article,@CookieParam("login") Cookie cookie)
     {
        log.info("article:"+article);
        System.out.println("article:"+article);
         UserTest userTest = new UserTest();
        Response rs = Response.ok(userTest) .build();
        return rs;
    }

}
