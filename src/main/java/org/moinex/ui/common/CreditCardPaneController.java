/*
 * Filename: CreditCardPaneController.java
 * Created on: October 20, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.common;

import com.jfoenix.controls.JFXButton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.moinex.entities.CreditCard;
import org.moinex.services.CreditCardService;
import org.moinex.ui.dialog.AddCreditCardDebtController;
import org.moinex.ui.dialog.CreditCardInvoicePaymentController;
import org.moinex.ui.dialog.EditCreditCardController;
import org.moinex.ui.main.CreditCardController;
import org.moinex.util.Constants;
import org.moinex.util.UIUtils;
import org.moinex.util.WindowUtils;
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
    private Label invoiceMonth;

    @FXML
    private Label limitProgressLabel;

    @FXML
    private JFXButton prevButton;

    @FXML
    private JFXButton nextButton;

    @FXML
    private ProgressBar limitProgressBar;

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private CreditCardController creditCardController;

    private YearMonth currentDisplayedMonth;

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
    {
        WindowUtils.OpenModalWindow(
            Constants.ADD_CREDIT_CARD_DEBT_FXML,
            "Add Credit Card Debt",
            springContext,
            (AddCreditCardDebtController controller)
                -> { controller.SetCreditCard(creditCard); },
            List.of(() -> creditCardController.UpdateDisplay()));
    }

    @FXML
    private void handleEditCreditCard()
    {
        WindowUtils.OpenModalWindow(
            Constants.EDIT_CREDIT_CARD_FXML,
            "Edit Credit Card",
            springContext,
            (EditCreditCardController controller)
                -> { controller.SetCreditCard(creditCard); },
            List.of(() -> creditCardController.UpdateDisplay()));
    }

    @FXML
    private void handleArchiveCreditCard()
    {
        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Archive credit card " + creditCard.GetName(),
                "Are you sure you want to archive this credit card?"))
        {
            try
            {
                creditCardService.ArchiveCreditCard(creditCard.GetId());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Credit card archived",
                                              "Credit card " + creditCard.GetName() +
                                                  " has been archived");

                // Update credit card display in the main window
                creditCardController.UpdateDisplay();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error archiving credit card",
                                            e.getMessage());
                return;
            }
        }
    }

    @FXML
    private void handleDeleteCreditCard()
    {
        // Prevent the removal of a credit card with associated debts
        if (creditCardService.GetDebtCountByCreditCard(creditCard.GetId()) > 0)
        {
            WindowUtils.ShowErrorDialog(
                "Error",
                "Credit card has debts",
                "Cannot delete a credit card with associated debts");
            return;
        }

        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Delete credit card " + creditCard.GetName(),
                "Are you sure you want to remove this credit card?"))
        {
            try
            {
                creditCardService.DeleteCreditCard(creditCard.GetId());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Credit card deleted",
                                              "Credit card " + creditCard.GetName() +
                                                  " has been deleted");

                // Update credit card display in the main window
                creditCardController.UpdateDisplay();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error removing credit card",
                                            e.getMessage());
                return;
            }
        }
    }

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
    {
        WindowUtils.OpenModalWindow(
            Constants.CREDIT_CARD_INVOICE_PAYMENT_FXML,
            "Register Payment",
            springContext,
            (CreditCardInvoicePaymentController controller)
                -> { controller.SetCreditCard(creditCard, currentDisplayedMonth); },
            // Update the display after the payment is registered with the current month
            List.of(
                () -> { creditCardController.UpdateDisplay(currentDisplayedMonth); }));
    }

    /**
     * Load the Credit Card Pane
     * @param creditCard Credit Card to load
     * @return The updated VBox
     */
    public VBox UpdateCreditCardPane(CreditCard crc, YearMonth month)
    {
        // If the crc is null, do not update the pane
        if (crc == null)
        {
            SetDefaultValues();
            return rootVBox;
        }

        this.creditCard            = crc;
        this.currentDisplayedMonth = month;

        crcName.setText(creditCard.GetName());
        crcOperator.setText(creditCard.GetOperator().GetName());
        crcOperatorIcon.setImage(new Image(Constants.CRC_OPERATOR_ICONS_PATH +
                                           creditCard.GetOperator().GetIcon()));

        BigDecimal limit = creditCard.GetMaxDebt();
        BigDecimal pendingPayments =
            creditCardService.GetTotalPendingPayments(creditCard.GetId());

        BigDecimal limitAvailable =
            creditCardService.GetAvailableCredit(creditCard.GetId());

        limitLabel.setText(UIUtils.FormatCurrency(limit));

        pendingPaymentsLabel.setText(UIUtils.FormatCurrency(pendingPayments));

        limitAvailableLabel.setText(UIUtils.FormatCurrency(limitAvailable));

        // Set percentage of the usage of the limit
        BigDecimal limitProgress =
            limit.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : pendingPayments.divide(limit, 2, RoundingMode.HALF_UP);

        limitProgressBar.setProgress(limitProgress.doubleValue());
        limitProgressLabel.setText(
            UIUtils.FormatPercentage(limitProgress.doubleValue() * 100));

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

    private void SetDefaultValues()
    {
        crcName.setText("");
        crcOperator.setText("");
        limitLabel.setText("");
        pendingPaymentsLabel.setText("");
        limitAvailableLabel.setText("");
        closureDayLabel.setText("");
        nextInvoiceLabel.setText("");
        dueDateLabel.setText("");
        invoiceStatus.setText("");
        invoiceTotal.setText("");
        invoiceMonth.setText("");
        crcOperatorIcon.setImage(new Image(Constants.DEFAULT_ICON));
    }
}
