/*
 * Filename: CreditCardOperator.java
 * Created on: September 17, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Represents a credit card operator
 * A credit card operator is a company that issues credit cards
 */
@Entity
@Table(name = "credit_card_operator")
public class CreditCardOperator
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "icon", nullable = true, length = 30)
    private String icon;

    /**
     * Default constructor for JPA
     */
    public CreditCardOperator() { }

    /**
     * Constructor for CreditCardOperator
     * @param name The name of the credit card operator
     */
    public CreditCardOperator(String name)

    {
        this.name = name;
    }

    /**
     * Get the id of the credit card operator
     * @return The id of the credit card operator
     */
    public Long GetId()
    {
        return id;
    }

    /**
     * Get the name of the credit card operator
     * @return The name of the credit card operator
     */
    public String GetName()
    {
        return name;
    }

    /**
     * Get the icon of the credit card operator
     * @return The icon of the credit card operator
     */
    public String GetIcon()
    {
        return icon;
    }

    /**
     * Set the name of the credit card operator
     * @param name The name of the credit card operator
     */
    public void SetName(String name)
    {
        this.name = name;
    }

    /**
     * Set the icon of the credit card operator
     * @param icon The icon of the credit card operator
     */
    public void SetIcon(String icon)
    {
        this.icon = icon;
    }
}
