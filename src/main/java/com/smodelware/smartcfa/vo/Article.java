package com.smodelware.smartcfa.vo;

/**
 * Created by varun on 7/4/2017.
 */
public class Article {
    Integer id;
    String title;
    String body;
    String user;
    String category;

    public Article() {
    }

    public Article(Integer id, String title, String body, String user, String category) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.user = user;
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public void setUser(String user) {
        this.user = user;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
