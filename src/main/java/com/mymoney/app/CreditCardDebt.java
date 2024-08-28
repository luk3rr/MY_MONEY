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

/**
 * Represents a credit card debt
 * A credit card debt is a debt that is associated with a credit card
 */
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

    /**
     * Default constructor for JPA
     */
    public CreditCardDebt() { }

    /**
     * Constructor for CreditCardDebt
     * @param creditCard The credit card of the debt
     * @param category The category of the debt
     * @param date The date of the debt
     * @param totalAmount The total amount of the debt
     * @param description The description of the debt
     */
    public CreditCardDebt(Long       id,
                          CreditCard creditCard,
                          Category   category,
                          String     date,
                          Double     totalAmount,
                          String     description)
    {
        m_id          = id;
        m_creditCard  = creditCard;
        m_category    = category;
        m_date        = date;
        m_totalAmount = totalAmount;
        m_description = description;
    }

    /**
     * Get the id of the debt
     * @return The id of the debt
     */
    public Long GetId()
    {
        return m_id;
    }

    /**
     * Get the credit card of the debt
     * @return The credit card of the debt
     */
    public CreditCard GetCreditCard()
    {
        return m_creditCard;
    }

    /**
     * Get the category of the debt
     * @return The category of the debt
     */
    public Category GetCategory()
    {
        return m_category;
    }

    /**
     * Get the date of the debt
     * @return The date of the debt
     */
    public String GetDate()
    {
        return m_date;
    }

    /**
     * Get the total amount of the debt
     * @return The total amount of the debt
     */
    public Double GetTotalAmount()
    {
        return m_totalAmount;
    }

    /**
     * Get the description of the debt
     * @return The description of the debt
     */
    public String GetDescription()
    {
        return m_description;
    }

    /**
     * Set the credit card of the debt
     * @param creditCard The new credit card of the debt
     */
    public void SetCreditCard(CreditCard creditCard)
    {
        m_creditCard = creditCard;
    }

    /**
     * Set the category of the debt
     * @param category The new category of the debt
     */
    public void SetCategory(Category category)
    {
        m_category = category;
    }

    /**
     * Set the date of the debt
     * @param date The new date of the debt
     */
    public void SetDate(String date)
    {
        m_date = date;
    }

    /**
     * Set the total amount of the debt
     * @param totalAmount The new total amount of the debt
     */
    public void SetTotalAmount(Double totalAmount)
    {
        m_totalAmount = totalAmount;
    }

    /**
     * Set the description of the debt
     * @param description The new description of the debt
     */
    public void SetDescription(String description)
    {
        m_description = description;
    }
}
