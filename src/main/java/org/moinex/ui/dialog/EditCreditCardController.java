/*
 * Filename: EditCreditCardController.java
 * Created on: October 24, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.dialog;

import java.math.BigDecimal;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.moinex.entities.CreditCard;
import org.moinex.entities.CreditCardOperator;
import org.moinex.entities.Wallet;
import org.moinex.services.CreditCardService;
import org.moinex.services.WalletService;
import org.moinex.util.Constants;
import org.moinex.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Edit Credit Card dialog
 */
@Controller
public class EditCreditCardController
{
    @FXML
    private TextField nameField;

    @FXML
    private TextField limitField;

    @FXML
    private TextField lastFourDigitsField;

    @FXML
    private ComboBox<String> closingDayComboBox;

    @FXML
    private ComboBox<String> dueDayComboBox;

    @FXML
    private ComboBox<String> operatorComboBox;

    @FXML
    private ComboBox<String> defaultBillingWalletComboBox;

    private CreditCardService creditCardService;

    private WalletService walletService;

    private List<CreditCardOperator> operators;

    private List<Wallet> wallets;

    private CreditCard crcToUpdate;

    public EditCreditCardController() { }

    /**
     * Constructor
     * @param creditCardService The credit card service
     * @param walletService The wallet service
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public EditCreditCardController(CreditCardService creditCardService,
                                    WalletService     walletService)
    {
        this.creditCardService = creditCardService;
        this.walletService     = walletService;
    }

    public void SetCreditCard(CreditCard crc)
    {
        crcToUpdate = crc;

        nameField.setText(crc.GetName());
        limitField.setText(crc.GetMaxDebt().toString());
        lastFourDigitsField.setText(crc.GetLastFourDigits());
        operatorComboBox.setValue(crc.GetOperator().GetName());
        closingDayComboBox.setValue(crc.GetClosingDay().toString());
        dueDayComboBox.setValue(crc.GetBillingDueDay().toString());

        if (crc.GetDefaultBillingWallet() != null)
        {
            defaultBillingWalletComboBox.setValue(
                crc.GetDefaultBillingWallet().GetName());
        }
        else
        {
            defaultBillingWalletComboBox.setValue(null);
        }
    }

    @FXML
    private void initialize()
    {
        PopulateComboBoxes();

        // Ensure that the limit field only accepts numbers and has a maximum of 4
        // digits
        lastFourDigitsField.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!newValue.matches(Constants.GetDigitsRegexUpTo(4)))
                {
                    lastFourDigitsField.setText(oldValue);
                }
            });

        limitField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(Constants.MONETARY_VALUE_REGEX))
            {
                limitField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)nameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave()
    {
        String crcName = nameField.getText();
        crcName        = crcName.strip(); // Remove leading and trailing whitespaces

        String crcLimitStr                 = limitField.getText();
        String crcLastFourDigitsStr        = lastFourDigitsField.getText();
        String crcClosingDayStr            = closingDayComboBox.getValue();
        String crcDueDayStr                = dueDayComboBox.getValue();
        String crcOperatorName             = operatorComboBox.getValue();
        String crcDefaultBillingWalletName = defaultBillingWalletComboBox.getValue();

        if (crcName.isEmpty() || crcLimitStr.isEmpty() ||
            crcLastFourDigitsStr.isEmpty() || crcOperatorName == null ||
            crcClosingDayStr == null || crcDueDayStr == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Empty fields",
                                        "Please fill all required fields.");

            return;
        }

        CreditCardOperator crcOperator =
            operators.stream()
                .filter(op -> op.GetName().equals(crcOperatorName))
                .findFirst()
                .get();

        try
        {
            BigDecimal crcLimit      = new BigDecimal(crcLimitStr);
            Integer    crcClosingDay = Integer.parseInt(crcClosingDayStr);
            Integer    crcDueDay     = Integer.parseInt(crcDueDayStr);

            Wallet crcDefaultBillingWallet =
                crcDefaultBillingWalletName != null &&
                        !crcDefaultBillingWalletName.isEmpty()
                    ? wallets.stream()
                          .filter(w -> w.GetName().equals(crcDefaultBillingWalletName))
                          .findFirst()
                          .get()
                    : null;

            Boolean defaultWalletChanged =
                (crcDefaultBillingWallet != null &&
                 crcToUpdate.GetDefaultBillingWallet() != null &&
                 crcDefaultBillingWallet.GetId() ==
                     crcToUpdate.GetDefaultBillingWallet().GetId()) ||
                (crcDefaultBillingWallet == null &&
                 crcToUpdate.GetDefaultBillingWallet() == null);

            // Check if has any modification
            if (crcToUpdate.GetName().equals(crcName) &&
                crcLimit.compareTo(crcToUpdate.GetMaxDebt()) == 0 &&
                crcToUpdate.GetLastFourDigits().equals(crcLastFourDigitsStr) &&
                crcToUpdate.GetClosingDay() == crcClosingDay &&
                crcToUpdate.GetBillingDueDay() == crcDueDay &&
                crcToUpdate.GetOperator().GetId() == crcOperator.GetId() &&
                defaultWalletChanged)
            {
                WindowUtils.ShowInformationDialog(
                    "Information",
                    "No changes",
                    "No changes were made to the credit card.");
            }
            else // If there is any modification, update the credit card
            {
                crcToUpdate.SetName(crcName);
                crcToUpdate.SetMaxDebt(crcLimit);
                crcToUpdate.SetLastFourDigits(crcLastFourDigitsStr);
                crcToUpdate.SetClosingDay(crcClosingDay);
                crcToUpdate.SetBillingDueDay(crcDueDay);
                crcToUpdate.SetOperator(crcOperator);
                crcToUpdate.SetDefaultBillingWallet(crcDefaultBillingWallet);

                creditCardService.UpdateCreditCard(crcToUpdate);

                WindowUtils.ShowSuccessDialog("Success",
                                              "Credit card updated",
                                              "The credit card updated successfully.");
            }

            Stage stage = (Stage)nameField.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid limit",
                                        "Please enter a valid limit");
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Error creating credit card",
                                        e.getMessage());
        }
    }

    private void LoadCreditCardOperators()
    {
        operators = creditCardService.GetAllCreditCardOperatorsOrderedByName();
    }

    private void LoadWallets()
    {
        wallets = walletService.GetAllNonArchivedWalletsOrderedByName();
    }

    private void PopulateComboBoxes()
    {
        for (int i = 1; i <= Constants.MAX_BILLING_DUE_DAY; i++)
        {
            closingDayComboBox.getItems().add(String.valueOf(i));
            dueDayComboBox.getItems().add(String.valueOf(i));
        }

        LoadCreditCardOperators();

        for (CreditCardOperator operator : operators)
        {
            operatorComboBox.getItems().add(operator.GetName());
        }

        LoadWallets();

        for (Wallet wallet : wallets)
        {
            defaultBillingWalletComboBox.getItems().add(wallet.GetName());
        }

        // Add blank option to the default billing wallet combo box
        defaultBillingWalletComboBox.getItems().add("");
    }
}
