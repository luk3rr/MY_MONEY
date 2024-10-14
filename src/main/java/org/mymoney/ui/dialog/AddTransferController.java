/*
 * Filename: AddTransferController.java
 * Created on: October  4, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mymoney.entities.Wallet;
import org.mymoney.services.WalletService;
import org.mymoney.util.Constants;
import org.mymoney.util.UIUtils;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Add Transfer dialog
 */
@Controller
public class AddTransferController
{
    @FXML
    private Label senderWalletAfterBalanceValueLabel;

    @FXML
    private Label receiverWalletAfterBalanceValueLabel;

    @FXML
    private Label senderWalletCurrentBalanceValueLabel;

    @FXML
    private Label receiverWalletCurrentBalanceValueLabel;

    @FXML
    private ComboBox<String> senderWalletComboBox;

    @FXML
    private ComboBox<String> receiverWalletComboBox;

    @FXML
    private TextField transferValueField;

    @FXML
    private TextField descriptionField;

    @FXML
    private DatePicker transferDatePicker;

    private WalletService walletService;

    private List<Wallet> wallets;

    public AddTransferController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public AddTransferController(WalletService walletService)
    {
        this.walletService = walletService;
    }

    @FXML
    private void initialize()
    {
        LoadWallets();

        // Configure the date picker
        UIUtils.SetDatePickerFormat(transferDatePicker);

        // Reset all labels
        ResetLabel(senderWalletAfterBalanceValueLabel);
        ResetLabel(receiverWalletAfterBalanceValueLabel);
        ResetLabel(senderWalletCurrentBalanceValueLabel);
        ResetLabel(receiverWalletCurrentBalanceValueLabel);

        senderWalletComboBox.setOnAction(e -> {
            UpdateSenderWalletBalance();
            UpdateSenderWalletAfterBalance();
        });

        receiverWalletComboBox.setOnAction(e -> {
            UpdateReceiverWalletBalance();
            UpdateReceiverWalletAfterBalance();
        });

        // Update sender wallet after balance when transfer value changes
        transferValueField.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                UpdateSenderWalletAfterBalance();
                UpdateReceiverWalletAfterBalance();
            });
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)descriptionField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave()
    {
        String    senderWalletName    = senderWalletComboBox.getValue();
        String    receiverWalletName  = receiverWalletComboBox.getValue();
        String    transferValueString = transferValueField.getText();
        String    description         = descriptionField.getText();
        LocalDate transferDate        = transferDatePicker.getValue();

        if (senderWalletName == null || receiverWalletName == null ||
            transferValueString == null || transferValueString.trim().isEmpty() ||
            description == null || description.trim().isEmpty() || transferDate == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Empty fields",
                                        "Please fill all the fields.");
            return;
        }

        try
        {
            Double transferValue = Double.parseDouble(transferValueString);

            Wallet senderWallet = wallets.stream()
                                      .filter(w -> w.GetName().equals(senderWalletName))
                                      .findFirst()
                                      .get();

            Wallet receiverWallet =
                wallets.stream()
                    .filter(w -> w.GetName().equals(receiverWalletName))
                    .findFirst()
                    .get();

            LocalTime     currentTime             = LocalTime.now();
            LocalDateTime dateTimeWithCurrentHour = transferDate.atTime(currentTime);

            walletService.TransferMoney(senderWallet.GetId(),
                                        receiverWallet.GetId(),
                                        dateTimeWithCurrentHour,
                                        transferValue,
                                        description);

            WindowUtils.ShowSuccessDialog("Success",
                                          "Transfer created",
                                          "The transfer was successfully created.");

            Stage stage = (Stage)descriptionField.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid transfer value",
                                        "Transfer value must be a number.");
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Error while creating transfer",
                                        e.getMessage());
            return;
        }
    }

    private void UpdateSenderWalletBalance()
    {
        String senderWalletName = senderWalletComboBox.getValue();

        if (senderWalletName == null)
        {
            return;
        }

        Wallet senderWallet = wallets.stream()
                                  .filter(w -> w.GetName().equals(senderWalletName))
                                  .findFirst()
                                  .get();

        senderWalletCurrentBalanceValueLabel.setText(
            UIUtils.FormatCurrency(senderWallet.GetBalance()));
    }

    private void UpdateReceiverWalletBalance()
    {
        String receiverWalletName = receiverWalletComboBox.getValue();

        if (receiverWalletName == null)
        {
            return;
        }

        Wallet receiverWallet = wallets.stream()
                                    .filter(w -> w.GetName().equals(receiverWalletName))
                                    .findFirst()
                                    .get();

        receiverWalletCurrentBalanceValueLabel.setText(
            UIUtils.FormatCurrency(receiverWallet.GetBalance()));
    }

    private void UpdateSenderWalletAfterBalance()
    {
        String transferValueString = transferValueField.getText();
        String senderWalletName    = senderWalletComboBox.getValue();

        if (transferValueString == null || transferValueString.trim().isEmpty() ||
            senderWalletName == null)
        {
            ResetLabel(senderWalletAfterBalanceValueLabel);
            return;
        }

        try
        {
            Double transferValue = Double.parseDouble(transferValueString);

            if (transferValue < 0)
            {
                ResetLabel(senderWalletAfterBalanceValueLabel);
                return;
            }

            Wallet senderWallet = wallets.stream()
                                      .filter(w -> w.GetName().equals(senderWalletName))
                                      .findFirst()
                                      .get();

            Double senderWalletAfterBalance = senderWallet.GetBalance() - transferValue;

            // Episilon is used to avoid floating point arithmetic errors
            if (senderWalletAfterBalance < Constants.EPSILON)
            {
                // Remove old style and add negative style
                SetLabelStyle(senderWalletAfterBalanceValueLabel,
                              Constants.NEGATIVE_BALANCE_STYLE);
            }
            else
            {
                // Remove old style and add neutral style
                SetLabelStyle(senderWalletAfterBalanceValueLabel,
                              Constants.NEUTRAL_BALANCE_STYLE);
            }

            senderWalletAfterBalanceValueLabel.setText(
                UIUtils.FormatCurrency(senderWalletAfterBalance));
        }
        catch (NumberFormatException e)
        {
            ResetLabel(senderWalletAfterBalanceValueLabel);
        }
    }

    private void UpdateReceiverWalletAfterBalance()
    {
        String transferValueString = transferValueField.getText();
        String receiverWalletName  = receiverWalletComboBox.getValue();

        if (transferValueString == null || transferValueString.trim().isEmpty() ||
            receiverWalletName == null)
        {
            ResetLabel(receiverWalletAfterBalanceValueLabel);
            return;
        }

        try
        {
            Double transferValue = Double.parseDouble(transferValueString);

            if (transferValue < 0)
            {
                ResetLabel(receiverWalletAfterBalanceValueLabel);
                return;
            }

            Wallet receiverWallet =
                wallets.stream()
                    .filter(w -> w.GetName().equals(receiverWalletName))
                    .findFirst()
                    .get();

            Double receiverWalletAfterBalance =
                receiverWallet.GetBalance() + transferValue;

            // Episilon is used to avoid floating point arithmetic errors
            if (receiverWalletAfterBalance < Constants.EPSILON)
            {
                // Remove old style and add negative style
                SetLabelStyle(receiverWalletAfterBalanceValueLabel,
                              Constants.NEGATIVE_BALANCE_STYLE);
            }
            else
            {
                // Remove old style and add neutral style
                SetLabelStyle(receiverWalletAfterBalanceValueLabel,
                              Constants.NEUTRAL_BALANCE_STYLE);
            }

            receiverWalletAfterBalanceValueLabel.setText(
                UIUtils.FormatCurrency(receiverWalletAfterBalance));
        }
        catch (NumberFormatException e)
        {
            ResetLabel(receiverWalletAfterBalanceValueLabel);
        }
    }

    private void LoadWallets()
    {
        wallets = walletService.GetAllWallets();

        senderWalletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());

        receiverWalletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());
    }

    private void ResetLabel(Label label)
    {
        label.setText("-");
        SetLabelStyle(label, Constants.NEUTRAL_BALANCE_STYLE);
    }

    private void SetLabelStyle(Label label, String style)
    {
        label.getStyleClass().removeAll(Constants.NEGATIVE_BALANCE_STYLE,
                                        Constants.POSITIVE_BALANCE_STYLE,
                                        Constants.NEUTRAL_BALANCE_STYLE);

        label.getStyleClass().add(style);
    }

    public void SetSenderWalletComboBox(Wallet wt)
    {
        if (wallets.stream().noneMatch(w -> w.GetId() == wt.GetId()))
        {
            return;
        }

        senderWalletComboBox.setValue(wt.GetName());

        UpdateSenderWalletBalance();
    }
}
