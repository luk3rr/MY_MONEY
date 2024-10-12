/*
 * Filename: AddWalletController.java
 * Created on: October  1, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui.dialog;

import com.mymoney.entities.WalletType;
import com.mymoney.services.WalletService;
import com.mymoney.util.Constants;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
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
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Empty fields");
            alert.setContentText("Please fill all the fields.");
            alert.showAndWait();
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

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setGraphic(new ImageView(
                new Image(this.getClass()
                              .getResource(Constants.COMMON_ICONS_PATH + "success.png")
                              .toString())));

            alert.setTitle("Success");
            alert.setHeaderText("Wallet created");
            alert.setContentText("The wallet was successfully created.");
            alert.showAndWait();

            Stage stage = (Stage)walletNameField.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid balance");
            alert.setContentText("Please enter a valid balance.");
            alert.showAndWait();
        }
        catch (RuntimeException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error while creating wallet");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
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
