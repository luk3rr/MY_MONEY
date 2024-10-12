/*
 * Filename: WalletTransaction.java
 * Created on: August 25, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.entities;

import com.mymoney.util.Constants;
import com.mymoney.util.TransactionStatus;
import com.mymoney.util.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Represents a transaction in a wallet
 */
@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "description", nullable = true)
    private String description;

    /**
     * Default constructor for JPA
     */
    public WalletTransaction() { }

    /**
     * Constructor for WalletTransaction
     * @param wallet The wallet that the transaction belongs to
     * @param category The category of the transaction
     * @param type The type of the transaction
     * @param date The date of the transaction
     * @param amount The amount of the transaction
     * @param description A description of the transaction
     */
    public WalletTransaction(Wallet            wallet,
                             Category          category,
                             TransactionType   type,
                             TransactionStatus status,
                             LocalDateTime     date,
                             Double            amount,
                             String            description)
    {
        this.wallet      = wallet;
        this.category    = category;
        this.type        = type;
        this.status      = status;
        this.date        = date.format(Constants.DATE_TIME_FORMATTER_WITH_TIME);
        this.amount      = amount;
        this.description = description;
    }

    /**
     * Get the transaction id
     * @return The transaction id
     */
    public Long GetId()
    {
        return id;
    }

    /**
     * Get the wallet that the transaction belongs to
     * @return The wallet that the transaction belongs to
     */
    public Wallet GetWallet()
    {
        return wallet;
    }

    /**
     * Get the category of the transaction
     * @return The category of the transaction
     */
    public Category GetCategory()
    {
        return category;
    }

    /**
     * Get the type of the transaction
     * @return The type of the transaction
     */
    public LocalDateTime GetDate()
    {
        return LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER_WITH_TIME);
    }

    /**
     * Get the description of the transaction
     * @return The description of the transaction
     */
    public String GetDescription()
    {
        return description;
    }

    /**
     * Get the amount of the transaction
     * @return The amount of the transaction
     */
    public Double GetAmount()
    {
        return amount;
    }

    /**
     * Get the type of the transaction
     * @return The type of the transaction
     */
    public TransactionType GetType()
    {
        return type;
    }

    /**
     * Get the status of the transaction
     * @return The status of the transaction
     */
    public TransactionStatus GetStatus()
    {
        return status;
    }

    /**
     * Set the wallet that the transaction belongs to
     * @param wallet The wallet that the transaction belongs to
     */
    public void SetWallet(Wallet wallet)
    {
        this.wallet = wallet;
    }

    /**
     * Set the category of the transaction
     * @param category The category of the transaction
     */
    public void SetCategory(Category category)
    {
        this.category = category;
    }

    /**
     * Set the type of the transaction
     * @param type The type of the transaction
     */
    public void SetDate(LocalDateTime date)
    {
        this.date = date.format(Constants.DATE_TIME_FORMATTER_WITH_TIME);
    }

    /**
     * Set the description of the transaction
     * @param description The description of the transaction
     */
    public void SetDescription(String description)
    {
        this.description = description;
    }

    /**
     * Set the amount of the transaction
     * @param amount The amount of the transaction
     */
    public void SetAmount(Double amount)
    {
        this.amount = amount;
    }

    /**
     * Set the type of the transaction
     * @param type The type of the transaction
     */
    public void SetType(TransactionType type)
    {
        this.type = type;
    }

    /**
     * Set the status of the transaction
     * @param status The status of the transaction
     */
    public void SetStatus(TransactionStatus status)
    {
        this.status = status;
    }
}
