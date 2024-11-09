/*
 * Filename: RenameWalletController.java
 * Created on: October  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.dialog;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.moinex.entities.Wallet;
import org.moinex.services.WalletService;
import org.moinex.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Rename Wallet dialog
 */
@Controller
public class RenameWalletController
{
    @FXML
    private ComboBox<String> walletComboBox;

    @FXML
    private TextField walletNewNameField;

    private List<Wallet> wallets;

    private WalletService walletService;

    public RenameWalletController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public RenameWalletController(WalletService walletService)
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
    }

    @FXML
    private void initialize()
    {
        LoadWallets();

        walletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());
    }

    @FXML
    private void handleSave()
    {
        String walletName    = walletComboBox.getValue();
        String walletNewName = walletNewNameField.getText();

        if (walletName == null || walletNewName.isBlank())
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
            walletService.RenameWallet(wallet.GetId(), walletNewName);
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
        wallets = walletService.GetAllWallets();
    }
}
