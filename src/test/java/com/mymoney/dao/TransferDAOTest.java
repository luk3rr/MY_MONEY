/*
 * Filename: TransferDAOTest.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mymoney.app.entities.Transfer;
import com.mymoney.app.entities.Wallet;
import com.mymoney.util.Constants;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test class for TransferDAO
 */
public class TransferDAOTest
{
    private static TransferDAO m_transferDAO;
    private static Wallet      m_wallet1;
    private static Wallet      m_wallet2;
    private static Wallet      m_walletToUpdate;
    private static LocalDate   m_date;

    @BeforeAll
    public static void SetUp()
    {
        m_transferDAO = TransferDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        // Create three Wallets and a LocalDate to be used in the tests
        m_wallet1        = new Wallet("wallet1", 10.0);
        m_wallet2        = new Wallet("wallet2", 25.0);
        m_walletToUpdate = new Wallet("walletToUpdate", 52.0);

        WalletDAO walletDAO = WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST);

        assertTrue(walletDAO.Save(m_wallet1), "The wallet1 should be saved");
        assertTrue(walletDAO.Save(m_wallet2), "The wallet2 should be saved");
        assertTrue(walletDAO.Save(m_walletToUpdate),
                   "The walletToUpdate should be saved");

        m_date = LocalDate.of(2000, 1, 1);
    }

    @BeforeEach
    public void ResetDatabase()
    {
        // Ensure that the database is clean before each test
        if (!m_transferDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }
    }

    @AfterAll
    public static void TearDown()
    {
        // Ensure that the database is clean before each test
        if (!m_transferDAO.ResetTable())
        {
            throw new RuntimeException("Error resetting the test database");
        }

        // Also reset the another tables used in the tests
        // NOTE: This is necessary because the test database is shared among all tests
        if (!WalletDAO.GetInstance(Constants.ENTITY_MANAGER_TEST).ResetTable())
        {
            throw new RuntimeException("Error resetting the Wallet after all tests");
        }
    }

    @Test
    public void TestCreateAndFindTransfer()
    {
        Transfer transfer =
            new Transfer(m_wallet1, m_wallet2, m_date, 10.0, "Test transfer");

        assertTrue(m_transferDAO.Save(transfer), "The transfer should be saved");

        Transfer foundTransfer = m_transferDAO.Find(transfer.GetId());

        assertNotNull(foundTransfer, "The transfer should be found");
    }

    @Test
    public void TestUpdateTransfer()
    {
        Transfer transfer =
            new Transfer(m_wallet1, m_wallet2, m_date, 10.0, "Test transfer");

        assertTrue(m_transferDAO.Save(transfer), "The transfer should be saved");

        transfer.SetReceiverWallet(m_walletToUpdate);
        transfer.SetSenderWallet(m_wallet2);
        transfer.SetAmount(23.0);
        transfer.SetDescription("Updated transfer");

        assertTrue(m_transferDAO.Update(transfer), "The transfer should be updated");

        Transfer foundTransfer = m_transferDAO.Find(transfer.GetId());

        assertEquals(m_wallet2,
                     foundTransfer.GetSenderWallet(),
                     "The transfer sender wallet should be updated");

        assertEquals(m_walletToUpdate,
                     foundTransfer.GetReceiverWallet(),
                     "The transfer receiver wallet should be updated");

        assertEquals(23.0,
                     foundTransfer.GetAmount(),
                     Constants.EPSILON,
                     "The transfer amount should be updated");

        assertEquals("Updated transfer",
                     foundTransfer.GetDescription(),
                     "The transfer description should be updated");
    }

    @Test
    public void TestDeleteTransfer()
    {
        Transfer transfer =
            new Transfer(m_wallet1, m_wallet2, m_date, 10.0, "Test transfer");

        assertTrue(m_transferDAO.Save(transfer), "The transfer should be saved");

        assertTrue(m_transferDAO.Delete(transfer.GetId()), "The transfer should be deleted");

        Transfer foundTransfer = m_transferDAO.Find(transfer.GetId());

        assertNull(foundTransfer, "The transfer should not be found");
    }

    @Test
    public void TestDeleteInvalidTransfer()
    {
        Transfer transfer =
            new Transfer(m_wallet1, m_wallet2, m_date, 10.0, "Test transfer");

        // Save to get an id
        assertTrue(m_transferDAO.Save(transfer), "The transfer should be saved");

        // Delete the transfer
        assertTrue(m_transferDAO.Delete(transfer.GetId()), "The transfer should be deleted");

        // Try to delete the transfer again
        assertFalse(m_transferDAO.Delete(transfer.GetId()), "The transfer should not be deleted");

        Transfer foundTransfer = m_transferDAO.Find(transfer.GetId());

        assertNull(foundTransfer, "The transfer should not be found");
    }

    @Test
    public void TestGetAllTransfers()
    {
        Transfer transfer1 =
            new Transfer(m_wallet1, m_wallet2, m_date, 10.0, "Test transfer 1");

        Transfer transfer2 =
            new Transfer(m_wallet1, m_wallet2, m_date, 20.0, "Test transfer 2");

        Transfer transfer3 =
            new Transfer(m_wallet1, m_wallet2, m_date, 30.0, "Test transfer 3");

        assertTrue(m_transferDAO.Save(transfer1), "The transfer1 should be saved");
        assertTrue(m_transferDAO.Save(transfer2), "The transfer2 should be saved");
        assertTrue(m_transferDAO.Save(transfer3), "The transfer3 should be saved");

        assertEquals(3, m_transferDAO.GetAll().size(), "The number of transfers should be 3");
    }
}
