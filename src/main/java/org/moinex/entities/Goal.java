/*
 * Filename: Goal.java
 * Created on: December  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.moinex.util.Constants;

/**
 * Represents a goal
 * A goal is a wallet with a target balance and a target date, good for saving money
 * for a specific purpose, e.g., a trip, a new computer, a new bike, etc.
 */
@Entity
@Table(name = "goal")
@PrimaryKeyJoinColumn(name = "wallet_id")
public class Goal extends Wallet
{
    @Column(name = "initial_balance", nullable = false)
    private BigDecimal initialBalance;

    @Column(name = "target_balance", nullable = false, scale = 2)
    private BigDecimal targetBalance;

    @Column(name = "target_date", nullable = false)
    private String targetDate;

    @Column(name = "completion_date")
    private String completionDate;

    @Column(name = "motivation", length = 500)
    private String motivation;

    /**
     * Default constructor for JPA
     */
    public Goal() { }

    /**
     * Constructor for testing purposes
     * @param id The ID of the goal
     * @param name The name of the goal
     * @param initialBalance The initial balance of the goal
     * @param targetBalance The target balance of the goal
     * @param targetDate The target date of the goal
     * @param motivation The motivation for the goal
     * @param walletType The wallet type of the goal
     */
    public Goal(Long          id,
                String        name,
                BigDecimal    initialBalance,
                BigDecimal    targetBalance,
                LocalDateTime targetDate,
                String        motivation,
                WalletType    walletType)
    {
        super(id, name, initialBalance);
        this.SetType(walletType);

        this.initialBalance = initialBalance;
        this.targetBalance  = targetBalance;
        this.targetDate     = targetDate.format(Constants.DB_DATE_FORMATTER);
        this.motivation     = motivation;
    }

    /**
     * Constructor for Goal
     * @param name The name of the goal
     * @param initialBalance The initial balance of the goal
     * @param targetBalance The target balance of the goal
     * @param targetDate The target date of the goal
     * @param motivation The motivation for the goal
     * @param walletType The wallet type of the goal
     */
    public Goal(String        name,
                BigDecimal    initialBalance,
                BigDecimal    targetBalance,
                LocalDateTime targetDate,
                String        motivation,
                WalletType    walletType)
    {
        super();
        this.SetName(name);
        this.SetBalance(initialBalance);
        this.SetType(walletType);

        this.initialBalance = initialBalance;
        this.targetBalance  = targetBalance;
        this.targetDate     = targetDate.format(Constants.DB_DATE_FORMATTER);
        this.motivation     = motivation;
    }

    public BigDecimal GetInitialBalance()
    {
        return initialBalance;
    }

    public BigDecimal GetTargetBalance()
    {
        return targetBalance;
    }

    public LocalDateTime GetCompletionDate()
    {
        if (completionDate == null)
        {
            return null;
        }

        return LocalDateTime.parse(completionDate, Constants.DB_DATE_FORMATTER);
    }

    public LocalDateTime GetTargetDate()
    {
        return LocalDateTime.parse(targetDate, Constants.DB_DATE_FORMATTER);
    }

    public String GetMotivation()
    {
        return motivation;
    }

    public void SetInitialBalance(BigDecimal initialBalance)
    {
        this.initialBalance = initialBalance;
    }

    public void SetTargetBalance(BigDecimal targetBalance)
    {
        this.targetBalance = targetBalance;
    }

    public void SetTargetDate(LocalDateTime targetDate)
    {
        this.targetDate = targetDate.format(Constants.DB_DATE_FORMATTER);
    }

    public void SetCompletionDate(LocalDateTime completionDate)
    {
        if (completionDate == null)
        {
            this.completionDate = null;
            return;
        }

        this.completionDate = completionDate.format(Constants.DB_DATE_FORMATTER);
    }

    public void SetMotivation(String motivation)
    {
        this.motivation = motivation;
    }
}
