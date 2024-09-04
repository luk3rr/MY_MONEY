/*
 * Filename: CreditCardPaymentRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.app.entities.CreditCardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardPaymentRepository
    extends JpaRepository<CreditCardPayment, Long> {

    /**
     * Sums the total amount of payments made for a specific credit card's debts
     * @param creditCardName The name of the credit card
     * @return The total amount of payments made
     */
    @Query("SELECT SUM(ccp.amount) "
           + "FROM CreditCardPayment ccp "
           + "JOIN ccp.creditCardDebt ccd "
           + "WHERE ccd.crc_name = :creditCardName "
           + "AND ccp.wallet IS NOT NULL")
    Double
    GetTotalPaidAmount(String creditCardName);
}
