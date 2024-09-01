/*
 * Filename: CreditCardPaymentRepository.java
 * Created on: August 31, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.repositories;

import com.mymoney.app.entities.CreditCardPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardPaymentRepository
    extends JpaRepository<CreditCardPayment, Long> { }
