package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;

@Controller
public class QuestionController {

    @Autowired
    private QuestionBusinessService questionBusinessService;

    /**
     * This method validates the user session and if active pulls all the questions from the database
     * Populates the uuid and content of each question posted earlier in the application and sends in the response
     * If session token is invalid, then throws the error message of Authorization failure
     *
     * @param authorization holds the Bearer access token for authenticating the user
     * @return The List of question details(uuid, question content) present in the database
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     */
    @RequestMapping(method = RequestMethod.GET, path = "/question/all")
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        final List<Question> allQuestions = questionBusinessService.getAllQuestions(authorization);
        return getQuestionDetailsResponse(allQuestions);
    }

    /**
     * This method takes the list of question as input and populates the corresponding response objects
     * setting the uuid and the content of each question. Add the Http Response code so that this method
     * return value can be used to return in the corresponding request mapped methods
     *
     * @param allQuestions The List of Questions retrieved from the Database to populate the responses
     * @return ResponseEntity with the required question details populated and the HTTP Status added
     */
    private ResponseEntity<List<QuestionDetailsResponse>> getQuestionDetailsResponse(List<Question> allQuestions) {
        List<QuestionDetailsResponse> allQuesDetailsResponse = new ArrayList<>();
        for (Question question : allQuestions) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.id(question.getUuid()).content(question.getContent());
            allQuesDetailsResponse.add(questionDetailsResponse);
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(allQuesDetailsResponse, HttpStatus.OK);
    }
    
    /**
     * This method is used to edit a question that has been posted by a user. Note, only the owner of the 
     * question can edit the question.
     * @param questionId for the question which needs to be edited.
     * @param authorization holds the Bearer access token for authenticating the user.
     * @return uuid of the edited question and message 'QUESTION EDITED' in the JSON response with the corresponding HTTP status.
     * @throws AuthorizationFailedException : if access token does not exit : if user has signed out : if non-owner tries to edit
     * @throws InvalidQuestionException : if question with uuid which is to be edited does not exist in the database
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}")
    public ResponseEntity<QuestionEditResponse> editQuestionContent(
            @PathVariable("questionId") final String questionId,
            @RequestHeader("authorization") final String authorization,
            final QuestionEditRequest questionEditRequest)
            throws InvalidQuestionException, AuthorizationFailedException{
            final Question question = new Question();
            question.setContent(questionEditRequest.getContent());
            final Question editQuestionEntity = questionBusinessService.editQuestionContent(question,questionId,authorization);
            QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(editQuestionEntity.getUuid()).status("QUESTION EDITED");
            return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }
}
