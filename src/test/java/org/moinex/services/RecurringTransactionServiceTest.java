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
import java.time.LocalDateTime;
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
import org.moinex.repositories.RecurringTransactionRepository;
import org.moinex.repositories.WalletRepository;
import org.moinex.util.RecurringTransactionFrequency;
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
    private Category      category;
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
        wallet      = new Wallet(1L, "Wallet", BigDecimal.valueOf(1000.0));
        category    = new Category("c1");
        startDate   = LocalDateTime.now();
        endDate     = LocalDateTime.now().plusMonths(1);
        nextDueDate = LocalDateTime.now();

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

        recurringTransactionService.CreateRecurringTransaction(dailyRT.GetWallet(),
                                                               dailyRT.GetCategory(),
                                                               dailyRT.GetType(),
                                                               dailyRT.GetAmount(),
                                                               dailyRT.GetStartDate(),
                                                               dailyRT.GetEndDate(),
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
        // assertEquals(dailyRT.GetFrequency(),
        //              recurringTransactionCaptor.getValue().GetFrequency());
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
                             dailyRT.GetWallet(),
                             dailyRT.GetCategory(),
                             dailyRT.GetType(),
                             dailyRT.GetAmount(),
                             dailyRT.GetStartDate(),
                             dailyRT.GetEndDate(),
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

        // Check if the date without time is the same
        assertEquals(LocalDateTime.now().toLocalDate(),
                     recurringTransactionCaptor.getValue().GetEndDate().toLocalDate());
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
        dailyRT.SetEndDate(LocalDateTime.now().minusDays(40));

        assertThrows(RuntimeException.class,
                     ()
                         -> recurringTransactionService.StopRecurringTransaction(
                             dailyRT.GetId()));

        verify(recurringTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test if the daily recurring transactions are processed correctly")
    public void TestProcessDailyRecurringTransaction()
    {
        LocalDateTime today =
            LocalDateTime.now().withHour(23).withMinute(59).withSecond(0).withNano(0);

        dailyRT.SetNextDueDate(today.minusDays(10));

        when(recurringTransactionRepository.findAll())
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
            LocalDateTime expectedDate = today.minusDays(10 - i);

            assertEquals(expectedDate,
                         capturedDates.get(i),
                         "The date of the transaction is not the expected one");
        }

        verify(recurringTransactionRepository, atLeastOnce()).save(dailyRT);
    }

    @Test
    @DisplayName("Test if the weekly recurring transactions are processed correctly")
    public void TestProcessWeeklyRecurringTransaction()
    {
        LocalDateTime today =
            LocalDateTime.now().withHour(23).withMinute(59).withSecond(0).withNano(0);

        weeklyRecurringTransaction.SetNextDueDate(today.minusWeeks(5));

        when(recurringTransactionRepository.findAll())
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
            LocalDateTime expectedDate = today.minusWeeks(5 - i);

            assertEquals(expectedDate,
                         capturedDates.get(i),
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
            LocalDateTime.now().withHour(23).withMinute(59).withSecond(0).withNano(0);

        monthlyRecurringTransaction.SetNextDueDate(today.minusMonths(12));

        when(recurringTransactionRepository.findAll())
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
            LocalDateTime expectedDate = today.minusMonths(12 - i);

            assertEquals(expectedDate,
                         capturedDates.get(i),
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
            LocalDateTime.now().withHour(23).withMinute(59).withSecond(0).withNano(0);

        yearlyRecurringTransaction.SetNextDueDate(today.minusYears(5));

        when(recurringTransactionRepository.findAll())
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
            LocalDateTime expectedDate = today.minusYears(5 - i);

            assertEquals(expectedDate,
                         capturedDates.get(i),
                         "The date of the transaction is not the expected one");
        }

        verify(recurringTransactionRepository, atLeastOnce())
            .save(yearlyRecurringTransaction);
    }
}
