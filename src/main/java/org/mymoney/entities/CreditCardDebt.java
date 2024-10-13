/*
 * Filename: CreditCardDebt.java
 * Created on: August 26, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.entities;

import org.mymoney.util.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Represents a credit card debt
 * A credit card debt is a debt that is associated with a credit card
 */
@Entity
@Table(name = "credit_card_debt")
public class CreditCardDebt
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "crc_id", referencedColumnName = "id", nullable = false)
    private CreditCard creditCard;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = false)
    private Category category;

    @Column(name = "date", nullable = false)
    private String date;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "description", nullable = true)
    private String description;

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
    public CreditCardDebt(CreditCard    creditCard,
                          Category      category,
                          LocalDateTime date,
                          Double        totalAmount,
                          String        description)
    {
        this.creditCard  = creditCard;
        this.category    = category;
        this.date        = date.format(Constants.DB_DATE_FORMATTER);
        this.totalAmount = totalAmount;
        this.description = description;
    }

    /**
     * Get the id of the debt
     * @return The id of the debt
     */
    public Long GetId()
    {
        return id;
    }

    /**
     * Get the credit card of the debt
     * @return The credit card of the debt
     */
    public CreditCard GetCreditCard()
    {
        return creditCard;
    }

    /**
     * Get the category of the debt
     * @return The category of the debt
     */
    public Category GetCategory()
    {
        return category;
    }

    /**
     * Get the date of the debt
     * @return The date of the debt
     */
    public LocalDateTime GetDate()
    {
        return LocalDateTime.parse(date, Constants.DB_DATE_FORMATTER);
    }

    /**
     * Get the total amount of the debt
     * @return The total amount of the debt
     */
    public Double GetTotalAmount()
    {
        return totalAmount;
    }

    /**
     * Get the description of the debt
     * @return The description of the debt
     */
    public String GetDescription()
    {
        return description;
    }

    /**
     * Set the credit card of the debt
     * @param creditCard The new credit card of the debt
     */
    public void SetCreditCard(CreditCard creditCard)
    {
        this.creditCard = creditCard;
    }

    /**
     * Set the category of the debt
     * @param category The new category of the debt
     */
    public void SetCategory(Category category)
    {
        this.category = category;
    }

    /**
     * Set the date of the debt
     * @param date The new date of the debt
     */
    public void SetDate(LocalDateTime date)
    {
        this.date = date.format(Constants.DB_DATE_FORMATTER);
    }

    /**
     * Set the total amount of the debt
     * @param totalAmount The new total amount of the debt
     */
    public void SetTotalAmount(Double totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    /**
     * Set the description of the debt
     * @param description The new description of the debt
     */
    public void SetDescription(String description)
    {
        this.description = description;
    }
}
