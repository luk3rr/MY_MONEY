/*
 * Filename: CreditCardPayment.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "credit_card_payment")
public class CreditCardPayment
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "payment_id")
    private Long m_id;

    @ManyToOne
    @JoinColumn(name = "wallet", referencedColumnName = "wallet_id")
    private Wallet m_wallet;

    @ManyToOne
    @JoinColumn(name = "debt_id", referencedColumnName = "debt_id")
    private CreditCardDebt m_creditCardDebt;

    @Column(name = "date", nullable = false)
    private String m_date;

    @Column(name = "amount", nullable = false)
    private Double m_amount;

    @Column(name = "installment", nullable = false)
    private Short m_installment;

    // Default constructor for JPA
    public CreditCardPayment() { }

    public CreditCardPayment(Wallet         wallet,
                             CreditCardDebt debtId,
                             String         date,
                             Double         amount,
                             Short          installment)
    {
        this.m_wallet         = wallet;
        this.m_creditCardDebt = debtId;
        this.m_date           = date;
        this.m_amount         = amount;
        this.m_installment    = installment;
    }

    // Getters and Setters
    public Long GetId()
    {
        return this.m_id;
    }

    public Wallet GetWallet()
    {
        return this.m_wallet;
    }

    public CreditCardDebt GetCreditCardDebt()
    {
        return this.m_creditCardDebt;
    }

    public String GetDate()
    {
        return this.m_date;
    }

    public Double GetAmount()
    {
        return this.m_amount;
    }

    public Short GetInstallment()
    {
        return this.m_installment;
    }

    public void SetWallet(Wallet wallet)
    {
        this.m_wallet = wallet;
    }

    public void SetCreditCardDebt(CreditCardDebt debtId)
    {
        this.m_creditCardDebt = debtId;
    }

    public void SetDate(String date)
    {
        this.m_date = date;
    }

    public void SetAmount(Double amount)
    {
        this.m_amount = amount;
    }

    public void SetInstallment(Short installment)
    {
        this.m_installment = installment;
    }
}
