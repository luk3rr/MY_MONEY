/*
 * Filename: CreditCardPayment.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

import com.mymoney.util.Constants;

/**
 * Represents a credit card payment
 * A credit card payment is a payment made to a credit card debt
 */
@Entity
@Table(name = "credit_card_payment")
public class CreditCardPayment
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = true)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "debt_id", referencedColumnName = "id", nullable = false)
    private CreditCardDebt creditCardDebt;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "installment", nullable = false)
    private Integer installment;

    /**
     * Default constructor for JPA
     */
    public CreditCardPayment() { }

    /**
     * Constructor for CreditCardPayment
     * @param wallet The wallet that made the payment
     * @param debt The credit card debt that was paid
     * @param date The date of the payment
     * @param amount The amount paid
     * @param installment The installment of the payment
     */
    public CreditCardPayment(Wallet         wallet,
                             CreditCardDebt debt,
                             LocalDateTime      date,
                             Double         amount,
                             Integer        installment)
    {
        this.wallet         = wallet;
        this.creditCardDebt = debt;
        this.date           = date.format(Constants.DATE_TIME_FORMATTER);
        this.amount         = amount;
        this.installment    = installment;
    }

    /**
     * Constructor for CreditCardPayment
     * @param debt The credit card debt that was paid
     * @param date The date of the payment
     * @param amount The amount paid
     * @param installment The installment of the payment
     */
    public CreditCardPayment(CreditCardDebt debt,
                             LocalDateTime      date,
                             Double         amount,
                             Integer        installment)
    {
        this.creditCardDebt = debt;
        this.date           = date.format(Constants.DATE_TIME_FORMATTER);
        this.amount         = amount;
        this.installment    = installment;
    }

    /**
     * Get the payment id
     * @return The payment id
     */
    public Long GetId()
    {
        return id;
    }

    /**
     * Get the wallet that made the payment
     * @return The wallet that made the payment
     */
    public Wallet GetWallet()
    {
        return wallet;
    }

    /**
     * Get the credit card debt that was paid
     * @return The credit card debt that was paid
     */
    public CreditCardDebt GetCreditCardDebt()
    {
        return creditCardDebt;
    }

    /**
     * Get the date of the payment
     * @return The date of the payment
     */
    public LocalDateTime GetDate()
    {
        return LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER);
    }

    /**
     * Get the amount paid
     * @return The amount paid
     */
    public Double GetAmount()
    {
        return amount;
    }

    /**
     * Get the installment of the payment
     * @return The installment of the payment
     */
    public Integer GetInstallment()
    {
        return installment;
    }

    /**
     * Set the wallet that made the payment
     * @param wallet The wallet that made the payment
     */
    public void SetWallet(Wallet wallet)
    {
        this.wallet = wallet;
    }

    /**
     * Set the credit card debt that was paid
     * @param debtId The credit card debt that was paid
     */
    public void SetCreditCardDebt(CreditCardDebt debtId)
    {
        creditCardDebt = debtId;
    }

    /**
     * Set the date of the payment
     * @param date The date of the payment
     */
    public void SetDate(LocalDateTime date)
    {
        this.date = date.format(Constants.DATE_TIME_FORMATTER);
    }

    /**
     * Set the amount paid
     * @param amount The amount paid
     */
    public void SetAmount(Double amount)
    {
        this.amount = amount;
    }

    /**
     * Set the installment of the payment
     * @param installment The installment of the payment
     */
    public void SetInstallment(Integer installment)
    {
        this.installment = installment;
    }
}
