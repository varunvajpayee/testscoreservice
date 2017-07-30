package com.smodelware.smartcfa.vo;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.List;

public class UserTest
{
	String testId="MultipleChoice";

	List<Question> questions = new ArrayList<Question>();

	public Integer getTotalQuestions() {
		return totalQuestions;
	}

	public void setTotalQuestions(Integer totalQuestions) {
		this.totalQuestions = totalQuestions;
	}

	Integer totalQuestions; //This field represents total questions in current section
	Integer totalCorrectAnswered;
	Integer totalAnswered;
	Double scoredPercentage;

	public Double getScoredPercentage() {
		return scoredPercentage;
	}

	public void setScoredPercentage(Double scoredPercentage) {
		this.scoredPercentage = scoredPercentage;
	}

	public Integer getTotalAnswered() {
		return totalAnswered;
	}

	public void setTotalAnswered(Integer totalAnswered) {
		this.totalAnswered = totalAnswered;
	}

	public Integer getTotalCorrectAnswered() {
		return totalCorrectAnswered;
	}

	public void setTotalCorrectAnswered(Integer totalCorrectAnswered) {
		this.totalCorrectAnswered = totalCorrectAnswered;
	}

	public String getTestId()
	{
		return testId;
	}
	public void setTestId(String testId) 
	{
		this.testId = testId;
	}	
	public List<Question> getQuestions() 
	{
		return questions;
	}	
	public void setQuestions(List<Question> questions) 
	{
		this.questions = questions;
	}

}
