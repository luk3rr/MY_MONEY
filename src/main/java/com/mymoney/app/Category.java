/*
 * Filename: Category.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "category")
public class Category
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "category_id")
    private Short m_id;

    @Column(name = "name", nullable = false)
    public String m_name;

    // Default constructor for JPA
    public Category() { }

    public Category(String name)
    {
        this.m_name = name;
    }

    // Getters and Setters
    public Short GetId()
    {
        return m_id;
    }

    public String GetName()
    {
        return m_name;
    }

    public void SetName(String name)
    {
        m_name = name;
    }
}
