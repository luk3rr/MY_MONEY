/*
 * Filename: CreditCardDebtRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.entities.CreditCardDebt;
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
    @Query("SELECT COALESCE(SUM(ccd.m_totalAmount), 0) FROM CreditCardDebt ccd "
           + "JOIN ccd.m_creditCard cc "
           + "WHERE cc.m_name = :creditCardName")
    Double
    GetTotalDebt(@Param("creditCardName") String creditCardName);
}
