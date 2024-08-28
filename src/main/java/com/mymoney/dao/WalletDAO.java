/*
 * Filename: WalletDAO.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.Wallet;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for Wallet
 */
public class WalletDAO
{
    private static WalletDAO m_instance; // Singleton instance
    private EntityManager    m_entityManager;

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
     */
    public void Save(Wallet wallet)
    {
        m_entityManager.getTransaction().begin();
        m_entityManager.persist(wallet);
        m_entityManager.getTransaction().commit();
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
}
