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

    // Empty constructor for JPA
    public Wallet() { }

    public Wallet(String name, Double balance)
    {
        this.m_name    = name;
        this.m_balance = balance;
    }

    public String GetName()
    {
        return m_name;
    }

    public void SetName(String name)
    {
        m_name = name;
    }

    public Double GetBalance()
    {
        return m_balance;
    }

    public void SetBalance(Double balance)
    {
        m_balance = balance;
    }
}
