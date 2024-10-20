/*
 * Filename: CreditCardPaneController.java
 * Created on: October 20, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.common;

import com.jfoenix.controls.JFXButton;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.Transfer;
import org.mymoney.entities.Wallet;
import org.mymoney.entities.WalletTransaction;
import org.mymoney.services.CreditCardService;
import org.mymoney.services.WalletService;
import org.mymoney.services.WalletTransactionService;
import org.mymoney.ui.dialog.AddExpenseController;
import org.mymoney.ui.dialog.AddIncomeController;
import org.mymoney.ui.dialog.AddTransferController;
import org.mymoney.ui.dialog.ChangeWalletTypeController;
import org.mymoney.ui.dialog.RenameWalletController;
import org.mymoney.ui.main.WalletController;
import org.mymoney.util.Constants;
import org.mymoney.util.TransactionStatus;
import org.mymoney.util.TransactionType;
import org.mymoney.util.UIUtils;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Credit Card Pane
 *
 * @note prototype is necessary so that each scene knows to which credit card it is
 * associated
 */
@Controller
@Scope("prototype") // Each instance of this controller is unique
public class CreditCardPaneController
{
    @FXML
    private VBox rootVBox;

    @FXML
    private ImageView crcOperatorIcon;

    @FXML
    private Label crcName;

    @FXML
    private Label crcOperator;

    @FXML
    private Label limitLabel;

    @FXML
    private Label pendingPaymentsLabel;

    @FXML
    private Label limitAvailableLabel;

    @FXML
    private Label closureDayLabel;

    @FXML
    private Label nextInvoiceLabel;

    @FXML
    private Label dueDateLabel;

    @FXML
    private Label invoiceStatus;

    @FXML
    private Label invoiceTotal;

    @FXML
    private Label debitedTransfersSign;

    @FXML
    private Label invoiceMonth;

    @FXML
    private JFXButton prevButton;

    @FXML
    private JFXButton nextButton;

    @FXML
    private ProgressBar limitProgressBar;

    private YearMonth currentDisplayedMonth;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private CreditCardService creditCardService;

    private CreditCard creditCard;

    public CreditCardPaneController() { }

    /**
     * Constructor
     * @param creditCardService CreditCardService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public CreditCardPaneController(CreditCardService creditCardService)
    {
        this.creditCardService = creditCardService;
    }

    @FXML
    private void initialize()
    {
        currentDisplayedMonth = YearMonth.now();
    }

    @FXML
    private void handleAddDebt()
    { }

    @FXML
    private void handleRenameCreditCard()
    { }

    @FXML
    private void handleChangeOperator()
    { }

    @FXML
    private void handleChangeLimit()
    { }

    @FXML
    private void handleDeleteCreditCard()
    { }

    @FXML
    private void handlePrevMonth()
    {
        currentDisplayedMonth = currentDisplayedMonth.minusMonths(1);
        UpdateInvoiceInfo();
    }

    @FXML
    private void handleNextMonth()
    {
        currentDisplayedMonth = currentDisplayedMonth.plusMonths(1);
        UpdateInvoiceInfo();
    }

    @FXML
    private void handleRegisterPayment()
    { }

    /**
     * Load the Credit Card Pane
     * @param creditCard Credit Card to load
     * @return The updated VBox
     */
    public VBox UpdateCreditCardPane(CreditCard crc)
    {
        // If the crc is null, do not update the pane
        if (crc == null)
        {
            SetDefaultValues();
            return rootVBox;
        }

        this.creditCard = crc;

        LoadCreditCardInfo();

        crcName.setText(creditCard.GetName());
        crcOperator.setText(creditCard.GetOperator().GetName());
        crcOperatorIcon.setImage(new Image(Constants.CRC_OPERATOR_ICONS_PATH +
                                           creditCard.GetOperator().GetIcon()));

        Double limit = creditCard.GetMaxDebt();
        Double pendingPayments =
            creditCardService.GetPendingPayments(creditCard.GetId(),
                                                 LocalDate.now().getMonthValue(),
                                                 LocalDate.now().getYear());

        Double limitAvailable =
            creditCardService.GetAvailableCredit(creditCard.GetId());

        limitLabel.setText(UIUtils.FormatCurrency(limit));

        pendingPaymentsLabel.setText(UIUtils.FormatCurrency(pendingPayments));

        limitAvailableLabel.setText(UIUtils.FormatCurrency(limitAvailable));

        // Set percentage of the usage of the limit
        limitProgressBar.setProgress(limit == 0 ? 0 : pendingPayments / limit);

        dueDateLabel.setText(creditCard.GetBillingDueDay().toString());

        closureDayLabel.setText(creditCard.GetClosingDay().toString());

        // Fromat LocalDateTime to MM/YYYY
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy");

        nextInvoiceLabel.setText(
            creditCardService.GetNextInvoiceDate(creditCard.GetId()).format(formatter));

        UpdateInvoiceInfo();

        return rootVBox;
    }

    public void UpdateInvoiceInfo()
    {
        if (creditCard == null)
        {
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy");

        invoiceMonth.setText(currentDisplayedMonth.format(formatter));

        invoiceTotal.setText(UIUtils.FormatCurrency(
            creditCardService.GetInvoiceAmount(creditCard.GetId(),
                                               currentDisplayedMonth.getMonthValue(),
                                               currentDisplayedMonth.getYear())));

        invoiceStatus.setText(
            creditCardService
                .GetInvoiceStatus(creditCard.GetId(),
                                  currentDisplayedMonth.getMonthValue(),
                                  currentDisplayedMonth.getYear())
                .toString());
    }

    /**
     * Load Credit Card information
     */
    public void LoadCreditCardInfo()
    {
        if (creditCard == null)
        {
            return;
        }
    }

    private void SetDefaultValues()
    {
        crcName.setText("");
        crcOperator.setText("");
        crcOperatorIcon.setImage(new Image(Constants.DEFAULT_ICON));

        // SetLabelValue(openingBalanceSign, openingBalanceValue, 0.0);
    }

    /**
     * Set the value of a label
     * @param signLabel Label to set the sign
     * @param valueLabel Label to set the value
     * @param value Value to set
     */
    private void SetLabelValue(Label signLabel, Label valueLabel, Double value)
    {
        if (value + Constants.EPSILON < 0)
        {
            signLabel.setText("-");
            valueLabel.setText(UIUtils.FormatCurrency(-value));
            UIUtils.SetLabelStyle(signLabel, Constants.NEGATIVE_BALANCE_STYLE);
            UIUtils.SetLabelStyle(valueLabel, Constants.NEGATIVE_BALANCE_STYLE);
        }
        else
        {
            signLabel.setText(" ");
            valueLabel.setText(UIUtils.FormatCurrency(value));
            UIUtils.SetLabelStyle(signLabel, Constants.NEUTRAL_BALANCE_STYLE);
            UIUtils.SetLabelStyle(valueLabel, Constants.NEUTRAL_BALANCE_STYLE);
        }
    }
}
