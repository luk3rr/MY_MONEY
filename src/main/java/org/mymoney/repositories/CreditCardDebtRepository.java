/*
 * Filename: CreditCardDebtRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.repositories;

import org.mymoney.entities.CreditCardDebt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardDebtRepository extends JpaRepository<CreditCardDebt, Long> {

    /**
     * Get the total debt of a credit card
     * @param creditCardName The name of the credit card
     * @return The total debt of the credit card
     */
    @Query("SELECT COALESCE(SUM(ccd.totalAmount), 0) FROM CreditCardDebt ccd "
           + "JOIN ccd.creditCard cc "
           + "WHERE cc.id = :creditCardId")
    Double
    GetTotalDebt(@Param("creditCardId") Long creditCardId);

    /**
     * Get the date of the oldest debt
     * @return The date of the oldest debt
     */
    @Query("SELECT MIN(ccd.date) FROM CreditCardDebt ccd")
    String GetOldestDebtDate();

    /**
     * Get the date of the newest debt
     * @return The date of the newest debt
     */
    @Query("SELECT MAX(ccd.date) FROM CreditCardDebt ccd")
    String GetNewestDebtDate();
}
