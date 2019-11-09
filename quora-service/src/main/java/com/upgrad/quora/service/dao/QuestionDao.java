package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.Question;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.NoResultException;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retrieves all the questions present in the Database question table and returns as a list
     *
     * @return The list of questions present in the question table
     */
    public List<Question> getAllQuestions() {
        final List<Question> allQuestions = entityManager.createQuery("select q from Question q", Question.class).getResultList();
        return allQuestions;
    }
    
    /**
     * Retrieves question present in database by ID
     *
     * @return The question present in the question table
     */
    public Question getQuestionByUUID(String questionUUID) {
        try {
            return entityManager.createNamedQuery("questionByUUID", Question.class).setParameter("uuid", questionUUID).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    
    // Edit Question method (JPA merge state)
    @Transactional
    public Question updateQuestion(Question question){
        entityManager.merge(question);
        return question;
    }
}
