/*
 * Filename: CreditCardRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.entities.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    /**
     * Check if a credit card with the given name exists
     * @param name The name of the credit card
     * @return True if a credit card with the given name exists, false otherwise
     */
    Boolean existsByName(String name);

    /**
     * Get a credit card by its name
     * @param name The name of the credit card
     * @return The credit card with the given name
     */
    CreditCard findByName(String name);
}
