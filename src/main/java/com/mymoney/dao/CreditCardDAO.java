/*
 * Filename: CreditCardDAO.java
 * Created on: August 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.entities.CreditCard;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for CreditCard
 */
public class CreditCardDAO
{
    private static CreditCardDAO m_instance; // Singleton instance
    private EntityManager        m_entityManager;
    private static final Logger  m_logger = LoggerConfig.GetLogger();

    public CreditCardDAO(String entityManagerName)
    {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(entityManagerName);

        if (m_entityManager == null)
        {
            m_entityManager = entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Get the singleton instance of CreditCardDAO
     * @return The singleton instance of CreditCardDAO
     */
    public static CreditCardDAO GetInstance(String entityManagerName)
    {
        if (m_instance == null)
        {
            m_instance = new CreditCardDAO(entityManagerName);
        }

        return m_instance;
    }

    /**
     * Save a credit card in the database
     * @param creditCard The credit card to be saved
     * @return True if the credit card was saved, false otherwise
     */
    public boolean Save(CreditCard creditCard)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.persist(creditCard);
            m_entityManager.getTransaction().commit();

            m_logger.info("CreditCard with name " + creditCard.GetName() +
                          " saved successfully");
        }
        catch (EntityExistsException e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("CreditCard with name " + creditCard.GetName() +
                            " already exists in the database");
            return false;
        }

        return true;
    }

    /**
     * Find a credit card by its name
     * @param name The name of the credit card
     * @return The credit card with the given name
     */
    public CreditCard Find(String name)
    {
        return m_entityManager.find(CreditCard.class, name);
    }

    /**
     * Update a credit card in the database
     * @param creditCard The credit card to be updated
     * @return True if the credit card was updated, false otherwise
     */
    public boolean Update(CreditCard creditCard)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.merge(creditCard);
            m_entityManager.getTransaction().commit();

            m_logger.info("CreditCard with name " + creditCard.GetName() +
                          " updated successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error updating credit card with name " +
                            creditCard.GetName() + ": " + e.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Delete a credit card from the database
     * @param name The name of the credit card to be deleted
     * @return True if the credit card was deleted, false otherwise
     */
    public boolean Delete(String name)
    {
        try
        {

            CreditCard creditCard = Find(name);

            if (creditCard == null)
            {
                m_logger.severe("CreditCard with name " + name + " not found");
                return false;
            }

            m_entityManager.getTransaction().begin();
            m_entityManager.remove(creditCard);
            m_entityManager.getTransaction().commit();

            m_logger.info("CreditCard with name " + name + " deleted successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error deleting credit card with name " + name + ": " +
                            e.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Get all credit cards in the database
     * @return A list with all credit cards in the database
     */
    public List<CreditCard> GetAll()
    {
        return m_entityManager
            .createQuery("SELECT c FROM CreditCard c", CreditCard.class)
            .getResultList();
    }

    /**
     * Reset the table CreditCard in the test database
     * @return True if the table was reset, false otherwise
     * @note This method is used for testing purposes only
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
            m_entityManager.createQuery("DELETE FROM CreditCard").executeUpdate();
            m_entityManager.getTransaction().commit();
            m_entityManager.clear(); // Clear the persistence context
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error resetting the table CreditCard: " + e.getMessage());
            return false;
        }

        return true;
    }
}
