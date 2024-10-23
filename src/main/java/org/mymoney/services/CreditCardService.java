/*
 * Filename: CreditCardService.java
 * Created on: September  4, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.mymoney.entities.Category;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.CreditCardDebt;
import org.mymoney.entities.CreditCardPayment;
import org.mymoney.repositories.CategoryRepository;
import org.mymoney.repositories.CreditCardDebtRepository;
import org.mymoney.repositories.CreditCardPaymentRepository;
import org.mymoney.repositories.CreditCardRepository;
import org.mymoney.util.Constants;
import org.mymoney.util.CreditCardInvoiceStatus;
import org.mymoney.util.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for the business logic Credit Card entities
 */
@Service
public class CreditCardService
{
    @Autowired
    private CreditCardDebtRepository m_creditCardDebtRepository;

    @Autowired
    private CreditCardPaymentRepository m_creditCardPaymentRepository;

    @Autowired
    private CreditCardRepository m_creditCardRepository;

    @Autowired
    private CategoryRepository m_categoryRepository;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public CreditCardService() { }

    /**
     * Creates a new credit card
     * @param name The name of the credit card
     * @param billingDueDay The day of the month the credit card bill is due
     * @param closingDay The day of the month the credit card bill is closed
     * @param maxDebt The maximum debt of the credit card
     * @throws RuntimeException If the credit card name is already in use
     * @throws RuntimeException If the billingDueDay is not in the range [1,
     *     Constants.MAX_BILLING_DUE_DAY]
     * @throws RuntimeException If the maxDebt is negative
     * @return The id of the created credit card
     */
    @Transactional
    public Long
    CreateCreditCard(String name, Integer dueDate, Integer closingDay, Double maxDebt)
    {
        // Remove leading and trailing whitespaces
        name = name.strip();

        if (name.isBlank())
        {
            throw new RuntimeException("Credit card name cannot be empty");
        }

        if (m_creditCardRepository.existsByName(name))
        {
            throw new RuntimeException("Credit card with name " + name +
                                       " already exists");
        }

        if (dueDate < 1 || dueDate > Constants.MAX_BILLING_DUE_DAY)
        {
            throw new RuntimeException("Billing due day must be in the range [1, " +
                                       Constants.MAX_BILLING_DUE_DAY + "]");
        }

        if (closingDay < 1 || closingDay > Constants.MAX_BILLING_DUE_DAY)
        {
            throw new RuntimeException("Closing day must be in the range [1, " +
                                       Constants.MAX_BILLING_DUE_DAY + "]");
        }

        if (maxDebt < 0)
        {
            throw new RuntimeException("Max debt must be non-negative");
        }

        CreditCard newCreditCard = m_creditCardRepository.save(
            new CreditCard(name, dueDate, closingDay, maxDebt));

        m_logger.info("Credit card " + name + " created with due date " + dueDate +
                      " and max debt " + maxDebt);

        return newCreditCard.GetId();
    }

    /**
     * Delete a credit card
     * @param id The id of the credit card
     * @throws RuntimeException If the credit card does not exist
     */
    @Transactional
    public void DeleteCreditCard(Long id)
    {
        CreditCard creditCard = m_creditCardRepository.findById(id).orElseThrow(
            ()
                -> new RuntimeException("Credit card with id " + id +
                                        " does not exist"));

        m_creditCardRepository.delete(creditCard);

        m_logger.info("Credit card with id " + id + " deleted");
    }

    /**
     * Get available credit of a credit card
     * @param id The id of the credit card
     * @return The available credit of the credit card
     * @throws RuntimeException If the credit card does not exist
     */
    public Double GetAvailableCredit(Long id)
    {
        CreditCard creditCard = m_creditCardRepository.findById(id).orElseThrow(
            ()
                -> new RuntimeException("Credit card with id " + id +
                                        " does not exist"));

        Double totalDebt = m_creditCardDebtRepository.GetTotalDebt(id);
        Double totalPaid = m_creditCardPaymentRepository.GetTotalPaidAmount(id);

        return creditCard.GetMaxDebt() - totalDebt + totalPaid;
    }

    /**
     * Register a debt on the credit card and its respective future payment
     * @param creditCardName The name of the credit card
     * @param category The category of the debt
     * @param date The date of the debt
     * @param value The value of the debt
     * @param installment The number of installments of the debt
     * @param description The description of the debt
     * @throws RuntimeException If the credit card does not exist
     * @throws RuntimeException If the category does not exist
     * @throws RuntimeException If the value is negative
     * @throws RuntimeException If the installment is not in range [1,
     *     Constants.MAX_INSTALLMENTS]
     * @throws RuntimeException If the credit card does not have enough credit
     */
    @Transactional
    public void RegisterDebt(Long          crcId,
                             Category      category,
                             LocalDateTime date,
                             Double        value,
                             Integer       installment,
                             String        description)
    {
        CreditCard creditCard = m_creditCardRepository.findById(crcId).orElseThrow(
            ()
                -> new RuntimeException("Credit card with id " + crcId +
                                        " does not exist"));

        Category cat =
            m_categoryRepository.findById(category.GetId())
                .orElseThrow(()
                                 -> new RuntimeException("Category with name " +
                                                         category + " does not exist"));

        if (value < 0)
        {
            throw new RuntimeException("Value must be non-negative");
        }

        if (installment < 1 || installment > Constants.MAX_INSTALLMENTS)
        {
            throw new RuntimeException("Installment must be in the range [1, " +
                                       Constants.MAX_INSTALLMENTS + "]");
        }

        Double availableCredit = GetAvailableCredit(crcId);

        if (value > availableCredit)
        {
            throw new RuntimeException(
                "Credit card with id " + crcId +
                " does not have enough credit to register debt");
        }

        CreditCardDebt debt =
            new CreditCardDebt(creditCard, cat, date, value, description);

        m_creditCardDebtRepository.save(debt);

        m_logger.info("Debit registered on credit card with id " + crcId +
                      " with value " + value + " and description " + description);

        Double installmentValue = value / installment;

        for (Integer i = 1; i <= installment; i++)
        {
            LocalDateTime paymentDate =
                date.plusMonths(i).withDayOfMonth(creditCard.GetBillingDueDay());

            CreditCardPayment payment =
                new CreditCardPayment(debt, paymentDate, installmentValue, i);

            m_creditCardPaymentRepository.save(payment);

            m_logger.info("Payment of debt " + description +
                          " on credit card with id " + crcId +
                          " registered with value " + installmentValue +
                          " and due date " + paymentDate);
        }
    }

    /**
     * Get all credit cards
     * @return A list with all credit cards
     */
    public List<CreditCard> GetAllCreditCards()
    {
        return m_creditCardRepository.findAll();
    }

    /**
     * Get all credit cards ordered by name
     * @return A list with all credit cards ordered by name
     */
    public List<CreditCard> GetAllCreditCardsOrderedByName()
    {
        return m_creditCardRepository.findAllByOrderByNameAsc();
    }

    /**
     * Get credit card payments in a month and year
     * @param month The month
     * @param year The year
     * @return A list with all credit card payments in a month and year
     */
    public List<CreditCardPayment> GetCreditCardPayments(Integer month, Integer year)
    {
        return m_creditCardPaymentRepository.GetCreditCardPayments(month, year);
    }

    /**
     * Get the total debt amount of all credit cards in a month and year
     * @param month The month
     * @param year The year
     * @return The total debt amount of all credit cards in a month and year
     */
    public Double GetTotalDebtAmount(Integer month, Integer year)
    {
        return m_creditCardPaymentRepository.GetTotalDebtAmount(month, year);
    }

    /**
     * Get the total debt amount of all credit cards in a year
     * @param year The year
     * @return The total debt amount of all credit cards in a year
     */
    public Double GetTotalDebtAmount(Integer year)
    {
        return m_creditCardPaymentRepository.GetTotalDebtAmount(year);
    }

    /**
     * Get the total of all pending payments of all credit cards from a specified month
     * and year onward, including future months and the current month
     * @param month The starting month (inclusive)
     * @param year The starting year (inclusive)
     * @return The total of all pending payments of all credit cards from the specified
     *     month and year onward
     */
    public Double GetTotalPendingPayments(Integer month, Integer year)
    {
        return m_creditCardPaymentRepository.GetTotalPendingPayments(month, year);
    }

    /**
     * Get the total of all pending payments of all credit cards from a specified year
     * onward, including future years and the current year
     * @param year The starting year (inclusive)
     * @return The total of all pending payments of all credit cards from the specified
     *    year onward
     */
    public Double GetTotalPendingPayments(Integer year)
    {
        return m_creditCardPaymentRepository.GetTotalPendingPayments(year);
    }

    /**
     * Get the total of all pending payments of a credit card
     * @return The total of all pending payments of all credit cards
     */
    public Double GetTotalPendingPayments(Long crcId)
    {
        return m_creditCardPaymentRepository.GetTotalPendingPayments(crcId);
    }

    /**
     * Get the total of all pending payments of all credit cards
     * @return The total of all pending payments of all credit cards
     */
    public Double GetTotalPendingPayments()
    {
        return m_creditCardPaymentRepository.GetTotalPendingPayments();
    }

    /**
     * Get the invoice amount of a credit card in a specified month and year
     * @param creditCardId The credit card id
     * @param month The month
     * @param year The year
     * @return The invoice amount of the credit card in the specified month and year
     */
    public Double GetInvoiceAmount(Long crcId, Integer month, Integer year)
    {
        return m_creditCardPaymentRepository.GetInvoiceAmount(crcId, month, year);
    }

    /**
     * Get the invoice status of a credit card in a specified month and year
     * The invoice status can be either 'Open' or 'Closed'
     * @param creditCardId The credit card id
     * @param month The month
     * @param year The year
     * @return The invoice status of the credit card in the specified month and year
     * @throws RuntimeException If the credit card does not exist
     */
    public CreditCardInvoiceStatus
    GetInvoiceStatus(Long crcId, Integer month, Integer year)
    {
        LocalDateTime nextInvoiceDate = GetNextInvoiceDate(crcId);
        nextInvoiceDate               = nextInvoiceDate.withHour(0).withMinute(0);

        LocalDateTime dateToCompare =
            LocalDateTime.of(year, month, nextInvoiceDate.getDayOfMonth(), 23, 59);

        if (dateToCompare.isAfter(nextInvoiceDate) ||
            dateToCompare.isEqual(nextInvoiceDate))
        {
            return CreditCardInvoiceStatus.OPEN;
        }

        return CreditCardInvoiceStatus.CLOSED;
    }

    /**
     * Get next invoice date of a credit card
     * @param crcId The id of the credit card
     * @return The next invoice date of the credit card
     * @throws RuntimeException If the credit card does not exist
     */
    public LocalDateTime GetNextInvoiceDate(Long crcId)
    {
        String nextInvoiceDate =
            m_creditCardPaymentRepository.GetNextInvoiceDate(crcId);

        // If there is no next invoice date, calculate it
        // If the current day is greater than the closing day, the next invoice date is
        // billingDueDay of the next month
        // Otherwise, the next invoice date is billingDueDay of the current month
        if (nextInvoiceDate == null)
        {
            LocalDateTime now = LocalDateTime.now();

            CreditCard creditCard = m_creditCardRepository.findById(crcId).orElseThrow(
                ()
                    -> new RuntimeException("Credit card with id " + crcId +
                                            " does not exist"));

            Integer currentDay = now.getDayOfMonth();
            Integer closingDay = creditCard.GetClosingDay();

            if (currentDay > closingDay)
            {
                return now.plusMonths(1).withDayOfMonth(creditCard.GetBillingDueDay());
            }

            return now.withDayOfMonth(creditCard.GetBillingDueDay());
        }

        return LocalDateTime.parse(nextInvoiceDate, Constants.DB_DATE_FORMATTER);
    }

    /**
     * Get the date of the oldest debt
     * @return The date of the oldest debt or the current date if there are no debts
     */
    public LocalDateTime GetOldestDebtDate()
    {
        String date = m_creditCardDebtRepository.GetOldestDebtDate();

        if (date == null)
        {
            return LocalDateTime.now();
        }

        return LocalDateTime.parse(date, Constants.DB_DATE_FORMATTER);
    }

    /**
     * Get the date of the newest debt
     * @return The date of the newest debt or the current date if there are no debts
     */
    public LocalDateTime GetNewestDebtDate()
    {
        String date = m_creditCardDebtRepository.GetNewestDebtDate();

        if (date == null)
        {
            return LocalDateTime.now();
        }

        return LocalDateTime.parse(date, Constants.DB_DATE_FORMATTER);
    }
}
