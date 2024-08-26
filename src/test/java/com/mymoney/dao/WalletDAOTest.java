/*
 * Filename: WalletDAOTest.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.mymoney.app.Wallet;

public class WalletDAOTest
{

    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager        entityManager;
    private static WalletDAO            walletDAO;

    @BeforeAll
    public static void setUp()
    {
        entityManagerFactory = Persistence.createEntityManagerFactory("my_money");
        entityManager        = entityManagerFactory.createEntityManager();
        walletDAO            = new WalletDAO(entityManager);
    }

    @Test
    public void testSaveAndFindWallet()
    {
        Wallet wallet = new Wallet("My Wallet", 100.0);
        walletDAO.save(wallet);
        Wallet foundWallet = walletDAO.find(wallet.GetName());
        assertNotNull(foundWallet, "The wallet should be found");
    }

    @Test
    public void testUpdateWallet()
    {
        Wallet wallet = new Wallet("Update Wallet", 200.0);
        walletDAO.save(wallet);
        wallet.SetBalance(300.0);
        walletDAO.update(wallet);
        Wallet updatedWallet = walletDAO.find(wallet.GetName());
        assertNotNull(updatedWallet, "The wallet should be updated");
    }

    @Test
    public void testDeleteWallet()
    {
        Wallet wallet = new Wallet("Delete Wallet", 400.0);
        walletDAO.save(wallet);
        walletDAO.delete(wallet);
        Wallet deletedWallet = walletDAO.find(wallet.GetName());
        assertNull(deletedWallet, "The wallet should be deleted");
    }

    @AfterAll
    public static void tearDown()
    {
        if (entityManager != null)
        {
            entityManager.close();
        }
        if (entityManagerFactory != null)
        {
            entityManagerFactory.close();
        }
    }
}
