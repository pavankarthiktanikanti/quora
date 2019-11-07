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
    
    // method to edit question content : checks for all the conditions
    public Question EditQuestionContent(final Question question,final String questionid, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = questionDao.getUserAuthToken(authorization);

        if(userAuthEntity != null){
            ZonedDateTime logout = userAuthEntity.getLogoutAt();
            // If the user has signed out, throw 'AuthorizationFailedException'
            if (logout != null) {
                throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit the question");
            }

            Question Entity = questionDao.getQuestionByUUID(questionid);
            // If the question with uuid which is to be edited does not exist in the database, throw 'InvalidQuestionException'
            if (Entity == null)
            {
                throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
            }
            else
            {
                // if the user who is not the owner of the question tries to edit the question throw "AuthorizationFailedException"
                if (Entity.getUser() != userAuthEntity.getUser()){
                    throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
                }
            }

            question.setId(Entity.getId());
            question.setUuid(Entity.getUuid());
            question.setDate(Entity.getDate());
            question.setUser(Entity.getUser());
            return questionDao.editquestion(question);
        }
        // If the access token provided by the user does not exist in the database throw 'AuthorizationFailedException'
        throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
    }
}
