/*
 * Filename: CreditCard.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a credit card
 */
@Entity
@Table(name = "credit_card")
public class CreditCard
{
    @Id
    @Column(name = "name")
    private String m_name;

    @Column(name = "billing_due_day", nullable = false)
    private Short m_billingDueDay;

    @Column(name = "max_debt", nullable = false)
    private Double m_maxDebt;

    /**
     * Default constructor for JPA
     */
    public CreditCard() { }

    /**
     * Constructor for CreditCard
     * @param name The name of the credit card
     * @param billingDueDay The day of the month the bill is due
     * @param maxDebt The maximum debt allowed for the credit card
     */
    public CreditCard(String name, Short billingDueDay, Double maxDebt)
    {
        m_name          = name;
        m_billingDueDay = billingDueDay;
        m_maxDebt       = maxDebt;
    }

    /**
     * Get the name of the credit card
     * @return The name of the credit card
     */
    public String GetName()
    {
        return m_name;
    }

    /**
     * Get the day of the month the bill is due
     * @return The day of the month the bill is due
     */
    public Short GetBillingDueDay()
    {
        return m_billingDueDay;
    }

    /**
     * Get the maximum debt allowed for the credit card
     * @return The maximum debt allowed for the credit card
     */
    public Double GetMaxDebt()
    {
        return m_maxDebt;
    }

    /**
     * Set the name of the credit card
     * @param name The new name of the credit card
     */
    public void SetName(String name)
    {
        m_name = name;
    }

    /**
     * Set the day of the month the bill is due
     * @param billingDueDay The new day of the month the bill is due
     */
    public void SetBillingDueDay(Short billingDueDay)
    {
        m_billingDueDay = billingDueDay;
    }

    /**
     * Set the maximum debt allowed for the credit card
     * @param maxDebt The new maximum debt allowed for the credit card
     */
    public void SetMaxDebt(Double maxDebt)
    {
        m_maxDebt = maxDebt;
    }
}
