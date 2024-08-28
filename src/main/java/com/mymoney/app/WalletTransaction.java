/*
 * Filename: WalletTransaction.java
 * Created on: August 25, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import java.time.LocalDate;
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

/**
 * ENUM that represents the type of a transaction
 */
enum TransactionType
{
    INCOME,
    OUTCOME
}

/**
 * Represents a transaction in a wallet
 */
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
    @JoinColumn(name = "category_id", referencedColumnName = "category_id")
    private Category m_category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType m_type;

    @Column(name = "date", nullable = false)
    private LocalDate m_date;

    @Column(name = "amount", nullable = false)
    private Double m_amount;

    @Column(name = "description")
    private String m_description;

    /**
     * Default constructor for JPA
     */
    public WalletTransaction() { }

    /**
     * Constructor for WalletTransaction
     * @param wallet The wallet that the transaction belongs to
     * @param category The category of the transaction
     * @param date The date of the transaction
     * @param description A description of the transaction
     * @param amount The amount of the transaction
     */
    public WalletTransaction(Wallet    wallet,
                             Category  category,
                             LocalDate date,
                             String    description,
                             Double    amount)
    {
        m_wallet      = wallet;
        m_category    = category;
        m_date        = date;
        m_description = description;
        m_amount      = amount;
    }

    /**
     * Get the transaction id
     * @return The transaction id
     */
    public Long GetId()
    {
        return m_id;
    }

    /**
     * Get the wallet that the transaction belongs to
     * @return The wallet that the transaction belongs to
     */
    public Wallet GetWallet()
    {
        return m_wallet;
    }

    /**
     * Get the category of the transaction
     * @return The category of the transaction
     */
    public Category GetCategory()
    {
        return m_category;
    }

    /**
     * Get the type of the transaction
     * @return The type of the transaction
     */
    public LocalDate GetDate()
    {
        return m_date;
    }

    /**
     * Get the description of the transaction
     * @return The description of the transaction
     */
    public String GetDescription()
    {
        return m_description;
    }

    /**
     * Get the amount of the transaction
     * @return The amount of the transaction
     */
    public Double GetAmount()
    {
        return m_amount;
    }

    /**
     * Set the wallet that the transaction belongs to
     * @param wallet The wallet that the transaction belongs to
     */
    public void SetWallet(Wallet wallet)
    {
        m_wallet = wallet;
    }

    /**
     * Set the category of the transaction
     * @param category The category of the transaction
     */
    public void SetCategory(Category category)
    {
        m_category = category;
    }

    /**
     * Set the type of the transaction
     * @param type The type of the transaction
     */
    public void SetDate(LocalDate date)
    {
        m_date = date;
    }

    /**
     * Set the description of the transaction
     * @param description The description of the transaction
     */
    public void SetDescription(String description)
    {
        m_description = description;
    }

    /**
     * Set the amount of the transaction
     * @param amount The amount of the transaction
     */
    public void SetAmount(Double amount)
    {
        m_amount = amount;
    }
}
