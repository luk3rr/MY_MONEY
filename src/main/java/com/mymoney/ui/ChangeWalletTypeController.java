/*
 * Filename: ChangeWalletTypeController.java
 * Created on: October  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.mymoney.entities.Wallet;
import com.mymoney.entities.WalletType;
import com.mymoney.services.WalletService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Controller for the Change Wallet Type dialog
 */
@Component
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

    @FXML
    private void initialize()
    {
        LoadWallets();
        LoadWalletTypes();

        walletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());

        newTypeComboBox.getItems().addAll(
            walletTypes.stream().map(WalletType::GetName).toList());

        // Set the current type label
        walletComboBox.setOnAction(e -> {
            String walletName = walletComboBox.getValue();
            Wallet wallet = wallets.stream()
                            .filter(w -> w.GetName().equals(walletName))
                            .findFirst()
                            .get();

            UpdateCurrentTypeLabel(wallet);
        });
    }

    @FXML
    private void handleSave()
    {
        String walletName    = walletComboBox.getValue();
        String walletNewTypeStr = newTypeComboBox.getValue();

        if (walletName == null || walletNewTypeStr == null)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid input");
            alert.setContentText("Please fill all fields");
            alert.showAndWait();
            return;
        }

        Wallet wallet = wallets.stream()
                            .filter(w -> w.GetName().equals(walletName))
                            .findFirst()
                            .get();

        WalletType walletNewType = walletTypes.stream()
                                    .filter(wt -> wt.GetName().equals(walletNewTypeStr))
                                    .findFirst()
                                    .get();

        try
        {
            walletService.ChangeWalletType(wallet.GetId(), walletNewType);
        }
        catch (RuntimeException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid input");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
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
        wallets = walletService.GetAllWallets();
    }

    private void LoadWalletTypes()
    {
        walletTypes = walletService.GetAllWalletTypes();
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

    public void SetWalletComboBox(Wallet wt)
    {
        if (wallets.stream().noneMatch(w -> w.GetId() == wt.GetId()))
        {
            return;
        }

        walletComboBox.setValue(wt.GetName());

        UpdateCurrentTypeLabel(wt);
    }
}
