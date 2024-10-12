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
import com.mymoney.entities.WalletType;
import com.mymoney.repositories.TransferRepository;
import com.mymoney.repositories.WalletRepository;
import com.mymoney.repositories.WalletTransactionRepository;
import com.mymoney.repositories.WalletTypeRepository;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import com.mymoney.util.TransactionStatus;
import com.mymoney.util.TransactionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

    @Autowired
    private WalletTypeRepository m_walletTypeRepository;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public WalletService() { }

    /**
     * Creates a new wallet
     * @param name The name of the wallet
     * @param balance The initial balance of the wallet
     * @throws RuntimeException If the wallet name is already in use
     * @return The id of the created wallet
     */
    @Transactional
    public Long CreateWallet(String name, Double balance)
    {
        // Remove leading and trailing whitespaces
        name = name.strip();

        if (m_walletRepository.existsByName(name))
        {
            throw new RuntimeException("Wallet with name " + name + " already exists");
        }

        m_logger.info("Wallet " + name + " created with balance " + balance);

        Wallet wt = new Wallet(name, balance);

        m_walletRepository.save(wt);

        return wt.GetId();
    }

    /**
     * Creates a new wallet
     * @param name The name of the wallet
     * @param balance The initial balance of the wallet
     * @param walletType The type of the wallet
     * @throws RuntimeException If the wallet name is already in use
     * @return The id of the created wallet
     */
    @Transactional
    public Long CreateWallet(String name, Double balance, WalletType walletType)
    {
        // Remove leading and trailing whitespaces
        name = name.strip();

        if (m_walletRepository.existsByName(name))
        {
            throw new RuntimeException("Wallet with name " + name + " already exists");
        }

        m_logger.info("Wallet " + name + " created with balance " + balance);

        Wallet wt = new Wallet(name, balance, walletType);

        m_walletRepository.save(wt);

        return wt.GetId();
    }

    /**
     * Delete a wallet
     * @param id The id of the wallet to be deleted
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void DeleteWallet(Long id)
    {
        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            ()
                -> new RuntimeException("Wallet with id " + id +
                                        " not found and cannot be deleted"));

        m_walletRepository.delete(wallet);

        m_logger.info("Wallet with id " + id + " was permanently deleted");
    }

    /**
     * Archive a wallet
     * @param id The id of the wallet to be archived
     * @throws RuntimeException If the wallet does not exist
     * @note This method is used to archive a wallet, which means that the wallet
     * will not be deleted from the database, but it will not be used in the
     * application anymore
     */
    @Transactional
    public void ArchiveWallet(Long id)
    {
        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            ()
                -> new RuntimeException("Wallet with id " + id +
                                        " not found and cannot be deleted"));

        wallet.SetArchived(true);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet with id " + id + " was archived");
    }

    /**
     * Rename a wallet
     * @param id The id of the wallet to be renamed
     * @param newName The new name of the wallet
     * @throws RuntimeException If the wallet does not exist
     * @throws RuntimeException If the new name is already in use
     */
    @Transactional
    public void RenameWallet(Long id, String newName)
    {
        // Remove leading and trailing whitespaces
        newName = newName.strip();

        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Wallet with id " + id + " not found"));

        if (m_walletRepository.existsByName(newName))
        {
            throw new RuntimeException("Wallet with name " + newName +
                                       " already exists");
        }

        wallet.SetName(newName);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet with id " + id + " renamed to " + newName);
    }

    /**
     * Change wallet type
     * @param id The id of the wallet to change the type
     * @param newType The new type of the wallet
     * @throws RuntimeException If the wallet does not exist
     * @throws RuntimeException If the new type does not exist
     * @throws RuntimeException If the wallet type is already the new type
     */
    @Transactional
    public void ChangeWalletType(Long id, WalletType newType)
    {
        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Wallet with id " + id + " not found"));

        if (newType == null || !m_walletTypeRepository.existsById(newType.GetId()))
        {
            throw new RuntimeException("Wallet type not found");
        }

        if (wallet.GetType().GetId() == newType.GetId())
        {
            throw new RuntimeException("Wallet with name " + wallet.GetName() +
                                       " already has type " + newType.GetName());
        }

        wallet.SetType(newType);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet with id " + id + " type changed to " + newType.GetName());
    }

    /**
     * Update the balance of a wallet
     * @param id The id of the wallet
     * @param newBalance The new balance of the wallet
     * @throws RuntimeException If the wallet does not exist
     */
    @Transactional
    public void UpdateWalletBalance(Long id, Double newBalance)
    {
        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Wallet with id " + id + " not found"));

        wallet.SetBalance(newBalance);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet with id " + id + " balance updated to " + newBalance);
    }

    /**
     * Transfer money between two wallets
     * @param senderId The id of the wallet that sends the money
     * @param receiverId The id of the wallet that receives the money
     * @param amount The amount of money to be transferred
     * @param description A description of the transfer
     * @return The id of the created transfer
     * @throws RuntimeException If the sender and receiver wallets are the same
     * @throws RuntimeException If the amount to transfer is less than or equal to zero
     * @throws RuntimeException If the sender wallet does not exist
     * @throws RuntimeException If the receiver wallet does not exist
     * @throws RuntimeException If the sender wallet does not have enough balance
     * to transfer
     */
    @Transactional
    public Long TransferMoney(Long          senderId,
                              Long          receiverId,
                              LocalDateTime date,
                              Double        amount,
                              String        description)
    {
        if (senderId.equals(receiverId))
        {
            throw new RuntimeException("Sender and receiver wallets must be different");
        }

        if (amount <= 0)
        {
            throw new RuntimeException("Amount to transfer must be greater than zero");
        }

        Wallet senderWallet = m_walletRepository.findById(senderId).orElseThrow(
            ()
                -> new RuntimeException(
                    "Sender wallet not found and cannot transfer money"));

        Wallet receiverWallet =
            m_walletRepository.findById(receiverId)
                .orElseThrow(
                    ()
                        -> new RuntimeException(
                            "Receiver wallet not found and cannot transfer money"));

        if (senderWallet.GetBalance() < amount)
        {
            throw new RuntimeException(
                "Sender wallet does not have enough balance to transfer");
        }

        Transfer transfer = m_transferRepository.save(
            new Transfer(senderWallet, receiverWallet, date, amount, description));

        senderWallet.SetBalance(senderWallet.GetBalance() - amount);
        receiverWallet.SetBalance(receiverWallet.GetBalance() + amount);

        m_walletRepository.save(senderWallet);
        m_walletRepository.save(receiverWallet);

        m_logger.info("Transfer from wallet with id " + senderId +
                      " to wallet with id " + receiverId + " of " + amount +
                      " was successful");

        return transfer.GetId();
    }

    /**
     * Add an income to a wallet
     * @param walletId The id of the wallet that receives the income
     * @param category The category of the income
     * @param date The date of the income
     * @param amount The amount of the income
     * @param description A description of the income
     * @param status The status of the transaction
     * @return The id of the created transaction
     * @throws RuntimeException If the wallet does not exist
     * @throws RuntimeException If the amount to transfer is less than or equal to zero
     */
    @Transactional
    public Long AddIncome(Long              walletId,
                          Category          category,
                          LocalDateTime     date,
                          Double            amount,
                          String            description,
                          TransactionStatus status)
    {
        Wallet wallet = m_walletRepository.findById(walletId).orElseThrow(
            () -> new RuntimeException("Wallet with id " + walletId + " not found"));

        if (amount <= 0)
        {
            throw new RuntimeException("Amount to transfer must be greater than zero");
        }

        WalletTransaction wt = new WalletTransaction(wallet,
                                                     category,
                                                     TransactionType.INCOME,
                                                     status,
                                                     date,
                                                     amount,
                                                     description);

        m_walletTransactionRepository.save(wt);

        if (status == TransactionStatus.CONFIRMED)
        {
            wallet.SetBalance(wallet.GetBalance() + amount);
            m_walletRepository.save(wallet);
        }

        m_logger.info("Income with status " + status.toString() + " of " + amount +
                      " added to wallet with id " + walletId);

        return wt.GetId();
    }

    /**
     * Add an expense to a wallet
     * @param walletId The id of the wallet that receives the expense
     * @param category The category of the expense
     * @param date The date of the expense
     * @param amount The amount of the expense
     * @param description A description of the expense
     * @param status The status of the transaction
     * @return The id of the created transaction
     * @throws RuntimeException If the wallet does not exist
     * @throws RuntimeException If the amount to transfer is less than or equal to zero
     * @throws RuntimeException If the wallet does not have enough balance to confirm
     */
    @Transactional
    public Long AddExpense(Long              walletId,
                           Category          category,
                           LocalDateTime     date,
                           Double            amount,
                           String            description,
                           TransactionStatus status)
    {
        Wallet wallet = m_walletRepository.findById(walletId).orElseThrow(
            () -> new RuntimeException("Wallet with id " + walletId + " not found"));

        if (amount <= 0)
        {
            throw new RuntimeException("Amount to transfer must be greater than zero");
        }

        if (wallet.GetBalance() < amount)
        {
            throw new RuntimeException(
                "Wallet " + wallet.GetName() +
                " does not have enough balance to confirm expense");
        }

        WalletTransaction wt = new WalletTransaction(wallet,
                                                     category,
                                                     TransactionType.EXPENSE,
                                                     status,
                                                     date,
                                                     amount,
                                                     description);

        m_walletTransactionRepository.save(wt);

        if (status == TransactionStatus.CONFIRMED)
        {
            wallet.SetBalance(wallet.GetBalance() - amount);
            m_walletRepository.save(wallet);
        }

        m_logger.info("Expense with status " + status.toString() + " of " + amount +
                      " added to wallet with id " + walletId);

        return wt.GetId();
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
        Double amount = transaction.GetAmount();

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

        if (transaction.GetType() == TransactionType.EXPENSE)
        {
            if (wallet.GetBalance() < transaction.GetAmount())
            {
                throw new RuntimeException(
                    "Wallet " + wallet.GetName() +
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

    /**
     * Get all wallets
     * @return A list with all wallets
     */
    public List<Wallet> GetAllWallets()
    {
        return m_walletRepository.findAll();
    }

    /**
     * Get all wallets ordered by name
     * @return A list with all wallets ordered by name
     */
    public List<Wallet> GetAllWalletsOrderedByName()
    {
        return m_walletRepository.findAllByOrderByNameAsc();
    }

    /**
     * Get wallet by name
     * @param name The name of the wallet
     * @return The wallet with the provided name
     * @throws RuntimeException If the wallet does not exist
     */
    public Wallet GetWalletByName(String name)
    {
        return m_walletRepository.findByName(name).orElseThrow(
            () -> new RuntimeException("Wallet with name " + name + " not found"));
    }

    /**
     * Get wallet by id
     * @param id The id of the wallet
     * @return The wallet with the provided id
     * @throws RuntimeException If the wallet does not exist
     */
    public Wallet GetWalletById(Long id)
    {
        return m_walletRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Wallet with id " + id + " not found"));
    }

    /**
     * Get the last n transactions of all wallets
     * @param n The number of transactions to get
     * @return A list with the last n transactions of all wallets
     */
    public List<WalletTransaction> GetLastTransactions(Integer n)
    {
        return m_walletTransactionRepository.GetLastTransactions(PageRequest.ofSize(n));
    }

    /**
     * Get all transactions by month
     * @param month The month of the transactions
     * @param year The year of the transactions
     */
    public List<WalletTransaction> GetAllTransactionsByMonth(Integer month,
                                                             Integer year)
    {
        return m_walletTransactionRepository.GetAllTransactionsByMonth(month, year);
    }

    /**
     * Get all transactions by year
     * @param year The year of the transactions
     * @return A list with all transactions of the year
     */
    public List<WalletTransaction> GetAllTransactionsByYear(Integer year)
    {
        return m_walletTransactionRepository.GetAllTransactionsByYear(year);
    }

    /**
     * Get all transactions by wallet and month
     * @param walletId The id of the wallet
     * @param month The month of the transactions
     * @param year The year of the transactions
     */
    public List<WalletTransaction>
    GetTransactionsByWalletAndMonth(Long walletId, Integer month, Integer year)
    {
        return m_walletTransactionRepository.GetTransactionsByWalletAndMonth(walletId,
                                                                             month,
                                                                             year);
    }

    /**
     * Get all pending transactions by month
     * @param month The month of the transactions
     * @param year The year of the transactions
     */
    public List<WalletTransaction> GetPendingTransactionsByMonth(Integer month,
                                                                 Integer year)
    {
        return m_walletTransactionRepository.GetPendingTransactionsByMonth(month, year);
    }

    /**
     * Get all transactions between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return A list with all transactions between the two dates
     */
    public List<WalletTransaction> GetTransactionsBetweenDates(LocalDateTime startDate,
                                                               LocalDateTime endDate)
    {
        String startDateStr = startDate.format(Constants.DATE_TIME_FORMATTER_WITH_TIME);
        String endDateStr   = endDate.format(Constants.DATE_TIME_FORMATTER_WITH_TIME);

        return m_walletTransactionRepository.GetTransactionsBetweenDates(startDateStr,
                                                                         endDateStr);
    }

    /**
     * Get all confirmed transactions by month
     * @param month The month of the transactions
     * @param year The year of the transactions
     */
    public List<WalletTransaction> GetConfirmedTransactionsByMonth(Integer month,
                                                                   Integer year)
    {
        return m_walletTransactionRepository.GetConfirmedTransactionsByMonth(month,
                                                                             year);
    }

    /**
     * Get transaction by id
     * @param id The id of the transaction
     * @return The transaction with the provided id
     */
    public WalletTransaction GetTransactionById(Long id)
    {
        return m_walletTransactionRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Transaction with id " + id + " not found"));
    }

    /**
     * Get all wallet types
     * @return A list with all wallet types
     */
    public List<WalletType> GetAllWalletTypes()
    {
        return m_walletTypeRepository.findAllByOrderByNameAsc();
    }

    /**
     * Get the transfers by wallet
     * @param walletId The id of the wallet
     * @return A list with the transfers in the wallet
     */
    public List<Transfer> GetTransfersByWallet(Long walletId)
    {
        return m_transferRepository.GetTransfersByWallet(walletId);
    }

    /**
     * Get the transfers by month and year
     * @param month The month
     * @param year The year
     * @return A list with the transfers by month and year
     */
    public List<Transfer> GetTransfersByMonthAndYear(Integer month, Integer year)
    {
        return m_transferRepository.GetTransferByMonthAndYear(month, year);
    }

    /**
     * Get the transfers by wallet and month
     * @param walletId The id of the wallet
     * @param month The month
     * @param year The year
     * @return A list with the transfers in the wallet by month
     */
    public List<Transfer>
    GetTransfersByWalletAndMonth(Long walletId, Integer month, Integer year)
    {
        return m_transferRepository.GetTransfersByWalletAndMonth(walletId, month, year);
    }

    /**
     * Get the date of the oldest transaction
     * @return The date of the oldest transaction or the current date if there are no
     *     transactions
     */
    public LocalDateTime GetOldestTransactionDate()
    {
        String date = m_walletTransactionRepository.GetOldestTransactionDate();

        if (date == null)
        {
            return LocalDateTime.now();
        }

        return LocalDateTime.parse(date, Constants.DATE_TIME_FORMATTER_WITH_TIME);
    }
}
