/*
 * Filename: EditCategoryController.java
 * Created on: October 13, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mymoney.entities.Category;
import org.mymoney.services.CategoryService;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Edit Category dialog
 */
@Controller
public class EditCategoryController
{
    @FXML
    private Label selectedCategoryLabel;

    @FXML
    private TextField categoryNewNameField;

    private Category selectedCategory; // The category to be edited

    private CategoryService categoryService;

    @Autowired
    public EditCategoryController(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }

    @FXML
    public void initialize()
    { }

    public void SetCategory(Category ct)
    {
        selectedCategoryLabel.setText(ct.GetName());
        selectedCategory = ct;
    }

    @FXML
    private void handleSave()
    {
        String newName = categoryNewNameField.getText();

        if (newName == null || newName.isBlank())
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid input",
                                        "Please fill all fields");
            return;
        }

        try
        {
            categoryService.RenameCategory(selectedCategory.GetId(), newName);

            WindowUtils.ShowSuccessDialog("Success",
                                          "Category updated",
                                          "Category updated successfully");
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Error updating category",
                                        e.getMessage());
            return;
        }

        Stage stage = (Stage)categoryNewNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)categoryNewNameField.getScene().getWindow();
        stage.close();
    }
}
