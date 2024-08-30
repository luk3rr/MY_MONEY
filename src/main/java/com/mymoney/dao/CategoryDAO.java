/*
 * Filename: CategoryDAO.java
 * Created on: August 29, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import com.mymoney.app.Category;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Data Access Object for Category
 */
public class CategoryDAO
{
    private static CategoryDAO  m_instance; // Singleton instance
    private EntityManager       m_entityManager;
    private static final Logger m_logger = LoggerConfig.GetLogger();

    /**
     * Default constructor for JPA
     */
    public CategoryDAO(String entityManagerName)
    {
        EntityManagerFactory entityManagerFactory =
            Persistence.createEntityManagerFactory(entityManagerName);

        if (m_entityManager == null)
        {
            m_entityManager = entityManagerFactory.createEntityManager();
        }
    }

    /**
     * Get the singleton instance of CategoryDAO
     * @return The singleton instance of CategoryDAO
     */
    public static CategoryDAO GetInstance(String entityManagerName)
    {
        if (m_instance == null)
        {
            m_instance = new CategoryDAO(entityManagerName);
        }

        return m_instance;
    }

    /**
     * Save a category in the database
     * @param category The category to be saved
     * @return True if the category was saved, false otherwise
     */
    public boolean Save(Category category)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.persist(category);
            m_entityManager.getTransaction().commit();

            m_logger.info("Category with id " + category.GetId() +
                          " saved successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error saving category with id " + category.GetId() + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get a category by its id
     * @param id The id of the category
     * @return The category with the given id or null if it does not exist
     */
    public Category Find(Short id)
    {
        return m_entityManager.find(Category.class, id);
    }

    /**
     * Update a category in the database
     * @param category The category to be updated
     * @return True if the category was updated, false otherwise
     */
    public boolean Update(Category category)
    {
        try
        {
            m_entityManager.getTransaction().begin();
            m_entityManager.merge(category);
            m_entityManager.getTransaction().commit();

            m_logger.info("Category with id " + category.GetId() +
                          " updated successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error updating category with id " + category.GetId() +
                            ": " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Delete a category from the database
     * @param id The id of the category to be deleted
     * @return True if the category was deleted, false otherwise
     */
    public boolean Delete(Short id)
    {
        try
        {
            Category category = Find(id);

            if (category == null)
            {
                m_logger.warning("Category with id " + id + " not found");
                return false;
            }

            m_entityManager.getTransaction().begin();
            m_entityManager.remove(category);
            m_entityManager.getTransaction().commit();

            m_logger.info("Category with id " + id + " deleted successfully");
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error deleting category with id " + id + ": " +
                            e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Get all categories from the database
     * @return A list with all categories
     */
    public List<Category> GetAll()
    {
        return m_entityManager.createQuery("SELECT c FROM Category c", Category.class)
            .getResultList();
    }

    /**
     * Reset the table Category in the test database
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
            m_entityManager.createQuery("DELETE FROM Category").executeUpdate();
            m_entityManager.getTransaction().commit();
            m_entityManager.clear(); // Clear the persistence context
        }
        catch (Exception e)
        {
            if (m_entityManager.getTransaction().isActive())
            {
                m_entityManager.getTransaction().rollback();
            }

            m_logger.severe("Error resetting table Category: " + e.getMessage());
            return false;
        }

        return true;
    }
}
