/*
 * Filename: RecurringTransactionServiceTest.java
 * Created on: November 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.moinex.entities.Category;
import org.moinex.entities.RecurringTransaction;
import org.moinex.entities.Wallet;
import org.moinex.entities.WalletTransaction;
import org.moinex.repositories.RecurringTransactionRepository;
import org.moinex.repositories.WalletRepository;
import org.moinex.util.Constants;
import org.moinex.util.RecurringTransactionFrequency;
import org.moinex.util.RecurringTransactionStatus;
import org.moinex.util.TransactionStatus;
import org.moinex.util.TransactionType;

@ExtendWith(MockitoExtension.class)
public class RecurringTransactionServiceTest
{
    @Mock
    private RecurringTransactionRepository recurringTransactionRepository;

    @Mock
    private WalletTransactionService walletTransactionService;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private RecurringTransactionService recurringTransactionService;

    private Wallet        wallet;
    private Wallet        wallet2;
    private Category      category;
    private Category      category2;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime nextDueDate;

    private RecurringTransaction dailyRT;
    private RecurringTransaction weeklyRecurringTransaction;
    private RecurringTransaction monthlyRecurringTransaction;
    private RecurringTransaction yearlyRecurringTransaction;

    private RecurringTransaction
    CreateRecurringTransaction(Long                          id,
                               Wallet                        wallet,
                               Category                      category,
                               TransactionType               type,
                               BigDecimal                    amount,
                               LocalDateTime                 startDate,
                               LocalDateTime                 endDate,
                               LocalDateTime                 nextDueDate,
                               RecurringTransactionFrequency frequency,
                               String                        description)
    {
        RecurringTransaction recurringTransaction =
            new RecurringTransaction(id,
                                     wallet,
                                     category,
                                     type,
                                     amount,
                                     startDate,
                                     endDate,
                                     nextDueDate,
                                     frequency,
                                     description);

        return recurringTransaction;
    }

    @BeforeEach
    public void SetUp()
    {
        wallet  = new Wallet(1L, "Wallet", BigDecimal.valueOf(1000.0));
        wallet2 = new Wallet(2L, "Wallet 2", BigDecimal.valueOf(500.0));

        category  = new Category("c1");
        category2 = new Category("c2");

        startDate =
            LocalDateTime.now().with(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);
        endDate = LocalDateTime.now().plusMonths(1).with(
            Constants.RECURRING_TRANSACTION_DEFAULT_TIME);
        nextDueDate = LocalDateTime.now().with(
            Constants.RECURRING_TRANSACTION_DUE_DATE_DEFAULT_TIME);

        dailyRT = CreateRecurringTransaction(1L,
                                             wallet,
                                             category,
                                             TransactionType.EXPENSE,
                                             BigDecimal.valueOf(100.0),
                                             startDate,
                                             endDate,
                                             nextDueDate,
                                             RecurringTransactionFrequency.DAILY,
                                             "Daily transaction");

        weeklyRecurringTransaction =
            CreateRecurringTransaction(2L,
                                       wallet,
                                       category,
                                       TransactionType.EXPENSE,
                                       BigDecimal.valueOf(100.0),
                                       startDate,
                                       endDate,
                                       nextDueDate,
                                       RecurringTransactionFrequency.WEEKLY,
                                       "Weekly transaction");

        monthlyRecurringTransaction =
            CreateRecurringTransaction(3L,
                                       wallet,
                                       category,
                                       TransactionType.EXPENSE,
                                       BigDecimal.valueOf(100.0),
                                       startDate,
                                       endDate,
                                       nextDueDate,
                                       RecurringTransactionFrequency.MONTHLY,
                                       "Monthly transaction");

        yearlyRecurringTransaction =
            CreateRecurringTransaction(4L,
                                       wallet,
                                       category,
                                       TransactionType.EXPENSE,
                                       BigDecimal.valueOf(100.0),
                                       startDate,
                                       endDate,
                                       nextDueDate,
                                       RecurringTransactionFrequency.YEARLY,
                                       "Yearly transaction");
    }

    @Test
    @DisplayName("Test if the recurring transactions are created successfully")
    public void TestCreateRecurringTransaction()
    {
        when(walletRepository.findById(wallet.GetId())).thenReturn(Optional.of(wallet));

        recurringTransactionService.CreateRecurringTransaction(
            dailyRT.GetWallet().GetId(),
            dailyRT.GetCategory(),
            dailyRT.GetType(),
            dailyRT.GetAmount(),
            dailyRT.GetStartDate().toLocalDate(),
            dailyRT.GetEndDate().toLocalDate(),
            dailyRT.GetDescription(),
            dailyRT.GetFrequency());

        // Capture the recurring transaction that was saved
        ArgumentCaptor<RecurringTransaction> recurringTransactionCaptor =
            ArgumentCaptor.forClass(RecurringTransaction.class);

        verify(recurringTransactionRepository)
            .save(recurringTransactionCaptor.capture());

        assertEquals(dailyRT.GetWallet(),
                     recurringTransactionCaptor.getValue().GetWallet());
        assertEquals(dailyRT.GetCategory(),
                     recurringTransactionCaptor.getValue().GetCategory());
        assertEquals(dailyRT.GetType(),
                     recurringTransactionCaptor.getValue().GetType());
        assertEquals(dailyRT.GetAmount(),
                     recurringTransactionCaptor.getValue().GetAmount());
        assertEquals(dailyRT.GetStartDate(),
                     recurringTransactionCaptor.getValue().GetStartDate());
        assertEquals(dailyRT.GetEndDate(),
                     recurringTransactionCaptor.getValue().GetEndDate());
        assertEquals(dailyRT.GetDescription(),
                     recurringTransactionCaptor.getValue().GetDescription());
        assertEquals(dailyRT.GetStatus(),
                     recurringTransactionCaptor.getValue().GetStatus());
        assertEquals(dailyRT.GetFrequency(),
                     recurringTransactionCaptor.getValue().GetFrequency());
    }

    @Test
    @DisplayName("Test if the recurring transactions is not created when the wallet "
                 + "is not found")
    public void
    TestCreateRecurringTransactionWalletNotFound()
    {
        when(walletRepository.findById(wallet.GetId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> recurringTransactionService.CreateRecurringTransaction(
                             dailyRT.GetWallet().GetId(),
                             dailyRT.GetCategory(),
                             dailyRT.GetType(),
                             dailyRT.GetAmount(),
                             dailyRT.GetStartDate().toLocalDate(),
                             dailyRT.GetEndDate().toLocalDate(),
                             dailyRT.GetDescription(),
                             dailyRT.GetFrequency()));

        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the recurring transactions are stopped successfully")
    public void TestStopRecurringTransaction()
    {
        when(recurringTransactionRepository.findById(dailyRT.GetId()))
            .thenReturn(Optional.of(dailyRT));

        // Change the end date to a date in the future
        dailyRT.SetEndDate(LocalDateTime.now().plusDays(40));

        recurringTransactionService.StopRecurringTransaction(dailyRT.GetId());

        // Capture the recurring transaction that was saved
        ArgumentCaptor<RecurringTransaction> recurringTransactionCaptor =
            ArgumentCaptor.forClass(RecurringTransaction.class);

        verify(recurringTransactionRepository)
            .save(recurringTransactionCaptor.capture());

        // Check if the status of the recurring transaction is INACTIVE
        assertEquals(recurringTransactionCaptor.getValue().GetStatus(),
                     RecurringTransactionStatus.INACTIVE);
    }

    @Test
    @DisplayName("Test if the recurring transactions is not stopped when the "
                 + "recurring transaction is not found")
    public void
    TestStopRecurringTransactionNotFound()
    {
        when(recurringTransactionRepository.findById(dailyRT.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> recurringTransactionService.StopRecurringTransaction(
                             dailyRT.GetId()));

        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the recurring transactions is not stopped when the "
                 + "recurring transaction has already ended")
    public void
    TestStopRecurringTransactionAlreadyEnded()
    {
        when(recurringTransactionRepository.findById(dailyRT.GetId()))
            .thenReturn(Optional.of(dailyRT));

        // Change the end date to a date in the past
        dailyRT.SetStatus(RecurringTransactionStatus.INACTIVE);

        assertThrows(RuntimeException.class,
                     ()
                         -> recurringTransactionService.StopRecurringTransaction(
                             dailyRT.GetId()));

        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the recurring transactions are deleted successfully")
    public void TestDeleteRecurringTransaction()
    {
        when(recurringTransactionRepository.findById(dailyRT.GetId()))
            .thenReturn(Optional.of(dailyRT));

        recurringTransactionService.DeleteRecurringTransaction(dailyRT.GetId());

        verify(recurringTransactionRepository).delete(dailyRT);
    }

    @Test
    @DisplayName("Test if the recurring transactions is not deleted when the "
                 + "recurring transaction is not found")
    public void
    TestDeleteRecurringTransactionNotFound()
    {
        when(recurringTransactionRepository.findById(dailyRT.GetId()))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                     ()
                         -> recurringTransactionService.DeleteRecurringTransaction(
                             dailyRT.GetId()));

        verify(recurringTransactionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Test if the recurring transactions are updated successfully")
    public void TestUpdateRecurringTransaction()
    {
        RecurringTransaction updatedRT =
            new RecurringTransaction(dailyRT.GetId(),
                                     dailyRT.GetWallet(),
                                     dailyRT.GetCategory(),
                                     dailyRT.GetType(),
                                     dailyRT.GetAmount(),
                                     dailyRT.GetStartDate(),
                                     dailyRT.GetEndDate(),
                                     dailyRT.GetNextDueDate(),
                                     dailyRT.GetFrequency(),
                                     dailyRT.GetDescription());

        when(recurringTransactionRepository.findById(updatedRT.GetId()))
            .thenReturn(Optional.of(dailyRT));

        // Update the recurring transaction
        updatedRT.SetWallet(dailyRT.GetWallet().GetId() == wallet.GetId() ? wallet2
                                                                          : wallet);

        updatedRT.SetCategory(
            updatedRT.GetCategory().GetName().equals(category.GetName()) ? category2
                                                                         : category);

        updatedRT.SetType(updatedRT.GetType() == TransactionType.EXPENSE
                              ? TransactionType.INCOME
                              : TransactionType.EXPENSE);

        updatedRT.SetAmount(BigDecimal.valueOf(200.0));
        updatedRT.SetEndDate(updatedRT.GetEndDate().plusDays(10));
        updatedRT.SetNextDueDate(updatedRT.GetNextDueDate().plusDays(10));
        updatedRT.SetFrequency(updatedRT.GetFrequency() ==
                                       RecurringTransactionFrequency.DAILY
                                   ? RecurringTransactionFrequency.WEEKLY
                                   : RecurringTransactionFrequency.DAILY);
        updatedRT.SetDescription("Updated description");

        recurringTransactionService.UpdateRecurringTransaction(updatedRT);

        // Capture the recurring transaction that was saved
        ArgumentCaptor<RecurringTransaction> recurringTransactionCaptor =
            ArgumentCaptor.forClass(RecurringTransaction.class);

        verify(recurringTransactionRepository)
            .save(recurringTransactionCaptor.capture());

        assertEquals(updatedRT.GetWallet(),
                     recurringTransactionCaptor.getValue().GetWallet());
        assertEquals(updatedRT.GetCategory(),
                     recurringTransactionCaptor.getValue().GetCategory());
        assertEquals(updatedRT.GetType(),
                     recurringTransactionCaptor.getValue().GetType());
        assertEquals(updatedRT.GetAmount(),
                     recurringTransactionCaptor.getValue().GetAmount());
        assertEquals(updatedRT.GetStartDate(),
                     recurringTransactionCaptor.getValue().GetStartDate());
        assertEquals(updatedRT.GetEndDate(),
                     recurringTransactionCaptor.getValue().GetEndDate());
        assertEquals(updatedRT.GetDescription(),
                     recurringTransactionCaptor.getValue().GetDescription());
        assertEquals(updatedRT.GetStatus(),
                     recurringTransactionCaptor.getValue().GetStatus());
        assertEquals(updatedRT.GetFrequency(),
                     recurringTransactionCaptor.getValue().GetFrequency());
    }

    @Test
    @DisplayName("Test if the daily recurring transactions are processed correctly")
    public void TestProcessDailyRecurringTransaction()
    {
        LocalDateTime today =
            LocalDateTime.now().with(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);

        dailyRT.SetNextDueDate(today.minusDays(10));

        when(recurringTransactionRepository.findByStatus(
                 RecurringTransactionStatus.ACTIVE))
            .thenReturn(Collections.singletonList(dailyRT));

        recurringTransactionService.ProcessRecurringTransactions();

        // Capture the dates of the transactions
        ArgumentCaptor<LocalDateTime> dateCaptor =
            ArgumentCaptor.forClass(LocalDateTime.class);

        verify(walletTransactionService, times(10))
            .AddExpense(eq(dailyRT.GetWallet().GetId()),
                        eq(dailyRT.GetCategory()),
                        dateCaptor.capture(),
                        eq(dailyRT.GetAmount()),
                        eq(dailyRT.GetDescription()),
                        eq(TransactionStatus.PENDING));

        // Get the captured dates
        List<LocalDateTime> capturedDates = dateCaptor.getAllValues();

        // Check if the captured dates correspond to the expected dates for each of the
        // 10 days
        for (int i = 0; i < 10; i++)
        {
            LocalDate expectedDate = today.minusDays(10 - i).toLocalDate();

            assertEquals(expectedDate,
                         capturedDates.get(i).toLocalDate(),
                         "The date of the transaction is not the expected one");
        }

        verify(recurringTransactionRepository, atLeastOnce()).save(dailyRT);
    }

    @Test
    @DisplayName("Test if the weekly recurring transactions are processed correctly")
    public void TestProcessWeeklyRecurringTransaction()
    {
        LocalDateTime today =
            LocalDateTime.now().with(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);

        weeklyRecurringTransaction.SetNextDueDate(today.minusWeeks(5));

        when(recurringTransactionRepository.findByStatus(
                 RecurringTransactionStatus.ACTIVE))
            .thenReturn(Collections.singletonList(weeklyRecurringTransaction));

        recurringTransactionService.ProcessRecurringTransactions();

        // Capture the dates of the transactions
        ArgumentCaptor<LocalDateTime> dateCaptor =
            ArgumentCaptor.forClass(LocalDateTime.class);

        verify(walletTransactionService, times(5))
            .AddExpense(eq(weeklyRecurringTransaction.GetWallet().GetId()),
                        eq(weeklyRecurringTransaction.GetCategory()),
                        dateCaptor.capture(),
                        eq(weeklyRecurringTransaction.GetAmount()),
                        eq(weeklyRecurringTransaction.GetDescription()),
                        eq(TransactionStatus.PENDING));

        // Get the captured dates
        List<LocalDateTime> capturedDates = dateCaptor.getAllValues();

        // Check if the captured dates correspond to the expected dates for each of the
        // 2 weeks
        for (int i = 0; i < 5; i++)
        {
            LocalDate expectedDate = today.minusWeeks(5 - i).toLocalDate();

            assertEquals(expectedDate,
                         capturedDates.get(i).toLocalDate(),
                         "The date of the transaction is not the expected one");
        }

        verify(recurringTransactionRepository, atLeastOnce())
            .save(weeklyRecurringTransaction);
    }

    @Test
    @DisplayName("Test if the monthly recurring transactions are processed correctly")
    public void TestProcessMonthlyRecurringTransaction()
    {
        LocalDateTime today =
            LocalDateTime.now().with(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);

        monthlyRecurringTransaction.SetNextDueDate(today.minusMonths(12));

        when(recurringTransactionRepository.findByStatus(
                 RecurringTransactionStatus.ACTIVE))
            .thenReturn(Collections.singletonList(monthlyRecurringTransaction));

        recurringTransactionService.ProcessRecurringTransactions();

        // Capture the dates of the transactions
        ArgumentCaptor<LocalDateTime> dateCaptor =
            ArgumentCaptor.forClass(LocalDateTime.class);

        verify(walletTransactionService, times(12))
            .AddExpense(eq(monthlyRecurringTransaction.GetWallet().GetId()),
                        eq(monthlyRecurringTransaction.GetCategory()),
                        dateCaptor.capture(),
                        eq(monthlyRecurringTransaction.GetAmount()),
                        eq(monthlyRecurringTransaction.GetDescription()),
                        eq(TransactionStatus.PENDING));

        // Get the captured dates
        List<LocalDateTime> capturedDates = dateCaptor.getAllValues();

        // Check if the captured dates correspond to the expected dates for each of the
        // 1 month
        for (int i = 0; i < 12; i++)
        {
            LocalDate expectedDate = today.minusMonths(12 - i).toLocalDate();

            assertEquals(expectedDate,
                         capturedDates.get(i).toLocalDate(),
                         "The date of the transaction is not the expected one");
        }

        verify(recurringTransactionRepository, atLeastOnce())
            .save(monthlyRecurringTransaction);
    }

    @Test
    @DisplayName("Test if the yearly recurring transactions are processed correctly")
    public void TestProcessYearlyRecurringTransaction()
    {
        LocalDateTime today =
            LocalDateTime.now().with(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);

        yearlyRecurringTransaction.SetNextDueDate(today.minusYears(5));

        when(recurringTransactionRepository.findByStatus(
                 RecurringTransactionStatus.ACTIVE))
            .thenReturn(Collections.singletonList(yearlyRecurringTransaction));

        recurringTransactionService.ProcessRecurringTransactions();

        // Capture the dates of the transactions
        ArgumentCaptor<LocalDateTime> dateCaptor =
            ArgumentCaptor.forClass(LocalDateTime.class);

        verify(walletTransactionService, times(5))
            .AddExpense(eq(yearlyRecurringTransaction.GetWallet().GetId()),
                        eq(yearlyRecurringTransaction.GetCategory()),
                        dateCaptor.capture(),
                        eq(yearlyRecurringTransaction.GetAmount()),
                        eq(yearlyRecurringTransaction.GetDescription()),
                        eq(TransactionStatus.PENDING));

        // Get the captured dates
        List<LocalDateTime> capturedDates = dateCaptor.getAllValues();

        // Check if the captured dates correspond to the expected dates for each of the
        // 1 year
        for (int i = 0; i < 5; i++)
        {
            LocalDate expectedDate = today.minusYears(5 - i).toLocalDate();

            assertEquals(expectedDate,
                         capturedDates.get(i).toLocalDate(),
                         "The date of the transaction is not the expected one");
        }

        verify(recurringTransactionRepository, atLeastOnce())
            .save(yearlyRecurringTransaction);
    }

    @Test
    @DisplayName(
        "Test if the active recurring transactions with end date in the past are "
        + "stopped")
    public void
    TestProcessRecurringTransactionEnds()
    {
        LocalDateTime today =
            LocalDateTime.now().with(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);

        dailyRT.SetNextDueDate(today.minusDays(10));
        dailyRT.SetEndDate(today.minusDays(5));
        dailyRT.SetStatus(RecurringTransactionStatus.ACTIVE);

        when(recurringTransactionRepository.findByStatus(
                 RecurringTransactionStatus.ACTIVE))
            .thenReturn(Collections.singletonList(dailyRT));

        recurringTransactionService.ProcessRecurringTransactions();

        // Captures and check if the recurring transaction was saved with the status as
        // INACTIVE
        ArgumentCaptor<RecurringTransaction> captor =
            ArgumentCaptor.forClass(RecurringTransaction.class);
        verify(recurringTransactionRepository, times(1)).save(captor.capture());

        RecurringTransaction capturedTransaction = captor.getValue();

        assertEquals(RecurringTransactionStatus.INACTIVE,
                     capturedTransaction.GetStatus());
    }

    @Test
    @DisplayName(
        "Test if get future recurring transactions by month returns the correct "
        + "transactions")
    public void
    TestGetFutureRecurringTransactionsByMonth()
    {
        YearMonth     november2011YearMonth = YearMonth.of(2011, 11);
        LocalDateTime november2011DateTime =
            LocalDate.of(2011, 11, 1)
                .atTime(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);

        Integer expectedTransactions = 0;

        dailyRT.SetNextDueDate(november2011DateTime);
        expectedTransactions += november2011YearMonth.lengthOfMonth();

        weeklyRecurringTransaction.SetNextDueDate(november2011DateTime);
        expectedTransactions += 5; // In 4 weeks there are 5 transactions, because it
                                   // includes the transaction of November 1st

        monthlyRecurringTransaction.SetNextDueDate(november2011DateTime);
        expectedTransactions += 1;

        yearlyRecurringTransaction.SetNextDueDate(november2011DateTime);
        expectedTransactions += 1;

        when(recurringTransactionRepository.findByStatus(
                 RecurringTransactionStatus.ACTIVE))
            .thenReturn(List.of(dailyRT,
                                weeklyRecurringTransaction,
                                monthlyRecurringTransaction,
                                yearlyRecurringTransaction));

        List<WalletTransaction> futureRecurringTransactions =
            recurringTransactionService.GetFutureTransactionsByMonth(
                november2011YearMonth,
                november2011YearMonth);

        assertEquals(expectedTransactions, futureRecurringTransactions.size());
    }

    @Test
    @DisplayName(
        "Test if get future recurring transactions by year returns the correct "
        + "transactions")
    public void
    TestGetFutureRecurringTransactionsByYear()
    {
        Year          year2011 = Year.of(2011);
        LocalDateTime january2011DateTime =
            LocalDate.of(2011, 1, 1)
                .atTime(Constants.RECURRING_TRANSACTION_DEFAULT_TIME);

        Integer expectedTransactions = 0;

        dailyRT.SetNextDueDate(january2011DateTime);
        expectedTransactions += 365;

        weeklyRecurringTransaction.SetNextDueDate(january2011DateTime);
        expectedTransactions += 53; // In 52 weeks there are 53 transactions, because it
                                    // includes the transaction of January 1st

        monthlyRecurringTransaction.SetNextDueDate(january2011DateTime);
        expectedTransactions += 12;

        yearlyRecurringTransaction.SetNextDueDate(january2011DateTime);
        expectedTransactions += 1;

        when(recurringTransactionRepository.findByStatus(
                 RecurringTransactionStatus.ACTIVE))
            .thenReturn(List.of(dailyRT,
                                weeklyRecurringTransaction,
                                monthlyRecurringTransaction,
                                yearlyRecurringTransaction));

        List<WalletTransaction> futureRecurringTransactions =
            recurringTransactionService.GetFutureTransactionsByYear(year2011, year2011);

        assertEquals(expectedTransactions, futureRecurringTransactions.size());
    }
}
