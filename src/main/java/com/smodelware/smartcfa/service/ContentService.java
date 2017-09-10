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
import com.google.common.collect.Table;
import com.google.common.io.Files;
import com.smodelware.smartcfa.CatalogManager;
import com.smodelware.smartcfa.util.ContentType;
import com.smodelware.smartcfa.vo.Item;
import com.smodelware.smartcfa.vo.Question;
import org.springframework.stereotype.Service;

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
	Item courseItem = new Item();
	courseItem.setKind("COURSE");
	courseItem.setId("CFA_LEVEL_3");
	courseItem.setLeaf(false);
	courseItem.setText("CFA LEVEL 3");
	String contentLevel = getContentLevel(userId, contentTypeStr);
	String cAncestorId = "COURSE-CFA_LEVEL_3";
	List<Entity> bookEntities = queryEntityBasedOnKindAndName(cAncestorId,ContentType.BOOK.getContentType());
	for(Entity bookEntity:bookEntities)
	{
		Item bookItem =	Item.convertEntityToItem(bookEntity,false,cAncestorId,contentTypeStr);
		courseItem.getItems().add(bookItem);
		if(!ContentType.BOOK.getContentType().equals(contentLevel))
		{
			String cbAncestorId = cAncestorId + "|" + bookItem.getKind() + "-" + bookItem.getId();
			List<Entity> ssEntities = queryEntityBasedOnKindAndName(cbAncestorId, ContentType.STUDY_SESSION.getContentType());
			for (Entity ssEntity : ssEntities) {
				Item ssItem = Item.convertEntityToItem(ssEntity, false,cbAncestorId,contentTypeStr);
				bookItem.getItems().add(ssItem);
				if(!ContentType.STUDY_SESSION.getContentType().equals(contentLevel))
				{
					String scbAncestorId =cbAncestorId + "|" + ssItem.getKind() + "-" + ssItem.getId();
					List<Entity> readingEntities = queryEntityBasedOnKindAndName(scbAncestorId, ContentType.READING.getContentType());
					for (Entity readingEntity : readingEntities) {
						Item readingItem = Item.convertEntityToItem(readingEntity, false,scbAncestorId,contentTypeStr);
						ssItem.getItems().add(readingItem);
						if(!ContentType.READING.getContentType().equals(contentLevel))
						{
							String rscbAncestorId =scbAncestorId + "|" + readingItem.getKind() + "-" + readingItem.getId();
							List<Entity> losEntities = queryEntityBasedOnKindAndName(rscbAncestorId, "LOS");
							List<Item> losItems = Item.convertEntitiesToItem(losEntities, true,rscbAncestorId,contentTypeStr);
							readingItem.setItems(losItems);
						}
						else
						{
							readingItem.setLeaf(true);
						}
					}
				}
				else
				{
					ssItem.setLeaf(true);
				}
			}
		}
		else
		{
			bookItem.setLeaf(true);
		}
		
	}	
	
	return courseItem;
}

	private String getContentLevel(String userId, String contentTypeStr) {
		UserService userService = new UserService();

		String contentLevel=null;
		if(ContentType.NOTE.getContentType().equals(contentTypeStr)) {
			if(userService.getUserSetting(userId)!=null) {
				contentLevel = String.valueOf(userService.getUserSetting(userId).getProperty("noteContentLevel"));
			}
        }
        else if(ContentType.QUESTION.getContentType().equals(contentTypeStr)) {
			if(userService.getUserSetting(userId)!=null) {
				contentLevel = String.valueOf(userService.getUserSetting(userId).getProperty("questionContentLevel"));
			}
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


		   uri = "https://storage.googleapis.com/"+ URLEncoder.encode("testscoreservice.appspot.com/"+relativePath, "UTF-8");
		    
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

	public Entity loadCatalog(InputStream ins ) {
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  //InputStream ins = getInputStream(fileUrl);

	  CatalogManager cm = new CatalogManager();
	
	  Table<String, String, LinkedHashMap<String, ArrayList<String>>> catalogTable = cm.readCourseCatalog(ins);
    
      Entity course = new Entity(ContentType.COURSE.getContentType(), "CFA_LEVEL_3");
      course.setProperty("YEAR", "2017");
      course.setProperty("name", "CFA_LEVEL_3");
      //course.setProperty("id", "cfa");

      int cqCount=0;
      Map<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowMap =  catalogTable.rowMap();
      for(Entry<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowEntry:rowMap.entrySet())
      {
		  int bqCount=0;
    	  Entity aBook = new Entity(ContentType.BOOK.getContentType(),  rowEntry.getKey().split("\\$")[0],course.getKey() );
    	  aBook.setProperty("IDENTITY", rowEntry.getKey().split("\\$")[0]);
    	  aBook.setProperty("name", rowEntry.getKey().split("\\$")[1]);
          aBook.setProperty("PARENT_ID", "CFA_LEVEL_3");

    	  for(Entry<String, LinkedHashMap<String, ArrayList<String>>>  valueMap:rowEntry.getValue().entrySet())
    	  {
			  int sqCount=0;
    		  Entity aStudySession = new Entity(ContentType.STUDY_SESSION.getContentType(),  valueMap.getKey().split("\\$")[0],aBook.getKey());
    		  aStudySession.setProperty("IDENTITY", valueMap.getKey().split("\\$")[0]);
    		  aStudySession.setProperty("name", valueMap.getKey().split("\\$")[1]);
              aStudySession.setProperty("PARENT_ID",  rowEntry.getKey().split("\\$")[0]);

    		  for(Entry<String, ArrayList<String>>  entryMap:valueMap.getValue().entrySet())
        	  {
				  int rqCount=0;
    			  Entity aReading = new Entity(ContentType.READING.getContentType(),  entryMap.getKey().split("\\$")[0],aStudySession.getKey());
    			  aReading.setProperty("IDENTITY", entryMap.getKey().split("\\$")[0]);
    			  aReading.setProperty("name", entryMap.getKey().split("\\$")[1]);
                  aReading.setProperty("PARENT_ID", valueMap.getKey().split("\\$")[0]);

        		  //aReading.setProperty("los", entryMap.getValue());
        		  for(String aLosStr:entryMap.getValue())
        		  {
					  int lqCount =0;
        			  Entity aLos = new Entity(ContentType.LOS.getContentType(), aLosStr.split("\\$")[0],aReading.getKey());
        			  aLos.setProperty("IDENTITY", aLosStr.split("\\$")[0]);
        			  aLos.setProperty("name", aLosStr.split("\\$")[1]);
                      aLos.setProperty("PARENT_ID",entryMap.getKey().split("\\$")[0]);
					  lqCount = Integer.parseInt(aLosStr.split("\\$")[2]);
					  rqCount = rqCount+lqCount;
					  aLos.setProperty("Q_COUNT",lqCount);
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

	private InputStream getInputStream(String fileUrl) {
		InputStream ins =null;

		if (false) {
            ins = getContentFromCloudStorage("SmartCFA.csv");
        }
        else {
            ins = getContentFromLocalStorage(fileUrl);
        }
		return ins;
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


		  for(Entry<String, Entity> valueEntry :rowEntry.getValue().entrySet())
		  {
			   Entity aContent = new Entity(ContentType.QUESTION.getContentType(),aLos.getKey());
			   aContent.setPropertiesFrom(valueEntry.getValue());  		
			   datastore.put(aContent);
		  }	  
      }	 
        	  
	    return new Entity("ADDED");
  }


}
