/*
 * Filename: Category.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

public class Category {
    public String m_name;
    public String m_description;
    public Category m_parent;

    public Category(String name, String description, Category parent) {
        this.m_name = name;
        this.m_description = description;
        this.m_parent = parent;
    }

    public String GetName() {
        return m_name;
    }

    public void SetName(String name) {
        m_name = name;
    }

    public String GetDescription() {
        return m_description;
    }

    public void SetDescription(String description) {
        m_description = description;
    }

    public Category GetParent() {
        return m_parent;
    }

    public void SetParent(Category parent) {
        m_parent = parent;
    }
}
