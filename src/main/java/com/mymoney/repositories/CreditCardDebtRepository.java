/*
 * Filename: CreditCardDebtRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.app.entities.CreditCardDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardDebtRepository extends JpaRepository<CreditCardDebt, Long> {

    /**
     * Get the total debt of a credit card
     * @param creditCardName The name of the credit card
     * @return The total debt of the credit card
     */
    @Query(value = "SELECT SUM(total_amount) FROM credit_card_debt WHERE crc_name = " +
                   ":creditCardName",
           nativeQuery = true)
    Double
    GetTotalDebt(String creditCardName);
}
