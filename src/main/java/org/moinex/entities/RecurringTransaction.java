/*
 * Filename: RecurringTransaction.java
 * Created on: November 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.moinex.util.TransactionStatus;
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

    /**
     * Default constructor for JPA
     */
    public RecurringTransaction() { }

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
    public RecurringTransaction(Wallet            wallet,
                                Category          category,
                                TransactionType   type,
                                TransactionStatus status,
                                String            startDate,
                                String            endDate,
                                String            nextDueDate,
                                BigDecimal        amount,
                                String            description)
    {
        super(wallet, category, type, status, amount, description);

        this.startDate   = startDate;
        this.endDate     = endDate;
        this.nextDueDate = nextDueDate;
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
    public String GetStartDate()
    {
        return startDate;
    }

    /**
     * Get the end date of the transaction
     * @return The end date of the transaction
     */
    public String GetEndDate()
    {
        return endDate;
    }

    public String GetNextDueDate()
    {
        return nextDueDate;
    }

    /**
     * Set the start date of the transaction
     * @param startDate The start date of the transaction
     */
    public void SetStartDate(String startDate)
    {
        this.startDate = startDate;
    }

    /**
     * Set the end date of the transaction
     * @param endDate The end date of the transaction
     */
    public void SetEndDate(String endDate)
    {
        this.endDate = endDate;
    }

    public void SetNextDueDate(String nextDueDate)
    {
        this.nextDueDate = nextDueDate;
    }
}
