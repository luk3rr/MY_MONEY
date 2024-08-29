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
import java.io.File;
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
        // Delete the test database file if it exists before running the tests
        File dbFile = new File(Constants.DB_TEST_FILE);
        if (dbFile.exists())
        {
            dbFile.delete();
        }

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
    { }

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
    public void TestDeleteCategory()
    {
        Category category = new Category("My Category");
        assertTrue(m_categoryDAO.Save(category), "The category should be saved");

        assertTrue(m_categoryDAO.Delete(category.GetId()),
                   "The category should be deleted");

        Category foundCategory = m_categoryDAO.Find(category.GetId());
        assertNull(foundCategory, "The category should not be found");
    }

    @Test
    public void TestFindAllCategories()
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
