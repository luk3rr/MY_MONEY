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
    public static final String LOG_FILE            = "log/mymoney.log";
    public static final String FXML_PATH           = "/ui/main.fxml";
    public static final Double EPSILON             = 1e-6;
    public static final int    MAX_BILLING_DUE_DAY = 28;
    public static final Short  MAX_INSTALLMENTS    = 12 * 3;

    // Animation constants
    public static final double MENU_COLLAPSED_WIDTH    = 50;
    public static final double MENU_EXPANDED_WIDTH     = 200;
    public static final double MENU_ANIMATION_DURATION = 200; // ms

    // Prevent instantiation
    private Constants() { }
}
