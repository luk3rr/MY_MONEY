/*
 * Filename: CreditCardPayment.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app.entities;

import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents a credit card payment
 * A credit card payment is a payment made to a credit card debt
 */
@Entity
@Table(name = "credit_card_payment")
public class CreditCardPayment
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_id")
    private Long m_id;

    @ManyToOne
    @JoinColumn(name = "wallet", referencedColumnName = "name", nullable = true)
    private Wallet m_wallet;

    @ManyToOne
    @JoinColumn(name = "debt_id", referencedColumnName = "debt_id")
    private CreditCardDebt m_creditCardDebt;

    @Column(name = "date", nullable = false)
    private LocalDate m_date;

    @Column(name = "amount", nullable = false)
    private Double m_amount;

    @Column(name = "installment", nullable = false)
    private Short m_installment;

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
                             LocalDate      date,
                             Double         amount,
                             Short          installment)
    {
        m_wallet         = wallet;
        m_creditCardDebt = debt;
        m_date           = date;
        m_amount         = amount;
        m_installment    = installment;
    }

    /**
     * Constructor for CreditCardPayment
     * @param debt The credit card debt that was paid
     * @param date The date of the payment
     * @param amount The amount paid
     * @param installment The installment of the payment
     */
    public CreditCardPayment(CreditCardDebt debt,
                             LocalDate      date,
                             Double         amount,
                             Short          installment)
    {
        m_creditCardDebt = debt;
        m_date           = date;
        m_amount         = amount;
        m_installment    = installment;
    }

    /**
     * Get the payment id
     * @return The payment id
     */
    public Long GetId()
    {
        return m_id;
    }

    /**
     * Get the wallet that made the payment
     * @return The wallet that made the payment
     */
    public Wallet GetWallet()
    {
        return m_wallet;
    }

    /**
     * Get the credit card debt that was paid
     * @return The credit card debt that was paid
     */
    public CreditCardDebt GetCreditCardDebt()
    {
        return m_creditCardDebt;
    }

    /**
     * Get the date of the payment
     * @return The date of the payment
     */
    public LocalDate GetDate()
    {
        return m_date;
    }

    /**
     * Get the amount paid
     * @return The amount paid
     */
    public Double GetAmount()
    {
        return m_amount;
    }

    /**
     * Get the installment of the payment
     * @return The installment of the payment
     */
    public Short GetInstallment()
    {
        return m_installment;
    }

    /**
     * Set the wallet that made the payment
     * @param wallet The wallet that made the payment
     */
    public void SetWallet(Wallet wallet)
    {
        m_wallet = wallet;
    }

    /**
     * Set the credit card debt that was paid
     * @param debtId The credit card debt that was paid
     */
    public void SetCreditCardDebt(CreditCardDebt debtId)
    {
        m_creditCardDebt = debtId;
    }

    /**
     * Set the date of the payment
     * @param date The date of the payment
     */
    public void SetDate(LocalDate date)
    {
        m_date = date;
    }

    /**
     * Set the amount paid
     * @param amount The amount paid
     */
    public void SetAmount(Double amount)
    {
        m_amount = amount;
    }

    /**
     * Set the installment of the payment
     * @param installment The installment of the payment
     */
    public void SetInstallment(Short installment)
    {
        m_installment = installment;
    }
}
