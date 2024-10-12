/*
 * Filename: CreditCardOperatorRepository.java
 * Created on: September 17, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.repositories;

import org.mymoney.entities.CreditCardOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardOperatorRepository
    extends JpaRepository<CreditCardOperator, Long> { }
