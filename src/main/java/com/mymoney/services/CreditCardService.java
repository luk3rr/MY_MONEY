/*
 * Filename: CreditCardService.java
 * Created on: September  4, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.services;

import com.mymoney.app.entities.Category;
import com.mymoney.app.entities.CreditCard;
import com.mymoney.app.entities.CreditCardDebt;
import com.mymoney.app.entities.CreditCardPayment;
import com.mymoney.app.entities.Wallet;
import com.mymoney.repositories.CategoryRepository;
import com.mymoney.repositories.CreditCardDebtRepository;
import com.mymoney.repositories.CreditCardPaymentRepository;
import com.mymoney.repositories.CreditCardRepository;
import com.mymoney.repositories.WalletRepository;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import com.mymoney.util.TransactionType;
import java.time.LocalDate;
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
     */
    @Transactional
    public void CreateCreditCard(String name, Short dueDate, double maxDebt)
    {
        if (m_creditCardRepository.existsById(name))
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

        m_creditCardRepository.save(new CreditCard(name, dueDate, maxDebt));

        m_logger.info("Credit card " + name + " created with due date " + dueDate +
                      " and max debt " + maxDebt);
    }

    /**
     * Delete a credit card
     * @param name The name of the credit card to be deleted
     * @throws RuntimeException If the credit card does not exist
     */
    @Transactional
    public void DeleteCreditCard(String name)
    {
        CreditCard creditCard = m_creditCardRepository.findById(name).orElseThrow(
            ()
                -> new RuntimeException("Credit card with name " + name +
                                        " does not exist"));

        m_creditCardRepository.delete(creditCard);

        m_logger.info("Credit card " + name + " deleted");
    }

    /**
     * Get available credit of a credit card
     * @param name The name of the credit card
     * @return The available credit of the credit card
     * @throws RuntimeException If the credit card does not exist
     */
    public double GetAvailableCredit(String name)
    {
        CreditCard creditCard = m_creditCardRepository.findById(name).orElseThrow(
            ()
                -> new RuntimeException("Credit card with name " + name +
                                        " does not exist"));

        double totalDebt = m_creditCardDebtRepository.GetTotalDebt(name);
        double totalPaid = m_creditCardPaymentRepository.GetTotalPaidAmount(name);

        return creditCard.GetMaxDebt() - totalDebt + totalPaid;
    }

    /**
     * Register a debit on the credit card and its respective future payment
     * @param creditCardName The name of the credit card
     * @param category The category of the debit
     * @param date The date of the debit
     * @param value The value of the debit
     * @param installment The number of installments of the debit
     * @param description The description of the debit
     * @throws RuntimeException If the credit card does not exist
     * @throws RuntimeException If the category does not exist
     * @throws RuntimeException If the value is negative
     * @throws RuntimeException If the installment is not in range [1,
     *     Constants.MAX_INSTALLMENTS]
     * @throws RuntimeException If the credit card does not have enough credit
     */
    @Transactional
    public void RegisterDebit(String    creditCardName,
                              Category  category,
                              LocalDate date,
                              double    value,
                              int       installment,
                              String    description)
    {
        CreditCard creditCard =
            m_creditCardRepository.findById(creditCardName)
                .orElseThrow(()
                                 -> new RuntimeException("Credit card with name " +
                                                         creditCardName +
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

        double availableCredit = GetAvailableCredit(creditCardName);

        if (value > availableCredit)
        {
            throw new RuntimeException(
                "Credit card " + creditCardName +
                " does not have enough credit to register debit");
        }

        CreditCardDebt debt =
            new CreditCardDebt(creditCard, cat, date, value, description);

        m_creditCardDebtRepository.save(debt);

        m_logger.info("Debit registered on credit card " + creditCardName +
                      " with value " + value + " and description " + description);

        double installmentValue = value / installment;

        for (Short i = 1; i <= installment; i++)
        {
            LocalDate paymentDate =
                date.plusMonths(i).withDayOfMonth(creditCard.GetBillingDueDay());

            CreditCardPayment payment =
                new CreditCardPayment(debt, paymentDate, installmentValue, i);

            m_creditCardPaymentRepository.save(payment);

            m_logger.info("Payment of debit " + description + " on credit card " +
                          creditCardName + " registered with value " +
                          installmentValue + " and due date " + paymentDate);
        }
    }
}