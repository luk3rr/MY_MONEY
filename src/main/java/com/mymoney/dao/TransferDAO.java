/*
 * Filename: TransferDAO.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.entities.Transfer;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for Transfer
 */
public class TransferDAO
{
    private static TransferDAO  m_instance; // Singleton instance
    private EntityManager       m_entityManager;
    private static final Logger m_logger = LoggerConfig.GetLogger();

    public TransferDAO(String entityManagerName)
    {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(entityManagerName);

        if (m_entityManager == null)
        {
            m_entityManager = entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Get the singleton instance of TransferDAO
     * @return The singleton instance of TransferDAO
     */
    public static TransferDAO GetInstance(String entityManagerName)
    {
        if (m_instance == null)
        {
            m_instance = new TransferDAO(entityManagerName);
        }

        return m_instance;
    }

    /**
     * Save a transfer in the database
     * @param transfer The transfer to be saved
     * @return True if the transfer was saved, false otherwise
     */
    public boolean Save(Transfer transfer)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.persist(transfer);
            m_entityManager.getTransaction().commit();

            m_logger.info("Transfer with id " + transfer.GetId() +
                          " saved successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error saving transfer with id " + transfer.GetId() + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Find a transfer by its ID
     * @param id The ID of the transfer
     * @return The transfer with the given ID, or null if it was not found
     */
    public Transfer Find(Long id)
    {
        return m_entityManager.find(Transfer.class, id);
    }

    /**
     * Update a transfer in the database
     * @param transfer The transfer to be updated
     * @return True if the transfer was updated, false otherwise
     */
    public boolean Update(Transfer transfer)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.merge(transfer);
            m_entityManager.getTransaction().commit();

            m_logger.info("Transfer with id " + transfer.GetId() +
                          " updated successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error updating transfer with id " + transfer.GetId() +
                            ": " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Delete a transfer from the database
     * @param transfer The transfer to be deleted
     * @return True if the transfer was deleted, false otherwise
     */
    public boolean Delete(Long id)
    {
        try
        {
            Transfer transferToDelete = Find(id);

            if (transferToDelete == null)
            {
                m_logger.warning("Transfer with id " + id + " not found");
                return false;
            }

            m_entityManager.getTransaction().begin();
            m_entityManager.remove(transferToDelete);
            m_entityManager.getTransaction().commit();

            m_logger.info("Transfer with id " + id + " deleted successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error deleting transfer with id " + id + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get all transfers from the database
     * @return A list with all transfers
     */
    public List<Transfer> GetAll()
    {
        return m_entityManager.createQuery("SELECT t FROM Transfer t", Transfer.class)
            .getResultList();
    }

    /**
     * Reset the table Transfer in the test database
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
            m_entityManager.createQuery("DELETE FROM Transfer").executeUpdate();
            m_entityManager.getTransaction().commit();
            m_entityManager.clear(); // Clear the entity manager cache
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error resetting table Transfer: " + e.getMessage());
            return false;
        }

        return true;
    }
}
