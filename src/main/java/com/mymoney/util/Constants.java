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
    public static final String LOG_FILE = "log/mymoney.log";
    public static final String APP_NAME = "My Money";

    // FXML files
    public static final String MAIN_FXML        = "/ui/main.fxml";
    public static final String HOME_FXML        = "/ui/home.fxml";
    public static final String WALLET_FXML      = "/ui/wallet.fxml";
    public static final String CREDIT_CARD_FXML = "/ui/credit_card.fxml";
    public static final String TRANSACTION_FXML = "/ui/transaction.fxml";

    // Stylesheets
    public static final String MAIN_STYLE_SHEET        = "/css/main.css";
    public static final String HOME_STYLE_SHEET        = "/css/home.css";
    public static final String WALLET_STYLE_SHEET      = "/css/wallet.css";
    public static final String CREDIT_CARD_STYLE_SHEET = "/css/credit_card.css";
    public static final String TRANSACTION_STYLE_SHEET = "/css/transaction.css";

    // Styles
    public static final String SIDEBAR_SELECTED_BUTTON_STYLE =
        "sidebar-button-selected";
    public static final String HOME_LAST_TRANSACTIONS_DESCRIPTION_VALUE_STYLE =
        "description-value-box";
    public static final String HOME_LAST_TRANSACTIONS_WALLET_DATE_STYLE =
        "wallet-date-box";
    public static final String HOME_LAST_TRANSACTIONS_TRANSACTION_DETAILS_STYLE =
        "transaction-details";
    public static final String HOME_LAST_TRANSACTIONS_INCOME_ITEM_STYLE = "income-item";
    public static final String HOME_LAST_TRANSACTIONS_EXPENSE_ITEM_STYLE =
        "expense-item";
    public static final String HOME_CREDIT_CARD_ITEM_STYLE = "credit-card-item";
    public static final String HOME_WALLET_ITEM_STYLE      = "wallet-item";
    public static final String HOME_MONTH_RESUME_POSITIVE_LABEL_STYLE =
        "month-resume-positive-label";
    public static final String HOME_MONTH_RESUME_NEGATIVE_LABEL_STYLE =
        "month-resume-negative-label";
    public static final String HOME_MONTH_RESUME_ZERO_LABEL_STYLE =
        "month-resume-zero-label";

    // Icons
    public static final String EXPENSE_ICON = "/icons/expense.png";
    public static final String INCOME_ICON  = "/icons/income.png";

    public static final Integer HOME_LAST_TRANSACTIONS_SIZE      = 6;
    public static final Integer HOME_LAST_TRANSACTIONS_ICON_SIZE = 32; // 32x32 px
    public static final Integer HOME_LAST_TRANSACTIONS_DESCRIPTION_LABEL_WIDTH = 180;
    public static final Integer HOME_LAST_TRANSACTIONS_VALUE_LABEL_WIDTH       = 70;
    public static final Integer HOME_LAST_TRANSACTIONS_DATE_LABEL_WIDTH        = 80;
    public static final Integer HOME_LAST_TRANSACTIONS_WALLET_LABEL_WIDTH      = 100;
    public static final Integer HOME_PANES_ITEMS_PER_PAGE                      = 2;
    public static final Integer HOME_BAR_CHART_MONTHS                          = 12;
    public static final Integer HOME_BAR_CHART_TICK_UNIT                       = 250;
    public static final Integer HOME_MONTH_RESUME_TEXT_LABEL_WIDTH             = 100;
    public static final Integer HOME_MONTH_RESUME_VALUE_LABEL_WIDTH            = 100;
    public static final Integer HOME_MONTH_RESUME_SIGN_LABEL_WIDTH             = 10;

    public static final Double EPSILON          = 1e-6;
    public static final Double ONE_SECOND_IN_NS = 1_000_000_000.0;

    // Credit card
    public static final Integer MAX_BILLING_DUE_DAY = 28;
    public static final Short   MAX_INSTALLMENTS    = 12 * 3;

    // Animation constants
    public static final Double  MENU_COLLAPSED_WIDTH              = 50.0;
    public static final Double  MENU_EXPANDED_WIDTH               = 250.0;
    public static final Double  MENU_ANIMATION_DURATION           = 200.0; // ms
    public static final Integer HOME_BAR_CHART_ANIMATION_FRAMES   = 30;
    public static final Double  HOME_BAR_CHART_ANIMATION_DURATION = 0.7; // s

    // Prevent instantiation
    private Constants() { }
}
