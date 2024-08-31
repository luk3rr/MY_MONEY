/*
 * Filename: CreditCardPaymentDAOTest.java
 * Created on: August 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mymoney.app.entities.Category;
import com.mymoney.app.entities.CreditCard;
import com.mymoney.app.entities.CreditCardDebt;
import com.mymoney.app.entities.CreditCardPayment;
import com.mymoney.app.entities.Wallet;
import com.mymoney.util.Constants;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for CreditCardPaymentDAO
 */
public class CreditCardPaymentDAOTest
{
    private static CreditCardPaymentDAO m_creditCardPaymentDAO;
    private static CreditCard           m_creditCard;
    private static CreditCardDebt       m_creditCardDebt;
    private static Category             m_category;
    private static LocalDate            m_date;
    private static Wallet               m_wallet;

    @BeforeAll
    public static void SetUp()
    {
        m_creditCardPaymentDAO =
            CreditCardPaymentDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        // Create a CreditCard, Category, CreditCardDebt, LocalDate and Wallet to be
        // used in the tests
        m_creditCard = new CreditCard("TestCard", (short)1, 20.0);
        CreditCardDAO creditCardDAO =
            CreditCardDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        assertTrue(creditCardDAO.Save(m_creditCard), "The credit card should be saved");

        m_category = new Category("TestCategory");
        CategoryDAO categoryDAO =
            CategoryDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        assertTrue(categoryDAO.Save(m_category), "The category should be saved");

        m_date = LocalDate.of(2000, 1, 1);

        m_creditCardDebt =
            new CreditCardDebt(m_creditCard, m_category, m_date, 10.0, "TestDebt");
        CreditCardDebtDAO creditCardDebtDAO =
            CreditCardDebtDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        assertTrue(creditCardDebtDAO.Save(m_creditCardDebt),
                   "The credit card debt should be saved");

        m_wallet            = new Wallet("TestWallet", 100.0);
        WalletDAO walletDAO = WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        assertTrue(walletDAO.Save(m_wallet), "The wallet should be saved");
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the test database is reset before each test
        if (!m_creditCardPaymentDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    {
        // Ensure that the test database is reset after all tests
        if (!m_creditCardPaymentDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }

        // Also reset the another tables used in the tests
        // NOTE: This is necessary because the test database is shared among all tests
        if (!WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST).ResetTable())
        {
            throw new RuntimeException("Error resetting table Wallet after all tests");
        }

        if (!CreditCardDebtDAO.GetInstance(Constants.ENTITY_MANAGER_TEST).ResetTable())
        {
            throw new RuntimeException(
                "Error resetting table CreditCardDebt after all tests");
        }

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
    public void TestCreateAndFindCreditCardPayment()
    {
        CreditCardPayment creditCardPayment =
            new CreditCardPayment(m_wallet, m_creditCardDebt, m_date, 10.0, (short)1);

        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment),
                   "The credit card payment should be saved");

        CreditCardPayment foundCreditCardPayment =
            m_creditCardPaymentDAO.Find(creditCardPayment.GetId());

        assertNotNull(foundCreditCardPayment,
                      "The credit card payment should be found");
    }

    @Test
    public void TestUpdateCreditCardPayment()
    {
        CreditCardPayment creditCardPayment =
            new CreditCardPayment(m_wallet, m_creditCardDebt, m_date, 10.0, (short)1);

        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment),
                   "The credit card payment should be saved");

        creditCardPayment.SetAmount(20.0);
        creditCardPayment.SetInstallment((short)2);

        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment),
                   "The credit card payment should be updated");

        CreditCardPayment foundCreditCardPayment =
            m_creditCardPaymentDAO.Find(creditCardPayment.GetId());

        assertNotNull(foundCreditCardPayment,
                      "The credit card payment should be found");

        assertEquals(creditCardPayment.GetId(),
                     foundCreditCardPayment.GetId(),
                     "The credit card payment id should be the same");

        assertEquals(creditCardPayment.GetAmount(),
                     foundCreditCardPayment.GetAmount(),
                     "The credit card payment amount should be the same");

        assertEquals(creditCardPayment.GetInstallment(),
                     foundCreditCardPayment.GetInstallment(),
                     "The credit card payment installment should be the same");
    }

    @Test
    public void TestDeleteValidCreditCardPayment()
    {
        CreditCardPayment creditCardPayment =
            new CreditCardPayment(m_wallet, m_creditCardDebt, m_date, 10.0, (short)1);

        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment),
                   "The credit card payment should be saved");

        assertTrue(m_creditCardPaymentDAO.Delete(creditCardPayment.GetId()),
                   "The credit card payment should be deleted");

        CreditCardPayment foundCreditCardPayment =
            m_creditCardPaymentDAO.Find(creditCardPayment.GetId());

        assertNull(foundCreditCardPayment,
                   "The credit card payment should not be found");
    }

    @Test
    public void TestDeleteInvalidCreditCardPayment()
    {
        CreditCardPayment creditCardPayment =
            new CreditCardPayment(m_wallet, m_creditCardDebt, m_date, 10.0, (short)1);

        // Save to get an id
        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment),
                   "The credit card payment should be saved");

        // Delete the credit card payment
        assertTrue(m_creditCardPaymentDAO.Delete(creditCardPayment.GetId()),
                   "The credit card payment should be deleted");

        // Try to delete the credit card payment again
        assertFalse(m_creditCardPaymentDAO.Delete(creditCardPayment.GetId()),
                    "The credit card payment should not be deleted");

        CreditCardPayment foundCreditCardPayment =
            m_creditCardPaymentDAO.Find(creditCardPayment.GetId());

        assertNull(foundCreditCardPayment,
                   "The credit card payment should not be found");
    }

    @Test
    public void TestGetAllCreditCardPayments()
    {
        CreditCardPayment creditCardPayment1 =
            new CreditCardPayment(m_wallet, m_creditCardDebt, m_date, 10.0, (short)1);

        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment1),
                   "The first credit card payment should be saved");

        CreditCardPayment creditCardPayment2 =
            new CreditCardPayment(m_wallet, m_creditCardDebt, m_date, 20.0, (short)2);

        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment2),
                   "The second credit card payment should be saved");

        CreditCardPayment creditCardPayment3 =
            new CreditCardPayment(m_wallet, m_creditCardDebt, m_date, 30.0, (short)3);

        assertTrue(m_creditCardPaymentDAO.Save(creditCardPayment3),
                   "The third credit card payment should be saved");

        assertEquals(3,
                     m_creditCardPaymentDAO.GetAll().size(),
                     "The number of credit card payments should be 3");
    }
}
