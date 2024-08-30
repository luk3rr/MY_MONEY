/*
 * Filename: CreditCardDAOTest.java
 * Created on: August 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mymoney.app.CreditCard;
import com.mymoney.util.Constants;
import java.io.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CreditCardDAO
 */
public class CreditCardDAOTest
{
    private static CreditCardDAO m_creditCardDAO;

    @BeforeAll
    public static void SetUp()
    {
        // Delete the test database file if it exists before running the tests
        File dbFile = new File(Constants.DB_TEST_FILE);
        if (dbFile.exists())
        {
            dbFile.delete();
        }

        m_creditCardDAO = CreditCardDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the test database is reset before each test
        if (!m_creditCardDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    { }

    @Test
    public void TestCreateAndFindCreditCard()
    {
        CreditCard creditCard = new CreditCard("My Credit Card", (short)10, 1000.0);

        assertTrue(m_creditCardDAO.Save(creditCard), "The credit card should be saved");

        CreditCard foundCreditCard = m_creditCardDAO.Find(creditCard.GetName());

        assertNotNull(foundCreditCard, "The credit card should be found");

        assertEquals(creditCard.GetName(),
                     foundCreditCard.GetName(),
                     "The credit card name should be the same");

        assertEquals(creditCard.GetBillingDueDay(),
                     foundCreditCard.GetBillingDueDay(),
                     "The credit card billing due day should be the same");

        assertEquals(creditCard.GetMaxDebt(),
                     foundCreditCard.GetMaxDebt(),
                     Constants.EPSILON,
                     "The credit card max debt should be the same");
    }

    @Test
    public void TestUpdateCreditCard()
    {
        CreditCard creditCard = new CreditCard("My Credit Card", (short)10, 1000.0);
        assertTrue(m_creditCardDAO.Save(creditCard), "The credit card should be saved");

        creditCard.SetBillingDueDay((short)15);
        creditCard.SetMaxDebt(2000.0);

        assertTrue(m_creditCardDAO.Update(creditCard),
                   "The credit card should be updated");

        CreditCard foundCreditCard = m_creditCardDAO.Find(creditCard.GetName());

        assertNotNull(foundCreditCard, "The credit card should be found");

        assertEquals(creditCard.GetName(),
                     foundCreditCard.GetName(),
                     "The credit card name should be the same");

        assertEquals(creditCard.GetBillingDueDay(),
                     foundCreditCard.GetBillingDueDay(),
                     "The credit card billing due day should be the same");

        assertEquals(creditCard.GetMaxDebt(),
                     foundCreditCard.GetMaxDebt(),
                     Constants.EPSILON,
                     "The credit card max debt should be the same");
    }

    @Test
    public void TestDeleteValidCreditCard()
    {
        CreditCard creditCard = new CreditCard("My Credit Card", (short)10, 1000.0);
        assertTrue(m_creditCardDAO.Save(creditCard), "The credit card should be saved");

        assertTrue(m_creditCardDAO.Delete(creditCard.GetName()),
                   "The credit card should be deleted");

        CreditCard foundCreditCard = m_creditCardDAO.Find(creditCard.GetName());
        assertNull(foundCreditCard, "The credit card should not be found");
    }

    @Test
    public void TestDeleteInvalidCreditCard()
    {
        CreditCard creditCard = new CreditCard("My Credit Card", (short)10, 1000.0);

        // The CreditCard is not saved in the database
        assertFalse(m_creditCardDAO.Delete(creditCard.GetName()),
                    "The credit card should not be deleted");

        CreditCard foundCreditCard = m_creditCardDAO.Find(creditCard.GetName());

        assertNull(foundCreditCard, "The credit card should not be found");
    }

    @Test
    public void TestGetAllCreditCard()
    {
        CreditCard creditCard1 = new CreditCard("Credit Card 1", (short)10, 1000.0);
        CreditCard creditCard2 = new CreditCard("Credit Card 2", (short)15, 2000.0);
        CreditCard creditCard3 = new CreditCard("Credit Card 3", (short)20, 3000.0);

        assertTrue(m_creditCardDAO.Save(creditCard1),
                   "The first credit card should be saved");

        assertTrue(m_creditCardDAO.Save(creditCard2),
                   "The second credit card should be saved");

        assertTrue(m_creditCardDAO.Save(creditCard3),
                   "The third credit card should be saved");

        assertEquals(m_creditCardDAO.GetAll().size(),
                     3,
                     "The number of credit cards should be 3");
    }
}
