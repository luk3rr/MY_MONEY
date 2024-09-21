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
    public static final String LOG_FILE                = "log/mymoney.log";

    // FXML files
    public static final String MAIN_FXML               = "/ui/main.fxml";
    public static final String HOME_FXML               = "/ui/home.fxml";
    public static final String WALLET_FXML             = "/ui/wallet.fxml";
    public static final String CREDIT_CARD_FXML        = "/ui/credit_card.fxml";
    public static final String TRANSACTION_FXML        = "/ui/transaction.fxml";

    // Stylesheets
    public static final String MAIN_STYLE_SHEET        = "/css/main.css";
    public static final String HOME_STYLE_SHEET        = "/css/home.css";
    public static final String WALLET_STYLE_SHEET      = "/css/wallet.css";
    public static final String CREDIT_CARD_STYLE_SHEET = "/css/credit_card.css";
    public static final String TRANSACTION_STYLE_SHEET = "/css/transaction.css";

    public static final Integer HOME_PANES_ITEMS_PER_PAGE = 2;

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
