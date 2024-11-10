/*
 * Filename: RecurringTransactionRepository.java
 * Created on: November 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.repositories;

import org.moinex.entities.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for the RecurringTransaction entity
 *
 * This repository provides methods to query the database for RecurringTransaction
 */
@Repository
public interface RecurringTransactionRepository
    extends JpaRepository<RecurringTransaction, Long> { }
