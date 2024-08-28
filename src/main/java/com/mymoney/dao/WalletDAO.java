/*
 * Filename: WalletDAO.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.Wallet;
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

    /**
     * Default constructor for JPA
     */
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
        }
        catch (EntityExistsException e)
        {
            m_entityManager.getTransaction().rollback();

            m_logger.severe("Wallet already exists in the database");
            return false;
        }

        m_logger.info("Wallet saved successfully");
        return true;
    }

    /**
     * Find a wallet by its name
     * @param name The name of the wallet
     * @return The wallet found
     */
    public Wallet Find(String name)
    {
        return m_entityManager.find(Wallet.class, name);
    }

    /**
     * Update a wallet in the database
     * @param wallet The wallet to be updated
     */
    public void Update(Wallet wallet)
    {
        m_entityManager.getTransaction().begin();
        m_entityManager.merge(wallet);
        m_entityManager.getTransaction().commit();
    }

    /**
     * Delete a wallet from the database
     * @param wallet The wallet to be deleted
     */
    public void Delete(Wallet wallet)
    {
        m_entityManager.getTransaction().begin();
        m_entityManager.remove(wallet);
        m_entityManager.getTransaction().commit();
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
     * Reset the test database
     * @return True if the database was cleaned, false otherwise
     * @note This method is only for testing purposes
     */
    public boolean ResetTestDatabase()
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
        }
        catch (Exception e)
        {
            m_entityManager.getTransaction().rollback();

            m_logger.severe("Error resetting the test database: " + e.getMessage());
            return false;
        }

        return true;
    }
}
