/*
 * Filename: RecurringTransactionService.java
 * Created on: November 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Logger;
import org.moinex.entities.Category;
import org.moinex.entities.RecurringTransaction;
import org.moinex.entities.Wallet;
import org.moinex.repositories.CategoryRepository;
import org.moinex.repositories.RecurringTransactionRepository;
import org.moinex.repositories.WalletRepository;
import org.moinex.util.LoggerConfig;
import org.moinex.util.RecurringTransactionFrequency;
import org.moinex.util.RecurringTransactionStatus;
import org.moinex.util.TransactionStatus;
import org.moinex.util.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for managing the recurring transactions
 */
@Service
public class RecurringTransactionService
{
    @Autowired
    private RecurringTransactionRepository recurringTransactionRepository;

    @Autowired
    private WalletTransactionService walletTransactionService;

    @Autowired
    private WalletRepository walletRepository;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public RecurringTransactionService() { }

    /**
     * Check if the date and interval between start and end date is valid
     * @param startDate The start date
     * @param endDate The end date
     * @param frequency The frequency of the recurring transaction
     * TODO: Adicionar verificação aos testes
     */
    private void CheckDateAndIntervalIsValid(LocalDate                     startDate,
                                             LocalDate                     endDate,
                                             RecurringTransactionFrequency frequency)
    {
        // Check if interval between start and end date is valid
        if (startDate.isBefore(LocalDate.now()))
        {
            throw new RuntimeException("Start date cannot be before today");
        }

        if (endDate.isBefore(startDate))
        {
            throw new RuntimeException("End date cannot be before start date");
        }

        // Check if any transaction can be generated
        if (frequency == RecurringTransactionFrequency.DAILY &&
            !(startDate.plusDays(1).isBefore(endDate) ||
              startDate.plusDays(1).equals(endDate)))
        {
            throw new RuntimeException(
                "End date must be at least one day after the start date");
        }
        else if (frequency == RecurringTransactionFrequency.WEEKLY &&
                 !(startDate.plusWeeks(1).isBefore(endDate) ||
                   startDate.plusWeeks(1).equals(endDate)))
        {
            throw new RuntimeException(
                "End date must be at least one week after the start date");
        }
        else if (frequency == RecurringTransactionFrequency.MONTHLY &&
                 !(startDate.plusMonths(1).isBefore(endDate) ||
                   startDate.plusMonths(1).equals(endDate)))
        {
            throw new RuntimeException(
                "End date must be at least one month after the start date");
        }
        else if (frequency == RecurringTransactionFrequency.YEARLY &&
                 !(startDate.plusYears(1).isBefore(endDate) ||
                   startDate.plusYears(1).equals(endDate)))
        {
            throw new RuntimeException(
                "End date must be at least one year after the start date");
        }
    }

    @Transactional
    public Long CreateRecurringTransaction(Long                          walletId,
                                           Category                      category,
                                           TransactionType               type,
                                           BigDecimal                    amount,
                                           LocalDate                     startDate,
                                           String                        description,
                                           RecurringTransactionFrequency frequency)
    {
        LocalDate defaultEndDate = LocalDate.now().plusYears(100);

        return CreateRecurringTransaction(walletId,
                                          category,
                                          type,
                                          amount,
                                          startDate,
                                          defaultEndDate,
                                          description,
                                          frequency);
    }

    @Transactional
    public Long CreateRecurringTransaction(Long                          walletId,
                                           Category                      category,
                                           TransactionType               type,
                                           BigDecimal                    amount,
                                           LocalDate                     startDate,
                                           LocalDate                     endDate,
                                           String                        description,
                                           RecurringTransactionFrequency frequency)
    {
        Wallet wt = walletRepository.findById(walletId).orElseThrow(
            () -> new RuntimeException("Wallet with id " + walletId + " not found"));

        if (startDate == null || endDate == null)
        {
            throw new RuntimeException("Start and end date cannot be null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new RuntimeException("Amount must be greater than zero");
        }

        // Define the end date as the last second of the day
        LocalDateTime startDateWithTime = startDate.atTime(0, 0, 0, 0);

        LocalDateTime endDateWithTime = endDate.atTime(23, 59, 59, 0);

        // Ensure the date and interval between start and end date is valid
        CheckDateAndIntervalIsValid(startDate, endDate, frequency);

        RecurringTransaction recurringTransaction =
            new RecurringTransaction(wt,
                                     category,
                                     type,
                                     amount,
                                     startDateWithTime,
                                     endDateWithTime,
                                     startDateWithTime,
                                     frequency,
                                     description);

        recurringTransactionRepository.save(recurringTransaction);

        m_logger.info("Created recurring transaction " + recurringTransaction.GetId());

        return recurringTransaction.GetId();
    }

    @Transactional
    public void StopRecurringTransaction(Long recurringTransactionId)
    {
        RecurringTransaction recurringTransaction =
            recurringTransactionRepository.findById(recurringTransactionId)
                .orElseThrow(
                    () -> new RuntimeException("Recurring transaction not found"));

        // Check if the recurring transaction has already ended
        if (recurringTransaction.GetStatus().equals(
                RecurringTransactionStatus.INACTIVE))
        {
            throw new RuntimeException("Recurring transaction has already ended");
        }

        recurringTransaction.SetStatus(RecurringTransactionStatus.INACTIVE);
        recurringTransactionRepository.save(recurringTransaction);

        m_logger.info("Stopped recurring transaction " + recurringTransaction.GetId());
    }

    /**
     * Process the recurring transactions
     * This method checks if the next due date of the recurring transactions has
     * already passed and generates the missing transactions
     */
    @Transactional
    public void ProcessRecurringTransactions()
    {
        List<RecurringTransaction> activeRecurringTransactions =
            recurringTransactionRepository.findByStatus(
                RecurringTransactionStatus.ACTIVE);

        LocalDateTime today = LocalDateTime.now();

        for (RecurringTransaction recurring : activeRecurringTransactions)
        {
            LocalDateTime nextDueDate = recurring.GetNextDueDate();

            // Check if the next due date has already passed and generate the missing
            // transactions
            if (!nextDueDate.isAfter(today) && !recurring.GetEndDate().isBefore(today))
            {
                while (!nextDueDate.isAfter(today))
                {
                    CreateTransactionForDate(recurring, nextDueDate);

                    nextDueDate =
                        CalculateNextDueDate(nextDueDate, recurring.GetFrequency());
                }

                // Update the next due date in the recurring transaction
                recurring.SetNextDueDate(nextDueDate);
                recurringTransactionRepository.save(recurring);
            }

            // Check if the recurring transaction has ended
            if (recurring.GetEndDate().isBefore(today))
            {
                recurring.SetStatus(RecurringTransactionStatus.INACTIVE);
                recurringTransactionRepository.save(recurring);
            }
        }
    }

    /**
     * Create a wallet transaction for a recurring transaction
     * @param recurring The recurring transaction
     * @param dueDate The due date of the transaction
     */
    private void CreateTransactionForDate(RecurringTransaction recurring,
                                          LocalDateTime        dueDate)
    {
        try
        {
            if (recurring.GetType().equals(TransactionType.INCOME))
            {
                walletTransactionService.AddIncome(recurring.GetWallet().GetId(),
                                                   recurring.GetCategory(),
                                                   dueDate,
                                                   recurring.GetAmount(),
                                                   recurring.GetDescription(),
                                                   TransactionStatus.PENDING);
            }
            else if (recurring.GetType().equals(TransactionType.EXPENSE))
            {
                walletTransactionService.AddExpense(recurring.GetWallet().GetId(),
                                                    recurring.GetCategory(),
                                                    dueDate,
                                                    recurring.GetAmount(),
                                                    recurring.GetDescription(),
                                                    TransactionStatus.PENDING);
            }
            else
            {
                throw new RuntimeException("Invalid transaction type");
            }
        }
        catch (RuntimeException e)
        {
            m_logger.warning("Failed to create transaction for recurring transaction " +
                             recurring.GetId() + ": " + e.getMessage());
        }
    }

    /**
     * Calculate the next due date of a recurring transaction
     * @param currentDueDate The current due date
     * @param frequency The frequency of the recurring transaction
     * @return The next due date with the time set to 23:59
     */
    private LocalDateTime CalculateNextDueDate(LocalDateTime currentDueDate,
                                               RecurringTransactionFrequency frequency)
    {
        LocalDateTime nextDueDate;

        switch (frequency)
        {
            case DAILY:
                nextDueDate = currentDueDate.plusDays(1);
                break;
            case WEEKLY:
                nextDueDate = currentDueDate.plusWeeks(1);
                break;
            case MONTHLY:
                nextDueDate = currentDueDate.plusMonths(1);
                break;
            case YEARLY:
                nextDueDate = currentDueDate.plusYears(1);
                break;
            default:
                throw new RuntimeException("Invalid frequency");
        }

        // Set the time to 23:59
        return nextDueDate.withHour(23).withMinute(59).withSecond(0).withNano(0);
    }

    /**
     * Get the date of the last transaction that will be generated
     * @param startDate The start date
     * @param endDate The end date
     * @param frequency The frequency of the recurring transaction
     * @return The date of the last transaction
     * @throws RuntimeException If the frequency is invalid
     */
    public LocalDate GetLastTransactionDate(LocalDate                     startDate,
                                            LocalDate                     endDate,
                                            RecurringTransactionFrequency frequency)
    {
        CheckDateAndIntervalIsValid(startDate, endDate, frequency);

        Long interval = 0L;

        switch (frequency)
        {
            case DAILY:
                interval = ChronoUnit.DAYS.between(startDate, endDate);
                break;
            case WEEKLY:
                interval = ChronoUnit.DAYS.between(startDate, endDate) / 7;
                break;
            case MONTHLY:
                interval = ChronoUnit.MONTHS.between(startDate, endDate);
                break;
            case YEARLY:
                interval = ChronoUnit.YEARS.between(startDate, endDate);
                break;
        }

        LocalDate lastTransactionDate =
            AddFrequencyToDate(startDate, interval, frequency);

        if (lastTransactionDate.isAfter(endDate))
        {
            interval--;
            lastTransactionDate = AddFrequencyToDate(startDate, interval, frequency);
        }

        return lastTransactionDate;
    }

    /**
     * Add the frequency to a date
     * @param date The date
     * @param interval The interval
     * @param frequency The frequency
     * @return The new date
     * @throws RuntimeException If the frequency is invalid
     */
    private LocalDate AddFrequencyToDate(LocalDate                     date,
                                         long                          interval,
                                         RecurringTransactionFrequency frequency)
    {
        switch (frequency)
        {
            case DAILY:
                return date.plusDays(interval);
            case WEEKLY:
                return date.plusWeeks(interval);
            case MONTHLY:
                return date.plusMonths(interval);
            case YEARLY:
                return date.plusYears(interval);
            default:
                throw new RuntimeException("Invalid frequency");
        }
    }

    /**
     * Get all recurring transactions
     * @return List of recurring transactions
     */
    public List<RecurringTransaction> GetAllRecurringTransactions()
    {
        return recurringTransactionRepository.findAll();
    }
}
