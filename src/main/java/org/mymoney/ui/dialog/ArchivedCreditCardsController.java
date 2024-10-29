/*
 * Filename: ArchivedCreditCardsController.java
 * Created on: October 29, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

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
import org.mymoney.entities.CreditCard;
import org.mymoney.services.CreditCardService;
import org.mymoney.util.WindowUtils;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Archived Credit Cards dialog
 */
@Controller
public class ArchivedCreditCardsController
{
    @FXML
    private TableView<CreditCard> creditCardTableView;

    @FXML
    private TextField searchField;

    private List<CreditCard> archivedCreditCards;

    private CreditCardService creditCardService;

    /**
     * Constructor
     * @param creditCardService CreditCardService
     * @note This constructor is used for dependency injection
     */
    public ArchivedCreditCardsController(CreditCardService creditCardService)
    {
        this.creditCardService = creditCardService;
    }

    @FXML
    public void initialize()
    {
        LoadArchivedCreditCardsFromDatabase();

        ConfigureTableView();

        UpdateCreditCardTableView();

        // Add listener to the search field
        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> UpdateCreditCardTableView());
    }

    @FXML
    private void handleUnarchive()
    {
        CreditCard selectedCrc =
            creditCardTableView.getSelectionModel().getSelectedItem();

        if (selectedCrc == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "No credit card selected",
                                        "Please select a credit card to unarchive");
            return;
        }

        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Unarchive credit card " + selectedCrc.GetName(),
                "Are you sure you want to unarchive this credit card?"))
        {
            try
            {
                creditCardService.UnarchiveCreditCard(selectedCrc.GetId());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Credit Card unarchived",
                                              "Credit Card " + selectedCrc.GetName() +
                                                  " has been unarchived");

                // Remove this credit card from the list and update the table view
                archivedCreditCards.remove(selectedCrc);
                UpdateCreditCardTableView();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error unarchiving credit card",
                                            e.getMessage());
                return;
            }
        }
    }

    @FXML
    private void handleDelete()
    {
        CreditCard selectedCrc =
            creditCardTableView.getSelectionModel().getSelectedItem();

        if (selectedCrc == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "No credit card selected",
                                        "Please select a credit card to delete");
            return;
        }

        // Prevent the removal of a credit card with associated transactions
        if (creditCardService.GetDebtCountByCreditCard(selectedCrc.GetId()) > 0)
        {
            WindowUtils.ShowErrorDialog(
                "Error",
                "Credit Card has debts",
                "Cannot delete a credit card with associated debts");
            return;
        }

        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Delete credit card " + selectedCrc.GetName(),
                "Are you sure you want to remove this credit card?"))
        {
            try
            {
                creditCardService.DeleteCreditCard(selectedCrc.GetId());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Credit card deleted",
                                              "Credit card " + selectedCrc.GetName() +
                                                  " has been deleted");

                // Remove this credit card from the list and update the table view
                archivedCreditCards.remove(selectedCrc);
                UpdateCreditCardTableView();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error removing credit card",
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
     * Loads the credit cards from the database
     */
    private void LoadArchivedCreditCardsFromDatabase()
    {
        archivedCreditCards = creditCardService.GetAllArchivedCreditCards();
    }

    /**
     * Updates the credit card table view
     */
    private void UpdateCreditCardTableView()
    {
        String similarTextOrId = searchField.getText().toLowerCase();

        creditCardTableView.getItems().clear();

        // Populate the table view
        if (similarTextOrId.isEmpty())
        {
            creditCardTableView.getItems().setAll(archivedCreditCards);
        }
        else
        {
            archivedCreditCards.stream()
                .filter(c
                        -> c.GetName().toLowerCase().contains(similarTextOrId) ||
                               c.GetId().toString().contains(similarTextOrId))
                .forEach(creditCardTableView.getItems()::add);
        }

        creditCardTableView.refresh();
    }

    /**
     * Configures the table view columns
     */
    private void ConfigureTableView()
    {
        TableColumn<CreditCard, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetId()));

        idColumn.setCellFactory(column -> new TableCell<CreditCard, Long>() {
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

        TableColumn<CreditCard, String> crcColumn = new TableColumn<>("Credit Card");
        crcColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetName()));

        TableColumn<CreditCard, String> operatorColumn = new TableColumn<>("Operator");
        operatorColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(param.getValue().GetOperator().GetName()));

        TableColumn<CreditCard, Long> numOfDebtsColumn =
            new TableColumn<>("Associated Debts");
        numOfDebtsColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                creditCardService.GetDebtCountByCreditCard(param.getValue().GetId())));

        numOfDebtsColumn.setCellFactory(column -> new TableCell<CreditCard, Long>() {
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

        creditCardTableView.getColumns().add(idColumn);
        creditCardTableView.getColumns().add(crcColumn);
        creditCardTableView.getColumns().add(operatorColumn);
        creditCardTableView.getColumns().add(numOfDebtsColumn);
    }
}
