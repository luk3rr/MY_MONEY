/*
 * Filename: ManageCategoryController.java
 * Created on: October 13, 2024
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
import org.mymoney.entities.Category;
import org.mymoney.services.CategoryService;
import org.mymoney.util.Constants;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Remove category dialog
 */
@Controller
public class ManageCategoryController
{
    @FXML
    private TableView<Category> categoryTableView;

    @FXML
    private TextField searchField;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private List<Category> categories;

    private CategoryService categoryService;

    public ManageCategoryController(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize()
    {
        LoadCategoryFromDatabase();

        ConfigureTableView();

        UpdateCategoryTableView();

        // Add listener to the search field
        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> UpdateCategoryTableView());
    }

    @FXML
    private void handleCreate()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_CATEGORY_FXML,
                                    "Add Category",
                                    springContext,
                                    (AddCategoryController controller)
                                        -> {},
                                    List.of(() -> {
                                        LoadCategoryFromDatabase();
                                        UpdateCategoryTableView();
                                    }));
    }

    @FXML
    private void handleEdit()
    {
        Category selectedCategory =
            categoryTableView.getSelectionModel().getSelectedItem();

        if (selectedCategory == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "No category selected",
                                        "Please select a category to edit");
            return;
        }

        WindowUtils.OpenModalWindow(Constants.EDIT_CATEGORY_FXML,
                                    "Edit Category",
                                    springContext,
                                    (EditCategoryController controller)
                                        -> controller.SetCategory(selectedCategory),
                                    List.of(() -> {
                                        LoadCategoryFromDatabase();
                                        UpdateCategoryTableView();
                                    }));
    }

    @FXML
    private void handleDelete()
    {
        Category selectedCategory =
            categoryTableView.getSelectionModel().getSelectedItem();

        if (selectedCategory == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "No category selected",
                                        "Please select a category to remove");
            return;
        }

        // Prevent the removal of categories with associated transactions
        if (categoryService.CountTransactions(selectedCategory.GetId()) > 0)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Category has transactions",
                                        "Cannot remove a category with transactions");
            // TODO: Implement a way to change the category of the transactions
            // TODO: Implement a way to archive the category
            return;
        }

        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Remove category " + selectedCategory.GetName(),
                "Are you sure you want to remove this category?"))
        {
            categoryService.DeleteCategory(selectedCategory.GetId());
            LoadCategoryFromDatabase();
            UpdateCategoryTableView();
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
    private void LoadCategoryFromDatabase()
    {
        categories = categoryService.GetCategories();
    }

    /**
     * Updates the category table view
     */
    private void UpdateCategoryTableView()
    {
        String similarTextOrId = searchField.getText().toLowerCase();

        categoryTableView.getItems().clear();

        // Populate the table view
        if (similarTextOrId.isEmpty())
        {
            categoryTableView.getItems().setAll(categories);
        }
        else
        {
            categories.stream()
                .filter(category
                        -> category.GetName().toLowerCase().contains(similarTextOrId) ||
                               category.GetId().toString().contains(similarTextOrId))
                .forEach(categoryTableView.getItems()::add);
        }

        categoryTableView.refresh();
    }

    /**
     * Configures the table view columns
     */
    private void ConfigureTableView()
    {
        TableColumn<Category, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetId()));

        idColumn.setCellFactory(column -> new TableCell<Category, Long>() {
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

        TableColumn<Category, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetName()));

        TableColumn<Category, Long> numOfTransactionsColumn =
            new TableColumn<>("Associated Transactions");
        numOfTransactionsColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                categoryService.CountTransactions(param.getValue().GetId())));

        numOfTransactionsColumn.setCellFactory(
            column -> new TableCell<Category, Long>() {
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

        TableColumn<Category, String> archivedColumn = new TableColumn<>("Archived");
        archivedColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(param.getValue().IsArchived() ? "Yes" : "No"));

        archivedColumn.setCellFactory(column -> new TableCell<Category, String>() {
            @Override
            protected void updateItem(String item, boolean empty)
            {
                super.updateItem(item, empty);
                if (item == null || empty)
                {
                    setText(null);
                }
                else
                {
                    setText(item);
                    setAlignment(Pos.CENTER);
                    setStyle("-fx-padding: 0;");
                }
            }
        });

        categoryTableView.getColumns().add(idColumn);
        categoryTableView.getColumns().add(categoryColumn);
        categoryTableView.getColumns().add(archivedColumn);
        categoryTableView.getColumns().add(numOfTransactionsColumn);
    }
}
