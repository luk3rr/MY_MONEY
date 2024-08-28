/*
 * Filename: Wallet.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "wallet")
public class Wallet
{
    @Id
    @Column(name = "name")
    private String m_name;

    @Column(name = "balance", nullable = false)
    private Double m_balance;

    /**
     * Default constructor for JPA
     */
    public Wallet() { }

    /**
     * Constructor for Wallet
     * @param name The name of the wallet
     * @param balance The balance of the wallet
     */
    public Wallet(String name, Double balance)
    {
        m_name    = name;
        m_balance = balance;
    }

    /**
     * Get the name of the wallet
     * @return The name of the wallet
     */
    public String GetName()
    {
        return m_name;
    }

    /**
     * Set the name of the wallet
     * @param name The new name of the wallet
     */
    public void SetName(String name)
    {
        m_name = name;
    }

    /**
     * Get the balance of the wallet
     * @return The balance of the wallet
     */
    public Double GetBalance()
    {
        return m_balance;
    }

    /**
     * Set the balance of the wallet
     * @param balance The new balance of the wallet
     */
    public void SetBalance(Double balance)
    {
        m_balance = balance;
    }

    /**
     * Add income to the wallet
     * @param amount The amount of the income
     * @throws IllegalArgumentException If the amount is negative
     */
    public void AddIncome(Double amount)
    {
        // TODO: Write tests for this method
        // TODO: Implement the logic for adding WalletTransaction
        if (amount < 0)
        {
            throw new IllegalArgumentException("Income amount must be positive");
        }

        m_balance += amount;
    }

    /**
     * Add expense to the wallet
     * @param amount The amount of the expense
     * @throws IllegalArgumentException If the amount is negative
     */
    public void AddExpense(Double amount)
    {
        // TODO: Write tests for this method
        // TODO: Implement the logic for adding WalletTransaction
        if (amount < 0)
        {
            throw new IllegalArgumentException("Expense amount must be positive");
        }

        m_balance -= amount;
    }
}
