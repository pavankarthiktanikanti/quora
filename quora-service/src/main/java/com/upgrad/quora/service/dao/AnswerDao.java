package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.Answer;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

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

    /**
     * This method is to get a answer by uuid from db
     *
     * @param answerUUID is the uuid of answer to get from db
     * @return the answer present in db
     */
    public Answer getAnswerByUUID(String answerUUID) {
        try {
            return entityManager.createNamedQuery("answerByUUID", Answer.class).setParameter("uuid", answerUUID).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * This method is used to update the answer content to db
     *
     * @param answer Is the answer that needed to be merged to db
     * @return answer that is merged in db
     */
    public Answer updateAnswerContent(Answer answer) {
        entityManager.merge(answer);
        return answer;
    }

    /**
     * This method is used to delete the answer record from db
     *
     * @param answer entity to be deleted from db
     */
    public void deleteAnswer(Answer answer) {
        entityManager.remove(answer);
    }

    /**
     * Retrieves all the answer records based on the question
     *
     * @param questionId The question Id attribute to pull the answers with foreign key value
     * @return The list of all answers matched with the question Id
     */
    public List<Answer> getAllAnswersByQuestionId(Integer questionId) {
        return entityManager.createNamedQuery("answerByQuestionId", Answer.class).setParameter("questionId", questionId).getResultList();
    }

}
