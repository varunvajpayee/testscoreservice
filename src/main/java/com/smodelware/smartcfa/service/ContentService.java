/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.smodelware.smartcfa.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.KeyFactory.Builder;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.repackaged.com.google.datastore.v1.PropertyFilter;
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.smodelware.smartcfa.CatalogManager;
import com.smodelware.smartcfa.util.ContentType;
import com.smodelware.smartcfa.util.CourseType;
import com.smodelware.smartcfa.vo.Item;
import com.smodelware.smartcfa.vo.Question;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

@Service
public class ContentService 
{
	
	/** Global configuration of Google Cloud Storage OAuth 2.0 scope. */
	
	private static final String STORAGE_SCOPE ="https://www.googleapis.com/auth/devstorage.read_write";
	 private static final Logger log = Logger.getLogger(ContentService.class.getName());

  public  List<Entity> getContent(String parentKey, String queryKind)  
 {
    List<Entity> nextLevelChildList = new ArrayList<Entity>();  
    	 List<Entity> results = queryEntityBasedOnKindAndName(parentKey, queryKind);
		
		for(Entity result :results)
		{
			log.info("result-->"+result);
			nextLevelChildList.add(result);
		}	
    
    return nextLevelChildList;
  }

	public Entity queryEntityBasedOnKindAndIdWithoutParent(String kind,String id)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query.Filter propertyFilter =	new Query.FilterPredicate("IDENTITY", Query.FilterOperator.EQUAL, id);
		Query query = new Query(kind).setFilter(propertyFilter);
		return datastore.prepare(query).asSingleEntity();
	}

public List<Entity> queryEntityBasedOnKindAndName(String parentKey,String queryKind)
{
	
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	Entity parentEntity=null;
	Key parent = getParentKey(parentKey);
	List<Entity> results = null;

	if(queryKind!=null && parent.getKind().equals(queryKind))
	{
		Query.Filter propertyFilter =	new Query.FilterPredicate("IDENTITY", Query.FilterOperator.EQUAL, parent.getName());
		Query query = new Query(queryKind).setFilter(propertyFilter);
		return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
	}

	Map<Integer,List<Entity>> sortMap = new TreeMap<>();
	 try {
		parentEntity = datastore.get(parent);
		Query nextLevelChild = queryKind!=null?new Query(queryKind).setAncestor(parentEntity.getKey()):new Query().setAncestor(parentEntity.getKey());
		results =   datastore.prepare(nextLevelChild).asList(FetchOptions.Builder.withDefaults());
		if(queryKind == null || ContentType.QUESTION.getContentType().equals(queryKind)){
			return results;
		}

		for(Entity entity:results)
		{
			String id = ContentType.LOS.getContentType().equals(queryKind)?String.valueOf(entity.getProperties().get("PARENT_ID")):String.valueOf(entity.getProperties().get("IDENTITY"));
			String[] idArray =id.split("_");

			List<Entity> valObject =sortMap.get(Integer.parseInt(idArray[idArray.length - 1]));
			if(valObject==null) {
					List<Entity> entityList = new ArrayList<>();
					entityList.add(entity);
					sortMap.put(Integer.parseInt(idArray[idArray.length - 1]),entityList);
			}
			else{
				sortMap.get(Integer.parseInt(idArray[idArray.length - 1])).add(entity);
			}
		}
	} catch (EntityNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	List<Entity> returnEntities = new ArrayList();
	for(List<Entity> subList:sortMap.values())
	{
		returnEntities.addAll(subList);
	}

	 return new ArrayList(returnEntities);
}

public List<Question>  getQuestions(List<Entity> entites ,Integer sIndex,Integer eIndex,boolean iAllowAnswers)
{

	List<Question> questions = new ArrayList<Question>();
	int i =0;
	for(Entity entity:entites)
	{
		if(i<eIndex && i>=sIndex) {
			Question question = Question.convertEntityToQuestion(entity,iAllowAnswers);
			question.setQuestionNum(String.valueOf(i));
			question.setScreenQuestionNum(String.valueOf(i+1));
			questions.add(question);

		}
		i++;
	}	
	
	return questions;
}



public Item getCatalogTree(String userId,String contentTypeStr)
{
	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	/*Item courseItem = new Item();
	courseItem.setKind("COURSE");
	courseItem.setId("CFA_LEVEL_3");
	courseItem.setLeaf(false);
	courseItem.setText("CFA_LEVEL_3");
	String cAncestorId = "COURSE-CFA_LEVEL_3";//"COURSE-CFA_LEVEL_1"*/
	Item rootItem = new Item();
	Entity userSettingEntity = getUserSetting(userId);
	String contentLevel = getContentLevel(userSettingEntity, contentTypeStr);
	String showCourses = String.valueOf(userSettingEntity.getProperty("showCourses"));
	showCourses = showCourses == null?"ALL":showCourses;
	String enrolledCourse = getUserEnrolledCourse(userId);
	Boolean filterCourses = false;
	if("ENROLLED".equals(showCourses) && !StringUtils.isEmpty(enrolledCourse)  ){
		filterCourses  = true;
	}

	Query query = new Query("COURSE");
	List<Entity> courseEntities = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
	for(Entity courseEntity:courseEntities){
		log.info("key:" + courseEntity.getProperties().get("IDENTITY"));
		String courseId = String.valueOf(courseEntity.getProperty("IDENTITY"));
		if(filterCourses &&  !enrolledCourse.equals(courseId)){
			continue;
		}

		Item courseItem = Item.convertEntityToItem(courseEntity, false, null, contentTypeStr, null);
		String cAncestorId = courseItem.getKind()+"-"+courseItem.getId();//"COURSE-CFA_LEVEL_1"

		List<Entity> bookEntities = queryEntityBasedOnKindAndName(cAncestorId, ContentType.BOOK.getContentType());
		rootItem.getItems().add(courseItem);
		for (Entity bookEntity : bookEntities) {
			System.out.println("key:" + bookEntity.getProperties().get("BOOK_ID"));
			Query q = new Query("BOOK_MAP").setFilter(new FilterPredicate("IDENTITY", FilterOperator.EQUAL, bookEntity.getProperties().get("BOOK_ID")));
			Entity userTestEntity = datastore.prepare(q).asSingleEntity();
			String url = String.valueOf(userTestEntity.getProperties().get("HTML_URL"));

			Item bookItem = Item.convertEntityToItem(bookEntity, false, cAncestorId, contentTypeStr, url);
			courseItem.getItems().add(bookItem);
			if (!ContentType.BOOK.getContentType().equals(contentLevel)) {
				String cbAncestorId = cAncestorId + "|" + bookItem.getKind() + "-" + bookItem.getId();
				List<Entity> ssEntities = queryEntityBasedOnKindAndName(cbAncestorId, ContentType.STUDY_SESSION.getContentType());
				for (Entity ssEntity : ssEntities) {
					Item ssItem = Item.convertEntityToItem(ssEntity, false, cbAncestorId, contentTypeStr, url);
					bookItem.getItems().add(ssItem);
					if (!ContentType.STUDY_SESSION.getContentType().equals(contentLevel)) {
						String scbAncestorId = cbAncestorId + "|" + ssItem.getKind() + "-" + ssItem.getId();
						List<Entity> readingEntities = queryEntityBasedOnKindAndName(scbAncestorId, ContentType.READING.getContentType());
						for (Entity readingEntity : readingEntities) {
							Item readingItem = Item.convertEntityToItem(readingEntity, false, scbAncestorId, contentTypeStr, url);
							ssItem.getItems().add(readingItem);
							if (!ContentType.READING.getContentType().equals(contentLevel)) {
								String rscbAncestorId = scbAncestorId + "|" + readingItem.getKind() + "-" + readingItem.getId();
								List<Entity> losEntities = queryEntityBasedOnKindAndName(rscbAncestorId, "LOS");
								List<Item> losItems = Item.convertEntitiesToItem(losEntities, true, rscbAncestorId, contentTypeStr, url);
								readingItem.setItems(losItems);
							} else {
								readingItem.setLeaf(true);
							}
						}
					} else {
						ssItem.setLeaf(true);
					}
				}
			} else {
				bookItem.setLeaf(true);
			}

		}

	}
	return rootItem;
}

	private Entity getUserSetting(String userId){
		UserService userService = new UserService();
		return userService.getUserSetting(userId);
	}

	private String getUserEnrolledCourse(String userId){
		UserService userService = new UserService();
		Entity user =userService.findUserBasedOnUserId(userId);
		if(user!=null){
			return String.valueOf(user.getProperty("course"));
		}
		return null;
	}

	private String getContentLevel(Entity userSettingEntity, String contentTypeStr) {
		UserService userService = new UserService();
		String contentLevel=null;
		if(ContentType.NOTE.getContentType().equals(contentTypeStr)) {
			contentLevel = String.valueOf(userSettingEntity.getProperty("noteContentLevel"));
        }
        else if(ContentType.QUESTION.getContentType().equals(contentTypeStr)) {
			contentLevel = String.valueOf(userSettingEntity.getProperty("questionContentLevel"));
        }
		contentLevel=contentLevel==null?ContentType.BOOK.getContentType():contentLevel;
		return contentLevel;
	}


	private Key getParentKey(String parentKey)
{
	Builder parent=null;
	String parentKeys[]=parentKey.split("\\|");
	if(parentKey.contains("|"))
	{
		parent= new KeyFactory.Builder(parentKeys[0].split("\\-")[0],parentKeys[0].split("\\-")[1]);

		for(int i=1;i<parentKeys.length;i++)
		{
			parent = parent.addChild(parentKeys[i].split("\\-")[0],parentKeys[i].split("\\-")[1]);
		}
	}
	else
	{
		parent= new KeyFactory.Builder(parentKey.split("\\-")[0],parentKey.split("\\-")[1]);
	}
	log.info("Key:"+parent.getKey());

	return parent.getKey();
}

  public InputStream getContentFromCloudStorage (String relativePath)
  {
	    /******Remote call for cloud storage: Start********/
	    GoogleCredential credential = null;
	    InputStream ins=null;
	  String uri = null;

	    try {
			Map<String, String> env = System.getenv();
			try{
				credential = GoogleCredential.getApplicationDefault().createScoped(Collections.singleton(STORAGE_SCOPE));
			}catch (Exception e)
			{
				log.info("Problem in setting up environmnet variable in IntelliJ AppEngine local server");
				e.printStackTrace();
			}

			if(relativePath.indexOf("http")== -1){
				uri = "https://storage.googleapis.com/"+ URLEncoder.encode("testscoreservice.appspot.com/"+relativePath, "UTF-8");
			}
			else{
				uri = relativePath;
			}

		    
		    HttpTransport httpTransport = null;
			try {
				httpTransport = GoogleNetHttpTransport.newTrustedTransport();
			} catch (GeneralSecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
		    GenericUrl url = new GenericUrl(uri);
	
		    HttpRequest grequest = requestFactory.buildGetRequest(url);
		    HttpResponse gresponse = grequest.execute();
		    ins=  gresponse.getContent();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    /******Remote call for cloud storage: End********/
	    
	    return ins;
  }

	private InputStream getContentFromLocalStorage (String filePath)
	{
		try {
			File initialFile = new File(filePath);
			return Files.asByteSource(initialFile).openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
  
  
  public Entity deleteContent(String parentKey)  
  {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<Entity> results = queryEntityBasedOnKindAndName(parentKey, null);
		
		for(Entity result :results)
		{
			datastore.delete(result.getKey());
		}
	    return new Entity("DELETED");
  }

	public Entity updateUserTestEntityWithAnswer(Entity userTestEntity,Map<String,String> userAnswers) {
		if(userAnswers.size()>0) {
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			EmbeddedEntity ee = (EmbeddedEntity) userTestEntity.getProperty("RECORD");
			for (Entry<String, String> mapEntry : userAnswers.entrySet()) {
				ee.setIndexedProperty(mapEntry.getKey(), mapEntry.getValue());
			}
			datastore.put(userTestEntity);
		}

		return userTestEntity;
	}


  public List<Question> decorateQuestionWithAnswer(Entity userTestEntity,List<Question> questions, Map<String,String> questionAnswersMap)
  {
	  EmbeddedEntity ee =(EmbeddedEntity) userTestEntity.getProperty("RECORD");
	  for(Question question:questions)
	  {
		  if(questionAnswersMap.containsKey(question.getQuestionId()))
		  {
			  question.setAnswerSelected(questionAnswersMap.get(question.getQuestionId()));
		  }
		  else {
			  String answerSelected = String.valueOf(ee.getProperty(question.getQuestionId()));
			  if (answerSelected != null) {
				  question.setAnswerSelected(answerSelected);
			  }
		  }
	  }

	  return questions;
  }

	public Entity getUserTestEntity(String userId)
	{
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Filter propertyFilter =	new FilterPredicate("userId", FilterOperator.EQUAL, userId);
		Query query = new Query("USER_TEST").setFilter(propertyFilter);
		Entity userTestEntity = datastore.prepare(query).asSingleEntity();

		if(userTestEntity==null)
         {
            userTestEntity = new Entity("USER_TEST",userId);
            userTestEntity.setProperty("userId",userId);
			EmbeddedEntity ee = new EmbeddedEntity();
            userTestEntity.setProperty("RECORD",ee);
            datastore.put(userTestEntity);
        }
		return userTestEntity;
	}

	public Entity loadCatalog(InputStream ins, String courseName, String courseYear,String url ) {
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  //InputStream ins = getInputStream(fileUrl);
	  CatalogManager cm = new CatalogManager();
	
	  Table<String, String, LinkedHashMap<String, ArrayList<String>>> catalogTable = cm.readCourseCatalog(ins);
    
      Entity course = new Entity(ContentType.COURSE.getContentType(), courseName);
      course.setProperty("IDENTITY", courseName);
      course.setProperty("YEAR", courseYear);
      course.setProperty("name", courseName);
      course.setProperty("URL",url);

      int cqCount=0;
      Map<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowMap =  catalogTable.rowMap();
      for(Entry<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowEntry:rowMap.entrySet())
      {
		  int bqCount=0;
    	  Entity aBook = new Entity(ContentType.BOOK.getContentType(),  rowEntry.getKey().split("\\$")[0],course.getKey() );
    	  aBook.setProperty("IDENTITY", rowEntry.getKey().split("\\$")[0]);
    	  aBook.setProperty("name", rowEntry.getKey().split("\\$")[1]);
		  aBook.setProperty("URI", rowEntry.getKey().split("\\$")[2]);
          aBook.setProperty("PARENT_ID", courseName);
		  aBook.setProperty("BOOK_ID",aBook.getProperty("IDENTITY"));

    	  for(Entry<String, LinkedHashMap<String, ArrayList<String>>>  valueMap:rowEntry.getValue().entrySet())
    	  {
			  int sqCount=0;
    		  Entity aStudySession = new Entity(ContentType.STUDY_SESSION.getContentType(),  valueMap.getKey().split("\\$")[0],aBook.getKey());
    		  aStudySession.setProperty("IDENTITY", valueMap.getKey().split("\\$")[0]);
    		  aStudySession.setProperty("name", valueMap.getKey().split("\\$")[1]);
			  aStudySession.setProperty("URI", valueMap.getKey().split("\\$")[2]);
              aStudySession.setProperty("PARENT_ID",  aBook.getProperty("IDENTITY"));
			  aStudySession.setProperty("BOOK_ID",aBook.getProperty("IDENTITY"));

    		  for(Entry<String, ArrayList<String>>  entryMap:valueMap.getValue().entrySet())
        	  {
				  int rqCount=0;
    			  Entity aReading = new Entity(ContentType.READING.getContentType(),  entryMap.getKey().split("\\$")[0],aStudySession.getKey());
    			  aReading.setProperty("IDENTITY", entryMap.getKey().split("\\$")[0]);
    			  aReading.setProperty("name", entryMap.getKey().split("\\$")[1]);
				  aReading.setProperty("URI", entryMap.getKey().split("\\$")[2]);
                  aReading.setProperty("PARENT_ID", aStudySession.getProperty("IDENTITY"));
				  aReading.setProperty("BOOK_ID",aBook.getProperty("IDENTITY"));
        		  //aReading.setProperty("los", entryMap.getValue());
        		  for(String aLosStr:entryMap.getValue())
        		  {
					  int lqCount =0;
        			  Entity aLos = new Entity(ContentType.LOS.getContentType(), aLosStr.split("\\$")[0],aReading.getKey());
        			  aLos.setProperty("IDENTITY", aLosStr.split("\\$")[0]);
        			  aLos.setProperty("name", aLosStr.split("\\$")[1]);
					  aLos.setProperty("URI", aLosStr.split("\\$")[2]);
                      aLos.setProperty("PARENT_ID",aReading.getProperty("IDENTITY"));
					  aLos.setProperty("BOOK_ID",aBook.getProperty("IDENTITY"));

					  lqCount = Integer.parseInt(aLosStr.split("\\$")[3]);
					  rqCount = rqCount+lqCount;
					  aLos.setProperty("Q_COUNT",lqCount);
					  /*
					  aLos.setProperty("URL", aLosStr.split("\\$")[3]);
					  if(aReading.getProperty("URL")==null){
						  aReading.setProperty("URL",aLosStr.split("\\$")[3]);
					  }
					  if(aStudySession.getProperty("URL")==null){
						  aStudySession.setProperty("URL",aLosStr.split("\\$")[3]);
					  }
					  if(aBook.getProperty("URL")==null){
						  aBook.setProperty("URL",aLosStr.split("\\$")[3]);
					  }*/
        			  datastore.put(aLos);
        		  }
				  sqCount = sqCount+rqCount;
				  aReading.setProperty("Q_COUNT",rqCount);
				  datastore.put(aReading);
        	  }
			  bqCount = bqCount+sqCount;
			  aStudySession.setProperty("Q_COUNT",sqCount);
			  datastore.put(aStudySession);
    	  }

		  cqCount =cqCount+bqCount;
		  aBook.setProperty("Q_COUNT",bqCount);
		  datastore.put(aBook);
      }
		course.setProperty("Q_COUNT",cqCount);
		datastore.put(course);
  		return course;
  }

	/*private InputStream getInputStream(String fileUrl) {
		InputStream ins =null;

		if (false) {
            ins = getContentFromCloudStorage("SmartCFA.csv");
        }
        else {
            ins = getContentFromLocalStorage(fileUrl);
        }
		return ins;
	}*/

	public void performScriptOps() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		/*UserService userService = new UserService();
		Query query = new Query("USER");
		Iterable<Entity> entityIterator = datastore.prepare(query).asIterable();
		for(Entity entity: entityIterator){

			entity.setProperty("course", CourseType.CFA_LEVEL_3.getCourseType());
			datastore.put(entity);
			userService.saveUserSetting(entity.getProperty("userId").toString(), ContentType.LOS.getContentType(),ContentType.LOS.getContentType(),"5","ENROLLED");
		}*/

		Entity aBook = new Entity("BOOK_MAP","CFA3_B_1");
		aBook.setProperty("IDENTITY","CFA3_B_1");
		aBook.setProperty("URL","https://docs.google.com/document/d/1VMvUZNLDU_sB56ZkSeVyLVY5x1ceY45SHJClADZxYJs");
		aBook.setProperty("Q_URL","https://drive.google.com/open?id=0B0zUJ7BVTHu5Ym0zZmRuQm9CR3c");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level3/CFA3_B_1.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA3_B_2");
		aBook.setProperty("IDENTITY","CFA3_B_2");
		aBook.setProperty("URL","https://docs.google.com/document/d/1kdizqGUzdPLpfJa44SSn4mia5otFvsFP-cLPe7196Ww");
		aBook.setProperty("Q_URL","https://drive.google.com/open?id=0B0zUJ7BVTHu5LTM5SkIyYVhwNkE");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level3/CFA3_B_2.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA3_B_3");
		aBook.setProperty("IDENTITY","CFA3_B_3");
		aBook.setProperty("URL","https://docs.google.com/document/d/1RTxcTVfOoql-_NXVxaNXCyLd3GBZBQ_F51M5-1NdOaE");
		aBook.setProperty("Q_URL","https://drive.google.com/open?id=0B0zUJ7BVTHu5ZXNnSmJrVF95MHc");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level3/CFA3_B_3.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA3_B_4");
		aBook.setProperty("IDENTITY","CFA3_B_4");
		aBook.setProperty("URL","https://docs.google.com/document/d/17KeZ1FRiqBOwBTxdvzoYg6L8q0BHYj6MyzAO44a5JzU");
		aBook.setProperty("Q_URL","https://drive.google.com/open?id=0B0zUJ7BVTHu5UUlsdWh6RzhicFE");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level3/CFA3_B_4.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA3_B_5");
		aBook.setProperty("IDENTITY","CFA3_B_5");
		aBook.setProperty("URL","https://docs.google.com/document/d/1l2c7QP7LxZ_Gd-_mgqdX2ByUEXV8rn5HDzRfJJnpaYg");
		aBook.setProperty("Q_URL","https://drive.google.com/open?id=0B0zUJ7BVTHu5dU5FV1lJZnVETGs");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level3/CFA3_B_5.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA3_B_6");
		aBook.setProperty("IDENTITY","CFA3_B_6");
		aBook.setProperty("URL","https://docs.google.com/document/d/1nOnziSXJJW84I7ftiZCEIrJcDMz-bUheG84aRNJAYyg");
		aBook.setProperty("Q_URL","https://docs.google.com/document/d/e/2PACX-1vQ_poLw00ohiW_aTZWnKEIsNPmu9Wv1KZO2WQhDRIKJAtf6-sN911CZK3xHQAZOnjWnPvoSCGmRXKtt/pub?output=pdf");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level3/CFA3_B_6.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA1_B_1");
		aBook.setProperty("IDENTITY","CFA1_B_1");
		aBook.setProperty("URL","https://docs.google.com/document/d/1KcJanOoQa9RlYTVAGI18SGtNLeYcyS340G3hZK2NNU4");
		aBook.setProperty("Q_URL","https://docs.google.com/spreadsheets/d/e/2PACX-1vRl6t7dSWKdOLqGwwLAF_mHOwZ5BExyu0sdZhhhIc9yrhV1doQ5WSkMqFGXKd0JpZUDoy3gjE7PJIvu/pub?output=pdf");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level1/CFA1_B_1.html");
		//aBook.setProperty("HTML_URL","https://docs.google.com/document/d/e/2PACX-1vTlQNRJEcgofpI_KOP49nDzQrzZGkurbrxSueiWlwuLfSRGd5IHbJRLFWLtO2UlQs2-OEZXUeFxP0sz/pub?output=pdf");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA1_B_2");
		aBook.setProperty("IDENTITY","CFA1_B_2");
		aBook.setProperty("URL","https://docs.google.com/document/d/18C9Rwr5aNCtjXu7zcc9v-NDwsSD2E8V1GNIWQhxTq_Q");
		aBook.setProperty("Q_URL","https://docs.google.com/spreadsheets/d/e/2PACX-1vR1IaBKkHoLa7V2zmjLprzcAukxNVew9ZpeEbAHglEnnPwmjKDFgFTamsR_ecxOO76n8swXKuWywqAE/pub?output=pdf");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level1/CFA1_B_2.html");
		datastore.put(aBook);


		aBook = new Entity("BOOK_MAP","CFA1_B_3");
		aBook.setProperty("IDENTITY","CFA1_B_3");
		aBook.setProperty("URL","https://docs.google.com/document/d/1dNQ81zw46K6NRc2Ei3f1Asy2pgad2_D3aJtkMMZkq0Q");
		aBook.setProperty("Q_URL","https://docs.google.com/spreadsheets/d/e/2PACX-1vTE_9X9GAnRvZJI8LT02aQNVpJgoDzqQlOKpIS1FZ6H9CGzu2KPi6eifn24RiqAbCjDO-2yPAHKL4J7/pub?output=pdf");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level1/CFA1_B_3.html");
		datastore.put(aBook);


		aBook = new Entity("BOOK_MAP","CFA1_B_4");
		aBook.setProperty("IDENTITY","CFA1_B_4");
		aBook.setProperty("URL","https://docs.google.com/document/d/1IN2YuBKe_vQRlZ456BRCgPalgfhKkkvuMjjaGQ2XDmw");
		aBook.setProperty("Q_URL","https://docs.google.com/spreadsheets/d/e/2PACX-1vSo-1sWk99Dmhan7xCWGyPkWlZ2xLhAt5cm8nZ6JL9HmJHixSkmNVxg3t23toQCUQeTBgcjuXtOuDj3/pub?output=pdf");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level1/CFA1_B_4.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA1_B_5");
		aBook.setProperty("IDENTITY","CFA1_B_5");
		aBook.setProperty("URL","https://docs.google.com/document/d/1jWpk7qTHe9waTsd3SThdW0nYcUyjGNaJCgp4i5d5o-w");
		aBook.setProperty("Q_URL","https://docs.google.com/spreadsheets/d/e/2PACX-1vSsDoqnRhQP5fEZRMcIvJdn9ESZO6lwrAX1slCDduddHjap74GVcn_QKlf4jK8_jy-Xy2SWyNfATfY_/pub?output=pdf");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level1/CFA1_B_5.html");
		datastore.put(aBook);

		aBook = new Entity("BOOK_MAP","CFA1_B_6");
		aBook.setProperty("IDENTITY","CFA1_B_6");
		aBook.setProperty("URL","https://docs.google.com/document/d/1iO6AZhvnmEgPtwTMlx1AQ7rruTBf3lPnJhrrt4u-bx8");
		aBook.setProperty("Q_URL","https://docs.google.com/spreadsheets/d/e/2PACX-1vQSOA3nTldlgKyJhkKcEcMqvmA_F0XOZGgequReBT90LVr-3Y00Y8Ci3msNA19zyiwmVPj3MDgLGDkt/pub?output=pdf");
		aBook.setProperty("HTML_URL","https://storage.googleapis.com/stoked-outlook-179704.appspot.com/BA-CFA-Level1/CFA1_B_6.html");
		datastore.put(aBook);



	}

	public Entity loadContent(InputStream ins,String sheetName)
  {
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  CatalogManager cm = new CatalogManager();
		
	  Table<String, String, Entity> catalogTable = cm.constructLOSContentMapFromExcel(ins, ContentType.QUESTION, sheetName);
	  Map<String, Map<String, Entity>> rowMap =  catalogTable.rowMap();
	  for(Entry<String, Map<String, Entity>> rowEntry:rowMap.entrySet())
      {
		  String losId	=	rowEntry.getKey();
		  Filter keyFilter =    new FilterPredicate("IDENTITY", FilterOperator.EQUAL, losId);
		  Query query = new Query(ContentType.LOS.getContentType()).setFilter(keyFilter);
		  //List<Entity> results =   datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
		  Entity aLos=datastore.prepare(query).asSingleEntity();
		  if(aLos==null){
		  	continue;
		  }
		  log.info("Writing LOS"+aLos.getKey());

		  for(Entry<String, Entity> valueEntry :rowEntry.getValue().entrySet())
		  {
		  	if(valueEntry!=null && valueEntry.getValue()!=null) {
				Entity aContent = new Entity(ContentType.QUESTION.getContentType(), aLos.getKey());
				aContent.setPropertiesFrom(valueEntry.getValue());
				datastore.put(aContent);
			}
		  }	  
      }	 
        	  
	    return new Entity("ADDED");
  }


}
