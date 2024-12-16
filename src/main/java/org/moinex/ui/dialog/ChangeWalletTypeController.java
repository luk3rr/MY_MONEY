/*
 * Filename: ChangeWalletTypeController.java
 * Created on: October  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.dialog;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.moinex.entities.Wallet;
import org.moinex.entities.WalletType;
import org.moinex.services.WalletService;
import org.moinex.util.Constants;
import org.moinex.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Change Wallet Type dialog
 */
@Controller
public class ChangeWalletTypeController
{
    @FXML
    private ComboBox<String> walletComboBox;

    @FXML
    private ComboBox<String> newTypeComboBox;

    @FXML
    private Label currentTypeLabel;

    private List<Wallet> wallets;

    private List<WalletType> walletTypes;

    private WalletService walletService;

    public ChangeWalletTypeController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public ChangeWalletTypeController(WalletService walletService)
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

        UpdateCurrentTypeLabel(wt);
    }

    @FXML
    private void initialize()
    {
        LoadWallets();
        LoadWalletTypes();

        walletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());

        // Add wallet types, but remove the default goal wallet type
        newTypeComboBox.getItems().addAll(
            walletTypes.stream()
                .filter(
                    wt -> !wt.GetName().equals(Constants.GOAL_DEFAULT_WALLET_TYPE_NAME))
                .map(WalletType::GetName)
                .toList());

        // Set the current type label
        walletComboBox.setOnAction(e -> {
            String walletName = walletComboBox.getValue();
            Wallet wallet     = wallets.stream()
                                .filter(w -> w.GetName().equals(walletName))
                                .findFirst()
                                .get();

            UpdateCurrentTypeLabel(wallet);
        });
    }

    @FXML
    private void handleSave()
    {
        String walletName       = walletComboBox.getValue();
        String walletNewTypeStr = newTypeComboBox.getValue();

        if (walletName == null || walletNewTypeStr == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Empty fields",
                                        "Please fill all the fields.");
            return;
        }

        Wallet wallet = wallets.stream()
                            .filter(w -> w.GetName().equals(walletName))
                            .findFirst()
                            .get();

        WalletType walletNewType =
            walletTypes.stream()
                .filter(wt -> wt.GetName().equals(walletNewTypeStr))
                .findFirst()
                .get();

        try
        {
            walletService.ChangeWalletType(wallet.GetId(), walletNewType);

            WindowUtils.ShowSuccessDialog("Success",
                                          "Wallet type changed",
                                          "The wallet type was successfully changed.");
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error", "Invalid input", e.getMessage());
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

    private void UpdateCurrentTypeLabel(Wallet wt)
    {
        if (wt == null)
        {
            currentTypeLabel.setText("-");
            return;
        }

        currentTypeLabel.setText(wt.GetType().GetName());
    }
}
