package com.smodelware.smartcfa.service;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.datastore.Query;
import com.smodelware.smartcfa.util.ContentType;
import org.springframework.stereotype.Service;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

@Service
public class UserService
{
	 private static final Logger log = Logger.getLogger(UserService.class.getName());
	 
	 public Entity saveUser(String fullName,String userName,String email,String password, String loginType,String foreignUserId)
	 {
		 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		 log.info(fullName);
		 String userId = userName+"_"+System.nanoTime();
		 Entity aUser = new Entity("USER", userName);
		 aUser.setProperty("fullName", fullName);
		 aUser.setProperty("userName", userName);
		 aUser.setProperty("email", email);
		 aUser.setProperty("password", password);
		 aUser.setProperty("isTermAccepted", "Y");
		 aUser.setProperty("loginType", loginType);
		 aUser.setProperty("foreignUserId", foreignUserId);
		 aUser.setProperty("userId",userId);
		 datastore.put(aUser);
		 saveUserSetting(userId, ContentType.LOS.getContentType(),ContentType.LOS.getContentType(),"5");
		 return aUser;
	 }

	public Entity saveUserSetting(String userId,String noteContentLevel,String questionContentLevel,String questionPerPage)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		log.info(noteContentLevel);

		Entity userSetting = new Entity("SETTING", userId);
		userSetting.setProperty("userId",userId);
		userSetting.setProperty("noteContentLevel", noteContentLevel);
		userSetting.setProperty("questionContentLevel", questionContentLevel);
		userSetting.setProperty("questionPerPage", questionPerPage);
		datastore.put(userSetting);
		return userSetting;
	}

	public Entity saveUserEntity(Entity aUser)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(aUser);
		return aUser;
	}

	public Entity findUserBasedOnUserName(String userName)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query.Filter propertyFilter =	new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);
		Query query = new Query("USER").setFilter(propertyFilter);
		return datastore.prepare(query).asSingleEntity();
	}
	
	public Entity findUserBasedOnUserId(String userId)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query.Filter propertyFilter =	new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
		Query query = new Query("USER").setFilter(propertyFilter);
		return datastore.prepare(query).asSingleEntity();
	}

	public Entity getUserSetting(String userId)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query.Filter propertyFilter =	new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
		Query query = new Query("SETTING").setFilter(propertyFilter);
		return datastore.prepare(query).asSingleEntity();
	}




	public Entity findUserBasedOnUserNameAndPassword(String userName,String password)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter userNameFilter =	new Query.FilterPredicate("userName", Query.FilterOperator.EQUAL, userName);

        Query.Filter passwordFilter =	new Query.FilterPredicate("password", Query.FilterOperator.EQUAL, password);

//Use CompositeFilter to combine multiple filters
        Query.Filter propertyFilter =   Query.CompositeFilterOperator.and(userNameFilter, passwordFilter);


        Query query = new Query("USER").setFilter(propertyFilter);
		return datastore.prepare(query).asSingleEntity();
	}
	 
	 /*public String saveGoogleUser(String isTermAccepted, HttpServletRequest httpRequest)
	 {
		 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		 com.google.appengine.api.users.UserService userService = UserServiceFactory.getUserService();

		 User user = userService.getCurrentUser();
		 
			if (user != null) 
			{
				Entity aUser = new Entity("USER", user.getUserId());
				 aUser.setProperty("fullName", user.getNickname());
				 aUser.setProperty("userName", user.getUserId());
				 aUser.setProperty("email", user.getEmail());
				 aUser.setProperty("password", "");
				 aUser.setProperty("isTermAccepted", isTermAccepted);
				 aUser.setProperty("loginType", "google");
				 datastore.put(aUser);
				 return user.getNickname();
				
			} else 
			{
				return userService.createLoginURL(httpRequest.getRequestURI());
				
			}
			
	 }*/
}
