/*
 * Filename: WalletTransactionServiceTest.java
 * Created on: October 16, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moinex.entities.Category;
import org.moinex.entities.Transfer;
import org.moinex.entities.Wallet;
import org.moinex.entities.WalletTransaction;
import org.moinex.repositories.CategoryRepository;
import org.moinex.repositories.TransferRepository;
import org.moinex.repositories.WalletRepository;
import org.moinex.repositories.WalletTransactionRepository;
import org.moinex.repositories.WalletTypeRepository;
import org.moinex.util.Constants;
import org.moinex.util.TransactionStatus;
import org.moinex.util.TransactionType;

@ExtendWith(MockitoExtension.class)
public class WalletTransactionServiceTest
{
    @Mock
    private WalletRepository m_walletRepository;

    @Mock
    private WalletTypeRepository m_walletTypeRepository;

    @Mock
    private TransferRepository m_transferRepository;

    @Mock
    private CategoryRepository m_categoryRepository;

    @Mock
    private WalletTransactionRepository m_walletTransactionRepository;

    @InjectMocks
    private WalletTransactionService m_walletTransactionService;

    private Wallet            m_wallet1;
    private Wallet            m_wallet2;
    private Transfer          m_transfer;
    private WalletTransaction m_wallet1IncomeTransaction;
    private WalletTransaction m_wallet1ExpenseTransaction;
    private Category          m_category;
    private LocalDateTime     m_date;
    private BigDecimal        m_incomeAmount;
    private BigDecimal        m_expenseAmount;
    private BigDecimal        m_transferAmount;
    private String            m_description = "";

    private Wallet CreateWallet(Long id, String name, BigDecimal balance)
    {
        Wallet wallet = new Wallet(id, name, balance);
        return wallet;
    }

    private Transfer CreateTransfer(Long          id,
                                    Wallet        sender,
                                    Wallet        receiver,
                                    LocalDateTime date,
                                    BigDecimal    amount,
                                    String        description)
    {
        Transfer transfer =
            new Transfer(id, sender, receiver, date, amount, description);
        return transfer;
    }

    private WalletTransaction CreateWalletTransaction(Wallet            wallet,
                                                      Category          category,
                                                      TransactionType   type,
                                                      TransactionStatus status,
                                                      LocalDateTime     date,
                                                      BigDecimal        amount,
                                                      String            description)
    {
        WalletTransaction walletTransaction = new WalletTransaction(wallet,
                                                                    category,
                                                                    type,
                                                                    status,
                                                                    date,
                                                                    amount,
                                                                    description);
        return walletTransaction;
    }

    @BeforeAll
    public static void SetUp()
    {
        MockitoAnnotations.openMocks(WalletTransactionServiceTest.class);
    }

    @BeforeEach
    public void BeforeEach()
    {
        m_incomeAmount   = new BigDecimal("500");
        m_expenseAmount  = new BigDecimal("200");
        m_transferAmount = new BigDecimal("125.5");

        m_date     = LocalDateTime.now();
        m_category = new Category("etc");

        m_wallet1 = CreateWallet(1L, "Wallet1", new BigDecimal("1000"));
        m_wallet2 = CreateWallet(2L, "Wallet2", new BigDecimal("2000"));

        m_transfer = CreateTransfer(1L,
                                    m_wallet1,
                                    m_wallet2,
                                    m_date,
                                    m_transferAmount,
                                    m_description);

        m_wallet1IncomeTransaction =
            CreateWalletTransaction(m_wallet1,
                                    m_category,
                                    TransactionType.INCOME,
                                    TransactionStatus.CONFIRMED,
                                    m_date,
                                    m_incomeAmount,
                                    m_description);

        m_wallet1ExpenseTransaction =
            CreateWalletTransaction(m_wallet1,
                                    m_category,
                                    TransactionType.EXPENSE,
                                    TransactionStatus.CONFIRMED,
                                    m_date,
                                    m_expenseAmount,
                                    m_description);
    }

    @Test
    @DisplayName("Test if the money transfer is successful")
    public void TestTransferMoneySuccess()
    {
        BigDecimal m_senderPreviousBalance   = m_wallet1.GetBalance();
        BigDecimal m_receiverPreviousBalance = m_wallet2.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.findById(m_wallet2.GetId()))
            .thenReturn(Optional.of(m_wallet2));

        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);
        when(m_walletRepository.save(m_wallet2)).thenReturn(m_wallet2);

        when(m_transferRepository.save(any(Transfer.class))).thenReturn(m_transfer);

        m_walletTransactionService.TransferMoney(m_wallet1.GetId(),
                                                 m_wallet2.GetId(),
                                                 m_transfer.GetDate(),
                                                 m_transfer.GetAmount(),
                                                 m_transfer.GetDescription());

        // Check if the sender and receiver balances were updated
        verify(m_walletRepository).findById(m_wallet1.GetId());
        verify(m_walletRepository).findById(m_wallet2.GetId());
        verify(m_walletRepository).save(m_wallet1);
        verify(m_walletRepository).save(m_wallet2);

        assertEquals(m_senderPreviousBalance.subtract(m_transferAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);

        assertEquals(m_receiverPreviousBalance.add(m_transferAmount).doubleValue(),
                     m_wallet2.GetBalance().doubleValue(),
                     Constants.EPSILON);

        // Check if the transfer was saved
        ArgumentCaptor<Transfer> transferCaptor =
            ArgumentCaptor.forClass(Transfer.class);

        verify(m_transferRepository).save(transferCaptor.capture());

        assertEquals(m_wallet1, transferCaptor.getValue().GetSenderWallet());
        assertEquals(m_wallet2, transferCaptor.getValue().GetReceiverWallet());
        assertEquals(m_transferAmount.doubleValue(),
                     transferCaptor.getValue().GetAmount().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the sender wallet does not exist")
    public void TestTransferMoneySenderDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletTransactionService.TransferMoney(m_wallet1.GetId(),
                                                                     m_wallet2.GetId(),
                                                                     m_date,
                                                                     m_transferAmount,
                                                                     m_description));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the receiver wallet does not exist")
    public void TestTransferMoneyReceiverDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletRepository.findById(m_wallet2.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletTransactionService.TransferMoney(m_wallet1.GetId(),
                                                                     m_wallet2.GetId(),
                                                                     m_date,
                                                                     m_transferAmount,
                                                                     m_description),
                     "Receiver wallet does not exist");

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName(
        "Test if exception is thrown when the sender and receiver wallets are the same")
    public void
    TestTransferMoneySameWallet()
    {
        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletTransactionService.TransferMoney(m_wallet1.GetId(),
                                                                     m_wallet1.GetId(),
                                                                     m_date,
                                                                     m_transferAmount,
                                                                     m_description));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the amount to transfer is less "
                 + "than or equal to zero")
    public void
    TestTransferMoneyAmountZero()
    {
        assertThrows(
            RuntimeException.class,
            ()
                -> m_walletTransactionService.TransferMoney(m_wallet1.GetId(),
                                                            m_wallet2.GetId(),
                                                            m_date,
                                                            new BigDecimal("0.0"),
                                                            m_description));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Test if the confirmed income is added successfully")
    public void TestAddConfirmedIncome()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(m_wallet1,
                                              m_category,
                                              TransactionType.INCOME,
                                              TransactionStatus.CONFIRMED,
                                              m_date,
                                              m_incomeAmount,
                                              m_description));

        m_walletTransactionService.AddIncome(m_wallet1.GetId(),
                                             m_category,
                                             m_date,
                                             m_incomeAmount,
                                             m_description,
                                             TransactionStatus.CONFIRMED);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(previousBalance.add(m_incomeAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending income is added successfully")
    public void TestAddPendingIncome()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(m_wallet1,
                                              m_category,
                                              TransactionType.INCOME,
                                              TransactionStatus.PENDING,
                                              m_date,
                                              m_incomeAmount,
                                              m_description));

        m_walletTransactionService.AddIncome(m_wallet1.GetId(),
                                             m_category,
                                             m_date,
                                             m_incomeAmount,
                                             m_description,
                                             TransactionStatus.PENDING);

        // Check if the wallet balance is the same
        verify(m_walletRepository, never()).save(any(Wallet.class));
        assertEquals(previousBalance.doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the wallet to receive the income "
                 + "does not exist")
    public void
    TestAddIncomeWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        // Check for confirmed income
        assertThrows(
            RuntimeException.class,
            ()
                -> m_walletTransactionService.AddIncome(m_wallet1.GetId(),
                                                        m_category,
                                                        m_date,
                                                        m_incomeAmount,
                                                        m_description,
                                                        TransactionStatus.CONFIRMED));

        // Check for pending income
        assertThrows(
            RuntimeException.class,
            ()
                -> m_walletTransactionService.AddIncome(m_wallet1.GetId(),
                                                        m_category,
                                                        m_date,
                                                        m_incomeAmount,
                                                        m_description,
                                                        TransactionStatus.PENDING));

        // Verify that the income was not added
        verify(m_walletTransactionRepository, never())
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if the confirmed expense is added successfully")
    public void TestAddConfirmedExpense()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(m_wallet1,
                                              m_category,
                                              TransactionType.EXPENSE,
                                              TransactionStatus.CONFIRMED,
                                              m_date,
                                              m_expenseAmount,
                                              m_description));

        m_walletTransactionService.AddExpense(m_wallet1.GetId(),
                                              m_category,
                                              m_date,
                                              m_expenseAmount,
                                              m_description,
                                              TransactionStatus.CONFIRMED);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(previousBalance.subtract(m_expenseAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending expense is added successfully")
    public void TestAddPendingExpense()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(m_wallet1,
                                              m_category,
                                              TransactionType.EXPENSE,
                                              TransactionStatus.PENDING,
                                              m_date,
                                              m_expenseAmount,
                                              m_description));

        m_walletTransactionService.AddExpense(m_wallet1.GetId(),
                                              m_category,
                                              m_date,
                                              m_expenseAmount,
                                              m_description,
                                              TransactionStatus.PENDING);

        // Check if the wallet balance is the same
        verify(m_walletRepository, never()).save(m_wallet1);
        assertEquals(previousBalance.doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the wallet to receive the expense "
                 + "does not exist")
    public void
    TestAddExpenseWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetId()))
            .thenReturn(Optional.empty());

        // Check for confirmed expense
        assertThrows(
            RuntimeException.class,
            ()
                -> m_walletTransactionService.AddExpense(m_wallet1.GetId(),
                                                         m_category,
                                                         m_date,
                                                         m_expenseAmount,
                                                         m_description,
                                                         TransactionStatus.CONFIRMED));

        // Check for pending expense
        assertThrows(
            RuntimeException.class,
            ()
                -> m_walletTransactionService.AddExpense(m_wallet1.GetId(),
                                                         m_category,
                                                         m_date,
                                                         m_expenseAmount,
                                                         m_description,
                                                         TransactionStatus.PENDING));

        // Verify that the expense was not added
        verify(m_walletTransactionRepository, never())
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if transaction type is changed from EXPENSE to INCOME and "
                 + "wallet balance is updated correctly")
    public void
    TestChangeTransactionTypeFromExpenseToIncome()
    {
        // Setup previous state
        BigDecimal oldBalance      = m_wallet1.GetBalance();
        BigDecimal expenseAmount   = m_wallet1ExpenseTransaction.GetAmount();
        BigDecimal newIncomeAmount = new BigDecimal("300.0");

        WalletTransaction updatedTransaction =
            CreateWalletTransaction(m_wallet1,
                                    m_category,
                                    TransactionType.INCOME,
                                    TransactionStatus.CONFIRMED,
                                    m_date,
                                    newIncomeAmount,
                                    m_description);

        when(
            m_walletTransactionRepository.findById(m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1ExpenseTransaction));

        when(m_walletTransactionRepository.FindWalletByTransactionId(
                 m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(updatedTransaction);

        m_walletTransactionService.UpdateTransaction(updatedTransaction);

        // Verify that the old expense was reverted and new income was applied
        assertEquals(oldBalance.add(expenseAmount).add(newIncomeAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);

        // Verify repository interactions
        verify(m_walletTransactionRepository)
            .findById(m_wallet1ExpenseTransaction.GetId());
        verify(m_walletRepository, times(2)).save(m_wallet1);
        verify(m_walletTransactionRepository, times(3))
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if transaction status is changed from CONFIRMED to PENDING "
                 + "and balance is reverted")
    public void
    TestChangeTransactionStatusFromConfirmedToPending()
    {
        BigDecimal        oldBalance = m_wallet1.GetBalance();
        WalletTransaction updatedTransaction =
            CreateWalletTransaction(m_wallet1,
                                    m_category,
                                    TransactionType.EXPENSE,
                                    TransactionStatus.PENDING,
                                    m_date,
                                    m_expenseAmount,
                                    m_description);

        when(
            m_walletTransactionRepository.findById(m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1ExpenseTransaction));

        when(m_walletTransactionRepository.FindWalletByTransactionId(
                 m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(updatedTransaction);

        m_walletTransactionService.UpdateTransaction(updatedTransaction);

        // Verify that the balance was reverted for the expense
        assertEquals(oldBalance.add(m_expenseAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);

        // Verify repository interactions
        verify(m_walletTransactionRepository)
            .findById(m_wallet1ExpenseTransaction.GetId());
        verify(m_walletRepository).save(m_wallet1);
        verify(m_walletTransactionRepository, times(2))
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName(
        "Test if the wallet is changed and transaction is applied to the new wallet")
    public void
    TestChangeTransactionWallet()
    {
        BigDecimal oldWallet1Balance = m_wallet1.GetBalance();
        BigDecimal oldWallet2Balance = m_wallet2.GetBalance();
        BigDecimal amount            = m_wallet1IncomeTransaction.GetAmount();

        WalletTransaction updatedTransaction =
            CreateWalletTransaction(m_wallet2,
                                    m_category,
                                    TransactionType.INCOME,
                                    TransactionStatus.CONFIRMED,
                                    m_date,
                                    amount,
                                    m_description);

        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1IncomeTransaction));

        when(m_walletTransactionRepository.FindWalletByTransactionId(
                 m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);

        when(m_walletRepository.save(m_wallet2)).thenReturn(m_wallet2);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(updatedTransaction);

        m_walletTransactionService.UpdateTransaction(updatedTransaction);

        // Verify that the amount was reverted from the old wallet and added to the new
        // wallet
        assertEquals(oldWallet1Balance.subtract(amount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);

        assertEquals(oldWallet2Balance.add(amount).doubleValue(),
                     m_wallet2.GetBalance().doubleValue(),
                     Constants.EPSILON);

        // Verify repository interactions
        verify(m_walletTransactionRepository)
            .findById(m_wallet1IncomeTransaction.GetId());
        verify(m_walletRepository).save(m_wallet1);
        verify(m_walletRepository).save(m_wallet2);
        verify(m_walletTransactionRepository, times(2))
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if an exception is thrown when transaction does not exist")
    public void TestUpdateNonExistentTransaction()
    {
        WalletTransaction nonExistentTransaction =
            CreateWalletTransaction(m_wallet1,
                                    m_category,
                                    TransactionType.INCOME,
                                    TransactionStatus.CONFIRMED,
                                    m_date,
                                    m_incomeAmount,
                                    m_description);

        when(m_walletTransactionRepository.findById(nonExistentTransaction.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            ()
                -> m_walletTransactionService.UpdateTransaction(nonExistentTransaction),
            "Transaction with id " + nonExistentTransaction.GetId() + " not found");

        verify(m_walletTransactionRepository, never())
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if the confirmed expense is deleted successfully")
    public void TestDeleteConfirmedExpense()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(
            m_walletTransactionRepository.findById(m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1ExpenseTransaction));

        m_walletTransactionService.DeleteTransaction(
            m_wallet1ExpenseTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1ExpenseTransaction);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);

        assertEquals(previousBalance.add(m_expenseAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending expense is deleted successfully")
    public void TestDeletePendingExpense()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();
        m_wallet1ExpenseTransaction.SetStatus(TransactionStatus.PENDING);

        when(
            m_walletTransactionRepository.findById(m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1ExpenseTransaction));

        m_walletTransactionService.DeleteTransaction(
            m_wallet1ExpenseTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1ExpenseTransaction);

        // Check if the wallet balance was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));

        assertEquals(previousBalance.doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the confirmed income transaction is deleted successfully")
    public void TestDeleteConfirmedIncome()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1IncomeTransaction));

        m_walletTransactionService.DeleteTransaction(
            m_wallet1IncomeTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1IncomeTransaction);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);

        assertEquals(previousBalance.subtract(m_incomeAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending income transaction is deleted successfully")
    public void TestDeletePendingIncome()
    {
        BigDecimal previousBalance = m_wallet1.GetBalance();
        m_wallet1IncomeTransaction.SetStatus(TransactionStatus.PENDING);

        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1IncomeTransaction));

        m_walletTransactionService.DeleteTransaction(
            m_wallet1IncomeTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1IncomeTransaction);

        // Check if the wallet balance was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));

        assertEquals(previousBalance.doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the transaction to delete does not "
                 + "exist")
    public void
    TestDeleteTransactionDoesNotExist()
    {
        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletTransactionService.DeleteTransaction(
                             m_wallet1IncomeTransaction.GetId()));
    }

    @Test
    @DisplayName("Test if the income transaction is confirmed successfully")
    public void TestConfirmIncomeTransaction()
    {
        m_wallet1IncomeTransaction.SetStatus(TransactionStatus.PENDING);
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1IncomeTransaction));

        m_walletTransactionService.ConfirmTransaction(
            m_wallet1IncomeTransaction.GetId());

        // Check if the transaction was confirmed
        verify(m_walletTransactionRepository).save(m_wallet1IncomeTransaction);
        assertEquals(TransactionStatus.CONFIRMED,
                     m_wallet1IncomeTransaction.GetStatus());

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(previousBalance.add(m_incomeAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the expense transaction is confirmed successfully")
    public void TestConfirmExpenseTransaction()
    {
        m_wallet1ExpenseTransaction.SetStatus(TransactionStatus.PENDING);
        BigDecimal previousBalance = m_wallet1.GetBalance();

        when(
            m_walletTransactionRepository.findById(m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1ExpenseTransaction));

        m_walletTransactionService.ConfirmTransaction(
            m_wallet1ExpenseTransaction.GetId());

        // Check if the transaction was confirmed
        verify(m_walletTransactionRepository).save(m_wallet1ExpenseTransaction);
        assertEquals(TransactionStatus.CONFIRMED,
                     m_wallet1ExpenseTransaction.GetStatus());

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(previousBalance.subtract(m_expenseAmount).doubleValue(),
                     m_wallet1.GetBalance().doubleValue(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the transaction to confirm does not "
                 + "exist")
    public void
    TestConfirmTransactionDoesNotExist()
    {
        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletTransactionService.ConfirmTransaction(
                             m_wallet1IncomeTransaction.GetId()));
    }

    @Test
    @DisplayName("Test if the transaction already confirmed is not confirmed again")
    public void TestConfirmTransactionAlreadyConfirmed()
    {
        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1IncomeTransaction));

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletTransactionService.ConfirmTransaction(
                             m_wallet1IncomeTransaction.GetId()));

        // Check if the transaction was not confirmed
        verify(m_walletTransactionRepository, never()).save(m_wallet1IncomeTransaction);

        // Check if the wallet balance was not updated
        verify(m_walletRepository, never()).save(m_wallet1);
    }
}
