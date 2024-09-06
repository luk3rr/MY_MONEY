/*
 * Filename: WalletServiceTest.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mymoney.entities.Category;
import com.mymoney.entities.Transfer;
import com.mymoney.entities.Wallet;
import com.mymoney.entities.WalletTransaction;
import com.mymoney.repositories.CategoryRepository;
import com.mymoney.repositories.TransferRepository;
import com.mymoney.repositories.WalletRepository;
import com.mymoney.repositories.WalletTransactionRepository;
import com.mymoney.util.Constants;
import com.mymoney.util.TransactionStatus;
import com.mymoney.util.TransactionType;
import java.time.LocalDate;
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

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest
{
    @Mock
    private WalletRepository m_walletRepository;

    @Mock
    private TransferRepository m_transferRepository;

    @Mock
    private CategoryRepository m_categoryRepository;

    @Mock
    private WalletTransactionRepository m_walletTransactionRepository;

    @InjectMocks
    private WalletService m_walletService;

    private Wallet            m_wallet1;
    private Wallet            m_wallet2;
    private Transfer          m_transfer12;
    private WalletTransaction m_wallet1IncomeTransaction;
    private WalletTransaction m_wallet1ExpenseTransaction;
    private Category          m_category;
    private LocalDate         m_date;
    private Double            m_incomeAmount;
    private Double            m_expenseAmount;
    private Double            m_transferAmount;
    private String            m_description = "";

    private Wallet CreateWallet(String name, double balance)
    {
        Wallet wallet = new Wallet(name, balance);
        return wallet;
    }

    private Transfer CreateTransfer(Wallet    sender,
                                    Wallet    receiver,
                                    LocalDate date,
                                    double    amount,
                                    String    description)
    {
        Transfer transfer = new Transfer(sender, receiver, date, amount, description);
        return transfer;
    }

    private WalletTransaction CreateWalletTransaction(Wallet            wallet,
                                                      Category          category,
                                                      TransactionType   type,
                                                      TransactionStatus status,
                                                      LocalDate         date,
                                                      double            amount,
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
        MockitoAnnotations.openMocks(WalletServiceTest.class);
    }

    @BeforeEach
    public void BeforeEach()
    {
        m_incomeAmount   = 500.0;
        m_expenseAmount  = 200.0;
        m_transferAmount = 125.5;
        m_date           = LocalDate.now();
        m_category       = new Category("etc");

        m_wallet1 = CreateWallet("Wallet1", 1000.0);
        m_wallet2 = CreateWallet("Wallet2", 2000.0);

        m_transfer12 = CreateTransfer(m_wallet1,
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
                                    TransactionType.OUTCOME,
                                    TransactionStatus.CONFIRMED,
                                    m_date,
                                    m_expenseAmount,
                                    m_description);
    }

    @Test
    @DisplayName("Test if the wallet is created successfully")
    public void TestCreateWallet()
    {
        m_walletService.CreateWallet(m_wallet1.GetName(), m_wallet1.GetBalance());

        // Capture the wallet object that was saved and check if the values are correct
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

        verify(m_walletRepository).save(walletCaptor.capture());

        assertEquals(m_wallet1.GetName(), walletCaptor.getValue().GetName());

        assertEquals(m_wallet1.GetBalance(),
                     walletCaptor.getValue().GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the wallet is not created when the name is already in use")
    public void TestCreateWalletAlreadyExists()
    {
        when(m_walletRepository.existsById(m_wallet1.GetName())).thenReturn(true);

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.CreateWallet(m_wallet1.GetName(),
                                                         m_wallet1.GetBalance()));

        // Verify that the wallet was not saved
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet is deleted successfully")
    public void TestDeleteWallet()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));

        m_walletService.DeleteWallet(m_wallet1.GetName());

        // Verify that the wallet was deleted
        verify(m_walletRepository).delete(m_wallet1);
    }

    @Test
    @DisplayName("Test if the wallet is not deleted when it does not exist")
    public void TestDeleteWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_walletService.DeleteWallet(m_wallet1.GetName()));

        // Verify that the wallet was not deleted
        verify(m_walletRepository, never()).delete(any(Wallet.class));
    }

    @DisplayName("Test if the wallet is archived successfully")
    @Test
    public void TestArchiveWallet()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(m_wallet1);

        m_walletService.ArchiveWallet(m_wallet1.GetName());

        // Check if the wallet was archived
        verify(m_walletRepository).save(m_wallet1);
        assertTrue(m_wallet1.IsArchived());
    }

    @Test
    @DisplayName(
        "Test if exception is thrown when trying to archive a non-existent wallet")
    public void
    TestArchiveWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_walletService.ArchiveWallet(m_wallet1.GetName()));

        // Verify that the wallet was not archived
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet balance is updated successfully")
    public void TestUpdateWalletBalance()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(m_wallet1);

        double newBalance = 2000.0;

        m_walletService.UpdateWalletBalance(m_wallet1.GetName(), newBalance);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(newBalance, m_wallet1.GetBalance(), Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when trying to update the balance of a "
                 + "non-existent wallet")
    public void
    TestUpdateWalletBalanceDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> m_walletService.UpdateWalletBalance(m_wallet1.GetName(), 1000.0));

        // Verify that the wallet balance was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the money transfer is successful")
    public void TestTransferMoneySuccess()
    {
        Double m_senderPreviousBalance   = m_wallet1.GetBalance();
        Double m_receiverPreviousBalance = m_wallet2.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.findById(m_wallet2.GetName()))
            .thenReturn(Optional.of(m_wallet2));

        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);
        when(m_walletRepository.save(m_wallet2)).thenReturn(m_wallet2);

        when(m_transferRepository.save(any(Transfer.class))).thenReturn(m_transfer12);

        m_walletService.TransferMoney(m_wallet1.GetName(),
                                      m_wallet2.GetName(),
                                      m_transfer12.GetDate(),
                                      m_transfer12.GetAmount(),
                                      m_transfer12.GetDescription());

        // Check if the sender and receiver balances were updated
        verify(m_walletRepository).findById(m_wallet1.GetName());
        verify(m_walletRepository).findById(m_wallet2.GetName());
        verify(m_walletRepository).save(m_wallet1);
        verify(m_walletRepository).save(m_wallet2);

        assertEquals(m_senderPreviousBalance - m_transferAmount,
                     m_wallet1.GetBalance(),
                     Constants.EPSILON);

        assertEquals(m_receiverPreviousBalance + m_transferAmount,
                     m_wallet2.GetBalance(),
                     Constants.EPSILON);

        // Check if the transfer was saved
        ArgumentCaptor<Transfer> transferCaptor =
            ArgumentCaptor.forClass(Transfer.class);

        verify(m_transferRepository).save(transferCaptor.capture());

        assertEquals(m_wallet1, transferCaptor.getValue().GetSenderWallet());
        assertEquals(m_wallet2, transferCaptor.getValue().GetReceiverWallet());
        assertEquals(m_transferAmount,
                     transferCaptor.getValue().GetAmount(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the sender wallet does not exist")
    public void TestTransferMoneySenderDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.TransferMoney(m_wallet1.GetName(),
                                                          m_wallet2.GetName(),
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
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.findById(m_wallet2.GetName()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.TransferMoney(m_wallet1.GetName(),
                                                          m_wallet2.GetName(),
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
                         -> m_walletService.TransferMoney(m_wallet1.GetName(),
                                                          m_wallet1.GetName(),
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
        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.TransferMoney(m_wallet1.GetName(),
                                                          m_wallet2.GetName(),
                                                          m_date,
                                                          0.0,
                                                          m_description));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Test if the confirmed income is added successfully")
    public void TestAddConfirmedIncome()
    {
        Double previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetName()))
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

        m_walletService.AddConfirmedIncome(m_wallet1.GetName(),
                                           m_category,
                                           m_date,
                                           m_incomeAmount,
                                           m_description);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(previousBalance + m_incomeAmount,
                     m_wallet1.GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending income is added successfully")
    public void TestAddPendingIncome()
    {
        Double previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(m_wallet1,
                                              m_category,
                                              TransactionType.INCOME,
                                              TransactionStatus.PENDING,
                                              m_date,
                                              m_incomeAmount,
                                              m_description));

        m_walletService.AddPendingIncome(m_wallet1.GetName(),
                                         m_category,
                                         m_date,
                                         m_incomeAmount,
                                         m_description);

        // Check if the wallet balance is the same
        verify(m_walletRepository, never()).save(any(Wallet.class));
        assertEquals(previousBalance, m_wallet1.GetBalance(), Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the wallet to receive the income "
                 + "does not exist")
    public void
    TestAddIncomeWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.empty());

        // Check for confirmed income
        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.AddConfirmedIncome(m_wallet1.GetName(),
                                                               m_category,
                                                               m_date,
                                                               m_incomeAmount,
                                                               m_description));

        // Check for pending income
        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.AddPendingIncome(m_wallet1.GetName(),
                                                             m_category,
                                                             m_date,
                                                             m_incomeAmount,
                                                             m_description));

        // Verify that the income was not added
        verify(m_walletTransactionRepository, never())
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if the confirmed expense is added successfully")
    public void TestAddConfirmedExpense()
    {
        Double previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));
        when(m_walletRepository.save(m_wallet1)).thenReturn(m_wallet1);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(m_wallet1,
                                              m_category,
                                              TransactionType.OUTCOME,
                                              TransactionStatus.CONFIRMED,
                                              m_date,
                                              m_expenseAmount,
                                              m_description));

        m_walletService.AddConfirmedExpense(m_wallet1.GetName(),
                                            m_category,
                                            m_date,
                                            m_expenseAmount,
                                            m_description);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);
        assertEquals(previousBalance - m_expenseAmount,
                     m_wallet1.GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending expense is added successfully")
    public void TestAddPendingExpense()
    {
        Double previousBalance = m_wallet1.GetBalance();

        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.of(m_wallet1));

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(m_wallet1,
                                              m_category,
                                              TransactionType.OUTCOME,
                                              TransactionStatus.PENDING,
                                              m_date,
                                              m_expenseAmount,
                                              m_description));

        m_walletService.AddPendingExpense(m_wallet1.GetName(),
                                          m_category,
                                          m_date,
                                          m_expenseAmount,
                                          m_description);

        // Check if the wallet balance is the same
        verify(m_walletRepository, never()).save(m_wallet1);
        assertEquals(previousBalance, m_wallet1.GetBalance(), Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the wallet to receive the expense "
                 + "does not exist")
    public void
    TestAddExpenseWalletDoesNotExist()
    {
        when(m_walletRepository.findById(m_wallet1.GetName()))
            .thenReturn(Optional.empty());

        // Check for confirmed expense
        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.AddConfirmedExpense(m_wallet1.GetName(),
                                                                m_category,
                                                                m_date,
                                                                m_expenseAmount,
                                                                m_description));

        // Check for pending expense
        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.AddPendingExpense(m_wallet1.GetName(),
                                                              m_category,
                                                              m_date,
                                                              m_expenseAmount,
                                                              m_description));

        // Verify that the expense was not added
        verify(m_walletTransactionRepository, never())
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if the confirmed expense is deleted successfully")
    public void TestDeleteConfirmedExpense()
    {
        Double previousBalance = m_wallet1.GetBalance();

        when(
            m_walletTransactionRepository.findById(m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1ExpenseTransaction));

        m_walletService.DeleteTransaction(m_wallet1ExpenseTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1ExpenseTransaction);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);

        assertEquals(previousBalance + m_expenseAmount,
                     m_wallet1.GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending expense is deleted successfully")
    public void TestDeletePendingExpense()
    {
        Double previousBalance = m_wallet1.GetBalance();
        m_wallet1ExpenseTransaction.SetStatus(TransactionStatus.PENDING);

        when(
            m_walletTransactionRepository.findById(m_wallet1ExpenseTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1ExpenseTransaction));

        m_walletService.DeleteTransaction(m_wallet1ExpenseTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1ExpenseTransaction);

        // Check if the wallet balance was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));

        assertEquals(previousBalance, m_wallet1.GetBalance(), Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the confirmed income transaction is deleted successfully")
    public void TestDeleteConfirmedIncome()
    {
        Double previousBalance = m_wallet1.GetBalance();

        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1IncomeTransaction));

        m_walletService.DeleteTransaction(m_wallet1IncomeTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1IncomeTransaction);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(m_wallet1);

        assertEquals(previousBalance - m_incomeAmount,
                     m_wallet1.GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the pending income transaction is deleted successfully")
    public void TestDeletePendingIncome()
    {
        Double previousBalance = m_wallet1.GetBalance();
        m_wallet1IncomeTransaction.SetStatus(TransactionStatus.PENDING);

        when(m_walletTransactionRepository.findById(m_wallet1IncomeTransaction.GetId()))
            .thenReturn(Optional.of(m_wallet1IncomeTransaction));

        m_walletService.DeleteTransaction(m_wallet1IncomeTransaction.GetId());

        // Check if the transaction was deleted
        verify(m_walletTransactionRepository).delete(m_wallet1IncomeTransaction);

        // Check if the wallet balance was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));

        assertEquals(previousBalance, m_wallet1.GetBalance(), Constants.EPSILON);
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
                         -> m_walletService.DeleteTransaction(
                             m_wallet1IncomeTransaction.GetId()));
    }
}
