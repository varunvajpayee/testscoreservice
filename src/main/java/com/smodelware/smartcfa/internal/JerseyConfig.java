package com.smodelware.smartcfa.internal;

import com.smodelware.smartcfa.FinanceDataEndPoint;
import com.smodelware.smartcfa.ForumEndPoint;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import com.smodelware.smartcfa.Catalog;
import com.smodelware.smartcfa.User;

public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        // Enable Spring DI
        register(RequestContextFilter.class);
        register(Catalog.class);
        register(User.class);
        register(ForumEndPoint.class);
        register(FinanceDataEndPoint.class);
    }
}