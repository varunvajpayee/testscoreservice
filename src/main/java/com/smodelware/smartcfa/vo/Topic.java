package com.smodelware.smartcfa.vo;

import com.google.appengine.api.datastore.Entity;

import java.util.Set;

/**
 * Created by varun on 7/3/2017.
 */
public class Topic {
    String title;
    String body;
    String user;
    String userName;
    String timeAsOff;
    String id;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUser() {
        return user;
    }

    public String getTimeAsOff() {
        return timeAsOff;
    }

    public void setTimeAsOff(String timeAsOff) {
        this.timeAsOff = timeAsOff;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public static Topic convertEntityToItem(Entity entity){
        Topic topic = new Topic();
        topic.setUser(String.valueOf(entity.getProperties().get("userId")));
        topic.setTitle(String.valueOf(entity.getProperties().get("title")));
        topic.setBody(String.valueOf(entity.getProperties().get("body")));
        topic.setTimeAsOff(String.valueOf(entity.getProperties().get("timestamp")));
        topic.setUserName(String.valueOf(entity.getProperties().get("userName")));
        topic.setId(String.valueOf(entity.getProperties().get("id")));
        return  topic;
    }
}
