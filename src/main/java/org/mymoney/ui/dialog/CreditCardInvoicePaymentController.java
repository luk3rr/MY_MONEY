/*
 * Filename: CreditCardInvoicePaymentController.java
 * Created on: October 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.CreditCardPayment;
import org.mymoney.entities.Wallet;
import org.mymoney.services.CreditCardService;
import org.mymoney.services.WalletService;
import org.mymoney.util.Constants;
import org.mymoney.util.UIUtils;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Credit card invoice payment dialog
 */
@Controller
public class CreditCardInvoicePaymentController
{
    @FXML
    private Label crcNameLabel;

    @FXML
    private Label crcInvoiceDueLabel;

    @FXML
    private Label crcInvoiceMonthLabel;

    @FXML
    private Label walletAfterBalanceLabel;

    @FXML
    private Label walletCurrentBalanceLabel;

    @FXML
    private ComboBox<String> walletComboBox;

    private WalletService walletService;

    private CreditCardService creditCardService;

    private List<Wallet> wallets;

    private CreditCard creditCard;

    private YearMonth invoiceDate;

    public CreditCardInvoicePaymentController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @param creditCardService CreditCardService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public CreditCardInvoicePaymentController(WalletService     walletService,
                                              CreditCardService creditCardService)
    {
        this.walletService     = walletService;
        this.creditCardService = creditCardService;
    }

    public void SetCreditCard(CreditCard crc, YearMonth invoiceDate)
    {
        this.creditCard  = crc;
        this.invoiceDate = invoiceDate;

        crcNameLabel.setText(crc.GetName());

        Double invoiceAmount =
            creditCardService
                .GetPendingCreditCardPayments(crc.GetId(),
                                              invoiceDate.getMonthValue(),
                                              invoiceDate.getYear())
                .stream()
                .mapToDouble(CreditCardPayment::GetAmount)
                .sum();

        crcInvoiceDueLabel.setText(UIUtils.FormatCurrency(invoiceAmount));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy");

        crcInvoiceMonthLabel.setText(invoiceDate.format(formatter));
    }

    @FXML
    private void initialize()
    {
        LoadWallets();

        // Reset all labels
        UIUtils.ResetLabel(walletAfterBalanceLabel);
        UIUtils.ResetLabel(walletCurrentBalanceLabel);
        UIUtils.ResetLabel(crcNameLabel);
        UIUtils.ResetLabel(crcInvoiceDueLabel);
        UIUtils.ResetLabel(crcInvoiceMonthLabel);

        walletComboBox.setOnAction(e -> {
            UpdateWalletBalance();
            WalletAfterBalance();
        });
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)crcNameLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave()
    {
        String walletName = walletComboBox.getValue();

        if (walletName == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Wallet not selected",
                                        "Please select a wallet");
            return;
        }

        Double invoiceAmount =
            creditCardService
                .GetPendingCreditCardPayments(creditCard.GetId(),
                                              invoiceDate.getMonthValue(),
                                              invoiceDate.getYear())
                .stream()
                .mapToDouble(CreditCardPayment::GetAmount)
                .sum();

        if (Math.abs(invoiceAmount) < Constants.EPSILON)
        {
            WindowUtils.ShowInformationDialog("Information",
                                              "Invoice already paid",
                                              "This invoice has already been paid");
        }
        else
        {
            try
            {
                Wallet wallet = wallets.stream()
                                    .filter(w -> w.GetName().equals(walletName))
                                    .findFirst()
                                    .get();

                creditCardService.PayInvoice(creditCard.GetId(),
                                             wallet.GetId(),
                                             invoiceDate.getMonthValue(),
                                             invoiceDate.getYear());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Invoice paid",
                                              "Invoice was successfully paid");

                Stage stage = (Stage)crcNameLabel.getScene().getWindow();
                stage.close();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error paying invoice",
                                            e.getMessage());
            }
        }

        Stage stage = (Stage)crcNameLabel.getScene().getWindow();
        stage.close();
    }

    private void UpdateWalletBalance()
    {
        String walletName = walletComboBox.getValue();

        if (walletName == null)
        {
            return;
        }

        Wallet wallet = wallets.stream()
                            .filter(w -> w.GetName().equals(walletName))
                            .findFirst()
                            .get();

        if (wallet.GetBalance() < 0)
        {
            UIUtils.SetLabelStyle(walletCurrentBalanceLabel,
                                  Constants.NEGATIVE_BALANCE_STYLE);
        }
        else
        {
            UIUtils.SetLabelStyle(walletCurrentBalanceLabel,
                                  Constants.NEUTRAL_BALANCE_STYLE);
        }

        walletCurrentBalanceLabel.setText(UIUtils.FormatCurrency(wallet.GetBalance()));
    }

    private void WalletAfterBalance()
    {
        String walletName = walletComboBox.getValue();

        if (walletName == null)
        {
            UIUtils.ResetLabel(walletAfterBalanceLabel);
            return;
        }

        Double invoiceAmount =
            creditCardService
                .GetPendingCreditCardPayments(creditCard.GetId(),
                                              invoiceDate.getMonthValue(),
                                              invoiceDate.getYear())
                .stream()
                .mapToDouble(CreditCardPayment::GetAmount)
                .sum();

        try
        {
            Wallet wallet = wallets.stream()
                                .filter(w -> w.GetName().equals(walletName))
                                .findFirst()
                                .get();

            Double walletAfterBalanceValue = wallet.GetBalance() - invoiceAmount;

            // Set the style according to the balance value after the expense
            if (walletAfterBalanceValue < 0)
            {
                // Remove old style and add negative style
                UIUtils.SetLabelStyle(walletAfterBalanceLabel,
                                      Constants.NEGATIVE_BALANCE_STYLE);
            }
            else
            {
                // Remove old style and add neutral style
                UIUtils.SetLabelStyle(walletAfterBalanceLabel,
                                      Constants.NEUTRAL_BALANCE_STYLE);
            }

            walletAfterBalanceLabel.setText(
                UIUtils.FormatCurrency(walletAfterBalanceValue));
        }
        catch (NumberFormatException e)
        {
            UIUtils.ResetLabel(walletAfterBalanceLabel);
        }
    }

    private void LoadWallets()
    {
        wallets = walletService.GetAllWallets();

        walletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());
    }
}
