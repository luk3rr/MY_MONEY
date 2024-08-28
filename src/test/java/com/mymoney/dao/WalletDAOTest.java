/*
 * Filename: WalletDAOTest.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import com.mymoney.app.Wallet;
import com.mymoney.util.Constants;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

    @AfterAll
    public static void TearDown()
    { }

    @Test
    public void TestSaveAndFindWallet()
    {
        Wallet wallet = new Wallet("My Wallet", 100.0);
        walletDAO.Save(wallet);
        Wallet foundWallet = walletDAO.Find(wallet.GetName());
        assertNotNull(foundWallet, "The wallet should be found");
    }

    @Test
    public void TestUpdateWallet()
    {
        Wallet wallet = new Wallet("Update Wallet", 200.0);
        walletDAO.Save(wallet);
        wallet.SetBalance(300.0);
        walletDAO.Update(wallet);
        Wallet updatedWallet = walletDAO.Find(wallet.GetName());
        assertNotNull(updatedWallet, "The wallet should be updated");
    }

    @Test
    public void TestDeleteWallet()
    {
        Wallet wallet = new Wallet("Delete Wallet", 400.0);
        walletDAO.Save(wallet);
        walletDAO.Delete(wallet);
        Wallet deletedWallet = walletDAO.Find(wallet.GetName());
        assertNull(deletedWallet, "The wallet should be deleted");
    }
}
