/*
 * Filename: Wallet.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents a wallet
 * A wallet is a container for money
 * TODO: If wallet is deleted, all transactions should be deleted ?
 */
@Entity
@Table(name = "wallet")
public class Wallet
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "type_id", referencedColumnName = "id")
    private WalletType type;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "balance", nullable = false)
    private Double balance;

    @Column(name     = "archived",
            nullable = false)
    private Boolean archived = false; // Default value is false

    /**
     * Default constructor for JPA
     */
    public Wallet() { }

    /**
     * Constructor for testing purposes
     * @param id The id of the wallet type
     * @param name The name of the wallet type
     */
    public Wallet(Long id, String name, Double balance)
    {
        this.id      = id;
        this.name    = name;
        this.balance = balance;
    }

    /**
     * Constructor for Wallet
     * @param name The name of the wallet
     * @param balance The balance of the wallet
     */
    public Wallet(String name, Double balance)
    {
        this.name    = name;
        this.balance = balance;
    }

    /**
     * Constructor for Wallet
     * @param name The name of the wallet
     * @param balance The balance of the wallet
     * @param type The type of the wallet
     */
    public Wallet(String name, Double balance, WalletType type)
    {
        this.name    = name;
        this.balance = balance;
        this.type    = type;
    }

    /**
     * Get the id of the wallet
     * @return The id of the wallet
     */
    public Long GetId()
    {
        return id;
    }

    /**
     * Get the type of the wallet
     * @return The type of the wallet
     */
    public WalletType GetType()
    {
        return type;
    }

    /**
     * Get the name of the wallet
     * @return The name of the wallet
     */
    public String GetName()
    {
        return name;
    }

    /**
     * Get the balance of the wallet
     * @return The balance of the wallet
     */
    public Double GetBalance()
    {
        return balance;
    }

    /**
     * Get the archived status of the wallet
     * @return True if the wallet is archived, false otherwise
     */
    public boolean IsArchived()
    {
        return archived;
    }

    /**
     * Set the type of the wallet
     * @param type The new type of the wallet
     */
    public void SetType(WalletType type)
    {
        this.type = type;
    }

    /**
     * Set the name of the wallet
     * @param name The new name of the wallet
     */
    public void SetName(String name)
    {
        this.name = name;
    }

    /**
     * Set the balance of the wallet
     * @param balance The new balance of the wallet
     */
    public void SetBalance(Double balance)
    {
        this.balance = balance;
    }

    /**
     * Set the archived status of the wallet
     * @param archived The new archived status of the wallet
     */
    public void SetArchived(boolean archived)
    {
        this.archived = archived;
    }
}
