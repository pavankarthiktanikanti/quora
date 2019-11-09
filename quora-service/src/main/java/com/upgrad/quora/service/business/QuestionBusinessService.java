package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionBusinessService {

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserBusinessService userBusinessService;

    /**
     * This method pulls all the question details from the database after validating the user authorization token
     * If the token is not valid, throws an Authorization failure
     *
     * @param authorization holds the Bearer access token for authenticating the user
     * @return All the Questions added in the application present in the Database
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     */
    public List<Question> getAllQuestions(String authorization) throws AuthorizationFailedException {
        userBusinessService.validateUserAuthentication(authorization);
        return questionDao.getAllQuestions();
    }
    
    /**
     * This method is used to edit question content :
     * checks for all the conditions and provides necessary response messages
     *
     * @param question      entity
     * @param questionId    for the question which needs to be edited
     * @param authorization holds the Bearer access token for authenticating
     * @return updates the question as per the questionId provided
     * @throws AuthorizationFailedException if access token does not exit, if user has signed out, if non-owner tries to edit
     * @throws InvalidQuestionException     if question with uuid which is to be edited does not exist in the database
     */
    public Question editQuestionContent(final Question question, final String questionId, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization);
        Question questionEntity = questionDao.getQuestionByUUID(questionId);
        // If the question with uuid which is to be edited does not exist in the database, throw 'InvalidQuestionException'
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        } else {
            // if the user who is not the owner of the question tries to edit the question throw "AuthorizationFailedException"
            if (questionEntity.getUser() != userAuthEntity.getUser()) {
                throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
            }
        }
        questionEntity.setContent(question.getContent());
        return questionDao.updateQuestion(questionEntity);
    }
}
