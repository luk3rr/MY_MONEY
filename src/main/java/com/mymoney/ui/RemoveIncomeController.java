/*
 * Filename: RemoveIncomeController.fxml
 * Created on: October 12, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.mymoney.entities.WalletTransaction;
import com.mymoney.services.WalletService;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class RemoveIncomeController
{
    @FXML
    private TableView<WalletTransaction> transactionsTableView;

    @FXML
    private TextField searchField;

    private List<WalletTransaction> incomes;

    private WalletService walletService;

    public RemoveIncomeController(WalletService walletService)
    {
        this.walletService = walletService;
    }

    @FXML
    public void initialize()
    {
        ConfigureTableView();
        LoadTransaction();

        UpdateTransactionTableView();

        // Add listener to the search field
        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> UpdateTransactionTableView());
    }

    @FXML
    public void handleRemove()
    {
        WalletTransaction selectedIncome =
            transactionsTableView.getSelectionModel().getSelectedItem();

        // If no income is selected, do nothing
        if (selectedIncome == null)
        {
            return;
        }

        // Confirm the deletion
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to remove this income?");

        Text descriptionBold = new Text("Description: ");
        descriptionBold.setStyle("-fx-font-weight: bold;");
        Text descriptionNormal = new Text(selectedIncome.GetDescription() + "\n");

        Text amountBold = new Text("Amount: ");
        amountBold.setStyle("-fx-font-weight: bold;");
        Text amountNormal = new Text("$ " + selectedIncome.GetAmount() + "\n");

        Text dateBold = new Text("Date: ");
        dateBold.setStyle("-fx-font-weight: bold;");
        Text dateNormal = new Text(selectedIncome.GetDate() + "\n");

        Text walletBold = new Text("Wallet: ");
        walletBold.setStyle("-fx-font-weight: bold;");
        Text walletNormal = new Text(selectedIncome.GetWallet().GetName() + "\n");

        Text balanceBold = new Text("Wallet balance: ");
        balanceBold.setStyle("-fx-font-weight: bold;");
        Text balanceNormal =
            new Text("$ " + selectedIncome.GetWallet().GetBalance() + "\n");

        Text afterDeletionBold = new Text("Wallet balance after deletion: ");
        afterDeletionBold.setStyle("-fx-font-weight: bold;");
        Text afterDeletionNormal = new Text(
            "$ " +
            (selectedIncome.GetWallet().GetBalance() - selectedIncome.GetAmount()) +
            "\n");

        alert.getDialogPane().setContent(new TextFlow(descriptionBold,
                                                      descriptionNormal,
                                                      amountBold,
                                                      amountNormal,
                                                      dateBold,
                                                      dateNormal,
                                                      walletBold,
                                                      walletNormal,
                                                      balanceBold,
                                                      balanceNormal,
                                                      afterDeletionBold,
                                                      afterDeletionNormal));

        // Show the confirmation dialog and wait for the user's response
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK)
        {
            walletService.DeleteTransaction(selectedIncome.GetId());
            transactionsTableView.getItems().remove(selectedIncome);
        }
    }

    @FXML
    public void handleCancel()
    {
        Stage stage = (Stage)searchField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void LoadTransaction()
    {
        incomes = walletService.GetAllIncomes();
    }

    private void UpdateTransactionTableView()
    {
        String similarTextOrId = searchField.getText().toLowerCase();

        transactionsTableView.getItems().clear();

        if (similarTextOrId.isEmpty())
        {
            transactionsTableView.getItems().addAll(incomes);
            transactionsTableView.refresh();
            return;
        }

        incomes.stream()
            .filter(
                transaction
                -> transaction.GetDescription().toLowerCase().contains(
                       similarTextOrId) ||
                       String.valueOf(transaction.GetId()).contains(similarTextOrId))
            .forEach(transactionsTableView.getItems()::add);

        transactionsTableView.refresh();
    }

    /**
     * Configures the TableView to display the incomes.
     */
    private void ConfigureTableView()
    {
        TableColumn<WalletTransaction, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetId()));

        // Align the ID column to the center
        idColumn.setCellFactory(column -> {
            return new TableCell<WalletTransaction, Long>() {
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
                        setStyle("-fx-padding: 0;"); // set padding to zero to ensure
                                                     // the text is centered
                    }
                }
            };
        });

        TableColumn<WalletTransaction, String> categoryColumn =
            new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(param.getValue().GetCategory().GetName()));

        TableColumn<WalletTransaction, String> statusColumn =
            new TableColumn<>("Status");
        statusColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetStatus().name()));

        TableColumn<WalletTransaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetDate().toString()));

        TableColumn<WalletTransaction, String> walletNameColumn =
            new TableColumn<>("Wallet");
        walletNameColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetWallet().GetName()));

        TableColumn<WalletTransaction, Double> amountColumn =
            new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetAmount()));

        TableColumn<WalletTransaction, String> descriptionColumn =
            new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetDescription()));

        transactionsTableView.getColumns().addAll(idColumn,
                                                  descriptionColumn,
                                                  amountColumn,
                                                  walletNameColumn,
                                                  dateColumn,
                                                  categoryColumn,
                                                  statusColumn);
    }
}
