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
}
