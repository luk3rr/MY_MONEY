/*
 * Filename: ResumePaneController.java
 * Created on: October 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.common;

import java.time.LocalDateTime;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.mymoney.entities.WalletTransaction;
import org.mymoney.services.CreditCardService;
import org.mymoney.services.WalletService;
import org.mymoney.util.Constants;
import org.mymoney.util.TransactionStatus;
import org.mymoney.util.TransactionType;
import org.mymoney.util.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Controller for the resume pane
 *
 * @note prototype is necessary so that each scene has its own controller
 * TODO: Fix calculation of the foreseen when the summary is not for the current year
 */
@Controller
@Scope("prototype") // Each instance of this controller is unique
public class ResumePaneController
{
    @FXML
    private Label incomesCurrentSign;

    @FXML
    private Label incomesCurrentValue;

    @FXML
    private Label incomesForeseenSign;

    @FXML
    private Label incomesForeseenValue;

    @FXML
    private Label expensesCurrentSign;

    @FXML
    private Label expensesCurrentValue;

    @FXML
    private Label expensesForeseenSign;

    @FXML
    private Label expensesForeseenValue;

    @FXML
    private Label balanceCurrentSign;

    @FXML
    private Label balanceCurrentValue;

    @FXML
    private Label balanceForeseenSign;

    @FXML
    private Label balanceForeseenValue;

    @FXML
    private Label savingsCurrentSign;

    @FXML
    private Label savingsCurrentValue;

    @FXML
    private Label savingsForeseenSign;

    @FXML
    private Label savingsForeseenValue;

    @FXML
    private Label savingsLabel;

    @FXML
    private Label creditCardsCurrentSign;

    @FXML
    private Label creditCardsCurrentValue;

    @FXML
    private Label creditCardsForeseenSign;

    @FXML
    private Label creditCardsForeseenValue;

    private WalletService walletService;

    CreditCardService creditCardService;

    @Autowired
    public ResumePaneController(WalletService     walletService,
                                CreditCardService creditCardService)
    {
        this.walletService     = walletService;
        this.creditCardService = creditCardService;
    }

    @FXML
    public void initialize()
    {
        LocalDateTime now = LocalDateTime.now();
        UpdateResumePane(now.getMonthValue(), now.getYear());
    }

    /**
     * Update the display of the resume
     */
    public void UpdateResumePane(Integer year)
    {
        List<WalletTransaction> allYearTransactions =
            walletService.GetAllTransactionsByYear(year);

        Double crcTotalDebtAmount = creditCardService.GetTotalDebtAmount(year);

        Double crcTotalPendingPayments =
            creditCardService.GetTotalPendingPayments(year);

        UpdateResumePane(allYearTransactions,
                         crcTotalDebtAmount,
                         crcTotalPendingPayments);
    }

    /**
     * Update the display of the month resume
     */
    public void UpdateResumePane(Integer month, Integer year)
    {
        List<WalletTransaction> transactions =
            walletService.GetAllTransactionsByMonth(month, year);

        Double crcTotalDebtAmount = creditCardService.GetTotalDebtAmount(month, year);

        Double crcTotalPendingPayments =
            creditCardService.GetTotalPendingPayments(month, year);

        UpdateResumePane(transactions, crcTotalDebtAmount, crcTotalPendingPayments);
    }

    private void UpdateResumePane(List<WalletTransaction> transactions,
                                  Double                  crcTotalDebtAmount,
                                  Double                  crcTotalPendingPayments)
    {
        Double totalConfirmedIncome =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.INCOME))
                .filter(t -> t.GetStatus().equals(TransactionStatus.CONFIRMED))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double totalConfirmedExpenses =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                .filter(t -> t.GetStatus().equals(TransactionStatus.CONFIRMED))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double totalForeseenIncome =
            transactions.stream()
                .filter(t -> t.GetType() == TransactionType.INCOME)
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double totalForeseenExpenses =
            transactions.stream()
                .filter(t -> t.GetType() == TransactionType.EXPENSE)
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double balance = totalConfirmedIncome - totalConfirmedExpenses;

        incomesCurrentValue.setText(UIUtils.FormatCurrency(totalConfirmedIncome));
        incomesCurrentSign.setText(" "); // default
        incomesCurrentValue.getStyleClass().clear();
        incomesCurrentValue.getStyleClass().add(Constants.POSITIVE_BALANCE_STYLE);

        incomesForeseenValue.setText(UIUtils.FormatCurrency(totalForeseenIncome));
        incomesForeseenSign.setText(" "); // default

        // Total Expenses
        expensesCurrentValue.setText(UIUtils.FormatCurrency(totalConfirmedExpenses));
        expensesCurrentSign.setText(" "); // default
        expensesCurrentValue.getStyleClass().clear();
        expensesCurrentValue.getStyleClass().add(Constants.NEGATIVE_BALANCE_STYLE);

        expensesForeseenValue.setText(UIUtils.FormatCurrency(totalForeseenExpenses));
        expensesForeseenSign.setText(" "); // default

        // Balance
        balanceCurrentValue.setText(UIUtils.FormatCurrency(balance));

        // Set the balance label and sign label according to the balance value
        if (balance > 0)
        {
            balanceCurrentValue.setText(UIUtils.FormatCurrency(balance));
            balanceCurrentSign.setText("+");

            balanceCurrentValue.getStyleClass().clear();
            balanceCurrentValue.getStyleClass().add(Constants.POSITIVE_BALANCE_STYLE);

            balanceCurrentSign.getStyleClass().clear();
            balanceCurrentSign.getStyleClass().add(Constants.POSITIVE_BALANCE_STYLE);
        }
        else if (balance < 0)
        {
            balanceCurrentValue.setText(UIUtils.FormatCurrency(-balance));
            balanceCurrentSign.setText("-");

            balanceCurrentValue.getStyleClass().clear();
            balanceCurrentValue.getStyleClass().add(Constants.NEGATIVE_BALANCE_STYLE);

            balanceCurrentSign.getStyleClass().clear();
            balanceCurrentSign.getStyleClass().add(Constants.NEGATIVE_BALANCE_STYLE);
        }
        else
        {
            balanceCurrentValue.setText(UIUtils.FormatCurrency(0.0));
            balanceCurrentSign.setText("");

            balanceCurrentValue.getStyleClass().clear();
            balanceCurrentValue.getStyleClass().add(Constants.NEUTRAL_BALANCE_STYLE);

            balanceCurrentSign.getStyleClass().clear();
            balanceCurrentSign.getStyleClass().add(Constants.NEUTRAL_BALANCE_STYLE);
        }

        Double foreseenBalance = totalForeseenIncome - totalForeseenExpenses;

        if (foreseenBalance > 0)
        {
            balanceForeseenValue.setText(UIUtils.FormatCurrency(foreseenBalance));
            balanceForeseenSign.setText("+");
        }
        else if (foreseenBalance < 0)
        {
            balanceForeseenValue.setText(UIUtils.FormatCurrency(-foreseenBalance));
            balanceForeseenSign.setText("-");
        }
        else
        {
            balanceForeseenValue.setText(UIUtils.FormatCurrency(0.0));
            balanceForeseenSign.setText(" ");
        }

        // Mensal Economies
        Double savingsPercentage = 0.0;

        if (totalConfirmedIncome <= 0)
        {
            savingsPercentage = 0.0;
        }
        else
        {
            savingsPercentage = (totalConfirmedIncome - totalConfirmedExpenses) /
                                totalConfirmedIncome * 100;
        }

        // Set the economy label and sign label according to the economy value
        if (savingsPercentage > 0)
        {
            savingsLabel.setText("Savings");
            savingsCurrentValue.setText(UIUtils.FormatPercentage(savingsPercentage));
            savingsCurrentSign.setText("+");

            savingsCurrentValue.getStyleClass().clear();
            savingsCurrentValue.getStyleClass().add(Constants.POSITIVE_BALANCE_STYLE);

            savingsCurrentSign.getStyleClass().clear();
            savingsCurrentSign.getStyleClass().add(Constants.POSITIVE_BALANCE_STYLE);
        }
        else if (savingsPercentage < 0)
        {
            savingsLabel.setText("No savings");
            savingsCurrentValue.setText(UIUtils.FormatPercentage(-savingsPercentage));
            savingsCurrentSign.setText("-");

            savingsCurrentValue.getStyleClass().clear();
            savingsCurrentValue.getStyleClass().add(Constants.NEGATIVE_BALANCE_STYLE);

            savingsCurrentSign.getStyleClass().clear();
            savingsCurrentSign.getStyleClass().add(Constants.NEGATIVE_BALANCE_STYLE);
        }
        else
        {
            savingsLabel.setText("No savings");
            savingsCurrentValue.setText(UIUtils.FormatPercentage(0.0));
            savingsCurrentSign.setText(" ");

            savingsCurrentValue.getStyleClass().clear();
            savingsCurrentValue.getStyleClass().add(Constants.NEUTRAL_BALANCE_STYLE);

            savingsCurrentSign.getStyleClass().clear();
            savingsCurrentSign.getStyleClass().add(Constants.NEUTRAL_BALANCE_STYLE);
        }

        Double foreseenSavingsPercentage = 0.0;

        if (totalForeseenIncome > 0)
        {
            foreseenSavingsPercentage = (totalForeseenIncome - totalForeseenExpenses) /
                                        totalForeseenIncome * 100;
        }

        if (foreseenSavingsPercentage > 0)
        {
            savingsForeseenValue.setText(
                UIUtils.FormatPercentage(foreseenSavingsPercentage));
            savingsForeseenSign.setText("+");
        }
        else if (foreseenSavingsPercentage < 0)
        {
            savingsForeseenValue.setText(
                UIUtils.FormatPercentage(-foreseenSavingsPercentage));
            savingsForeseenSign.setText("-");
        }
        else
        {
            savingsForeseenValue.setText(UIUtils.FormatPercentage(0.0));
            savingsForeseenSign.setText(" ");
        }

        // Credit Card
        creditCardsCurrentValue.setText(UIUtils.FormatCurrency(crcTotalDebtAmount));
        creditCardsCurrentSign.setText(" "); // default

        creditCardsForeseenValue.setText(
            String.format(UIUtils.FormatCurrency(crcTotalPendingPayments)));
        creditCardsForeseenSign.setText(" "); // default
    }
}
