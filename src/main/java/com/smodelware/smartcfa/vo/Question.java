package com.smodelware.smartcfa.vo;

import com.google.appengine.api.datastore.Entity;

public class Question 
{
	String losId;
	String questionText;
	String qA;
	String qB;
	String qC;
	String qD;
	String qAID;
	String qBID;
	String qCID;
	String qDID;

	public void setqAID(String qAID) {
		this.qAID = qAID;
	}

	public void setqBID(String qBID) {
		this.qBID = qBID;
	}

	public void setqCID(String qCID) {
		this.qCID = qCID;
	}

	public void setqDID(String qDID) {
		this.qDID = qDID;
	}

	public String getqAID() {
		return qAID;
	}

	public String getqBID() {
		return qBID;
	}

	public String getqCID() {
		return qCID;
	}

	public String getqDID() {
		return qDID;
	}

	String questionId;
	String questionNum;
	String answerId;
	String answerOption;
	String answerText;
	String answerEnabled;
	String answerSelected;

	public void setScreenQuestionNum(String screenQuestionNum) {
		this.screenQuestionNum = screenQuestionNum;
	}

	String screenQuestionNum;

    public String getScreenQuestionNum() {
        return screenQuestionNum;
    }

    public String getAnswerSelected() {
		return answerSelected;
	}

	public void setAnswerSelected(String answerSelected) {
		this.answerSelected = answerSelected;
	}

	public String getLosId() {
		return losId;
	}
	public void setLosId(String losId) {
		this.losId = losId;
	}
	public String getQuestionText() {
		return questionText;
	}
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	public String getqA() {
		return qA;
	}
	public void setqA(String qA) {
		this.qA = qA;
	}
	public String getqB() {
		return qB;
	}
	public void setqB(String qB) {
		this.qB = qB;
	}
	public String getqC() {
		return qC;
	}
	public void setqC(String qC) {
		this.qC = qC;
	}
	public String getqD() {
		return qD;
	}
	public void setqD(String qD) {
		this.qD = qD;
	}
	public String getQuestionId() {
		return questionId;
	}
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
	public String getQuestionNum() {
		return questionNum;
	}
	public void setQuestionNum(String questionNum) {
		this.questionNum = questionNum;
	}

	public String getAnswerId() {
		return answerId;
	}
	public void setAnswerId(String answerId) {
		this.answerId = answerId;
	}
	public String getAnswerText() {
		return answerText;
	}
	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}
	
	public String getAnswerOption() {
		return answerOption;
	}
	public void setAnswerOption(String answerOption) {
		this.answerOption = answerOption;
	}
	
	
	public String getAnswerEnabled() {
		return answerEnabled;
	}
	public void setAnswerEnabled(String answerEnabled) {
		this.answerEnabled = answerEnabled;
	}



	public static Question convertEntityToQuestion(Entity entity, boolean iAllowAnswers)
	{
		Question question = new Question();
		question.setLosId(String.valueOf(entity.getProperties().get("LOS")));
		question.setQuestionText(String.valueOf(entity.getProperties().get("QUESTION_TEXT")));
		question.setqA(String.valueOf(entity.getProperties().get("Q_A")));
		question.setqB(String.valueOf(entity.getProperties().get("Q_B")));
		question.setqC(String.valueOf(entity.getProperties().get("Q_C")));
		question.setqD(String.valueOf(entity.getProperties().get("Q_D")));
		question.setQuestionId(String.valueOf(entity.getProperties().get("QUESTION")));
		question.setAnswerId(String.valueOf(entity.getProperties().get("ANSWER")));
		question.setAnswerOption(String.valueOf(entity.getProperties().get("ANSWER_OPTION")));
		question.setAnswerText(String.valueOf(entity.getProperties().get("ANSWER_TEXT")));
		question.setqAID(question.getQuestionId()+"_ANS_A");
		question.setqBID(question.getQuestionId()+"_ANS_B");
		question.setqCID(question.getQuestionId()+"_ANS_C");
		question.setqDID(question.getQuestionId()+"_ANS_D");

		if(iAllowAnswers)
		{
			question.setAnswerEnabled("");
		}
		else
		{
			question.setAnswerEnabled("hidden");
		}

		return question;
	}
	
	
}
