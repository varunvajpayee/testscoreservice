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

package com.smodelware.smartcfa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.common.collect.Table;




@SuppressWarnings("serial")
public class CatalogService extends HttpServlet 
{


/** Global configuration of Google Cloud Storage OAuth 2.0 scope. */
	 private static final String STORAGE_SCOPE =
		      "https://www.googleapis.com/auth/devstorage.read_write";
	
	
	
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException {
      
    response.setContentType("text/plain");
    response.getWriter().println("Loadring Catalog!");
    /*DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    *//******Remote call for cloud storage: Start********//*
    GoogleCredential credential = GoogleCredential.getApplicationDefault().createScoped(Collections.singleton(STORAGE_SCOPE));
    String uri = "https://storage.googleapis.com/"+ URLEncoder.encode("testscoreservice.appspot.com/SmartCFA.csv", "UTF-8");
    
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
    InputStream ins=  gresponse.getContent();
    
    *//******Remote call for cloud storage: End********//*
    
	Key root= new KeyFactory.Builder("COURSE", "CFA_LEVEL_3").getKey();
	// FOR LOCAL SETTINGS ONLY:START
	//String uri = "/WEB-INF/classes/SmartCFA.csv"; 
	//ServletContext context = request.getSession().getServletContext();
	//InputStream ins= context.getResourceAsStream(uri);
	// FOR LOCAL SETTINGS ONLY:END
    loadCatalog(datastore,ins);
    //datastore.put(course);
  
  
	try {
		
		Entity courseR = datastore.get(root);
		response.getWriter().write(courseR.toString());
		
		Query nextLevelChild = new Query().setAncestor(courseR.getKey());
		List<Entity> results =   datastore.prepare(nextLevelChild).asList(FetchOptions.Builder.withDefaults());
		for(Entity result :results)
		{
			System.out.println("result-->"+result);
		}	
		
		System.out.println(courseR);
	} catch (EntityNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
  }
	/* 
  private Entity loadCatalog(DatastoreService datastore,InputStream ins)
  {
 System.out.println("Starting::loadCatalog");
	  CatalogManager cm = new CatalogManager();
	
	  Table<String, String, LinkedHashMap<String, ArrayList<String>>> catalogTable = cm.readCourseCatalog(ins);
    
      Entity course = new Entity("COURSE", "CFA_LEVEL_3");
      course.setProperty("YEAR", "2017");
      datastore.put(course);
      
      Map<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowMap =  catalogTable.rowMap();
      
      for(Entry<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowEntry:rowMap.entrySet())
      {
    	  Entity aBook = new Entity("BOOK",  rowEntry.getKey(),course.getKey() );
    	  aBook.setProperty("name", rowEntry.getKey());
    	  datastore.put(aBook);
    	  for(Entry<String, LinkedHashMap<String, ArrayList<String>>>  valueMap:rowEntry.getValue().entrySet())
    	  {
    		  Entity aStudySession = new Entity("STUDY_SESSION",  valueMap.getKey().split("$")[0],aBook.getKey());
    		  aStudySession.setProperty("name", valueMap.getKey().split("$")[1]);
    		  datastore.put(aStudySession);
    		  for(Entry<String, ArrayList<String>>  entryMap:valueMap.getValue().entrySet())
        	  {
    			  Entity aReading = new Entity("READING",  entryMap.getKey().split("$")[0],aStudySession.getKey());
        		  aReading.setProperty("name", entryMap.getKey().split("$")[1]);
        		  datastore.put(aReading);
        		  //aReading.setProperty("los", entryMap.getValue());
        		  for(String aLosStr:entryMap.getValue())
        		  {
        			  Entity aLos = new Entity("LOS", aLosStr.split("$")[0],aStudySession.getKey());
        			  aLos.setProperty("name", aLosStr.split("$")[1]);
        			  datastore.put(aLos);
        		  }  
        		  
        		  
        	  }
    	  }
    	
      }
      System.out.println("End::loadCatalog");
  		return course;
  }*/
  
  /*
  private Entity loadCatalog(DatastoreService datastore,HttpServletRequest request)
  {
	  CatalogManager cm = new CatalogManager();
	  
	  String fileToParse = "/WEB-INF/classes/SmartCFA.csv";
	  //String fileToParse = "C://app//contentservice//testscoreservice//src//main//resources//SmartCFA.csv";
	  ServletContext context = request.getSession().getServletContext();
	  InputStream ins= context.getResourceAsStream(fileToParse);
	  Table<String, String, LinkedHashMap<String, ArrayList<String>>> catalogTable = cm.readCourseCatalog(ins);
    
      Entity course = new Entity("COURSE", "CFA_LEVEL_3");
      course.setProperty("YEAR", "2017");
      
      Map<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowMap =  catalogTable.rowMap();
      List<EmbeddedEntity > books = new ArrayList<EmbeddedEntity>();
      course.setProperty("COURSE_BOOKS", books);
      
      for(Entry<String, Map<String, LinkedHashMap<String, ArrayList<String>>>> rowEntry:rowMap.entrySet())
      {
    	  EmbeddedEntity aBook = new EmbeddedEntity();
    	  Key k = new KeyFactory.Builder("COURSE", "CFA_LEVEL_3").addChild("BOOK", rowEntry.getKey()).getKey();
    	  aBook.setKey(k);
    	  aBook.setProperty("name", rowEntry.getKey());
    	  books.add(aBook);
    	  
    	  List<EmbeddedEntity > studySessions = new ArrayList<EmbeddedEntity>();
    	  aBook.setProperty("STUDY_SESSIONS", studySessions);
    	  for(Entry<String, LinkedHashMap<String, ArrayList<String>>>  valueMap:rowEntry.getValue().entrySet())
    	  {
    		  EmbeddedEntity aStudySession = new EmbeddedEntity();
    		  Key sskey= new KeyFactory.Builder("COURSE", "CFA_LEVEL_3")
    				  .addChild("BOOK", rowEntry.getKey()).addChild("STUDY_SESSION", valueMap.getKey()).getKey();
    		  aStudySession.setKey(sskey);
    		  aStudySession.setProperty("name", valueMap.getKey());
    		  studySessions.add(aStudySession);
    		  
    		  List<EmbeddedEntity > readings = new ArrayList<EmbeddedEntity>();
    		  aStudySession.setProperty("READINGS", readings);
    		  for(Entry<String, ArrayList<String>>  entryMap:valueMap.getValue().entrySet())
        	  {
    			  EmbeddedEntity aReading= new EmbeddedEntity();
        		  Key readingkey= new KeyFactory.Builder("COURSE", "CFA_LEVEL_3")
        				  .addChild("BOOK", rowEntry.getKey()).addChild("STUDY_SESSION", valueMap.getKey()).addChild("READING", entryMap.getKey()).getKey();
        		  aReading.setKey(readingkey);
        		  aReading.setProperty("name", entryMap.getKey());
        		  aReading.setProperty("los", entryMap.getValue());
        		  
        		  readings.add(aReading);
        	  }
    	  }
    	
      }
  		return course;
  }
  */
}
