/*
 * Filename: Category.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.entities;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_id")
    private Short m_id;

    @Column(name = "name", nullable = false)
    private String m_name;

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
        this.m_name = name;
    }

    /**
     * Get the category id
     * @return The category id
     */
    public Short GetId()
    {
        return m_id;
    }

    /**
     * Get the category name
     * @return The category name
     */
    public String GetName()
    {
        return m_name;
    }

    /**
     * Set the category name
     * @param name The category name
     */
    public void SetName(String name)
    {
        m_name = name;
    }
}
