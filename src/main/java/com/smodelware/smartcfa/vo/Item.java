package com.smodelware.smartcfa.vo;

import com.google.appengine.api.datastore.Entity;
import com.smodelware.smartcfa.util.ContentType;

import java.util.ArrayList;
import java.util.List;

public class Item 
{
	 String id;
	 String text;	 
	 boolean leaf;
	 String view="RevealLeft";
	 List<Item> items= new ArrayList<Item>();
	 String kind;
	String ancestorId;
	String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAncestorId() {
		return ancestorId;
	}
	public void setAncestorId(String ancestorId) {
		this.ancestorId = ancestorId;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isLeaf() {
		return leaf;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public String getView() 
	{		
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item> items) {
		this.items = items;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	
	public static Item convertEntityToItem(Entity entity,Boolean isLeaf,String ancestorId, String contentTypeStr)
	{
		Item item = new Item();
		item.setText(String.valueOf(entity.getProperties().get("name")));
		item.setId(String.valueOf(entity.getProperties().get("IDENTITY")));
		item.setKind(entity.getKind());
		item.setLeaf(isLeaf);
		if( ContentType.NOTE.getContentType().equals(contentTypeStr)) {
			item.setView("Npanel");
			item.setText(String.valueOf(entity.getProperties().get("name")));
		}
		else if( ContentType.QUESTION.getContentType().equals(contentTypeStr)){
			item.setView("QPanel");
			String questionCount = String.valueOf(entity.getProperties().get("Q_COUNT"));
			item.setText(String.valueOf(entity.getProperties().get("name"))+"<br><b>(Total Questions:"+questionCount+")</b>");
		}
		item.setUrl(String.valueOf(entity.getProperties().get("URL")));
		item.setAncestorId(ancestorId);
		return item;
	}
	
	public static List<Item> convertEntitiesToItem(List<Entity> entities,Boolean isLeaf,String ancestorId, String contentTypeStr)
	{
		List<Item> itemList = new ArrayList<Item>();  
		for(Entity entity:entities)
		{
			itemList.add(convertEntityToItem(entity,isLeaf,ancestorId,contentTypeStr));
		}	
		return itemList;
	}
	 
	
	 
}
