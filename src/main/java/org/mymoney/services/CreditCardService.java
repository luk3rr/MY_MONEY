/*
 * Filename: CreditCardService.java
 * Created on: September  4, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.services;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.logging.Logger;
import org.mymoney.entities.Category;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.CreditCardDebt;
import org.mymoney.entities.CreditCardOperator;
import org.mymoney.entities.CreditCardPayment;
import org.mymoney.repositories.CategoryRepository;
import org.mymoney.repositories.CreditCardDebtRepository;
import org.mymoney.repositories.CreditCardOperatorRepository;
import org.mymoney.repositories.CreditCardPaymentRepository;
import org.mymoney.repositories.CreditCardRepository;
import org.mymoney.repositories.WalletRepository;
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
    private CreditCardOperatorRepository m_creditCardOperatorRepository;

    @Autowired
    private WalletRepository m_walletRepository;

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
    public Long CreateCreditCard(String  name,
                                 Integer dueDate,
                                 Integer closingDay,
                                 Double  maxDebt,
                                 String  lastFourDigits,
                                 Long    operatorId)
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

        if (maxDebt <= 0)
        {
            throw new RuntimeException("Max debt must be positive");
        }

        if (lastFourDigits.isBlank() || lastFourDigits.length() != 4)
        {
            throw new RuntimeException("Last four digits must have length 4");
        }

        CreditCardOperator operator =
            m_creditCardOperatorRepository.findById(operatorId)
                .orElseThrow(
                    ()
                        -> new RuntimeException("Credit card operator with id " +
                                                operatorId + " does not exist"));

        CreditCard newCreditCard =
            m_creditCardRepository.save(new CreditCard(name,
                                                       dueDate,
                                                       closingDay,
                                                       maxDebt,
                                                       lastFourDigits,
                                                       operator));

        m_logger.info("Credit card " + name + " has created successfully");

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
     * @param registerDate The date the debt was registered
     * @param invoiceMonth The month of the invoice
     * @param value The value of the debt
     * @param installments The number of installments of the debt
     * @param description The description of the debt
     * @throws RuntimeException If the credit card does not exist
     * @throws RuntimeException If the category does not exist
     * @throws RuntimeException If the value is negative
     * @throws RuntimeException If the installments is not in range [1,
     *     Constants.MAX_INSTALLMENTS]
     * @throws RuntimeException If the credit card does not have enough credit
     */
    @Transactional
    public void RegisterDebt(Long          crcId,
                             Category      category,
                             LocalDateTime registerDate,
                             YearMonth     invoiceMonth,
                             Double        value,
                             Integer       installments,
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

        if (installments < 1 || installments > Constants.MAX_INSTALLMENTS)
        {
            throw new RuntimeException("Installment must be in the range [1, " +
                                       Constants.MAX_INSTALLMENTS + "]");
        }

        if (registerDate == null)
        {
            throw new RuntimeException("Register date cannot be null");
        }

        if (invoiceMonth == null)
        {
            throw new RuntimeException("Invoice month cannot be null");
        }

        Double availableCredit = GetAvailableCredit(crcId);

        if (value > availableCredit)
        {
            throw new RuntimeException(
                "Credit card with id " + crcId +
                " does not have enough credit to register debt");
        }

        CreditCardDebt debt = new CreditCardDebt(creditCard,
                                                 cat,
                                                 registerDate,
                                                 installments,
                                                 value,
                                                 description);

        m_creditCardDebtRepository.save(debt);

        m_logger.info("Debit registered on credit card with id " + crcId +
                      " with value " + value + " and description " + description);

        Double installmentValue = value / installments;

        for (Integer i = 0; i < installments; i++)
        {
            // Calculate the payment date
            LocalDateTime paymentDate = invoiceMonth.plusMonths(i)
                                            .atDay(creditCard.GetBillingDueDay())
                                            .atTime(23, 59);

            CreditCardPayment payment =
                new CreditCardPayment(debt, paymentDate, installmentValue, i + 1);

            m_creditCardPaymentRepository.save(payment);

            m_logger.info("Payment of debt " + description +
                          " on credit card with id " + crcId +
                          " registered with value " + installmentValue +
                          " and due date " + paymentDate);
        }
    }

    /**
     * Update the debt of a credit card
     * @param debt The debt to be updated
     * @param invoiceMonth The month of the invoice
     * @throws RuntimeException If the debt does not exist
     * @throws RuntimeException If the credit card does not exist
     * @throws RuntimeException If the total amount of the debt is less than or equal to
     *     zero
     */
    @Transactional
    public void UpdateCreditCardDebt(CreditCardDebt debt, YearMonth invoiceMonth)
    {
        CreditCardDebt oldDebt =
            m_creditCardDebtRepository.findById(debt.GetId())
                .orElseThrow(()
                                 -> new RuntimeException("Debt with id " +
                                                         debt.GetId() +
                                                         " does not exist"));

        m_creditCardRepository.findById(debt.GetCreditCard().GetId())
            .orElseThrow(()
                             -> new RuntimeException("Credit card with id " +
                                                     debt.GetCreditCard().GetId() +
                                                     " does not exist"));

        if (debt.GetTotalAmount() <= 0)
        {
            throw new RuntimeException("Total amount must be greater than zero");
        }

        // Complex update
        ChangeInvoiceMonth(oldDebt, invoiceMonth);
        ChangeDebtTotalAmount(oldDebt, debt.GetTotalAmount());
        ChangeDebtInstallments(oldDebt, debt.GetInstallments());

        // Trivial update
        oldDebt.SetCreditCard(debt.GetCreditCard());
        oldDebt.SetCategory(debt.GetCategory());
        oldDebt.SetDescription(debt.GetDescription());

        m_creditCardDebtRepository.save(oldDebt);

        m_logger.info("Debt with id " + debt.GetId() + " updated successfully");
    }

    /**
     * Update invoice month of a debt
     * @param debt The debt to be updated
     * @param invoiceMonth The new invoice month
     */
    private void ChangeInvoiceMonth(CreditCardDebt oldDebt, YearMonth invoice)
    {
        List<CreditCardPayment> payments = GetCreditCardPayments(oldDebt.GetId());

        CreditCardPayment firstPayment = payments.getFirst();

        // If the first payment is in the same month and year of the invoice, do not
        // update
        if (firstPayment == null ||
            (firstPayment.GetDate().getMonth() == invoice.getMonth() &&
             firstPayment.GetDate().getYear() == invoice.getYear()))
        {
            return;
        }

        for (Integer i = 0; i < oldDebt.GetInstallments(); i++)
        {
            CreditCardPayment payment = payments.get(i);

            // Calculate the payment date
            LocalDateTime paymentDate =
                invoice.plusMonths(i)
                    .atDay(oldDebt.GetCreditCard().GetBillingDueDay())
                    .atTime(23, 59);

            payment.SetDate(paymentDate);
            m_creditCardPaymentRepository.save(payment);

            m_logger.info("Payment number " + payment.GetInstallment() +
                          " of debt with id " + oldDebt.GetId() +
                          " on credit card with id " +
                          oldDebt.GetCreditCard().GetId() + " updated with due date " +
                          paymentDate);
        }
    }

    /**
     * Change the number of installments of a debt
     * @param debt The debt to be updated
     * @param newInstallments The new number of installments
     */
    private void ChangeDebtInstallments(CreditCardDebt oldDebt, Integer newInstallments)
    {
        if (oldDebt.GetInstallments() == newInstallments)
        {
            return;
        }

        List<CreditCardPayment> payments = GetCreditCardPayments(oldDebt.GetId());

        // New value for each installment
        Double installmentValue = oldDebt.GetTotalAmount() / newInstallments;

        // Delete and update payments
        if (newInstallments < oldDebt.GetInstallments())
        {
            for (Integer i = 0; i < oldDebt.GetInstallments(); i++)
            {
                CreditCardPayment payment = payments.get(i);

                // If the payment is greater than the new number of installments, delete
                // it
                if (payment.GetInstallment() > newInstallments)
                {
                    // If payment was made with a wallet, add the amount back to the
                    // wallet balance
                    if (payment.GetWallet() != null)
                    {
                        payment.GetWallet().SetBalance(
                            payment.GetWallet().GetBalance() + payment.GetAmount());

                        m_logger.info("Payment number " + payment.GetInstallment() +
                                      " of debt with id " + oldDebt.GetId() +
                                      " on credit card with id " +
                                      oldDebt.GetCreditCard().GetId() +
                                      " deleted and added to wallet with id " +
                                      payment.GetWallet().GetId());

                        m_walletRepository.save(payment.GetWallet());
                    }

                    m_creditCardPaymentRepository.delete(payment);

                    m_logger.info("Payment number " + payment.GetInstallment() +
                                  " of debt with id " + oldDebt.GetId() +
                                  " on credit card with id " +
                                  oldDebt.GetCreditCard().GetId() + " deleted");
                }
                // If the payment is less or equal than the new number of installments,
                // update it
                else
                {
                    payment.SetAmount(installmentValue);
                    m_creditCardPaymentRepository.save(payment);

                    m_logger.info("Payment number " + payment.GetInstallment() +
                                  " of debt with id " + oldDebt.GetId() +
                                  " on credit card with id " +
                                  oldDebt.GetCreditCard().GetId() +
                                  " updated with value " + installmentValue);
                }
            }
        }
        else // Insert and update payments
        {
            for (Integer i = 1; i <= newInstallments; i++)
            {
                if (i > oldDebt.GetInstallments())
                {
                    CreditCardPayment lastPayment = payments.getLast();

                    // Calculate the payment date
                    LocalDateTime paymentDate = lastPayment.GetDate().plusMonths(1);

                    CreditCardPayment payment = new CreditCardPayment(oldDebt,
                                                                      paymentDate,
                                                                      installmentValue,
                                                                      i);

                    m_creditCardPaymentRepository.save(payment);

                    m_logger.info("Payment number " + i + " of debt with id " +
                                  oldDebt.GetId() + " on credit card with id " +
                                  oldDebt.GetCreditCard().GetId() +
                                  " registered with value " + installmentValue +
                                  " and due date " + paymentDate);

                    // Add new payment to the list
                    payments.add(payment);
                }
                else
                {
                    CreditCardPayment payment = payments.get(i - 1);

                    payment.SetAmount(installmentValue);
                    m_creditCardPaymentRepository.save(payment);

                    m_logger.info("Payment number " + payment.GetInstallment() +
                                  " of debt with id " + oldDebt.GetId() +
                                  " on credit card with id " +
                                  oldDebt.GetCreditCard().GetId() +
                                  " updated with value " + installmentValue);
                }
            }
        }

        // Update the number of installments
        oldDebt.SetInstallments(newInstallments);
        m_creditCardDebtRepository.save(oldDebt);
    }

    /**
     * Change the total amount of a debt
     * @param oldDebt The debt to be updated
     * @param newAmount The new total amount
     */
    private void ChangeDebtTotalAmount(CreditCardDebt oldDebt, Double newAmount)
    {
        if (oldDebt.GetTotalAmount().equals(newAmount))
        {
            return;
        }

        List<CreditCardPayment> payments = GetCreditCardPayments(oldDebt.GetId());

        // New value for each installment
        Double installmentValue = newAmount / oldDebt.GetInstallments();

        // Update payments
        for (Integer i = 0; i < oldDebt.GetInstallments(); i++)
        {
            CreditCardPayment payment = payments.get(i);

            // If the payment was made with a wallet, add the amount difference back to
            // the wallet balance
            if (payment.GetWallet() != null)
            {
                Double difference = installmentValue - payment.GetAmount();

                payment.GetWallet().SetBalance(payment.GetWallet().GetBalance() +
                                               difference);

                m_logger.info("Payment number " + payment.GetInstallment() +
                              " of debt with id " + oldDebt.GetId() +
                              " on credit card with id " +
                              oldDebt.GetCreditCard().GetId() +
                              " updated and added to wallet with id " +
                              payment.GetWallet().GetId());

                m_walletRepository.save(payment.GetWallet());
            }

            payment.SetAmount(installmentValue);
            m_creditCardPaymentRepository.save(payment);

            m_logger.info("Payment number " + payment.GetInstallment() +
                          " of debt with id " + oldDebt.GetId() +
                          " on credit card with id " +
                          oldDebt.GetCreditCard().GetId() + " updated with value " +
                          installmentValue);
        }

        // Update the total amount
        oldDebt.SetTotalAmount(newAmount);
        m_creditCardDebtRepository.save(oldDebt);
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
     * Get all credit card operators ordered by name
     * @return A list with all credit card operators ordered by name
     */
    public List<CreditCardOperator> GetAllCreditCardOperatorsOrderedByName()
    {
        return m_creditCardOperatorRepository.findAllByOrderByNameAsc();
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
     * Get credit card payments by debt id
     * @param debtId The debt id
     * @return A list with all credit card payments by debt id
     */
    public List<CreditCardPayment> GetCreditCardPayments(Long crcId)
    {
        return m_creditCardPaymentRepository.GetCreditCardPayments(crcId);
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
     * Get the remaining debt of a purchase
     * @param debtId The id of the debt
     * @return The remaining debt of the purchase
     */
    public Double GetRemainingDebt(Long debtId)
    {
        return m_creditCardPaymentRepository.GetRemainingDebt(debtId);
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
     * Get the date of the latest payment
     * @return The date of the latest payment or the current date if there are no debts
     */
    public LocalDateTime GetEarliestPaymentDate()
    {
        String date = m_creditCardDebtRepository.FindEarliestPaymentDate();

        if (date == null)
        {
            return LocalDateTime.now();
        }

        return LocalDateTime.parse(date, Constants.DB_DATE_FORMATTER);
    }

    /**
     * Get the date of the latest payment
     * @return The date of the latest payment or the current date if there are no debts
     */
    public LocalDateTime GetLatestPaymentDate()
    {
        String date = m_creditCardDebtRepository.FindLatestPaymentDate();

        if (date == null)
        {
            return LocalDateTime.now();
        }

        return LocalDateTime.parse(date, Constants.DB_DATE_FORMATTER);
    }
}
