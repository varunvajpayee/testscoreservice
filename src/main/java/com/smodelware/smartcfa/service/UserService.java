package com.smodelware.smartcfa.service;

import com.google.appengine.api.datastore.*;
import com.smodelware.smartcfa.util.ContentType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Logger;

@Service
public class UserService
{
	 private static final Logger log = Logger.getLogger(UserService.class.getName());
	 
	 public Entity saveUser(String fullName,String userName,String email,String password, String loginType,String foreignUserId,String course)
	 {
		 DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		 log.info(fullName);

		 Entity aUser = findUserBasedOnUserName(userName);
		 String userId = null;
		 if(aUser==null)
		 {
			 aUser = new Entity("USER", userName);
			 userId = userName+"_"+System.nanoTime();
			 String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
			 aUser.setProperty("createTime",timeStamp);
		 }
		 else {
			 userId =String.valueOf(aUser.getProperty("userId"));
		 }


		 aUser.setProperty("fullName", fullName);
		 aUser.setProperty("userName", userName);
		 aUser.setProperty("email", email);
		 aUser.setProperty("password", password);
		 aUser.setProperty("isTermAccepted", "Y");
		 aUser.setProperty("loginType", loginType);
		 aUser.setProperty("foreignUserId", foreignUserId);
		 aUser.setProperty("userId",userId);
		 aUser.setProperty("type","FREE");
		 aUser.setProperty("course",course);
		 String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new java.util.Date());
		 aUser.setProperty("lastUpdatedTime",timeStamp);
		 datastore.put(aUser);
		 String showCourses =StringUtils.isEmpty(course)?"ALL":"ENROLLED";
		 saveUserSetting(userId, ContentType.LOS.getContentType(),ContentType.LOS.getContentType(),"5",showCourses);
		 return aUser;
	 }

	public Entity saveUserSetting(String userId,String noteContentLevel,String questionContentLevel,String questionPerPage,String showCourses)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		log.info(noteContentLevel);

		Query.Filter propertyFilter =	new Query.FilterPredicate("userId", Query.FilterOperator.EQUAL, userId);
		Query query = new Query("SETTING").setFilter(propertyFilter);
		List<Entity> ents =  datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

		Entity userSetting = null;
		if(ents.isEmpty()){
			userSetting = new Entity("SETTING", userId);
		}
		else {
			userSetting = ents.get(0);
		}
		if(ents.size()>1){
			cleanUpDuplicateUserSetting(datastore,ents);
		}

		userSetting.setProperty("userId",userId);
		userSetting.setProperty("noteContentLevel", noteContentLevel);
		userSetting.setProperty("questionContentLevel", questionContentLevel);
		userSetting.setProperty("questionPerPage", questionPerPage);
		userSetting.setProperty("showCourses", showCourses);

		datastore.put(userSetting);
		return userSetting;
	}

	private void cleanUpDuplicateUserSetting(DatastoreService datastore,List<Entity> ents){
	 	for(int i=1;i<ents.size();i++){
			datastore.delete(ents.get(i).getKey());
		}
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
		Entity userSetting =  datastore.prepare(query).asSingleEntity();
		if(userSetting!=null){
			return userSetting;
		}

        return getDefaultUserSetting();
	}

    public Entity getDefaultUserSetting() {
        Entity userSetting = new Entity("SETTING");
        userSetting.setProperty("userId",0);
        userSetting.setProperty("noteContentLevel", ContentType.LOS.toString());
        userSetting.setProperty("questionContentLevel", ContentType.LOS.toString());
        userSetting.setProperty("questionPerPage", 5);
		userSetting.setProperty("showCourses", "ALL");
        return userSetting;
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
