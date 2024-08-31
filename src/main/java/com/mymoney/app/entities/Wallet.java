/*
 * Filename: Wallet.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Represents a wallet
 * A wallet is a container for money
 */
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
}
