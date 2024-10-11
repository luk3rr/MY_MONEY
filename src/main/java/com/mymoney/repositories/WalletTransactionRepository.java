/*
 * Filename: WalletTransactionRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.entities.Transfer;
import com.mymoney.entities.WalletTransaction;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletTransactionRepository
    extends JpaRepository<WalletTransaction, Long> {

    /**
     * Get the transactions in a wallet by date
     * @param walletId The id of the wallet
     * @param startDate The start date of the period
     * @return A list with the last transactions in the wallet by date
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE wt.wallet.id = :walletId "
           + "AND wt.date >= :startDate "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetTransactionsByDate(@Param("walletId") Long    walletId,
                          @Param("startDate") String startDate);

    /**
     * Get the all transactions by month and year
     * @param month The month
     * @param year The year
     * @return A list with the transactions by month and year
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE strftime('%m', wt.date) = printf('%02d', :month) "
           + "AND strftime('%Y', wt.date) = printf('%04d', :year) "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetAllTransactionsByMonth(@Param("month") Integer month,
                              @Param("year") Integer  year);

    /**
     * Get the all transactions by year
     * @param year The year
     * @return A list with the transactions by year
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE strftime('%Y', wt.date) = printf('%04d', :year) "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetAllTransactionsByYear(@Param("year") Integer year);

    /**
     * Get the transactions by wallet and month
     * @param walletId The id of the wallet
     * @param month The month
     * @param year The year
     * @return A list with the transactions in the wallet by month
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE wt.wallet.id = :walletId "
           + "AND strftime('%m', wt.date) = printf('%02d', :month) "
           + "AND strftime('%Y', wt.date) = printf('%04d', :year) "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetTransactionsByWalletAndMonth(@Param("walletId") Long walletId,
                                    @Param("month") Integer month,
                                    @Param("year") Integer  year);

    /**
     * Get the pending transactions by month and year
     * @param month The month
     * @param year The year
     * @return A list with the transactions in the wallet by month and year
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE strftime('%m', wt.date) = printf('%02d', :month) "
           + "AND strftime('%Y', wt.date) = printf('%04d', :year) "
           + "AND wt.status = 'PENDING' "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetPendingTransactionsByMonth(@Param("month") Integer month,
                                  @Param("year") Integer  year);

    /**
     * Get all transactions between two dates
     * @param startDate The start date
     * @param endDate The end date
     * @return A list with the transactions between the two dates
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE wt.date >= :startDate "
           + "AND wt.date <= :endDate "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetTransactionsBetweenDates(@Param("startDate") String startDate,
                                @Param("endDate") String endDate);

    /**
     * Get the confirmed transactions by month and year
     * @param month The month
     * @param year The year
     * @return A list with the transactions in the wallet by month and year
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE strftime('%m', wt.date) = printf('%02d', :month) "
           + "AND strftime('%Y', wt.date) = printf('%04d', :year) "
           + "AND wt.status = 'CONFIRMED' "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetConfirmedTransactionsByMonth(@Param("month") Integer month,
                                    @Param("year") Integer  year);

    /**
     * Get the last n transactions in a wallet
     * @param walletId The id of the wallet
     * @param pageable The pageable object
     * @return A list with the last n transactions in the wallet
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "WHERE wt.wallet.id = :walletId "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetLastTransactions(@Param("walletId") Long walletId, Pageable pageable);

    /**
     * Get the last n transactions of all wallets
     * @param pageable The pageable object
     * @return A list with the last n transactions of all wallets
     */
    @Query("SELECT wt "
           + "FROM WalletTransaction wt "
           + "ORDER BY wt.date DESC")
    List<WalletTransaction>
    GetLastTransactions(Pageable pageable);

    /**
     * Get the date of the oldest transaction
     * @return The date of the oldest transaction
     */
    @Query("SELECT MIN(wt.date) "
           + "FROM WalletTransaction wt")
    String
    GetOldestTransactionDate();
}
