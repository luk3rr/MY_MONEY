/*
 * Filename: WalletService.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;
import org.moinex.entities.Wallet;
import org.moinex.entities.WalletType;
import org.moinex.repositories.TransferRepository;
import org.moinex.repositories.WalletRepository;
import org.moinex.repositories.WalletTransactionRepository;
import org.moinex.repositories.WalletTypeRepository;
import org.moinex.util.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class is responsible for the business logic of the Wallet entity
 */
@Service
public class WalletService
{
    @Autowired
    private WalletRepository m_walletRepository;

    @Autowired
    private TransferRepository m_transfersRepository;

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
    public Long CreateWallet(String name, BigDecimal balance)
    {
        // Remove leading and trailing whitespaces
        name = name.strip();

        if (name.isBlank())
        {
            throw new RuntimeException("Wallet name cannot be empty");
        }

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
    public Long CreateWallet(String name, BigDecimal balance, WalletType walletType)
    {
        // Remove leading and trailing whitespaces
        name = name.strip();

        if (name.isBlank())
        {
            throw new RuntimeException("Wallet name cannot be empty");
        }

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
     * @throws RuntimeException If the wallet has transactions
     */
    @Transactional
    public void DeleteWallet(Long id)
    {
        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            ()
                -> new RuntimeException("Wallet with id " + id +
                                        " not found and cannot be deleted"));

        if (m_walletTransactionRepository.GetTransactionCountByWallet(id) > 0 ||
            m_transfersRepository.GetTransferCountByWallet(id) > 0)
        {
            throw new RuntimeException(
                "Wallet with id " + id +
                " has transactions and cannot be deleted. Remove "
                + "the transactions first or archive the wallet");
        }

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
                                        " not found and cannot be archived"));

        wallet.SetArchived(true);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet with id " + id + " was archived");
    }

    /**
     * Unarchive a wallet
     * @param id The id of the wallet to be unarchived
     * @throws RuntimeException If the wallet does not exist
     * @note This method is used to unarchive a wallet, which means that the wallet
     * will be used in the application again
     */
    @Transactional
    public void UnarchiveWallet(Long id)
    {
        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            ()
                -> new RuntimeException("Wallet with id " + id +
                                        " not found and cannot be unarchived"));

        wallet.SetArchived(false);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet with id " + id + " was unarchived");
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

        if (newName.isBlank())
        {
            throw new RuntimeException("Wallet name cannot be empty");
        }

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
    public void UpdateWalletBalance(Long id, BigDecimal newBalance)
    {
        Wallet wallet = m_walletRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Wallet with id " + id + " not found"));

        wallet.SetBalance(newBalance);
        m_walletRepository.save(wallet);

        m_logger.info("Wallet with id " + id + " balance updated to " + newBalance);
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
     * Get all wallet types
     * @return A list with all wallet types
     */
    public List<WalletType> GetAllWalletTypes()
    {
        return m_walletTypeRepository.findAllByOrderByNameAsc();
    }

    /**
     * Get all archived wallets
     * @return A list with all archived wallets
     */
    public List<Wallet> GetAllArchivedWallets()
    {
        return m_walletRepository.findAllByArchivedTrue();
    }

    /**
     * Get all wallets that are not archived ordered by name
     * @return A list with all wallets that are not archived
     */
    public List<Wallet> GetAllNonArchivedWalletsOrderedByName()
    {
        return m_walletRepository.findAllByArchivedFalseOrderByNameAsc();
    }
}
