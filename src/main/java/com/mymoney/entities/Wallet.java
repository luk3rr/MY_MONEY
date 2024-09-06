/*
 * Filename: Wallet.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

    @Column(name = "archived", nullable = false)
    private Boolean m_archived =
        false; // Default value at creation in the database is false

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
     * Get the balance of the wallet
     * @return The balance of the wallet
     */
    public Double GetBalance()
    {
        return m_balance;
    }

    /**
     * Get the archived status of the wallet
     * @return True if the wallet is archived, false otherwise
     */
    public boolean IsArchived()
    {
        return m_archived;
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
     * Set the balance of the wallet
     * @param balance The new balance of the wallet
     */
    public void SetBalance(Double balance)
    {
        m_balance = balance;
    }

    /**
     * Set the archived status of the wallet
     * @param archived The new archived status of the wallet
     */
    public void SetArchived(boolean archived)
    {
        m_archived = archived;
    }
}
