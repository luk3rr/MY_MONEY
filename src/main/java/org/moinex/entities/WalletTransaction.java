/*
 * Filename: WalletTransaction.java
 * Created on: August 25, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.moinex.util.Constants;
import org.moinex.util.TransactionStatus;
import org.moinex.util.TransactionType;

/**
 * Represents a transaction in a wallet
 */
@Entity
@Table(name = "wallet_transaction")
public class WalletTransaction extends BaseTransaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "date", nullable = false)
    private String date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    /**
     * Default constructor for JPA
     */
    public WalletTransaction() { }

    /**
     * Constructor for WalletTransaction
     * @param wallet The wallet that the transaction belongs to
     * @param category The category of the transaction
     * @param type The type of the transaction
     * @param status The status of the transaction
     * @param date The date of the transaction
     * @param amount The amount of the transaction
     * @param description A description of the transaction
     */
    public WalletTransaction(Wallet            wallet,
                             Category          category,
                             TransactionType   type,
                             TransactionStatus status,
                             LocalDateTime     date,
                             BigDecimal        amount,
                             String            description)
    {
        super(wallet, category, type, amount, description);

        this.date   = date.format(Constants.DB_DATE_FORMATTER);
        this.status = status;
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
     * Get the type of the transaction
     * @return The type of the transaction
     */
    public LocalDateTime GetDate()
    {
        return LocalDateTime.parse(date, Constants.DB_DATE_FORMATTER);
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
     * Set the type of the transaction
     * @param type The type of the transaction
     */
    public void SetDate(LocalDateTime date)
    {
        this.date = date.format(Constants.DB_DATE_FORMATTER);
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
