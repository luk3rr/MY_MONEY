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

    /**
     * TODO: Create tests
     * Get the total debt amount of all credit cards in a month and year
     * @param month The month
     * @param year The year
     * @return The total debt amount of all credit cards in a month and year
     */
    @Query("SELECT COALESCE(SUM(ccp.amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "WHERE strftime('%m', ccp.date) = printf('%02d', :month) "
           + "AND strftime('%Y', ccp.date) = printf('%04d', :year)")
    Double
    GetTotalDebtAmount(@Param("month") Integer month, @Param("year") Integer year);

    /**
     * TODO: Create tests
     * Get the total of all pending payments of all credit cards from a specified month
     * and year onward, including future months and the current month
     * @param month The starting month (inclusive)
     * @param year The starting year (inclusive)
     * @return The total of all pending payments of all credit cards from the specified
     *     month and year onward
     */
    @Query("SELECT COALESCE(SUM(ccp.amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "WHERE strftime('%m', ccp.date) >= printf('%02d', :month) "
           + "AND strftime('%Y', ccp.date) >= printf('%04d', :year) "
           + "AND ccp.wallet IS NULL")
    Double
    GetTotalPendingPayments(@Param("month") Integer month, @Param("year") Integer year);
}
