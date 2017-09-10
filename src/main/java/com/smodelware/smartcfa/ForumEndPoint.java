package com.smodelware.smartcfa;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.smodelware.smartcfa.vo.Forum;
import com.smodelware.smartcfa.vo.Topic;
import com.smodelware.smartcfa.vo.UserTest;
import com.smodelware.smartcfa.vo.UserVO;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Iterator;
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
    @Produces({"application/json"})
    @Consumes({"application/json"})
    public Response saveTopic(String topicStr)
     {
        log.info("article:"+topicStr);

         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         Gson g =new Gson();
         Topic topic =g.fromJson(topicStr,Topic.class);
         long timeasOff = System.nanoTime();
         String topicId = "TOPIC_"+timeasOff;
         Entity topicEntity = new Entity("TOPIC", topicId);
         topicEntity.setProperty("userId",topic.getUser());
         topicEntity.setProperty("title", topic.getTitle());
         topicEntity.setProperty("body", topic.getBody());
         topicEntity.setProperty("timestamp", timeasOff);
         topicEntity.setProperty("userName",topic.getUserName());
         topicEntity.setProperty("id",topicId);
         datastore.put(topicEntity);

        Response rs = Response.ok(topicEntity) .build();
        return rs;
    }

    @GET
    @Path("/forum/getForumTopics")
    @Produces({"application/javascript"})
    public Response getForumTopics()
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query = new Query("TOPIC");
        Iterable<Entity> topicEntities = datastore.prepare(query).asIterable();
        Forum forum = new Forum();
        for(Entity topicEntry:topicEntities){
            forum.getTopicSet().add(Topic.convertEntityToItem(topicEntry));
        }

        Response rs = Response.ok(forum) .build();
        return rs;
    }

    @GET
    @Path("/forum/getForumTopic/{id}")
    @Produces({"application/javascript"})
    public Response getForumTopic(@PathParam("id")String topicId)
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter propertyFilter =	new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, topicId);
        Query query = new Query("TOPIC").setFilter(propertyFilter);
        Entity topicEntity = datastore.prepare(query).asSingleEntity();
        Topic topic = Topic.convertEntityToItem(topicEntity);

        Response rs = Response.ok(topic) .build();
        return rs;
    }

}
