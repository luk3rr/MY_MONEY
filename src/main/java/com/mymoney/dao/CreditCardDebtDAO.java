/*
 * Filename: CreditCardDebtDAO.java
 * Created on: August 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.CreditCardDebt;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for CreditCardDebt
 */
public class CreditCardDebtDAO
{
    private static CreditCardDebtDAO m_instance; // Singleton instance
    private EntityManager            m_entityManager;
    private static final Logger      m_logger = LoggerConfig.GetLogger();

    /**
     * Default constructor for JPA
     */
    public CreditCardDebtDAO(String entityManagerName)
    {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(entityManagerName);

        if (m_entityManager == null)
        {
            m_entityManager = entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Get the singleton instance of CreditCardDebtDAO
     * @return The singleton instance of CreditCardDebtDAO
     */
    public static CreditCardDebtDAO GetInstance(String entityManagerName)
    {
        if (m_instance == null)
        {
            m_instance = new CreditCardDebtDAO(entityManagerName);
        }

        return m_instance;
    }

    /**
     * Save a credit card debt in the database
     * @param creditCardDebt The credit card debt to be saved
     * @return True if the credit card debt was saved, false otherwise
     */
    public boolean Save(CreditCardDebt creditCardDebt)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.persist(creditCardDebt);
            m_entityManager.getTransaction().commit();

            m_logger.info("Credit card debt with id: " + creditCardDebt.GetId() +
                          " saved successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error saving credit card debt: " + e.getMessage());

            return false;
        }

        return true;
    }

    /**
     * Find a credit card debt in the database
     * @param id The id of the credit card debt to be found
     * @return The credit card debt if it was found, null otherwise
     */
    public CreditCardDebt Find(Long id)
    {
        return m_entityManager.find(CreditCardDebt.class, id);
    }

    /**
     * Update a credit card debt in the database
     * @param creditCardDebt The credit card debt to be updated
     * @return True if the credit card debt was updated, false otherwise
     */
    public boolean Update(CreditCardDebt creditCardDebt)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.merge(creditCardDebt);
            m_entityManager.getTransaction().commit();

            m_logger.info("Credit card debt with id: " + creditCardDebt.GetId() +
                          " updated successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error updating credit card debt: " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Delete a credit card debt from the database
     * @param creditCardDebt The credit card debt to be deleted
     * @return True if the credit card debt was deleted, false otherwise
     */
    public boolean Delete(Long id)
    {
        try
        {
            CreditCardDebt creditCardDebtToDelete = Find(id);

            if (creditCardDebtToDelete == null)
            {
                m_logger.warning("Credit card debt with id: " + id + " not found");
                return false;
            }

            m_entityManager.getTransaction().begin();
            m_entityManager.remove(creditCardDebtToDelete);
            m_entityManager.getTransaction().commit();

            m_logger.info("Credit card debt with id: " + id + " deleted successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error deleting credit card debt with id: " + id + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get all credit card debts from the database
     * @return A list with all credit card debts
     */
    public List<CreditCardDebt> GetAll()
    {
        return m_entityManager
            .createQuery("SELECT c FROM CreditCardDebt c", CreditCardDebt.class)
            .getResultList();
    }

    /**
     * Reset the table CreditCardDebt in the test database
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
            m_entityManager.createQuery("DELETE FROM CreditCardDebt").executeUpdate();
            m_entityManager.getTransaction().commit();
            m_entityManager.clear(); // Clear the persistence context
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error resetting table CreditCardDebt: " + e.getMessage());
            return false;
        }

        return true;
    }
}
