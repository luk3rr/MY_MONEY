/*
 * Filename: EditGoalController.java
 * Created on: December 14, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.dialog;

import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.moinex.entities.Goal;
import org.moinex.services.GoalService;
import org.moinex.util.Constants;
import org.moinex.util.UIUtils;
import org.moinex.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Edit Goal dialog
 */
@Controller
public class EditGoalController
{
    @FXML
    private TextField nameField;

    @FXML
    private TextField initialBalanceField;

    @FXML
    private TextField currentBalanceField;

    @FXML
    private TextField targetBalanceField;

    @FXML
    private DatePicker targetDatePicker;

    @FXML
    private TextArea motivationTextArea;

    @FXML
    private CheckBox archivedCheckBox;

    @FXML
    private CheckBox completedCheckBox;

    private GoalService goalService;

    private Goal goalToUpdate;

    public EditGoalController() { }

    /**
     * Constructor
     * @param goalService GoalService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public EditGoalController(GoalService goalService)
    {
        this.goalService = goalService;
    }

    public void SetGoal(Goal goal)
    {
        goalToUpdate = goal;

        nameField.setText(goal.GetName());
        initialBalanceField.setText(goal.GetInitialBalance().toString());
        currentBalanceField.setText(goal.GetBalance().toString());
        targetBalanceField.setText(goal.GetTargetBalance().toString());
        targetDatePicker.setValue(goal.GetTargetDate().toLocalDate());
        motivationTextArea.setText(goal.GetMotivation());
        archivedCheckBox.setSelected(goal.IsArchived());
        completedCheckBox.setSelected(goal.IsCompleted());
    }

    @FXML
    private void initialize()
    {
        UIUtils.SetDatePickerFormat(targetDatePicker);

        // Ensure that the balance fields only accept monetary values
        initialBalanceField.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!newValue.matches(Constants.MONETARY_VALUE_REGEX))
                {
                    initialBalanceField.setText(oldValue);
                }
            });

        currentBalanceField.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!newValue.matches(Constants.MONETARY_VALUE_REGEX))
                {
                    currentBalanceField.setText(oldValue);
                }
            });

        targetBalanceField.textProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (!newValue.matches(Constants.MONETARY_VALUE_REGEX))
                {
                    targetBalanceField.setText(oldValue);
                }
            });
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)nameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave()
    {
        String goalName = nameField.getText();
        goalName        = goalName.strip(); // Remove leading and trailing whitespaces

        String    initialBalanceStr = initialBalanceField.getText();
        String    currentBalanceStr = currentBalanceField.getText();
        String    targetBalanceStr  = targetBalanceField.getText();
        LocalDate targetDate        = targetDatePicker.getValue();
        String    motivation        = motivationTextArea.getText();
        Boolean   archived          = archivedCheckBox.isSelected();
        Boolean   completed         = completedCheckBox.isSelected();

        if (goalName.isEmpty() || initialBalanceStr.isEmpty() ||
            currentBalanceStr.isEmpty() || targetBalanceStr.isEmpty() ||
            targetDate == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Empty fields",
                                        "Please fill all required fields.");

            return;
        }

        try
        {
            BigDecimal initialBalance = new BigDecimal(initialBalanceStr);
            BigDecimal currentBalance = new BigDecimal(currentBalanceStr);
            BigDecimal targetBalance  = new BigDecimal(targetBalanceStr);

            // Check if has any modification
            if (goalToUpdate.GetName().equals(goalName) &&
                goalToUpdate.GetInitialBalance().equals(initialBalance) &&
                goalToUpdate.GetBalance().equals(currentBalance) &&
                goalToUpdate.GetTargetBalance().equals(targetBalance) &&
                goalToUpdate.GetTargetDate().toLocalDate().equals(targetDate) &&
                goalToUpdate.GetMotivation().equals(motivation) &&
                goalToUpdate.IsArchived() == archived &&
                goalToUpdate.IsCompleted() == completed)
            {
                WindowUtils.ShowInformationDialog("Information",
                                                  "No changes",
                                                  "No changes were made to the goal.");
            }
            else // If there is any modification, update the goal
            {
                goalToUpdate.SetName(goalName);
                goalToUpdate.SetInitialBalance(initialBalance);
                goalToUpdate.SetBalance(currentBalance);
                goalToUpdate.SetTargetBalance(targetBalance);
                goalToUpdate.SetTargetDate(targetDate.atStartOfDay());
                goalToUpdate.SetMotivation(motivation);
                goalToUpdate.SetArchived(archived);

                // If the goal was completed and the user unchecked the completed
                // checkbox, set the completion date to null, otherwise set the
                // completion date to the current date This is necessary for UpdateGoal
                // identify if the completed field was changed
                if (completed && !goalToUpdate.IsCompleted())
                {
                    goalToUpdate.SetCompletionDate(LocalDate.now().atStartOfDay());
                }
                else
                {
                    goalToUpdate.SetCompletionDate(null);
                }

                goalService.UpdateGoal(goalToUpdate);

                WindowUtils.ShowSuccessDialog("Success",
                                              "Goal updated",
                                              "The goal was successfully updated.");
            }

            Stage stage = (Stage)nameField.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid balance",
                                        "Please enter a valid balance.");
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error", "Error creating goal", e.getMessage());
        }
    }
}
