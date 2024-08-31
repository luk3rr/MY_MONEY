/*
 * Filename: WalletTransactionDAO.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.entities.WalletTransaction;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for WalletTransaction
 */
public class WalletTransactionDAO
{
    private static WalletTransactionDAO m_instance; // Singleton instance
    private EntityManager               m_entityManager;
    private static final Logger         m_logger = LoggerConfig.GetLogger();

    public WalletTransactionDAO(String entityManagerName)
    {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(entityManagerName);

        if (m_entityManager == null)
        {
            m_entityManager = entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Get the singleton instance of WalletTransactionDAO
     * @return The singleton instance of WalletTransactionDAO
     */
    public static WalletTransactionDAO GetInstance(String entityManagerName)
    {
        if (m_instance == null)
        {
            m_instance = new WalletTransactionDAO(entityManagerName);
        }

        return m_instance;
    }

    /**
     * Save a wallet transaction in the database
     * @param walletTransaction The wallet transaction to be saved
     * @return True if the wallet transaction was saved, false otherwise
     */
    public boolean Save(WalletTransaction walletTransaction)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.persist(walletTransaction);
            m_entityManager.getTransaction().commit();

            m_logger.info("Wallet transaction with id " + walletTransaction.GetId() +
                          " saved successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error saving wallet transaction with id " +
                            walletTransaction.GetId() + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Find a wallet transaction by its id
     * @param id The id of the wallet transaction
     * @return The wallet transaction with the given id, or null if it does not exist
     */
    public WalletTransaction Find(Long id)
    {
        return m_entityManager.find(WalletTransaction.class, id);
    }

    /**
     * Update a wallet transaction in the database
     * @param walletTransaction The wallet transaction to be updated
     * @return True if the wallet transaction was updated, false otherwise
     */
    public boolean Update(WalletTransaction walletTransaction)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.merge(walletTransaction);
            m_entityManager.getTransaction().commit();

            m_logger.info("Wallet transaction with id " + walletTransaction.GetId() +
                          " updated successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error updating wallet transaction with id " +
                            walletTransaction.GetId() + ": " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Delete a wallet transaction from the database
     * @param id The id of the wallet transaction to be deleted
     * @return True if the wallet transaction was deleted, false otherwise
     */
    public boolean Delete(Long id)
    {
        try
        {
            WalletTransaction walletTransaction = Find(id);

            if (walletTransaction == null)
            {
                m_logger.severe("Wallet transaction with id " + id + " not found");
                return false;
            }

            m_entityManager.getTransaction().begin();
            m_entityManager.remove(walletTransaction);
            m_entityManager.getTransaction().commit();

            m_logger.info("Wallet transaction with id " + walletTransaction.GetId() +
                          " deleted successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error deleting wallet transaction with id " + id + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get all wallet transactions from the database
     * @return A list with all wallet transactions
     */
    public List<WalletTransaction> GetAll()
    {
        return m_entityManager
            .createQuery("SELECT w FROM WalletTransaction w", WalletTransaction.class)
            .getResultList();
    }

    /**
     * Reset the table WalletTransaction in the test database
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
            m_entityManager.createQuery("DELETE FROM WalletTransaction").executeUpdate();
            m_entityManager.getTransaction().commit();
            m_entityManager.clear(); // Clear the persistence context
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error resetting table WalletTransaction: " + e.getMessage());
            return false;
        }

        return true;
    }
}
