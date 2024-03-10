/*
 * Filename: Transaction.java
 * Created on: March 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import java.util.UUID;
import java.time.LocalDateTime;

public class Expense extends Transaction {
    public Expense(UUID account, Category category, double value, LocalDateTime date, String description) {
        super(account, category, value, date, description);
    }
}
