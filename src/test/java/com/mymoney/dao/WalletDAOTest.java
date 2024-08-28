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
import java.io.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for WalletDAO
 */
public class WalletDAOTest
{
    private static WalletDAO walletDAO;

    @BeforeAll
    public static void SetUp()
    {
        // Delete the test database file if it exists before running the tests
        File dbFile = new File(Constants.DB_TEST_FILE);
        if (dbFile.exists())
        {
            dbFile.delete();
        }

        walletDAO = WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the test database is reset before each test
        if (!walletDAO.ResetTestDatabase())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    { }

    @Test
    public void TestCreateAndFindWallet()
    {
        Wallet wallet = new Wallet("My Wallet", 100.0);

        walletDAO.Save(wallet);

        Wallet foundWallet = walletDAO.Find(wallet.GetName());
        assertNotNull(foundWallet, "The wallet should be found");
    }

    @Test
    public void CreateWalletWithSameName()
    {
        Wallet wallet1 = new Wallet("Wallet 3", 700.0);
        Wallet wallet2 = new Wallet("Wallet 3", 800.0);

        assertTrue(walletDAO.Save(wallet1), "The first wallet should be saved");
        assertFalse(walletDAO.Save(wallet2), "The second wallet should not be saved");
    }

    @Test
    public void TestUpdateWallet()
    {
        Wallet wallet = new Wallet("Update Wallet", 200.0);

        walletDAO.Save(wallet);

        wallet.SetBalance(300.0);
        walletDAO.Update(wallet);

        Wallet updatedWallet = walletDAO.Find(wallet.GetName());

        assertEquals(updatedWallet.GetBalance(),
                     300.0,
                     "The wallet balance should be updated");
    }

    @Test
    public void TestDeleteValidWallet()
    {
        Wallet wallet = new Wallet("Delete Wallet", 400.0);

        walletDAO.Save(wallet);
        walletDAO.Delete(wallet);

        Wallet deletedWallet = walletDAO.Find(wallet.GetName());
        assertNull(deletedWallet, "The wallet should be deleted");
    }

    @Test
    public void TestDeleteInvalidWallet()
    {
        Wallet invalidWallet = new Wallet("Invalid Wallet", 500.0);
        // The wallet is not saved in the database
        walletDAO.Delete(invalidWallet);

        Wallet deletedWallet = walletDAO.Find(invalidWallet.GetName());

        assertNull(deletedWallet, "The wallet should not be deleted");
    }

    @Test
    public void TestFindAllWallets()
    {
        Wallet wallet1 = new Wallet("Wallet 1", 500.0);
        Wallet wallet2 = new Wallet("Wallet 2", 600.0);

        walletDAO.Save(wallet1);
        walletDAO.Save(wallet2);

        assertEquals(walletDAO.GetAll().size(), 2, "There should be two wallets");
    }
}
