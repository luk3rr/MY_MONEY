/*
 * Filename: CreditCardPaymentDAO.java
 * Created on: August 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.CreditCardPayment;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for CreditCardPayment
 */
public class CreditCardPaymentDAO
{
    private static CreditCardPaymentDAO m_instance; // Singleton instance
    private EntityManager               m_entityManager;
    private static final Logger         m_logger = LoggerConfig.GetLogger();

    public CreditCardPaymentDAO(String entityManagerName)
    {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(entityManagerName);

        if (m_entityManager == null)
        {
            m_entityManager = entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Get the singleton instance of CreditCardPaymentDAO
     * @return The singleton instance of CreditCardPaymentDAO
     */
    public static CreditCardPaymentDAO GetInstance(String entityManagerName)
    {
        if (m_instance == null)
        {
            m_instance = new CreditCardPaymentDAO(entityManagerName);
        }

        return m_instance;
    }

    /**
     * Save a credit card payment in the database
     * @param creditCardPayment The credit card payment to be saved
     * @return True if the credit card payment was saved, false otherwise
     */
    public boolean Save(CreditCardPayment creditCardPayment)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.persist(creditCardPayment);
            m_entityManager.getTransaction().commit();

            m_logger.info("Credit card payment with id: " + creditCardPayment.GetId() +
                          " was saved successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error saving credit card payment with id: " +
                            creditCardPayment.GetId() + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Find a credit card payment by its id
     * @param id The id of the credit card payment
     * @return The credit card payment with the given id
     */
    public CreditCardPayment Find(Long id)
    {
        return m_entityManager.find(CreditCardPayment.class, id);
    }

    /**
     * Update a credit card payment in the database
     * @param creditCardPayment The credit card payment to be updated
     * @return True if the credit card payment was updated, false otherwise
     */
    public boolean Update(CreditCardPayment creditCardPayment)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.merge(creditCardPayment);
            m_entityManager.getTransaction().commit();

            m_logger.info("Credit card payment with id: " + creditCardPayment.GetId() +
                          " was updated successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error updating credit card payment with id: " +
                            creditCardPayment.GetId() + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Delete a credit card payment from the database
     * @param id The id of the credit card payment to be deleted
     * @return True if the credit card payment was deleted, false otherwise
     */
    public boolean Delete(Long id)
    {
        try
        {
            CreditCardPayment creditCardPayment = Find(id);

            if (creditCardPayment == null)
            {
                m_logger.severe("Credit card payment with id: " + id +
                                " was not found");
                return false;
            }

            m_entityManager.getTransaction().begin();
            m_entityManager.remove(creditCardPayment);
            m_entityManager.getTransaction().commit();

            m_logger.info("Credit card payment with id: " + id +
                          " was deleted successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error deleting credit card payment with id: " + id + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get all credit card payments from the database
     * @return A list with all credit card payments
     */
    public List<CreditCardPayment> GetAll()
    {
        return m_entityManager
            .createQuery("SELECT c FROM CreditCardPayment c", CreditCardPayment.class)
            .getResultList();
    }

    /**
     * Reset the table CreditCardPayment in the test database
     * @return True if the table was reset, false otherwise
     * @note This method is used only for testing purposes
     */
    public boolean ResetTable()
    {
        // Check if entityManagerName is the test entity manager
        if (!m_entityManager.getEntityManagerFactory()
                 .getProperties()
                 .get("hibernate.ejb.persistenceUnitName")
                 .equals(Constants.ENTITY_MANAGER_TEST))
        {
            m_logger.severe(
                "The test database cannot be reset with the production entity manager");

            return false;
        }

        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.createQuery("DELETE FROM CreditCardPayment")
                .executeUpdate();
            m_entityManager.getTransaction().commit();
            m_entityManager.clear(); // Clear the persistence context
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error resetting table CreditCardPayment: " +
                            e.getMessage());
            return false;
        }

        return true;
    }
}
