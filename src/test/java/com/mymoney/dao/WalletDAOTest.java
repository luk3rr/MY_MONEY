/*
 * Filename: WalletDAOTest.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mymoney.app.Wallet;
import com.mymoney.util.Constants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for WalletDAO
 */
public class WalletDAOTest
{
    private static WalletDAO m_walletDAO;

    @BeforeAll
    public static void SetUp()
    {
        m_walletDAO = WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the test database is reset before each test
        if (!m_walletDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    {
        // Ensure that the test database is reset after all tests
        if (!m_walletDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @Test
    public void TestCreateAndFindWallet()
    {
        Wallet wallet = new Wallet("My Wallet", 100.0);

        assertTrue(m_walletDAO.Save(wallet), "The wallet should be saved");

        Wallet foundWallet = m_walletDAO.Find(wallet.GetName());
        assertNotNull(foundWallet, "The wallet should be found");
    }

    @Test
    public void CreateWalletWithSameName()
    {
        Wallet wallet1 = new Wallet("Wallet 3", 700.0);
        Wallet wallet2 = new Wallet("Wallet 3", 800.0);

        assertTrue(m_walletDAO.Save(wallet1), "The first wallet should be saved");
        assertFalse(m_walletDAO.Save(wallet2), "The second wallet should not be saved");
    }

    @Test
    public void TestUpdateWallet()
    {
        Wallet wallet = new Wallet("Update Wallet", 200.0);

        assertTrue(m_walletDAO.Save(wallet), "The wallet should be saved");

        wallet.SetBalance(300.0);

        assertTrue(m_walletDAO.Update(wallet), "The wallet should be updated");

        Wallet updatedWallet = m_walletDAO.Find(wallet.GetName());

        assertEquals(updatedWallet.GetBalance(),
                     300.0,
                     "The wallet balance should be updated");
    }

    @Test
    public void TestDeleteValidWallet()
    {
        Wallet wallet = new Wallet("Delete Wallet", 400.0);

        assertTrue(m_walletDAO.Save(wallet), "The wallet should be saved");
        assertTrue(m_walletDAO.Delete(wallet.GetName()),
                   "The wallet should be deleted");

        Wallet deletedWallet = m_walletDAO.Find(wallet.GetName());
        assertNull(deletedWallet, "The wallet should be deleted");
    }

    @Test
    public void TestDeleteInvalidWallet()
    {
        Wallet invalidWallet = new Wallet("Invalid Wallet", 500.0);

        // The wallet is not saved in the database
        assertFalse(m_walletDAO.Delete(invalidWallet.GetName()),
                    "The wallet should not be deleted");

        Wallet deletedWallet = m_walletDAO.Find(invalidWallet.GetName());

        assertNull(deletedWallet, "The wallet should not be deleted");
    }

    @Test
    public void TestGetAllWallets()
    {
        Wallet wallet1 = new Wallet("Wallet 1", 500.0);
        Wallet wallet2 = new Wallet("Wallet 2", 600.0);

        assertTrue(m_walletDAO.Save(wallet1), "The first wallet should be saved");
        assertTrue(m_walletDAO.Save(wallet2), "The second wallet should be saved");

        assertEquals(m_walletDAO.GetAll().size(), 2, "There should be two wallets");
    }
}
