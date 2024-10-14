/*
 * Filename: AddWalletController.java
 * Created on: October  1, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mymoney.entities.WalletType;
import org.mymoney.services.WalletService;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Add Wallet dialog
 */
@Controller
public class AddWalletController
{
    @FXML
    private TextField walletNameField;

    @FXML
    private TextField walletBalanceField;

    @FXML
    private ComboBox<String> walletTypeComboBox;

    private WalletService walletService;

    private List<WalletType> walletTypes;

    public AddWalletController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public AddWalletController(WalletService walletService)
    {
        this.walletService = walletService;
    }

    @FXML
    private void initialize()
    {
        LoadWalletTypes();

        walletTypeComboBox.getItems().addAll(
            walletTypes.stream().map(WalletType::GetName).toList());
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)walletNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave()
    {
        String walletName = walletNameField.getText();
        walletName = walletName.strip(); // Remove leading and trailing whitespaces

        String walletBalanceStr = walletBalanceField.getText();
        String walletTypeStr    = walletTypeComboBox.getValue();

        if (walletName.isEmpty() || walletBalanceStr.isEmpty() || walletTypeStr == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Empty fields",
                                        "Please fill all the fields.");
            return;
        }

        WalletType walletType = walletTypes.stream()
                                    .filter(wt -> wt.GetName().equals(walletTypeStr))
                                    .findFirst()
                                    .get();

        try
        {
            Double walletBalance = Double.parseDouble(walletBalanceStr);

            walletService.CreateWallet(walletName, walletBalance, walletType);

            WindowUtils.ShowSuccessDialog("Success",
                                          "Wallet created",
                                          "The wallet was successfully created");

            Stage stage = (Stage)walletNameField.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid balance",
                                        "Please enter a valid balance.");
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Error creating wallet",
                                        e.getMessage());
        }
    }

    /**
     * Load the wallet types
     */
    private void LoadWalletTypes()
    {
        walletTypes = walletService.GetAllWalletTypes();

        String nameToMove = "Others";

        // Move the "Others" wallet type to the end of the list
        if (walletTypes.stream()
                .filter(n -> n.GetName().equals(nameToMove))
                .findFirst()
                .isPresent())
        {
            WalletType walletType = walletTypes.stream()
                                        .filter(wt -> wt.GetName().equals(nameToMove))
                                        .findFirst()
                                        .get();

            walletTypes.remove(walletType);
            walletTypes.add(walletType);
        }
    }
}
