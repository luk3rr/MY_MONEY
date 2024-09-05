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

import com.mymoney.app.entities.Category;
import com.mymoney.app.entities.Transfer;
import com.mymoney.app.entities.Wallet;
import com.mymoney.app.entities.WalletTransaction;
import com.mymoney.repositories.CategoryRepository;
import com.mymoney.repositories.TransferRepository;
import com.mymoney.repositories.WalletRepository;
import com.mymoney.repositories.WalletTransactionRepository;
import com.mymoney.util.Constants;
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

    @BeforeAll
    public static void SetUp()
    {
        MockitoAnnotations.openMocks(WalletServiceTest.class);
    }

    @BeforeEach
    public void BeforeEach()
    { }

    @Test
    @DisplayName("Test if the wallet is created successfully")
    public void TestCreateWallet()
    {
        String walletName    = "My Wallet";
        double walletBalance = 1000.0;

        Wallet wallet = new Wallet(walletName, walletBalance);

        when(m_walletRepository.existsById(walletName)).thenReturn(false);
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        m_walletService.CreateWallet(walletName, walletBalance);

        // Capture the wallet object that was saved and check if the values are correct
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);

        verify(m_walletRepository).save(walletCaptor.capture());

        assertEquals(walletName, walletCaptor.getValue().GetName());
        assertEquals(walletBalance,
                     walletCaptor.getValue().GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if the wallet is not created when the name is already in use")
    public void TestCreateWalletAlreadyExists()
    {
        String walletName    = "My Wallet";
        double walletBalance = 1000.0;

        when(m_walletRepository.existsById(walletName)).thenReturn(true);

        assertThrows(RuntimeException.class,
                     () -> m_walletService.CreateWallet(walletName, walletBalance));

        // Verify that the wallet was not saved
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet is deleted successfully")
    public void TestDeleteWallet()
    {
        String walletName    = "My Wallet";
        double walletBalance = 1000.0;

        Wallet wallet = new Wallet(walletName, walletBalance);

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.of(wallet));

        m_walletService.DeleteWallet(walletName);

        // Verify that the wallet was deleted
        verify(m_walletRepository).delete(wallet);
    }

    @Test
    @DisplayName("Test if the wallet is not deleted when it does not exist")
    public void TestDeleteWalletDoesNotExist()
    {
        String walletName = "My Wallet";

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_walletService.DeleteWallet(walletName));

        // Verify that the wallet was not deleted
        verify(m_walletRepository, never()).delete(any(Wallet.class));
    }

    @DisplayName("Test if the wallet is archived successfully")
    @Test
    public void TestArchiveWallet()
    {
        String walletName    = "My Wallet";
        double walletBalance = 1000.0;

        Wallet wallet = new Wallet(walletName, walletBalance);

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.of(wallet));
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        m_walletService.ArchiveWallet(walletName);

        // Check if the wallet was archived
        verify(m_walletRepository).save(wallet);
        assertTrue(wallet.IsArchived());
    }

    @Test
    @DisplayName(
        "Test if exception is thrown when trying to archive a non-existent wallet")
    public void
    TestArchiveWalletDoesNotExist()
    {
        String walletName = "NonExistentWallet";

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_walletService.ArchiveWallet(walletName));

        // Verify that the wallet was not archived
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the wallet balance is updated successfully")
    public void TestUpdateWalletBalance()
    {
        String walletName    = "My Wallet";
        double walletBalance = 1000.0;

        Wallet wallet = new Wallet(walletName, walletBalance);

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.of(wallet));
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        double newBalance = 2000.0;

        m_walletService.UpdateWalletBalance(walletName, newBalance);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(wallet);
        assertEquals(newBalance, wallet.GetBalance(), Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when trying to update the balance of a "
                 + "non-existent wallet")
    public void
    TestUpdateWalletBalanceDoesNotExist()
    {
        String walletName = "NonExistentWallet";

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     () -> m_walletService.UpdateWalletBalance(walletName, 1000.0));

        // Verify that the wallet balance was not updated
        verify(m_walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("Test if the money transfer is successful")
    public void TestTransferMoneySuccess()
    {
        String    senderName      = "Sender";
        String    receiverName    = "Receiver";
        double    senderBalance   = 1000.0;
        double    receiverBalance = 500.0;
        double    transferAmount  = 200.0;
        LocalDate date            = LocalDate.now();

        Wallet sender   = new Wallet(senderName, senderBalance);
        Wallet receiver = new Wallet(receiverName, receiverBalance);

        when(m_walletRepository.findById(senderName)).thenReturn(Optional.of(sender));
        when(m_walletRepository.findById(receiverName))
            .thenReturn(Optional.of(receiver));

        when(m_walletRepository.save(sender)).thenReturn(sender);
        when(m_walletRepository.save(receiver)).thenReturn(receiver);

        when(m_transferRepository.save(any(Transfer.class)))
            .thenReturn(
                new Transfer(sender, receiver, date, transferAmount, "description"));

        m_walletService.TransferMoney(senderName,
                                      receiverName,
                                      date,
                                      transferAmount,
                                      "description");

        // Check if the sender and receiver balances were updated
        verify(m_walletRepository).findById(senderName);
        verify(m_walletRepository).findById(receiverName);
        verify(m_walletRepository).save(sender);
        verify(m_walletRepository).save(receiver);

        assertEquals(senderBalance - transferAmount,
                     sender.GetBalance(),
                     Constants.EPSILON);

        assertEquals(receiverBalance + transferAmount,
                     receiver.GetBalance(),
                     Constants.EPSILON);

        // Check if the transfer was saved
        ArgumentCaptor<Transfer> transferCaptor =
            ArgumentCaptor.forClass(Transfer.class);

        verify(m_transferRepository).save(transferCaptor.capture());

        assertEquals(sender, transferCaptor.getValue().GetSenderWallet());
        assertEquals(receiver, transferCaptor.getValue().GetReceiverWallet());
        assertEquals(transferAmount,
                     transferCaptor.getValue().GetAmount(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the sender wallet does not exist")
    public void TestTransferMoneySenderDoesNotExist()
    {
        String    senderName     = "Sender";
        String    receiverName   = "Receiver";
        double    transferAmount = 200.0;
        LocalDate date           = LocalDate.now();

        when(m_walletRepository.findById(senderName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.TransferMoney(senderName,
                                                          receiverName,
                                                          date,
                                                          transferAmount,
                                                          "description"));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the receiver wallet does not exist")
    public void TestTransferMoneyReceiverDoesNotExist()
    {
        String    senderName     = "Sender";
        String    receiverName   = "Receiver";
        double    senderBalance  = 1000.0;
        double    transferAmount = 200.0;
        LocalDate date           = LocalDate.now();

        Wallet sender = new Wallet(senderName, senderBalance);

        when(m_walletRepository.findById(senderName)).thenReturn(Optional.of(sender));
        when(m_walletRepository.findById(receiverName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.TransferMoney(senderName,
                                                          receiverName,
                                                          date,
                                                          transferAmount,
                                                          "description"));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName(
        "Test if exception is thrown when the sender and receiver wallets are the same")
    public void
    TestTransferMoneySameWallet()
    {
        String    senderName     = "Sender";
        double    transferAmount = 200.0;
        LocalDate date           = LocalDate.now();

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.TransferMoney(senderName,
                                                          senderName,
                                                          date,
                                                          transferAmount,
                                                          "description"));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Test if exception is thrown when the amount to transfer is less "
                 + "than or equal to zero")
    public void
    TestTransferMoneyAmountZero()
    {
        String    senderName     = "Sender";
        String    receiverName   = "Receiver";
        double    transferAmount = 0.0;
        LocalDate date           = LocalDate.now();

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.TransferMoney(senderName,
                                                          receiverName,
                                                          date,
                                                          transferAmount,
                                                          "description"));

        // Verify that the transfer was not saved
        verify(m_transferRepository, never()).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Test if the income is added successfully")
    public void TestAddIncome()
    {
        String    walletName    = "My Wallet";
        double    walletBalance = 1000.0;
        double    incomeAmount  = 500.0;
        LocalDate date          = LocalDate.now();
        Category  category      = new Category("Salary");
        String    description   = "Income";

        Wallet wallet = new Wallet(walletName, walletBalance);

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.of(wallet));
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(wallet,
                                              category,
                                              TransactionType.INCOME,
                                              date,
                                              incomeAmount,
                                              description));

        m_walletService.AddIncome(walletName,
                                  category,
                                  date,
                                  incomeAmount,
                                  description);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(wallet);
        assertEquals(walletBalance + incomeAmount,
                     wallet.GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the wallet to receive the income "
                 + "does not exist")
    public void
    TestAddIncomeWalletDoesNotExist()
    {
        String    walletName   = "NonExistentWallet";
        double    incomeAmount = 500.0;
        LocalDate date         = LocalDate.now();
        Category  category     = new Category("Salary");
        String    description  = "Income";

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.AddIncome(walletName,
                                                      category,
                                                      date,
                                                      incomeAmount,
                                                      description));

        // Verify that the income was not added
        verify(m_walletTransactionRepository, never())
            .save(any(WalletTransaction.class));
    }

    @Test
    @DisplayName("Test if the expense is added successfully")
    public void TestAddExpense()
    {
        String    walletName    = "My Wallet";
        double    walletBalance = 1000.0;
        double    expenseAmount = 500.0;
        LocalDate date          = LocalDate.now();
        Category  category      = new Category("Food");
        String    description   = "Expense";

        Wallet wallet = new Wallet(walletName, walletBalance);

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.of(wallet));
        when(m_walletRepository.save(any(Wallet.class))).thenReturn(wallet);

        when(m_walletTransactionRepository.save(any(WalletTransaction.class)))
            .thenReturn(new WalletTransaction(wallet,
                                              category,
                                              TransactionType.OUTCOME,
                                              date,
                                              expenseAmount,
                                              description));

        m_walletService.AddExpense(walletName,
                                   category,
                                   date,
                                   expenseAmount,
                                   description);

        // Check if the wallet balance was updated
        verify(m_walletRepository).save(wallet);
        assertEquals(walletBalance - expenseAmount,
                     wallet.GetBalance(),
                     Constants.EPSILON);
    }

    @Test
    @DisplayName("Test if exception is thrown when the wallet to receive the expense "
                 + "does not exist")
    public void
    TestAddExpenseWalletDoesNotExist()
    {
        String    walletName    = "NonExistentWallet";
        double    expenseAmount = 500.0;
        LocalDate date          = LocalDate.now();
        Category  category      = new Category("Food");
        String    description   = "Expense";

        when(m_walletRepository.findById(walletName)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> m_walletService.AddExpense(walletName,
                                                       category,
                                                       date,
                                                       expenseAmount,
                                                       description));

        // Verify that the expense was not added
        verify(m_walletTransactionRepository, never())
            .save(any(WalletTransaction.class));
    }
}
