package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.AnswerEditRequest;
import com.upgrad.quora.api.model.AnswerEditResponse;
import com.upgrad.quora.api.model.AnswerRequest;
import com.upgrad.quora.api.model.AnswerResponse;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@RestController
public class AnswerController {
    @Autowired
    private AnswerBusinessService answerBusinessService;

    /**
     * This method is used for the corresponding question which
     * is to be answered in the database
     *
     * @param questionId    To get respective question using unique key call questionId
     * @param authorization holds the Bearer access token for authenticating the user.
     * @return the response for the answer which is created along with httpStatus
     * @throws AuthorizationFailedException If the access token provided by the user does not exist
     *                                      in the database, If the user has signed out
     * @throws InvalidQuestionException     If the question uuid entered by the user whose answer
     *                                      is to be posted does not exist in the database
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/{questionId}/answer/create")
    public ResponseEntity<AnswerResponse> createAnswer(final AnswerRequest answerRequest,
                                                       @PathVariable("questionId") final String questionId,
                                                       @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {

        final Answer answer = new Answer();
        answer.setAns(answerRequest.getAnswer());
        answer.setDate(ZonedDateTime.now());
        answer.setUuid(UUID.randomUUID().toString());
        final Answer updatedAnswer = answerBusinessService.createAnswer(answer, questionId, authorization);
        AnswerResponse answerResponse = new AnswerResponse().id(updatedAnswer.getUuid()).status("ANSWER CREATED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.CREATED);
    }

    /**
     * This method is used to edit the content of a specfic answer in a database
     * Note,only the owner of the answer can edit the answer
     *
     * @param answerId          Is the uuid of the answer that needed to be edited
     * @param authorization     holds the Bearer access token for authenticating the user.
     * @param answerEditRequest Is uuid of the edited answer and message 'ANSWER EDITED' in the JSON response with the corresponding HTTP status.
     * @return answer uuid with the message 'ANSWER EDITED'
     * @throws AnswerNotFoundException      If answer with uuid which is to be edited does not exist in the database
     * @throws AuthorizationFailedException If access token does not exit : if user has signed out : if non-owner tries to edit
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/answer/edit/{answerId}")
    public ResponseEntity<AnswerEditResponse> editAnswerContent(
            @PathVariable("answerId") final String answerId,
            @RequestHeader("authorization") final String authorization,
            final AnswerEditRequest answerEditRequest)
            throws AnswerNotFoundException, AuthorizationFailedException {
        final Answer answer = new Answer();
        answer.setAns(answerEditRequest.getContent());
        final Answer editAnswerEntity = answerBusinessService.editAnswerContent(answer, answerId, authorization);
        AnswerEditResponse answerEditResponse = new AnswerEditResponse().id(editAnswerEntity.getUuid()).status("ANSWER EDITED");
        return new ResponseEntity<AnswerEditResponse>(answerEditResponse, HttpStatus.OK);
    }

    /**
     * This method is used to delete the question. Note,
     * only the owner of the answer or admin can delete the question
     *
     * @param answerId      It is the Uuid of answer to be deleted
     * @param authorization holds the Bearer access token for authenticating the user.
     * @return uuid of the deleted answer and message 'ANSWER DELETED' in the JSON response with the corresponding HTTP status.
     * @throws AuthorizationFailedException If the access token provided by the user does not exist in the database,
     *                                      If the user has signed out, if the user who is not the owner of the answer or the role of the user is ‘nonadmin’ and tries to delete the answer
     * @throws AnswerNotFoundException      If the answer with uuid which is to be deleted does not exist in the database
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/answer/delete/{answerId}")
    public ResponseEntity<AnswerResponse> deleteAnswer(
            @PathVariable("answerId") final String answerId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException, InvalidQuestionException {
        String answerUUID = answerBusinessService.deleteAnswer(answerId, authorization);
        final AnswerResponse answerResponse = new AnswerResponse();
        answerResponse.id(answerUUID).status("ANSWER DELETED");
        return new ResponseEntity<AnswerResponse>(answerResponse, HttpStatus.OK);
    }

}
