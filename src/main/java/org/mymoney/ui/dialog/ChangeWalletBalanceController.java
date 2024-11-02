/*
 * Filename: ChangeWalletBalanceController.java
 * Created on: October 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mymoney.entities.Wallet;
import org.mymoney.services.WalletService;
import org.mymoney.util.Constants;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Rename Wallet dialog
 */
@Controller
public class ChangeWalletBalanceController
{
    @FXML
    private ComboBox<String> walletComboBox;

    @FXML
    private TextField balanceField;

    private List<Wallet> wallets;

    private WalletService walletService;

    public ChangeWalletBalanceController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public ChangeWalletBalanceController(WalletService walletService)
    {
        this.walletService = walletService;
    }

    public void SetWalletComboBox(Wallet wt)
    {
        if (wallets.stream().noneMatch(w -> w.GetId() == wt.GetId()))
        {
            return;
        }

        walletComboBox.setValue(wt.GetName());
        balanceField.setText(wt.GetBalance().toString());
    }

    @FXML
    private void initialize()
    {
        LoadWallets();

        walletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());

        balanceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches(Constants.MONETARY_VALUE_REGEX))
            {
                balanceField.setText(oldValue);
            }
        });
    }

    @FXML
    private void handleSave()
    {
        String walletName    = walletComboBox.getValue();
        String newBalanceStr = balanceField.getText();

        if (walletName == null || newBalanceStr.isBlank())
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid input",
                                        "Please fill all fields");
            return;
        }

        Wallet wallet = wallets.stream()
                            .filter(w -> w.GetName().equals(walletName))
                            .findFirst()
                            .get();

        try
        {
            Double newBalance = Double.parseDouble(newBalanceStr);

            // Check if has modification
            if (Math.abs(wallet.GetBalance() - newBalance) < Constants.EPSILON)
            {
                WindowUtils.ShowInformationDialog("Information",
                                                  "No changes",
                                                  "The balance was not changed.");
                return;
            }
            else // Update balance
            {
                walletService.UpdateWalletBalance(wallet.GetId(), newBalance);

                WindowUtils.ShowSuccessDialog("Success",
                                              "Wallet updated",
                                              "The balance was updated successfully.");
            }

            Stage stage = (Stage)balanceField.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid input",
                                        "Balance must be a number");
            return;
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Error renaming wallet",
                                        e.getMessage());
            return;
        }

        Stage stage = (Stage)walletComboBox.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)walletComboBox.getScene().getWindow();
        stage.close();
    }

    private void LoadWallets()
    {
        wallets = walletService.GetAllNonArchivedWalletsOrderedByName();
    }
}
