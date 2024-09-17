/*
 * Filename: CreditCardPaymentRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.entities.CreditCardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardPaymentRepository
    extends JpaRepository<CreditCardPayment, Long> {

    /**
     * Get the total paid amount of a credit card
     * @param creditCardId The credit card id
     * @return The total paid amount of the credit card
     */
    @Query("SELECT COALESCE(SUM(ccp.amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "JOIN ccp.creditCardDebt ccd "
           + "WHERE ccd.creditCard.id = :creditCardId "
           + "AND ccp.wallet IS NOT NULL")
    Double
    GetTotalPaidAmount(@Param("creditCardId") Long creditCardId);
}
