package com.smodelware.smartcfa.vo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by varun on 7/3/2017.
 */
public class Forum {
    String forumText = "Default";
    String course = "Default";
    Set<Topic> topicSet = new HashSet<>();

    public Set<Topic> getTopicSet() {
        return topicSet;
    }

    public void setTopicSet(Set<Topic> topicSet) {
        this.topicSet = topicSet;
    }
}
