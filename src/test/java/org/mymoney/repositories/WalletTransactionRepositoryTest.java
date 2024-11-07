/*
 * Filename: WalletTransactionRepositoryTest.java
 * Created on: September 14, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mymoney.app.MainApplication;
import org.mymoney.entities.Category;
import org.mymoney.entities.Wallet;
import org.mymoney.entities.WalletTransaction;
import org.mymoney.util.TransactionStatus;
import org.mymoney.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests for the WalletTransactionRepository
 */
@DataJpaTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { MainApplication.class })
@ActiveProfiles("test")
public class WalletTransactionRepositoryTest
{
    @Autowired
    private WalletTransactionRepository m_walletTransactionRepository;

    @Autowired
    private WalletRepository m_walletRepository;

    @Autowired
    private CategoryRepository m_categoryRepository;

    private Wallet m_wallet1;

    private Wallet m_wallet2;

    private Category CreateCategory(String name)
    {
        Category category = new Category(name);
        m_categoryRepository.save(category);
        m_categoryRepository.flush();
        return category;
    }

    private WalletTransaction
    CreateWalletTransaction(Wallet walletName, BigDecimal amount, LocalDateTime date)
    {
        WalletTransaction walletTransaction = new WalletTransaction();
        walletTransaction.SetWallet(walletName);
        walletTransaction.SetAmount(amount);
        walletTransaction.SetDate(date);
        walletTransaction.SetStatus(TransactionStatus.CONFIRMED);
        walletTransaction.SetType(TransactionType.EXPENSE);
        walletTransaction.SetCategory(CreateCategory("Category"));
        m_walletTransactionRepository.save(walletTransaction);
        m_walletTransactionRepository.flush();
        return walletTransaction;
    }

    private Wallet CreateWallet(String name, BigDecimal balance)
    {
        Wallet wallet = new Wallet();
        wallet.SetName(name);
        wallet.SetBalance(balance);
        m_walletRepository.save(wallet);
        m_walletRepository.flush();
        return wallet;
    }

    @BeforeEach
    public void SetUp()
    {
        m_wallet1 = CreateWallet("Wallet1", new BigDecimal("1000.0"));
        m_wallet2 = CreateWallet("Wallet2", new BigDecimal("2000.0"));
    }

    @Test
    @DisplayName("Test if the last n transactions in a wallet are returned correctly")
    public void TestGetLastTransactions()
    {
        // Create the wallet transactions
        WalletTransaction walletTransaction1 =
            CreateWalletTransaction(m_wallet1,
                                    new BigDecimal("140.0"),
                                    LocalDateTime.now());
        WalletTransaction walletTransaction2 =
            CreateWalletTransaction(m_wallet1,
                                    new BigDecimal("210.0"),
                                    LocalDateTime.now().minusDays(1));

        WalletTransaction walletTransaction3 =
            CreateWalletTransaction(m_wallet1,
                                    new BigDecimal("300.0"),
                                    LocalDateTime.now().minusDays(2));

        CreateWalletTransaction(m_wallet1,
                                new BigDecimal("300.0"),
                                LocalDateTime.now().minusDays(3));

        // Request the last 3 transactions
        Pageable request = PageRequest.ofSize(3);

        // Get the last transactions in the wallet by date
        List<WalletTransaction> lastTransactions =
            m_walletTransactionRepository.FindLastTransactionsByWallet(
                m_wallet1.GetId(),
                request);

        // Check if the last transactions are correct
        assertEquals(3, lastTransactions.size());

        // Check if the last transactions are in the correct order
        assertEquals(walletTransaction1, lastTransactions.get(0));
        assertEquals(walletTransaction2, lastTransactions.get(1));
        assertEquals(walletTransaction3, lastTransactions.get(2));
    }

    @Test
    @DisplayName("Test if the last n transactions in a wallet are returned correctly "
                 + "when there are no transactions")
    public void
    TestGetLastTransactionsNoTransactions()
    {
        // Request the last 3 transactions
        Pageable request = PageRequest.ofSize(3);

        // Get the last transactions in the wallet by date
        List<WalletTransaction> lastTransactions =
            m_walletTransactionRepository.FindLastTransactionsByWallet(
                m_wallet1.GetId(),
                request);

        // Check if the last transactions are correct
        assertEquals(0, lastTransactions.size());
    }

    @Test
    @DisplayName(
        "Test if the last n transactions of all wallets are returned correctly")
    public void
    TestGetLastTransactionsAllWallets()
    {
        // Create the wallet transactions
        WalletTransaction walletTransaction1 =
            CreateWalletTransaction(m_wallet1,
                                    new BigDecimal("140.0"),
                                    LocalDateTime.now());
        WalletTransaction walletTransaction2 =
            CreateWalletTransaction(m_wallet1,
                                    new BigDecimal("210.0"),
                                    LocalDateTime.now().minusDays(1));

        WalletTransaction walletTransaction3 =
            CreateWalletTransaction(m_wallet2,
                                    new BigDecimal("300.0"),
                                    LocalDateTime.now().minusDays(2));

        CreateWalletTransaction(m_wallet2,
                                new BigDecimal("300.0"),
                                LocalDateTime.now().minusDays(3));

        // Request the last 3 transactions
        Pageable request = PageRequest.ofSize(3);

        // Get the last transactions in the wallet by date
        List<WalletTransaction> lastTransactions =
            m_walletTransactionRepository.FindLastTransactions(request);

        // Check if the last transactions are correct
        assertEquals(3, lastTransactions.size());

        // Check if the last transactions are in the correct order
        assertEquals(walletTransaction1, lastTransactions.get(0));
        assertEquals(walletTransaction2, lastTransactions.get(1));
        assertEquals(walletTransaction3, lastTransactions.get(2));
    }
}
