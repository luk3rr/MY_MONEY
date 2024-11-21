/*
 * Filename: BaseTransaction.java
 * Created on: November 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import java.math.BigDecimal;
import org.moinex.util.TransactionType;

/**
 * Base class for transactions
 */
@MappedSuperclass
public abstract class BaseTransaction
{
    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "amount", nullable = false, scale = 2)
    private BigDecimal amount;

    @Column(name = "description", nullable = true)
    private String description;

    /**
     * Default constructor for JPA
     */
    public BaseTransaction() { }

    /**
     * Constructor for BaseTransaction
     * @param wallet The wallet that the transaction belongs to
     * @param category The category of the transaction
     * @param type The type of the transaction
     * @param amount The amount of the transaction
     * @param description A description of the transaction
     */
    public BaseTransaction(Wallet            wallet,
                           Category          category,
                           TransactionType   type,
                           BigDecimal        amount,
                           String            description)
    {
        this.wallet      = wallet;
        this.category    = category;
        this.type        = type;
        this.amount      = amount;
        this.description = description;
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
    public BigDecimal GetAmount()
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
    public void SetAmount(BigDecimal amount)
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
}
