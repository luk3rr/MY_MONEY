/*
 * Filename: WalletTransactionDAOTest.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mymoney.app.Category;
import com.mymoney.app.Wallet;
import com.mymoney.app.WalletTransaction;
import com.mymoney.util.Constants;
import com.mymoney.util.TransactionType;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for WalletTransactionDAO
 */
public class WalletTransactionDAOTest
{
    private static WalletTransactionDAO m_walletTransactionDAO;
    private static Wallet               m_wallet;
    private static Category             m_category;
    private static LocalDate            m_date;

    @BeforeAll
    public static void SetUp()
    {
        m_walletTransactionDAO =
            WalletTransactionDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        // Create a Wallet, Category and LocalDate to be used in the tests
        m_wallet            = new Wallet("TestWallet", 10.0);
        WalletDAO walletDAO = WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        assertTrue(walletDAO.Save(m_wallet), "The wallet should be saved");

        m_category = new Category("TestCategory");
        CategoryDAO categoryDAO =
            CategoryDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        assertTrue(categoryDAO.Save(m_category), "The category should be saved");

        m_date = LocalDate.of(2000, 1, 1);
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the database is clean before each test
        if (!m_walletTransactionDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    {
        // Ensure that the database is clean before each test
        if (!m_walletTransactionDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }

        // Also reset the another tables used in the tests
        // NOTE: This is necessary because the test database is shared among all tests
        if (!WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST).ResetTable())
        {
            throw new RuntimeException("Error resetting the Wallet after all tests");
        }

        if (!CategoryDAO.GetInstance(Constants.ENTITY_MANAGER_TEST).ResetTable())
        {
            throw new RuntimeException("Error resetting the Category after all tests");
        }
    }

    @Test
    public void TestCreateAndFindWalletTransaction()
    {
        WalletTransaction walletTransaction =
            new WalletTransaction(m_wallet,
                                  m_category,
                                  TransactionType.INCOME,
                                  m_date,
                                  100.0,
                                  "description");

        assertTrue(m_walletTransactionDAO.Save(walletTransaction),
                   "The wallet transaction should be saved");

        WalletTransaction foundWalletTransaction =
            m_walletTransactionDAO.Find(walletTransaction.GetId());

        assertNotNull(foundWalletTransaction, "The wallet transaction should be found");
    }

    @Test
    public void TestUpdateWalletTransaction()
    {
        WalletTransaction walletTransaction =
            new WalletTransaction(m_wallet,
                                  m_category,
                                  TransactionType.INCOME,
                                  m_date,
                                  100.0,
                                  "description");

        assertTrue(m_walletTransactionDAO.Save(walletTransaction),
                   "The wallet transaction should be saved");

        // Update the WalletTransaction
        walletTransaction.SetAmount(200.0);
        walletTransaction.SetDescription("new description");
        walletTransaction.SetType(TransactionType.OUTCOME);

        assertTrue(m_walletTransactionDAO.Update(walletTransaction),
                   "The wallet transaction should be updated");

        WalletTransaction foundWalletTransaction =
            m_walletTransactionDAO.Find(walletTransaction.GetId());

        assertEquals(200.0,
                     foundWalletTransaction.GetAmount(),
                     "The wallet transaction amount should be updated");

        assertEquals("new description",
                     foundWalletTransaction.GetDescription(),
                     "The wallet transaction description should be updated");

        assertEquals(TransactionType.OUTCOME,
                     foundWalletTransaction.GetType(),
                     "The wallet transaction type should be updated");
    }

    @Test
    public void TestDeleteWalletTransaction()
    {
        WalletTransaction walletTransaction =
            new WalletTransaction(m_wallet,
                                  m_category,
                                  TransactionType.INCOME,
                                  m_date,
                                  100.0,
                                  "description");

        assertTrue(m_walletTransactionDAO.Save(walletTransaction),
                   "The wallet transaction should be saved");

        assertTrue(m_walletTransactionDAO.Delete(walletTransaction.GetId()),
                   "The wallet transaction should be deleted");

        WalletTransaction foundWalletTransaction =
            m_walletTransactionDAO.Find(walletTransaction.GetId());

        assertNull(foundWalletTransaction,
                   "The wallet transaction should not be found");
    }

    @Test
    public void TestDeleteInvalidWalletTransaction()
    {
        WalletTransaction walletTransaction =
            new WalletTransaction(m_wallet,
                                  m_category,
                                  TransactionType.INCOME,
                                  m_date,
                                  100.0,
                                  "description");

        // Save to get an id
        assertTrue(m_walletTransactionDAO.Save(walletTransaction),
                   "The wallet transaction should be saved");

        // Delete the WalletTransaction
        assertTrue(m_walletTransactionDAO.Delete(walletTransaction.GetId()),
                   "The wallet transaction should be deleted");

        // Try to delete the WalletTransaction again
        assertFalse(m_walletTransactionDAO.Delete(walletTransaction.GetId()),
                    "The wallet transaction should not be deleted");

        WalletTransaction foundWalletTransaction =
            m_walletTransactionDAO.Find(walletTransaction.GetId());

        assertNull(foundWalletTransaction,
                   "The wallet transaction should not be found");
    }

    @Test
    public void TestGetAllWalletTransactions()
    {
        WalletTransaction walletTransaction1 =
            new WalletTransaction(m_wallet,
                                  m_category,
                                  TransactionType.INCOME,
                                  m_date,
                                  100.0,
                                  "description1");

        WalletTransaction walletTransaction2 =
            new WalletTransaction(m_wallet,
                                  m_category,
                                  TransactionType.OUTCOME,
                                  m_date,
                                  200.0,
                                  "description2");

        assertTrue(m_walletTransactionDAO.Save(walletTransaction1),
                   "The wallet transaction 1 should be saved");

        assertTrue(m_walletTransactionDAO.Save(walletTransaction2),
                   "The wallet transaction 2 should be saved");

        assertEquals(2,
                     m_walletTransactionDAO.GetAll().size(),
                     "The number of wallet transactions should be 2");
    }
}
