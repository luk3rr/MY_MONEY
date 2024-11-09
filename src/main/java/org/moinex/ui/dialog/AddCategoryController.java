/*
 * Filename: AddCategoryController.java
 * Created on: October 13, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.dialog;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.moinex.services.CategoryService;
import org.moinex.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Add Category dialog
 */
@Controller
public class AddCategoryController
{
    @FXML
    private TextField categoryNameField;

    private CategoryService categoryService;

    public AddCategoryController() { }

    @Autowired
    public AddCategoryController(CategoryService categoryService)
    {
        this.categoryService = categoryService;
    }

    @FXML
    public void handleSave()
    {
        String name = categoryNameField.getText();

        try
        {
            categoryService.AddCategory(name);

            WindowUtils.ShowSuccessDialog("Success",
                                          "Category added",
                                          "Category " + name + " added successfully");

            Stage stage = (Stage)categoryNameField.getScene().getWindow();
            stage.close();
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Error adding category",
                                        e.getMessage());
        }
    }

    @FXML
    public void handleCancel()
    {
        Stage stage = (Stage)categoryNameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void initialize()
    { }
}
