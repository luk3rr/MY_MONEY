/*
 * Filename: ChangeWalletBalanceController.java
 * Created on: October 30, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.dialog;

import java.math.BigDecimal;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.moinex.entities.Wallet;
import org.moinex.services.WalletService;
import org.moinex.util.Constants;
import org.moinex.util.WindowUtils;
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
            BigDecimal newBalance = new BigDecimal(newBalanceStr);

            // Check if has modification
            if (wallet.GetBalance().compareTo(newBalance) == 0)
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
