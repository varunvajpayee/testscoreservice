package com.smodelware.smartcfa.util;

public enum ContentType 
{
	NOTE("NOTE"),
	QUESTION("QUESTION"),
	ANSWER("ANSWER"),
	VIDEO("VIDEO"),
	COURSE("COURSE"),
	BOOK("BOOK"),
	STUDY_SESSION("STUDY_SESSION"),
	READING("READING"),
	LOS("LOS"); //Learning Outcome
	
	private String contentType;

	private ContentType(String contentType) 
	{
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}
	
	
	public static ContentType getContentTypeFromValue(String inputValue)
	{
		for(ContentType ct:values())
		{
			if(ct.getContentType().equalsIgnoreCase(inputValue))
			{
				return ct;
			}	
		}	
		return null;
	}
	
}
