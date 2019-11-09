package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.Answer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class AnswerDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Saves the answer for the question
     *
     * @param answerEntity answer for the question
     * @return answer for the question
     */
    public Answer createAnswer(Answer answerEntity) {
        entityManager.persist(answerEntity);
        return answerEntity;
    }

}
