/*
 * Filename: CreditCardDebt.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "credit_card_debt")
public class CreditCardDebt
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "debt_id")
    private Long m_id;

    @ManyToOne
    @JoinColumn(name = "credit_card", referencedColumnName = "name")
    private CreditCard m_creditCard;

    @ManyToOne
    @JoinColumn(name = "category", referencedColumnName = "category_id")
    private Category m_category;

    @Column(name = "date", nullable = false)
    private String m_date;

    @Column(name = "total_amount", nullable = false)
    private Double m_totalAmount;

    @Column(name = "description")
    private String m_description;

    // Default constructor for JPA
    public CreditCardDebt() { }

    public CreditCardDebt(Long       id,
                          CreditCard creditCard,
                          Category   category,
                          String     date,
                          Double     totalAmount,
                          String     description)
    {
        this.m_id          = id;
        this.m_creditCard  = creditCard;
        this.m_category    = category;
        this.m_date        = date;
        this.m_totalAmount = totalAmount;
        this.m_description = description;
    }

    // Getters and Setters
    public Long GetId()
    {
        return this.m_id;
    }

    public CreditCard GetCreditCard()
    {
        return this.m_creditCard;
    }

    public Category GetCategory()
    {
        return this.m_category;
    }

    public String GetDate()
    {
        return this.m_date;
    }

    public Double GetTotalAmount()
    {
        return this.m_totalAmount;
    }

    public String GetDescription()
    {
        return this.m_description;
    }

    public void SetCreditCard(CreditCard creditCard)
    {
        this.m_creditCard = creditCard;
    }

    public void SetCategory(Category category)
    {
        this.m_category = category;
    }

    public void SetDate(String date)
    {
        this.m_date = date;
    }

    public void SetTotalAmount(Double totalAmount)
    {
        this.m_totalAmount = totalAmount;
    }

    public void SetDescription(String description)
    {
        this.m_description = description;
    }
}
