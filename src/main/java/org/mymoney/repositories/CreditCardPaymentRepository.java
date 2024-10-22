/*
 * Filename: CreditCardPaymentRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.repositories;

import java.util.List;
import org.mymoney.entities.CreditCardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardPaymentRepository
    extends JpaRepository<CreditCardPayment, Long> {

    /**
     * Get credit card payments in a month and year
     * @param month The month
     * @param year The year
     * @return A list with all credit card payments in a month and year
     */
    @Query("SELECT ccp "
           + "FROM CreditCardPayment ccp "
           + "WHERE strftime('%m', ccp.date) = printf('%02d', :month) "
           + "AND strftime('%Y', ccp.date) = printf('%04d', :year)")
    List<CreditCardPayment>
    GetCreditCardPayments(@Param("month") Integer month, @Param("year") Integer year);

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
     * Get the total debt amount of all credit cards in a year
     * @param year The year
     * @return The total debt amount of all credit cards in a year
     */
    @Query("SELECT COALESCE(SUM(ccp.amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "WHERE strftime('%Y', ccp.date) = printf('%04d', :year)")
    Double
    GetTotalDebtAmount(@Param("year") Integer year);

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

    /**
     * Get the total of all pending payments of all credit cards from a specified year
     * onward, including future years and the current year
     * @param year The starting year (inclusive)
     * @return The total of all pending payments of all credit cards from the specified
     *    year onward
     */
    @Query("SELECT COALESCE(SUM(ccp.amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "WHERE strftime('%Y', ccp.date) >= printf('%04d', :year) "
           + "AND ccp.wallet IS NULL")
    Double
    GetTotalPendingPayments(@Param("year") Integer year);

    /**
     * Get the total of all pending payments of a credit card from a specified month and
     * year onward, including future months and the current month
     * @param creditCardId The credit card id
     * @param month The starting month (inclusive)
     * @param year The starting year (inclusive)
     * @return The total of all pending payments of all credit cards from the current
     *     year onward
     */
    @Query("SELECT COALESCE(SUM(ccp.amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "JOIN ccp.creditCardDebt ccd "
           + "WHERE ccd.creditCard.id = :creditCardId "
           + "AND strftime('%m', ccp.date) >= printf('%02d', :month) "
           + "AND strftime('%Y', ccp.date) >= printf('%04d', :year) "
           + "AND ccp.wallet IS NULL")
    Double
    GetPendingPayments(@Param("creditCardId") Long creditCardId,
                       @Param("month") Integer     month,
                       @Param("year") Integer      year);

    /**
     * Get the invoice amount of a credit card in a specified month and year
     * @param creditCardId The credit card id
     * @param month The month
     * @param year The year
     * @return The invoice amount of the credit card in the specified month and year
     */
    @Query("SELECT COALESCE(SUM(ccp.amount), 0) "
           + "FROM CreditCardPayment ccp "
           + "JOIN ccp.creditCardDebt ccd "
           + "WHERE ccd.creditCard.id = :creditCardId "
           + "AND strftime('%m', ccp.date) = printf('%02d', :month) "
           + "AND strftime('%Y', ccp.date) = printf('%04d', :year)")
    Double
    GetInvoiceAmount(@Param("creditCardId") Long creditCardId,
                     @Param("month") Integer     month,
                     @Param("year") Integer      year);

    /**
     * Get next invoice date of a credit card
     * @param creditCardId The credit card id
     * @return The next invoice date of the credit card
     */
    @Query("SELECT MIN(ccp.date) "
           + "FROM CreditCardPayment ccp "
           + "JOIN ccp.creditCardDebt ccd "
           + "WHERE ccd.creditCard.id = :creditCardId "
           + "AND ccp.wallet IS NULL")
    String
    GetNextInvoiceDate(@Param("creditCardId") Long creditCardId);
}
