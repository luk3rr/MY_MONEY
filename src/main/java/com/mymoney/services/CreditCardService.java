/*
 * Filename: CreditCardService.java
 * Created on: September  4, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.services;

import com.mymoney.entities.Category;
import com.mymoney.entities.CreditCard;
import com.mymoney.entities.CreditCardDebt;
import com.mymoney.entities.CreditCardPayment;
import com.mymoney.repositories.CategoryRepository;
import com.mymoney.repositories.CreditCardDebtRepository;
import com.mymoney.repositories.CreditCardPaymentRepository;
import com.mymoney.repositories.CreditCardRepository;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
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
     * @param maxDebt The maximum debt of the credit card
     * @throws RuntimeException If the credit card name is already in use
     * @throws RuntimeException If the billingDueDay is not in the range [1,
     *     Constants.MAX_BILLING_DUE_DAY]
     * @throws RuntimeException If the maxDebt is negative
     * @return The id of the created credit card
     */
    @Transactional
    public Long CreateCreditCard(String name, Integer dueDate, Double maxDebt)
    {
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

        if (maxDebt < 0)
        {
            throw new RuntimeException("Max debt must be non-negative");
        }

        CreditCard newCreditCard =
            m_creditCardRepository.save(new CreditCard(name, dueDate, maxDebt));

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
}
