/*
 * Filename: WalletRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    /**
     * Check if a wallet with the given name exists
     * @param name The name of the wallet
     * @return True if a wallet with the given name exists, false otherwise
     */
    Boolean existsByName(String name);

    /**
     * Get a wallet by its name
     * @param name The name of the wallet
     * @return The wallet with the given name
     */
    Wallet findByName(String name);
}
