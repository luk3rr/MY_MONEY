/*
 * Filename: CreditCardPaymentRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.app.entities.CreditCardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardPaymentRepository
    extends JpaRepository<CreditCardPayment, Long> {

    /**
     * Get the total paid amount of a credit card
     * @param creditCardName The name of the credit card
     * @return The total paid amount of the credit card
     */
    @Query("SELECT COALESCE(SUM(ccp.m_amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "JOIN ccp.m_creditCardDebt ccd "
           + "WHERE ccd.m_creditCard.m_name = :creditCardName "
           + "AND ccp.m_wallet IS NOT NULL")
    Double
    GetTotalPaidAmount(@Param("creditCardName") String creditCardName);
}
