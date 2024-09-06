/*
 * Filename: WalletService.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.services;

import com.mymoney.entities.Category;
import com.mymoney.entities.Transfer;
import com.mymoney.entities.Wallet;
import com.mymoney.entities.WalletTransaction;
import com.mymoney.repositories.TransferRepository;
import com.mymoney.repositories.WalletRepository;
import com.mymoney.repositories.WalletTransactionRepository;
import com.mymoney.util.LoggerConfig;
import com.mymoney.util.TransactionStatus;
import com.mymoney.util.TransactionType;
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

    @Autowired
    private WalletTransactionRepository m_walletTransactionRepository;

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
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void DeleteWallet(String name)
    {
        Wallet wallet = m_walletRepository.findById(name).orElseThrow(
            ()
                -> new RuntimeException("Wallet with name " + name +
                                        " not found and cannot be deleted"));

        m_walletRepository.delete(wallet);

        m_logger.info("Wallet " + name + " was permanently deleted");
    }

    /**
     * Archive a wallet
     * @param name The name of the wallet
     * @throws RuntimeException If the wallet does not exist
     * @note This method is used to archive a wallet, which means that the wallet
     * will not be deleted from the database, but it will not be used in the
     * application anymore
     */
    @Transactional
    public void ArchiveWallet(String name)
    {
        Wallet wallet = m_walletRepository.findById(name).orElseThrow(
            ()
                -> new RuntimeException("Wallet with name " + name +
                                        " not found and cannot be deleted"));

        wallet.SetArchived(true);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet " + name + " was archived");
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
     * @throws RuntimeException If the sender wallet does not exist
     * @throws RuntimeException If the receiver wallet does not exist
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

    /**
     * Add an confirmed income to a wallet
     * @param walletName The name of the wallet that receives the income
     * @param category The category of the income
     * @param date The date of the income
     * @param amount The amount of the income
     * @param description A description of the income
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void AddConfirmedIncome(String    walletName,
                                   Category  category,
                                   LocalDate date,
                                   double    amount,
                                   String    description)
    {
        Wallet wallet =
            m_walletRepository.findById(walletName)
                .orElseThrow(()
                                 -> new RuntimeException("Wallet with name " +
                                                         walletName + " not found"));

        m_walletTransactionRepository.save(
            new WalletTransaction(wallet,
                                  category,
                                  TransactionType.INCOME,
                                  TransactionStatus.CONFIRMED,
                                  date,
                                  amount,
                                  description));

        wallet.SetBalance(wallet.GetBalance() + amount);
        m_walletRepository.save(wallet);

        m_logger.info("Confirmed income of " + amount + " added to wallet " +
                      walletName);
    }

    /**
     * Add an pending income to a wallet
     * @param walletName The name of the wallet that receives the income
     * @param category The category of the income
     * @param date The date of the income
     * @param amount The amount of the income
     * @param description A description of the income
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void AddPendingIncome(String    walletName,
                                 Category  category,
                                 LocalDate date,
                                 double    amount,
                                 String    description)
    {
        Wallet wallet =
            m_walletRepository.findById(walletName)
                .orElseThrow(()
                                 -> new RuntimeException("Wallet with name " +
                                                         walletName + " not found"));

        m_walletTransactionRepository.save(
            new WalletTransaction(wallet,
                                  category,
                                  TransactionType.INCOME,
                                  TransactionStatus.PENDING,
                                  date,
                                  amount,
                                  description));

        m_logger.info("Pending income of " + amount + " added to wallet " + walletName);
    }

    /**
     * Add an confirmed expense to a wallet
     * @param walletName The name of the wallet that receives the expense
     * @param category The category of the expense
     * @param date The date of the expense
     * @param amount The amount of the expense
     * @param description A description of the expense
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void AddConfirmedExpense(String    walletName,
                                    Category  category,
                                    LocalDate date,
                                    double    amount,
                                    String    description)
    {
        Wallet wallet =
            m_walletRepository.findById(walletName)
                .orElseThrow(()
                                 -> new RuntimeException("Wallet with name " +
                                                         walletName + " not found"));

        m_walletTransactionRepository.save(
            new WalletTransaction(wallet,
                                  category,
                                  TransactionType.OUTCOME,
                                  TransactionStatus.CONFIRMED,
                                  date,
                                  amount,
                                  description));

        wallet.SetBalance(wallet.GetBalance() - amount);
        m_walletRepository.save(wallet);

        m_logger.info("Expense of " + amount + " added to wallet " + walletName);
    }

    /**
     * Add an pending expense to a wallet
     * @param walletName The name of the wallet that receives the expense
     * @param category The category of the expense
     * @param date The date of the expense
     * @param amount The amount of the expense
     * @param description A description of the expense
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void AddPendingExpense(String    walletName,
                                  Category  category,
                                  LocalDate date,
                                  double    amount,
                                  String    description)
    {
        Wallet wallet =
            m_walletRepository.findById(walletName)
                .orElseThrow(()
                                 -> new RuntimeException("Wallet with name " +
                                                         walletName + " not found"));

        m_walletTransactionRepository.save(
            new WalletTransaction(wallet,
                                  category,
                                  TransactionType.OUTCOME,
                                  TransactionStatus.PENDING,
                                  date,
                                  amount,
                                  description));

        m_logger.info("Pending expense of " + amount + " added to wallet " +
                      walletName);
    }

    /**
     * Delete a transaction from a wallet
     * @param transactionId The id of the transaction to be removed
     * @throws RuntimeException If the transaction does not exist
     */
    @Transactional
    public void DeleteTransaction(Long transactionId)
    {
        WalletTransaction transaction =
            m_walletTransactionRepository.findById(transactionId)
                .orElseThrow(()
                                 -> new RuntimeException("Transaction with id " +
                                                         transactionId + " not found"));

        Wallet wallet = transaction.GetWallet();
        double amount = transaction.GetAmount();

        // Update the wallet balance if the transaction is confirmed
        if (transaction.GetStatus() == TransactionStatus.CONFIRMED)
        {
            if (transaction.GetType() == TransactionType.INCOME)
            {
                wallet.SetBalance(wallet.GetBalance() - amount);
            }
            else
            {
                wallet.SetBalance(wallet.GetBalance() + amount);
            }

            m_walletRepository.save(wallet);
        }

        m_walletTransactionRepository.delete(transaction);

        m_logger.info("Transaction " + transactionId + " deleted from wallet " +
                      wallet.GetName());
    }

    /**
     * Confirm a pending transaction
     * @param transactionId The id of the transaction to be confirmed
     * @throws RuntimeException If the transaction does not exist
     * @throws RuntimeException If the transaction is already confirmed
     * @throws RuntimeException If wallet does not have enough balance to confirm
     */
    @Transactional
    public void ConfirmTransaction(Long transactionId)
    {
        WalletTransaction transaction =
            m_walletTransactionRepository.findById(transactionId)
                .orElseThrow(()
                                 -> new RuntimeException("Transaction with id " +
                                                         transactionId + " not found"));

        if (transaction.GetStatus() == TransactionStatus.CONFIRMED)
        {
            throw new RuntimeException("Transaction with id " + transactionId +
                                       " is already confirmed");
        }

        Wallet wallet = transaction.GetWallet();

        if (transaction.GetType() == TransactionType.OUTCOME)
        {
            if (wallet.GetBalance() < transaction.GetAmount())
            {
                throw new RuntimeException("Wallet " + wallet.GetName() +
                                           " does not have enough balance to confirm transaction");
            }

            wallet.SetBalance(wallet.GetBalance() - transaction.GetAmount());
        }
        else
        {
            wallet.SetBalance(wallet.GetBalance() + transaction.GetAmount());
        }

        transaction.SetStatus(TransactionStatus.CONFIRMED);

        m_walletRepository.save(wallet);
        m_walletTransactionRepository.save(transaction);
    }
}
