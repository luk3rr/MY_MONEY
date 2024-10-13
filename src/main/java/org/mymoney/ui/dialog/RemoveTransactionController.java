/*
 * Filename: RemoveTransactionController.fxml
 * Created on: October 12, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

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
import org.mymoney.entities.WalletTransaction;
import org.mymoney.services.WalletService;
import org.mymoney.util.Constants;
import org.mymoney.util.TransactionType;
import org.mymoney.util.UIUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Remove Transaction dialog
 * @note Make sure to set the transaction type before calling the initialize method
 */
@Controller
@Scope("prototype") // Create a new instance each time it is requested
public class RemoveTransactionController
{
    @FXML
    private TableView<WalletTransaction> transactionsTableView;

    @FXML
    private TextField searchField;

    private List<WalletTransaction> incomes;

    private WalletService walletService;

    private TransactionType transactionType;

    public RemoveTransactionController(WalletService walletService)
    {
        this.walletService = walletService;
    }

    @FXML
    public void initialize()
    { }

    /**
     * Initializes the controller with the transaction type
     * @param transactionType TransactionType
     */
    public void InitializeWithTransactionType(TransactionType transactionType)
    {
        this.transactionType = transactionType;

        if (transactionType == null)
        {
            throw new IllegalStateException("Transaction type not set");
        }

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

        alert.setHeaderText("Are you sure you want to remove this " +
                            transactionType.toString().toLowerCase() + "?");

        Text descriptionBold = new Text("Description: ");
        descriptionBold.setStyle("-fx-font-weight: bold;");
        Text descriptionNormal = new Text(selectedIncome.GetDescription() + "\n");

        Text amountBold = new Text("Amount: ");
        amountBold.setStyle("-fx-font-weight: bold;");
        Text amountNormal =
            new Text(UIUtils.FormatCurrency(selectedIncome.GetAmount()) + "\n");

        Text dateBold = new Text("Date: ");
        dateBold.setStyle("-fx-font-weight: bold;");
        Text dateNormal = new Text(
            selectedIncome.GetDate().format(Constants.DATE_FORMATTER_WITH_TIME) +
            "\n");

        Text walletBold = new Text("Wallet: ");
        walletBold.setStyle("-fx-font-weight: bold;");
        Text walletNormal = new Text(selectedIncome.GetWallet().GetName() + "\n");

        Text balanceBold = new Text("Wallet balance: ");
        balanceBold.setStyle("-fx-font-weight: bold;");
        Text balanceNormal = new Text(
            UIUtils.FormatCurrency(selectedIncome.GetWallet().GetBalance()) + "\n");

        Text afterDeletionBold = new Text("Wallet balance after deletion: ");
        afterDeletionBold.setStyle("-fx-font-weight: bold;");

        Text afterDeletionNormal;

        if (transactionType == TransactionType.EXPENSE)
        {
            afterDeletionNormal = new Text(
                UIUtils.FormatCurrency(selectedIncome.GetWallet().GetBalance() +
                                       selectedIncome.GetAmount()) +
                "\n");
        }
        else
        {
            afterDeletionNormal = new Text(
                UIUtils.FormatCurrency(selectedIncome.GetWallet().GetBalance() -
                                       selectedIncome.GetAmount()) +
                "\n");
        }

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

    private void LoadTransaction()
    {
        if (transactionType == TransactionType.EXPENSE)
        {
            incomes = walletService.GetAllExpenses();
        }
        else
        {
            incomes = walletService.GetAllIncomes();
        }
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
            .filter(t
                    -> t.GetDescription().toLowerCase().contains(similarTextOrId) ||
                           String.valueOf(t.GetId()).contains(similarTextOrId))
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
            param
            -> new SimpleStringProperty(
                param.getValue().GetDate().format(Constants.DATE_FORMATTER_WITH_TIME)));

        TableColumn<WalletTransaction, String> walletNameColumn =
            new TableColumn<>("Wallet");
        walletNameColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetWallet().GetName()));

        TableColumn<WalletTransaction, String> amountColumn =
            new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                UIUtils.FormatCurrency(param.getValue().GetAmount())));

        TableColumn<WalletTransaction, String> descriptionColumn =
            new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetDescription()));

        transactionsTableView.getColumns().add(idColumn);
        transactionsTableView.getColumns().add(descriptionColumn);
        transactionsTableView.getColumns().add(amountColumn);
        transactionsTableView.getColumns().add(walletNameColumn);
        transactionsTableView.getColumns().add(dateColumn);
        transactionsTableView.getColumns().add(categoryColumn);
        transactionsTableView.getColumns().add(statusColumn);
    }
}
