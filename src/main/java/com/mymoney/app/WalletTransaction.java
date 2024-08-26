/*
 * Filename: WalletTransaction.java
 * Created on: August 25, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

// Enum for the transaction type
enum TransactionType
{
    INCOME,
    OUTCOME
}

@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transaction_id")
    private Long m_id;

    @ManyToOne
    @JoinColumn(name = "wallet", referencedColumnName = "name")
    private Wallet m_wallet;

    @ManyToOne
    @JoinColumn(name = "category", referencedColumnName = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType m_type;

    @Column(name = "date", nullable = false)
    private String m_date;

    @Column(name = "amount", nullable = false)
    private Double m_amount;

    @Column(name = "description")
    private String m_description;

    // Default constructor for JPA
    public WalletTransaction() { }

    public WalletTransaction(Wallet   wallet,
                             Category category,
                             String   date,
                             String   description,
                             Double   amount)
    {
        this.m_wallet      = wallet;
        this.category      = category;
        this.m_date        = date;
        this.m_description = description;
        this.m_amount      = amount;
    }

    // Getters and Setters
    public Long GetId()
    {
        return m_id;
    }

    public Wallet GetWallet()
    {
        return m_wallet;
    }

    public Category GetCategory()
    {
        return category;
    }

    public String GetDate()
    {
        return m_date;
    }

    public String GetDescription()
    {
        return m_description;
    }

    public Double GetAmount()
    {
        return m_amount;
    }

    public void SetWallet(Wallet wallet)
    {
        this.m_wallet = wallet;
    }

    public void SetCategory(Category category)
    {
        this.category = category;
    }

    public void SetDate(String date)
    {
        this.m_date = date;
    }

    public void SetDescription(String description)
    {
        this.m_description = description;
    }

    public void SetAmount(Double amount)
    {
        this.m_amount = amount;
    }
}
