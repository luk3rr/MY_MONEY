/*
 * Filename: Category.java
 * Created on: March 10, 2024
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
 * Represents a category of expenses and incomes
 */
@Entity
@Table(name = "category")
public class Category
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "archived", nullable = false)
    private Boolean archived = false; // Default value is false

    /**
     * Default constructor for JPA
     */
    public Category() { }

    /**
     * Constructor for Category
     * @param name The name of the category
     */
    public Category(String name)
    {
        this.name = name;
    }

    /**
     * Get the category id
     * @return The category id
     */
    public Long GetId()
    {
        return id;
    }

    /**
     * Get the category name
     * @return The category name
     */
    public String GetName()
    {
        return name;
    }

    /**
     * Get the archived status of the category
     * @return True if the category is archived, false otherwise
     */
    public Boolean IsArchived()
    {
        return archived;
    }

    /**
     * Set the category name
     * @param name The category name
     */
    public void SetName(String name)
    {
        this.name = name;
    }

    /**
     * Set the archived status of the category
     * @param archived The new archived status of the category
     */
    public void SetArchived(Boolean archived)
    {
        this.archived = archived;
    }
}
