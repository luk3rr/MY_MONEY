/*
 * Filename: Constants.java
 * Created on: August 28, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.util;

import java.time.format.DateTimeFormatter;

/**
 * Constants used in the application
 */
public final class Constants
{
    public static final String LOG_FILE = "log/mymoney.log";
    public static final String APP_NAME = "My Money";

    // main fxml
    public static final String MAIN_FXML        = "/ui/main/main.fxml";
    public static final String HOME_FXML        = "/ui/main/home.fxml";
    public static final String WALLET_FXML      = "/ui/main/wallet.fxml";
    public static final String CREDIT_CARD_FXML = "/ui/main/credit_card.fxml";
    public static final String TRANSACTION_FXML = "/ui/main/transaction.fxml";
    public static final String SAVINGS_FXML     = "/ui/main/savings.fxml";

    // dialog fxml
    public static final String ADD_WALLET_FXML   = "/ui/dialog/add_wallet.fxml";
    public static final String ADD_TRANSFER_FXML = "/ui/dialog/add_transfer.fxml";
    public static final String ADD_EXPENSE_FXML  = "/ui/dialog/add_expense.fxml";
    public static final String ADD_INCOME_FXML   = "/ui/dialog/add_income.fxml";
    public static final String REMOVE_TRANSACTION_FXML =
        "/ui/dialog/remove_transaction.fxml";
    public static final String CHANGE_WALLET_TYPE_FXML =
        "/ui/dialog/change_wallet_type.fxml";
    public static final String RENAME_WALLET_FXML = "/ui/dialog/rename_wallet.fxml";

    // common fxml
    public static final String WALLET_FULL_PANE_FXML =
        "/ui/common/wallet_full_pane.fxml";
    public static final String RESUME_PANE_FXML = "/ui/common/resume_pane.fxml";

    // Icons paths
    public static final String WALLET_TYPE_ICONS_PATH  = "/icon/wallet_type/";
    public static final String SIDEBAR_ICONS_PATH      = "/icon/sidebar/";
    public static final String CRC_OPERATOR_ICONS_PATH = "/icon/crc_operator/";
    public static final String COMMON_ICONS_PATH       = "/icon/common/";

    // Icons
    public static final String HOME_EXPENSE_ICON = "/icon/common/expense.png";
    public static final String HOME_INCOME_ICON  = "/icon/common/income.png";

    // Scenes styles
    public static final String MAIN_STYLE_SHEET        = "/css/scene/main.css";
    public static final String HOME_STYLE_SHEET        = "/css/scene/home.css";
    public static final String WALLET_STYLE_SHEET      = "/css/scene/wallet.css";
    public static final String CREDIT_CARD_STYLE_SHEET = "/css/scene/credit-card.css";
    public static final String TRANSACTION_STYLE_SHEET = "/css/scene/transaction.css";
    public static final String SAVINGS_STYLE_SHEET     = "/css/scene/savings.css";

    // Common styles
    public static final String COMMON_STYLE_SHEET = "/css/common/common-styles.css";

    // Main pane styles
    public static final String SIDEBAR_SELECTED_BUTTON_STYLE =
        "sidebar-button-selected";
    public static final String NEGATIVE_BALANCE_STYLE = "negative-balance";
    public static final String POSITIVE_BALANCE_STYLE = "positive-balance";
    public static final String NEUTRAL_BALANCE_STYLE  = "neutral-balance";

    // Home pane styles
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
    public static final String HOME_CREDIT_CARD_ITEM_NAME_STYLE =
        "credit-card-item-name";
    public static final String HOME_CREDIT_CARD_ITEM_BALANCE_STYLE =
        "credit-card-item-balance";
    public static final String HOME_CREDIT_CARD_ITEM_DIGITS_STYLE =
        "credit-card-item-digits";
    public static final String HOME_CREDIT_CARD_ITEM_OPERATOR_STYLE =
        "credit-card-item-operator";
    public static final String HOME_CREDIT_CARD_TYPE_STYLE    = "credit-card-item-type";
    public static final String HOME_WALLET_ITEM_STYLE         = "wallet-item";
    public static final String HOME_WALLET_ITEM_NAME_STYLE    = "wallet-item-name";
    public static final String HOME_WALLET_ITEM_BALANCE_STYLE = "wallet-item-balance";
    public static final String HOME_WALLET_TYPE_STYLE         = "wallet-item-type";
    public static final String HOME_MONTH_RESUME_POSITIVE_LABEL_STYLE =
        "month-resume-positive-label";
    public static final String HOME_MONTH_RESUME_NEGATIVE_LABEL_STYLE =
        "month-resume-negative-label";
    public static final String HOME_MONTH_RESUME_ZERO_LABEL_STYLE =
        "month-resume-zero-label";
    public static final String HOME_MONTH_RESUME_EXPECTED_LABEL_STYLE =
        "month-resume-expected-label";
    public static final String HOME_TOOLTIP_STYLE = "tooltip";

    // Wallet pane styles
    public static final String WALLET_TOTAL_BALANCE_VALUE_LABEL_STYLE =
        "total-balance-value-label";
    public static final String WALLET_TOTAL_BALANCE_FORESEEN_LABEL_STYLE =
        "total-balance-foreseen-label";
    public static final String WALLET_TOTAL_BALANCE_WALLETS_LABEL_STYLE =
        "total-balance-wallets-label";
    public static final String WALLET_CHECK_BOX_STYLE = "check-box";

    // Sizes
    public static final Integer WALLET_TYPE_ICONS_SIZE           = 42; // 42x42 px
    public static final Integer CRC_OPERATOR_ICONS_SIZE          = 42; // 42x42 px
    public static final Integer HOME_LAST_TRANSACTIONS_SIZE      = 7;
    public static final Integer HOME_LAST_TRANSACTIONS_ICON_SIZE = 32; // 32x32 px
    public static final Integer HOME_LAST_TRANSACTIONS_DESCRIPTION_LABEL_WIDTH = 280;
    public static final Integer HOME_LAST_TRANSACTIONS_VALUE_LABEL_WIDTH       = 70;
    public static final Integer HOME_LAST_TRANSACTIONS_DATE_LABEL_WIDTH        = 80;
    public static final Integer HOME_LAST_TRANSACTIONS_WALLET_LABEL_WIDTH      = 100;
    public static final Integer HOME_LAST_TRANSACTIONS_CATEGORY_LABEL_WIDTH    = 100;
    public static final Integer HOME_LAST_TRANSACTIONS_STATUS_LABEL_WIDTH      = 80;
    public static final Integer HOME_PANES_ITEMS_PER_PAGE                      = 2;
    public static final Integer HOME_BAR_CHART_MONTHS                          = 12;
    public static final Integer HOME_BAR_CHART_TICKS                           = 6;
    public static final Integer HOME_MONTH_RESUME_TEXT_LABEL_WIDTH             = 120;
    public static final Integer HOME_MONTH_RESUME_SIGN_LABEL_WIDTH             = 10;
    public static final Integer HOME_MONTH_RESUME_TEXT_EXPECTED_LABEL_WIDTH =
        HOME_MONTH_RESUME_TEXT_LABEL_WIDTH + HOME_MONTH_RESUME_SIGN_LABEL_WIDTH;

    public static final Double EPSILON          = 1e-6;
    public static final Double ONE_SECOND_IN_NS = 1_000_000_000.0;

    // Credit card
    public static final Integer MAX_BILLING_DUE_DAY = 28;
    public static final Short   MAX_INSTALLMENTS    = 12 * 3;

    // Animation constants
    public static final Double  MENU_COLLAPSED_WIDTH              = 80.0;
    public static final Double  MENU_EXPANDED_WIDTH               = 220.0;
    public static final Double  MENU_ANIMATION_DURATION           = 200.0; // ms
    public static final Integer HOME_BAR_CHART_ANIMATION_FRAMES   = 30;
    public static final Double  HOME_BAR_CHART_ANIMATION_DURATION = 0.7; // s
    public static final Double  HOME_TOOLTIP_ANIMATION_DURATION   = 0.5; // s
    public static final Double  HOME_TOOLTIP_ANIMATION_DELAY      = 0.5; // s

    public static final String DATE_FORMAT_WITH_TIME = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATE_FORMAT_NO_TIME   = "yyyy-MM-dd";

    public static final DateTimeFormatter DATE_TIME_FORMATTER_WITH_TIME =
        DateTimeFormatter.ofPattern(DATE_FORMAT_WITH_TIME);

    public static final DateTimeFormatter DATE_FORMATTER_NO_TIME =
        DateTimeFormatter.ofPattern(DATE_FORMAT_NO_TIME);

    // Prevent instantiation
    private Constants() { }
}
