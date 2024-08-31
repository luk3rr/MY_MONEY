/*
 * Filename: CategoryDAOTest.java
 * Created on: August 29, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mymoney.app.Category;
import com.mymoney.util.Constants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CategoryDAO
 */
public class CategoryDAOTest
{
    private static CategoryDAO m_categoryDAO;

    @BeforeAll
    public static void SetUp()
    {
        m_categoryDAO = CategoryDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the test database is reset before each test
        if (!m_categoryDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    {
        // Ensure that the test database is reset after all tests
        if (!m_categoryDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @Test
    public void TestCreateAndFindCategory()
    {
        Category category = new Category("My Category");
        assertTrue(m_categoryDAO.Save(category), "The category should be saved");

        Category foundCategory = m_categoryDAO.Find(category.GetId());

        assertNotNull(foundCategory, "The category should be found");

        assertEquals(category.GetId(),
                     foundCategory.GetId(),
                     "The category id should be the same");

        assertEquals(category.GetName(),
                     foundCategory.GetName(),
                     "The category name should be the same");
    }

    @Test
    public void TestUpdateCategory()
    {
        Category category = new Category("My Category");
        assertTrue(m_categoryDAO.Save(category), "The category should be saved");

        category.SetName("My Updated Category");
        assertTrue(m_categoryDAO.Save(category), "The category should be updated");

        Category foundCategory = m_categoryDAO.Find(category.GetId());

        assertNotNull(foundCategory, "The category should be found");

        assertEquals(category.GetId(),
                     foundCategory.GetId(),
                     "The category id should be the same");

        assertEquals(category.GetName(),
                     foundCategory.GetName(),
                     "The category name should be the same");
    }

    @Test
    public void TestDeleteValidCategory()
    {
        Category category = new Category("My Category");

        assertTrue(m_categoryDAO.Save(category), "The category should be saved");
        assertTrue(m_categoryDAO.Delete(category.GetId()),
                   "The category should be deleted");

        Category foundCategory = m_categoryDAO.Find(category.GetId());
        assertNull(foundCategory, "The category should be deleted");
    }

    @Test
    public void TestDeleteInvalidCategory()
    {
        Category category = new Category("My Category");

        // Save to generate an id
        assertTrue(m_categoryDAO.Save(category), "The category should be saved");

        // The category is saved in the database
        assertTrue(m_categoryDAO.Delete(category.GetId()),
                   "The category should be deleted");

        // Try to delete the category again
        assertFalse(m_categoryDAO.Delete(category.GetId()),
                    "The category should not be deleted");

        Category foundCategory = m_categoryDAO.Find(category.GetId());

        assertNull(foundCategory, "The category should not be found");
    }

    @Test
    public void TestGetAllCategories()
    {
        Category category1 = new Category("Category 1");
        Category category2 = new Category("Category 2");
        Category category3 = new Category("Category 3");

        assertTrue(m_categoryDAO.Save(category1), "The first category should be saved");

        assertTrue(m_categoryDAO.Save(category2),
                   "The second category should be saved");

        assertTrue(m_categoryDAO.Save(category3), "The third category should be saved");

        assertEquals(3,
                     m_categoryDAO.GetAll().size(),
                     "There should be three categories");
    }
}
