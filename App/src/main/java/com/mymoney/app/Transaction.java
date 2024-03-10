/*
 * Filename: Transaction.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import java.util.UUID;
import java.time.LocalDateTime;

public abstract class Transaction {
    private UUID m_uuid;
    private UUID m_account;
    private Category m_category;
    private double m_value;
    private LocalDateTime m_date;
    private String m_description;

    public Transaction(UUID account, Category category, double value, LocalDateTime date, String description) {
        this.m_uuid = UUID.randomUUID();
        this.m_account = account;
        this.m_category = category;
        this.m_value = value;
        this.m_date = date;
        this.m_description = description;
    }

    public UUID GetUUID() {
        return m_uuid;
    }

    public UUID GetAccount() {
        return m_account;
    }

    public void SetAccount(UUID account) {
        m_account = account;
    }

    public Category GetCategory() {
        return m_category;
    }

    public void SetCategory(Category category) {
        m_category = category;
    }

    public double GetValue() {
        return m_value;
    }

    public void SetValue(double value) {
        m_value = value;
    }

    public LocalDateTime GetDate() {
        return m_date;
    }

    public void SetDate(LocalDateTime date) {
        m_date = date;
    }

    public String GetDescription() {
        return m_description;
    }

    public void SetDescription(String description) {
        m_description = description;
    }
}
