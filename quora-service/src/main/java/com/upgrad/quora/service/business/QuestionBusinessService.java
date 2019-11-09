package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class QuestionBusinessService {

    @Autowired
    private UserDao userDao;

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

    /**
     * This method fetches all the questions posted by a particular user after
     * validating the authorization token is valid
     * If token is invalid or user is logged out then appropriate error message
     * is thrown back to the client
     * Same applies when the userId itself doesn't match with any user in DB
     *
     * @param userUUID      The user UUID whose questions have to be retrieved
     * @param authorization holds the Bearer access token for authenticating the user
     * @return The list of all questions posted by the user matched with userId
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     * @throws UserNotFoundException        If no user id with that UUID exists in DB
     */
    public List<Question> getAllQuestionsByUser(String userUUID, String authorization) throws AuthorizationFailedException, UserNotFoundException {
        final UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization);
        final User user = userDao.getUserByUUID(userUUID);
        // No user matched with the UUID
        if (user == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        return questionDao.findQuestionByUserId(user.getId());
    }
}
