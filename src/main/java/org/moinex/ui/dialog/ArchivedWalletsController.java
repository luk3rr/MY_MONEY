/*
 * Filename: ArchivedWalletsController.java
 * Created on: October 15, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.dialog;

import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.moinex.entities.Wallet;
import org.moinex.services.WalletService;
import org.moinex.services.WalletTransactionService;
import org.moinex.util.WindowUtils;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Archived Wallets dialog
 */
@Controller
public class ArchivedWalletsController
{
    @FXML
    private TableView<Wallet> walletTableView;

    @FXML
    private TextField searchField;

    private List<Wallet> archivedWallets;

    private WalletService walletService;

    private WalletTransactionService walletTransactionService;

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    public ArchivedWalletsController(WalletService            walletService,
                                     WalletTransactionService walletTransactionService)
    {
        this.walletService            = walletService;
        this.walletTransactionService = walletTransactionService;
    }

    @FXML
    public void initialize()
    {
        LoadArchivedWalletsFromDatabase();

        ConfigureTableView();

        UpdateWalletTableView();

        // Add listener to the search field
        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> UpdateWalletTableView());
    }

    @FXML
    private void handleUnarchive()
    {
        Wallet selectedWallet = walletTableView.getSelectionModel().getSelectedItem();

        if (selectedWallet == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "No wallet selected",
                                        "Please select a wallet to unarchive");
            return;
        }

        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Unarchive wallet " + selectedWallet.GetName(),
                "Are you sure you want to unarchive this wallet?"))
        {
            try
            {
                walletService.UnarchiveWallet(selectedWallet.GetId());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Wallet unarchived",
                                              "Wallet " + selectedWallet.GetName() +
                                                  " has been unarchived");

                // Remove this wallet from the list and update the table view
                archivedWallets.remove(selectedWallet);
                UpdateWalletTableView();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error unarchiving wallet",
                                            e.getMessage());
                return;
            }
        }
    }

    @FXML
    private void handleDelete()
    {
        Wallet selectedWallet = walletTableView.getSelectionModel().getSelectedItem();

        if (selectedWallet == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "No wallet selected",
                                        "Please select a wallet to delete");
            return;
        }

        // Prevent the removal of a wallet with associated transactions
        if (walletTransactionService.GetTransactionCountByWallet(
                selectedWallet.GetId()) > 0)
        {
            WindowUtils.ShowErrorDialog(
                "Error",
                "Wallet has transactions",
                "Cannot delete a wallet with associated transactions");
            return;
        }

        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Remove wallet " + selectedWallet.GetName(),
                "Are you sure you want to remove this wallet?"))
        {
            try
            {
                walletService.DeleteWallet(selectedWallet.GetId());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Wallet deleted",
                                              "Wallet " + selectedWallet.GetName() +
                                                  " has been deleted");

                // Remove this wallet from the list and update the table view
                archivedWallets.remove(selectedWallet);
                UpdateWalletTableView();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error removing wallet",
                                            e.getMessage());
                return;
            }
        }
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)searchField.getScene().getWindow();
        stage.close();
    }

    /**
     * Loads the categories from the database
     */
    private void LoadArchivedWalletsFromDatabase()
    {
        archivedWallets = walletService.GetAllArchivedWallets();
    }

    /**
     * Updates the category table view
     */
    private void UpdateWalletTableView()
    {
        String similarTextOrId = searchField.getText().toLowerCase();

        walletTableView.getItems().clear();

        // Populate the table view
        if (similarTextOrId.isEmpty())
        {
            walletTableView.getItems().setAll(archivedWallets);
        }
        else
        {
            archivedWallets.stream()
                .filter(w
                        -> w.GetName().toLowerCase().contains(similarTextOrId) ||
                               w.GetId().toString().contains(similarTextOrId))
                .forEach(walletTableView.getItems()::add);
        }

        walletTableView.refresh();
    }

    /**
     * Configures the table view columns
     */
    private void ConfigureTableView()
    {
        TableColumn<Wallet, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetId()));

        idColumn.setCellFactory(column -> new TableCell<Wallet, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty)
            {
                super.updateItem(item, empty);
                if (item == null || empty)
                {
                    setText(null);
                }
                else
                {
                    setText(item.toString());
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-padding: 0;");
                }
            }
        });

        TableColumn<Wallet, String> walletColumn = new TableColumn<>("Wallet");
        walletColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetName()));

        TableColumn<Wallet, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetType().GetName()));

        TableColumn<Wallet, Long> numOfTransactionsColumn =
            new TableColumn<>("Associated Transactions");
        numOfTransactionsColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                walletTransactionService.GetTransactionCountByWallet(
                    param.getValue().GetId())));

        numOfTransactionsColumn.setCellFactory(column -> new TableCell<Wallet, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty)
            {
                super.updateItem(item, empty);
                if (item == null || empty)
                {
                    setText(null);
                }
                else
                {
                    setText(item.toString());
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-padding: 0;");
                }
            }
        });

        walletTableView.getColumns().add(idColumn);
        walletTableView.getColumns().add(walletColumn);
        walletTableView.getColumns().add(typeColumn);
        walletTableView.getColumns().add(numOfTransactionsColumn);
    }
}
