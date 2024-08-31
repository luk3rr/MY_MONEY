/*
 * Filename: WalletDAO.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.entities.Wallet;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for Wallet
 */
public class WalletDAO
{
    private static WalletDAO    m_instance; // Singleton instance
    private EntityManager       m_entityManager;
    private static final Logger m_logger = LoggerConfig.GetLogger();

    public WalletDAO(String entityManagerName)
    {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(entityManagerName);

        if (m_entityManager == null)
        {
            m_entityManager = entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Get the singleton instance of WalletDAO
     * @return The singleton instance of WalletDAO
     */
    public static WalletDAO GetInstance(String entityManagerName)
    {
        if (m_instance == null)
        {
            m_instance = new WalletDAO(entityManagerName);
        }

        return m_instance;
    }

    /**
     * Save a wallet in the database
     * @param wallet The wallet to be saved
     * @return True if the wallet was saved, false otherwise
     */
    public boolean Save(Wallet wallet)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.persist(wallet);
            m_entityManager.getTransaction().commit();

            m_logger.info("Wallet with name " + wallet.GetName() +
                          " saved successfully");
        }
        catch (EntityExistsException e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Wallet with name " + wallet.GetName() +
                            " already exists in the database");
            return false;
        }

        return true;
    }

    /**
     * Find a wallet by its name
     * @param name The name of the wallet
     * @return The wallet with the given name or null if it does not exist
     */
    public Wallet Find(String name)
    {
        return m_entityManager.find(Wallet.class, name);
    }

    /**
     * Update a wallet in the database
     * @param wallet The wallet to be updated
     * @return True if the wallet was updated, false otherwise
     */
    public boolean Update(Wallet wallet)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.merge(wallet);
            m_entityManager.getTransaction().commit();

            m_logger.info("Wallet with name " + wallet.GetName() +
                          " updated successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error updating wallet with name " + wallet.GetName() +
                            ": " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Delete a wallet from the database
     * @param wallet The wallet to be deleted
     * @return True if the wallet was deleted, false otherwise
     */
    public boolean Delete(String name)
    {
        try
        {
            Wallet walletToDelete = Find(name);

            if (walletToDelete == null)
            {
                m_logger.warning("Wallet with name " + name + " not found");
                return false;
            }

            m_entityManager.getTransaction().begin();
            m_entityManager.remove(walletToDelete);
            m_entityManager.getTransaction().commit();

            m_logger.info("Wallet with name " + name + " deleted successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error deleting wallet with name " + name + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get all wallets from the database
     * @return All wallets from the database
     */
    public List<Wallet> GetAll()
    {
        return m_entityManager.createQuery("SELECT w FROM Wallet w", Wallet.class)
            .getResultList();
    }

    /**
     * Reset the table Wallet in the test database
     * @return True if the table was reset, false otherwise
     * @note This method is only for testing purposes
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
            m_entityManager.createQuery("DELETE FROM Wallet").executeUpdate();
            m_entityManager.getTransaction().commit();
            m_entityManager.clear(); // Clear the persistence context
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error resetting table Wallet: " + e.getMessage());
            return false;
        }

        return true;
    }
}
