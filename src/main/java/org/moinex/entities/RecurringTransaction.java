/*
 * Filename: RecurringTransaction.java
 * Created on: November 10, 2024
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
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.moinex.util.Constants;
import org.moinex.util.RecurringTransactionFrequency;
import org.moinex.util.TransactionType;

@Entity
@Inheritance
@Table(name = "recurring_transaction")
public class RecurringTransaction extends BaseTransaction
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "start_date", nullable = false)
    private String startDate;

    @Column(name = "end_date", nullable = false)
    private String endDate;

    @Column(name = "next_due_date", nullable = false)
    private String nextDueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private RecurringTransactionFrequency frequency;

    /**
     * Default constructor for JPA
     */
    public RecurringTransaction() { }

    /**
     * Constructor for testing purposes
     * @param id The id of the transaction
     * @param wallet The wallet that the transaction belongs to
     * @param category The category of the transaction
     * @param type The type of the transaction
     * @param startDate The start date of the transaction
     * @param endDate The end date of the transaction
     * @param nextDueDate The next due date of the transaction
     * @param amount The amount of the transaction
     * @param description A description of the transaction
     */
    public RecurringTransaction(Long                          id,
                                Wallet                        wallet,
                                Category                      category,
                                TransactionType               type,
                                BigDecimal                    amount,
                                LocalDateTime                 startDate,
                                LocalDateTime                 endDate,
                                LocalDateTime                 nextDueDate,
                                RecurringTransactionFrequency frequency,
                                String                        description)
    {
        super(wallet, category, type, amount, description);

        this.id          = id;
        this.startDate   = startDate.format(Constants.DB_DATE_FORMATTER);
        this.endDate     = endDate.format(Constants.DB_DATE_FORMATTER);
        this.nextDueDate = nextDueDate.format(Constants.DB_DATE_FORMATTER);
        this.frequency   = frequency;
    }

    /**
     * Constructor for RecurringTransaction
     * @param wallet The wallet that the transaction belongs to
     * @param category The category of the transaction
     * @param type The type of the transaction
     * @param status The status of the transaction
     * @param startDate The start date of the transaction
     * @param endDate The end date of the transaction
     * @param nextDueDate The next due date of the transaction
     * @param amount The amount of the transaction
     * @param description A description of the transaction
     */
    public RecurringTransaction(Wallet                        wallet,
                                Category                      category,
                                TransactionType               type,
                                BigDecimal                    amount,
                                LocalDateTime                 startDate,
                                LocalDateTime                 endDate,
                                LocalDateTime                 nextDueDate,
                                RecurringTransactionFrequency frequency,
                                String                        description)
    {
        super(wallet, category, type, amount, description);

        this.startDate   = startDate.format(Constants.DB_DATE_FORMATTER);
        this.endDate     = endDate.format(Constants.DB_DATE_FORMATTER);
        this.nextDueDate = nextDueDate.format(Constants.DB_DATE_FORMATTER);
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
     * Get the start date of the transaction
     * @return The start date of the transaction
     */
    public LocalDateTime GetStartDate()
    {
        return LocalDateTime.parse(startDate, Constants.DB_DATE_FORMATTER);
    }

    /**
     * Get the end date of the transaction
     * @return The end date of the transaction
     */
    public LocalDateTime GetEndDate()
    {
        return LocalDateTime.parse(endDate, Constants.DB_DATE_FORMATTER);
    }

    public LocalDateTime GetNextDueDate()
    {
        return LocalDateTime.parse(nextDueDate, Constants.DB_DATE_FORMATTER);
    }

    /**
     * Get the frequency of the transaction
     * @return The frequency of the transaction
     */
    public RecurringTransactionFrequency GetFrequency()
    {
        return frequency;
    }

    /**
     * Set the start date of the transaction
     * @param startDate The start date of the transaction
     */
    public void SetStartDate(LocalDateTime startDate)
    {
        this.startDate = startDate.format(Constants.DB_DATE_FORMATTER);
    }

    /**
     * Set the end date of the transaction
     * @param endDate The end date of the transaction
     */
    public void SetEndDate(LocalDateTime endDate)
    {
        this.endDate = endDate.format(Constants.DB_DATE_FORMATTER);
    }

    public void SetNextDueDate(LocalDateTime nextDueDate)
    {
        this.nextDueDate = nextDueDate.format(Constants.DB_DATE_FORMATTER);
    }

    /**
     * Set the frequency of the transaction
     * @param frequency The frequency of the transaction
     */
    public void SetFrequency(RecurringTransactionFrequency frequency)
    {
        this.frequency = frequency;
    }
}
