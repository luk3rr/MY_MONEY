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

/**
 * Represents a transfer between wallets
 */
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

    /**
     * Default constructor for JPA
     */
    public Transfer() { }

    /**
     * Constructor for Transfer
     * @param senderWallet The wallet that sends the money
     * @param receiverWallet The wallet that receives the money
     * @param date The date of the transfer
     * @param description A description of the transfer
     * @param amount The amount transferred
     */
    public Transfer(Wallet senderWallet,
                    Wallet receiverWallet,
                    String date,
                    String description,
                    Double amount)
    {
        m_senderWallet   = senderWallet;
        m_receiverWallet = receiverWallet;
        m_date           = date;
        m_description    = description;
        m_amount         = amount;
    }

    /*
     * Get the transfer id
     * @return The transfer id
     */
    public Long GetId()
    {
        return m_id;
    }

    /**
     * Get the wallet that sends the money
     * @return The wallet that sends the money
     */
    public Wallet GetSenderWallet()
    {
        return m_senderWallet;
    }

    /**
     * Get the wallet that receives the money
     * @return The wallet that receives the money
     */
    public Wallet GetReceiverWallet()
    {
        return m_receiverWallet;
    }

    /**
     * Get the date of the transfer
     * @return The date of the transfer
     */
    public String GetDate()
    {
        return m_date;
    }

    /**
     * Get the description of the transfer
     * @return The description of the transfer
     */
    public String GetDescription()
    {
        return m_description;
    }

    /**
     * Get the amount transferred
     * @return The amount transferred
     */
    public Double GetAmount()
    {
        return m_amount;
    }

    /**
     * Set the sender wallet
     * @param senderWallet The sender wallet
     */
    public void SetSenderWallet(Wallet senderWallet)
    {
        m_senderWallet = senderWallet;
    }

    /**
     * Set the receiver wallet
     * @param receiverWallet The receiver wallet
     */
    public void SetReceiverWallet(Wallet receiverWallet)
    {
        m_receiverWallet = receiverWallet;
    }

    /**
     * Set the date of the transfer
     * @param date The date of the transfer
     */
    public void SetDate(String date)
    {
        m_date = date;
    }

    /**
     * Set the description of the transfer
     * @param description The description of the transfer
     */
    public void SetDescription(String description)
    {
        m_description = description;
    }

    /**
     * Set the amount transferred
     * @param amount The amount transferred
     */
    public void SetAmount(Double amount)
    {
        m_amount = amount;
    }
}
