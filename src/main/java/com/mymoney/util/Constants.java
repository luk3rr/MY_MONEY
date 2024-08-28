/*
 * Filename: Constants.java
 * Created on: August 28, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.util;

/**
 * Constants used in the application
 */
public final class Constants
{
    public static final String ENTITY_MANAGER_PRODUCTION = "my_money_production";
    public static final String ENTITY_MANAGER_TEST       = "my_money_test";
    public static final String DB_TEST_FILE              = "data/test.db";

    // Prevent instantiation
    private Constants() { }
}
