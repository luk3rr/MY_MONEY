/*
 * Filename: Transfer.java
 * Created on: August 25, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "transfer")
public class Transfer
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transfer_id")
    private Long m_id;

    @ManyToOne
    @JoinColumn(name = "sender_wallet", referencedColumnName = "name")
    private Wallet m_senderWallet;

    @ManyToOne
    @JoinColumn(name = "receiver_wallet", referencedColumnName = "name")
    private Wallet m_receiverWallet;

    @Column(name = "date", nullable = false)
    private String m_date;

    @Column(name = "description")
    private String m_description;

    @Column(name = "amount", nullable = false)
    private Double m_amount;

    // Default constructor for JPA
    public Transfer() { }

    public Transfer(Wallet senderWallet,
                    Wallet receiverWallet,
                    String date,
                    String description,
                    Double amount)
    {
        this.m_senderWallet   = senderWallet;
        this.m_receiverWallet = receiverWallet;
        this.m_date           = date;
        this.m_description    = description;
        this.m_amount         = amount;
    }

    // Getters and Setters
    public Long GetId()
    {
        return this.m_id;
    }

    public Wallet GetSenderWallet()
    {
        return this.m_senderWallet;
    }

    public Wallet GetReceiverWallet()
    {
        return this.m_receiverWallet;
    }

    public String GetDate()
    {
        return this.m_date;
    }

    public String GetDescription()
    {
        return this.m_description;
    }

    public Double GetAmount()
    {
        return this.m_amount;
    }

    public void SetSenderWallet(Wallet senderWallet)
    {
        this.m_senderWallet = senderWallet;
    }

    public void SetReceiverWallet(Wallet receiverWallet)
    {
        this.m_receiverWallet = receiverWallet;
    }

    public void SetDate(String date)
    {
        this.m_date = date;
    }

    public void SetDescription(String description)
    {
        this.m_description = description;
    }

    public void SetAmount(Double amount)
    {
        this.m_amount = amount;
    }
}
