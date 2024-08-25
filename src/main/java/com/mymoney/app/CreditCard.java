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

    // Empty constructor for JPA
    public CreditCard() { }

    public CreditCard(String name, Short billingDueDay, Double maxDebt)
    {
        this.m_name          = name;
        this.m_billingDueDay = billingDueDay;
        this.m_maxDebt       = maxDebt;
    }

    public String GetName()
    {
        return this.m_name;
    }

    public Short GetBillingDueDay()
    {
        return this.m_billingDueDay;
    }

    public Double GetMaxDebt()
    {
        return this.m_maxDebt;
    }

    public void SetName(String name)
    {
        this.m_name = name;
    }

    public void SetBillingDueDay(Short billingDueDay)
    {
        this.m_billingDueDay = billingDueDay;
    }

    public void SetMaxDebt(Double maxDebt)
    {
        this.m_maxDebt = maxDebt;
    }
}
