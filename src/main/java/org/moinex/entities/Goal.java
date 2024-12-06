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
@PrimaryKeyJoinColumn(name = "id")
public class Goal extends Wallet
{
    @Column(name = "target", nullable = false, scale = 2)
    private BigDecimal target;

    @Column(name = "target_date", nullable = false)
    private String targetDate;

    @Column(name = "motivation", length = 500)
    private String motivation;

    /**
     * Default constructor for JPA
     */
    public Goal() { }

    /**
     * Constructor for Goal
     * @param name The name of the goal
     * @param initialBalance The initial balance of the goal
     * @param target The target balance of the goal
     * @param targetDate The target date of the goal
     * @param motivation The motivation for the goal
     */
    public Goal(String        name,
                BigDecimal    initialBalance,
                BigDecimal    target,
                LocalDateTime targetDate,
                String        motivation)
    {
        super();
        this.SetName(name);
        this.SetBalance(initialBalance);

        this.target     = target;
        this.targetDate = targetDate.format(Constants.DB_DATE_FORMATTER);
        this.motivation = motivation;
    }

    public BigDecimal GetTarget()
    {
        return target;
    }

    public LocalDateTime GetTargetDate()
    {
        return LocalDateTime.parse(targetDate, Constants.DB_DATE_FORMATTER);
    }

    public String GetMotivation()
    {
        return motivation;
    }

    public void SetTarget(BigDecimal target)
    {
        this.target = target;
    }

    public void SetTargetDate(LocalDateTime targetDate)
    {
        this.targetDate = targetDate.format(Constants.DB_DATE_FORMATTER);
    }

    public void SetMotivation(String motivation)
    {
        this.motivation = motivation;
    }
}
