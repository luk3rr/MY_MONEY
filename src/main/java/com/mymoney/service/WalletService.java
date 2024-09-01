/*
 * Filename: WalletService.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.service;

import com.mymoney.app.entities.Transfer;
import com.mymoney.app.entities.Wallet;
import com.mymoney.repositories.TransferRepository;
import com.mymoney.repositories.WalletRepository;
import com.mymoney.util.LoggerConfig;
import java.time.LocalDate;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for the business logic of the Wallet entity.
 */
@Service
public class WalletService
{
    @Autowired
    private WalletRepository m_walletRepository;

    @Autowired
    private TransferRepository m_transferRepository;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public WalletService() { }

    /**
     * Creates a new wallet
     * @param name The name of the wallet
     * @param balance The initial balance of the wallet
     * @throws RuntimeException If the wallet name is already in use
     */
    @Transactional
    public void CreateWallet(String name, double balance)
    {
        if (m_walletRepository.existsById(name))
        {
            throw new RuntimeException("Wallet with name " + name + " already exists");
        }

        m_walletRepository.save(new Wallet(name, balance));

        m_logger.info("Wallet " + name + " created with balance " + balance);
    }

    /**
     * Delete a wallet
     * @param name The name of the wallet to be deleted
     * @param softDelete If true, the wallet will be soft deleted
     * @throws RuntimeException If the wallet does not exist
     * @note If the wallet is soft deleted, it will not be removed from the database
     * but it will be marked as archived and will not be used in the application. The
     * wallet can be restored by setting the archived field to false.
     */
    public void DeleteWallet(String name, boolean softDelete)
    {
        Wallet wallet = m_walletRepository.findById(name).orElseThrow(
            ()
                -> new RuntimeException("Wallet with name " + name +
                                        " not found and cannot be deleted"));

        if (softDelete)
        {
            wallet.SetArchived(true);
            m_walletRepository.save(wallet);

            m_logger.info("Wallet " + name + " was soft deleted");
        }
        else
        {
            m_walletRepository.delete(wallet);

            m_logger.info("Wallet " + name + " was permanently deleted");
        }
    }

    /**
     * Update the balance of a wallet
     * @param name The name of the wallet to be updated
     * @param newBalance The new balance of the wallet
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void UpdateWalletBalance(String name, double newBalance)
    {
        Wallet wallet = m_walletRepository.findById(name).orElseThrow(
            () -> new RuntimeException("Wallet with name " + name + " not found"));

        wallet.SetBalance(newBalance);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet " + name + " balance updated to " + newBalance);
    }

    /**
     * Transfer money between two wallets
     * @param senderName The name of the wallet that sends the money
     * @param receiverName The name of the wallet that receives the money
     * @param amount The amount of money to be transferred
     * @param description A description of the transfer
     * @throws RuntimeException If the sender and receiver wallets are the same
     * @throws RuntimeException If the amount to transfer is less than or equal to zero
     * @throws RuntimeException If the sender wallet does not have enough balance
     * to transfer
     */
    @Transactional
    public void TransferMoney(String    senderName,
                              String    receiverName,
                              LocalDate date,
                              double    amount,
                              String    description)
    {
        if (senderName.equals(receiverName))
        {
            throw new RuntimeException("Sender and receiver wallets must be different");
        }

        if (amount <= 0)
        {
            throw new RuntimeException("Amount to transfer must be greater than zero");
        }

        Wallet senderWallet =
            m_walletRepository.findById(senderName)
                .orElseThrow(
                    ()
                        -> new RuntimeException(
                            "Sender wallet not found and cannot transfer money"));

        Wallet receiverWallet =
            m_walletRepository.findById(receiverName)
                .orElseThrow(
                    ()
                        -> new RuntimeException(
                            "Receiver wallet not found and cannot transfer money"));

        if (senderWallet.GetBalance() < amount)
        {
            throw new RuntimeException(
                "Sender wallet does not have enough balance to transfer");
        }

        m_transferRepository.save(
            new Transfer(senderWallet, receiverWallet, date, amount, description));

        senderWallet.SetBalance(senderWallet.GetBalance() - amount);
        receiverWallet.SetBalance(receiverWallet.GetBalance() + amount);

        m_walletRepository.save(senderWallet);
        m_walletRepository.save(receiverWallet);

        m_logger.info("Transfer from " + senderName + " to " + receiverName + " of " +
                      amount + " was successful");
    }
}
