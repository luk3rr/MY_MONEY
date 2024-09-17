/*
 * Filename: WalletTransactionRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.entities.WalletTransaction;
import java.time.LocalDate;
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
     * Get the last transactions in a wallet by date
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
    GetLastTransactionsByDate(@Param("walletId") Long       walletId,
                              @Param("startDate") LocalDate startDate);

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
}
