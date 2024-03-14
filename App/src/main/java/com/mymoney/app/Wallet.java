/*
 * Filename: Wallet.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import java.util.UUID;

public class Wallet {
    private UUID m_uuid;
    private String m_name;
    private double m_balance;

    public Wallet(String name, double balance) {
        this.m_uuid = UUID.randomUUID();
        this.m_name = name;
        this.m_balance = balance;
    }

    public UUID GetUUID() {
        return m_uuid;
    }

    public String GetName() {
        return m_name;
    }

    public void SetName(String name) {
        m_name = name;
    }

    public double GetBalance() {
        return m_balance;
    }

    public void SetBalance(double balance) {
        m_balance = balance;
    }

    public void AddTransaction(Transaction transaction) {
        if (transaction instanceof Revenue) {
            m_balance += transaction.GetValue();
        } else if (transaction instanceof Expense) {
            m_balance -= transaction.GetValue();
        }
    }

    public void RemoveTransaction(Transaction transaction) {
        if (transaction instanceof Revenue) {
            m_balance -= transaction.GetValue();
        } else if (transaction instanceof Expense) {
            m_balance += transaction.GetValue();
        }
    }
}
