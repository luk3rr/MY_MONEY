/*
 * Filename: CreditCardDebtDAOTest.java
 * Created on: August 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mymoney.app.Category;
import com.mymoney.app.CreditCard;
import com.mymoney.app.CreditCardDebt;
import com.mymoney.util.Constants;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CreditCardDebtDAO
 */
public class CreditCardDebtDAOTest
{
    private static CreditCardDebtDAO m_creditCardDebtDAO;
    private static CreditCard        m_creditCard;
    private static CreditCard        m_creditCardToUpdate;
    private static Category          m_categoryToUpdate;
    private static Category          m_category;
    private static LocalDate         m_date;
    private static LocalDate         m_dateToUpdate;

    @BeforeAll
    public static void SetUp()
    {
        // Create a CreditCard, Category and LocalDate to be used in the tests
        CreditCard    m_creditCard = new CreditCard("TestCard", (short)1, 20.0);
        CreditCardDAO creditCardDAO =
            CreditCardDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);
        assertTrue(creditCardDAO.Save(m_creditCard), "The credit card should be saved");

        Category    m_category = new Category("TestCategory");
        CategoryDAO categoryDAO =
            CategoryDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);
        assertTrue(categoryDAO.Save(m_category), "The category should be saved");

        m_date = LocalDate.of(2000, 1, 1);

        // Create another CreditCard, Category and LocalDate to be used in the update
        // tests
        m_creditCardToUpdate = new CreditCard("TestCardToUpdate", (short)1, 20.0);
        assertTrue(creditCardDAO.Save(m_creditCardToUpdate),
                   "The credit card to update should be saved");

        m_categoryToUpdate = new Category("TestCategoryToUpdate");
        assertTrue(categoryDAO.Save(m_categoryToUpdate),
                   "The category to update should be saved");

        m_dateToUpdate = LocalDate.of(2024, 8, 30);

        m_creditCardDebtDAO =
            CreditCardDebtDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the test database is reset before each test
        if (!m_creditCardDebtDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    {
        // Ensure that the test database is reset after all tests
        if (!m_creditCardDebtDAO.ResetTable())
        {
            throw new RuntimeException(
                "Error resetting table CreditCardDebt after all tests");
        }

        // Also reset the another tables used in the tests
        // NOTE: This is necessary because the test database is shared among all tests
        if (!CreditCardDAO.GetInstance(Constants.ENTITY_MANAGER_TEST).ResetTable())
        {
            throw new RuntimeException(
                "Error resetting table CreditCard after all tests");
        }

        if (!CategoryDAO.GetInstance(Constants.ENTITY_MANAGER_TEST).ResetTable())
        {
            throw new RuntimeException(
                "Error resetting table Category after all tests");
        }
    }

    @Test
    public void TestCreateAndFindCreditCardDebt()
    {
        CreditCardDebt creditCardDebt =
            new CreditCardDebt(m_creditCard, m_category, m_date, 100.0, "TestDebt");

        assertTrue(m_creditCardDebtDAO.Save(creditCardDebt),
                   "The credit card debt should be saved");

        CreditCardDebt foundCreditCardDebt =
            m_creditCardDebtDAO.Find(creditCardDebt.GetId());

        assertNotNull(foundCreditCardDebt, "The credit card debt should be found");
    }

    @Test
    public void TestUpdateCreditCardDebt()
    {
        CreditCardDebt creditCardDebt =
            new CreditCardDebt(m_creditCard, m_category, m_date, 100.0, "TestDebt");

        assertTrue(m_creditCardDebtDAO.Save(creditCardDebt),
                   "The credit card debt should be saved");

        creditCardDebt.SetCategory(m_categoryToUpdate);
        creditCardDebt.SetDate(m_dateToUpdate);
        creditCardDebt.SetDescription("TestDebtUpdated");
        creditCardDebt.SetTotalAmount(200.0);

        assertTrue(m_creditCardDebtDAO.Update(creditCardDebt),
                   "The credit card debt should be updated");

        CreditCardDebt foundCreditCardDebt =
            m_creditCardDebtDAO.Find(creditCardDebt.GetId());

        assertNotNull(foundCreditCardDebt, "The credit card debt should be found");

        assertEquals(creditCardDebt.GetId(),
                     foundCreditCardDebt.GetId(),
                     "The credit card debt id should be the same");

        assertEquals(creditCardDebt.GetCategory().GetId(),
                     foundCreditCardDebt.GetCategory().GetId(),
                     "The credit card debt category id should be the same");

        assertEquals(creditCardDebt.GetDate(),
                     foundCreditCardDebt.GetDate(),
                     "The credit card debt date should be the same");

        assertEquals(creditCardDebt.GetDescription(),
                     foundCreditCardDebt.GetDescription(),
                     "The credit card debt description should be the same");

        assertEquals(creditCardDebt.GetTotalAmount(),
                     foundCreditCardDebt.GetTotalAmount(),
                     Constants.EPSILON,
                     "The credit card debt total amount should be the same");
    }

    @Test
    public void TestDeleteValidCreditCardDebt()
    {
        CreditCardDebt creditCardDebt =
            new CreditCardDebt(m_creditCard, m_category, m_date, 100.0, "TestDebt");

        assertTrue(m_creditCardDebtDAO.Save(creditCardDebt),
                   "The credit card debt should be saved");

        assertTrue(m_creditCardDebtDAO.Delete(creditCardDebt.GetId()),
                   "The credit card debt should be deleted");

        CreditCardDebt foundCreditCardDebt =
            m_creditCardDebtDAO.Find(creditCardDebt.GetId());

        assertNull(foundCreditCardDebt, "The credit card debt should be deleted");
    }

    @Test
    public void TestDeleteInvalidCreditCardDebt()
    {
        CreditCardDebt creditCardDebt =
            new CreditCardDebt(m_creditCard, m_category, m_date, 100.0, "TestDebt");

        // Save to get an id
        assertTrue(m_creditCardDebtDAO.Save(creditCardDebt),
                   "The credit card debt should be saved");

        // Delete the credit card debt
        assertTrue(m_creditCardDebtDAO.Delete(creditCardDebt.GetId()),
                   "The credit card debt should be deleted");

        // Try to delete the credit card debt again
        assertFalse(m_creditCardDebtDAO.Delete(creditCardDebt.GetId()),
                    "The credit card debt should not be deleted");

        CreditCardDebt foundCreditCardDebt =
            m_creditCardDebtDAO.Find(creditCardDebt.GetId());

        assertNull(foundCreditCardDebt, "The credit card debt should not be found");
    }

    @Test
    public void TestGetAllCreditCardDebts()
    {
        CreditCardDebt creditCardDebt1 =
            new CreditCardDebt(m_creditCard, m_category, m_date, 100.0, "TestDebt1");

        CreditCardDebt creditCardDebt2 =
            new CreditCardDebt(m_creditCard, m_category, m_date, 200.0, "TestDebt2");

        assertTrue(m_creditCardDebtDAO.Save(creditCardDebt1),
                   "The first credit card debt should be saved");

        assertTrue(m_creditCardDebtDAO.Save(creditCardDebt2),
                   "The second credit card debt should be saved");

        assertEquals(2,
                     m_creditCardDebtDAO.GetAll().size(),
                     "The number of credit card debts should be 2");
    }
}
