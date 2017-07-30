package com.smodelware.smartcfa.util;

public enum QuestionType 
{
	SINGLE("NOTE"),
	VIGNETTE("VIGNETTE"),
	ESSAY("ESSAY"); //Learning Outcome
	
	private String questionType;

	private QuestionType(String questionType) 
	{
		this.questionType = questionType;
	}

	public String getQuestionType() {
		return questionType;
	}
	
	public QuestionType getQuestionTypeFromValue(String inputValue)
	{
		for(QuestionType ct:values())
		{
			if(ct.getQuestionType().equalsIgnoreCase(inputValue))
			{
				return ct;
			}	
		}	
		return null;
	}
}
