/*
 * Filename: GoalFullPaneController.java
 * Created on: December 15, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.moinex.charts.CircularProgressBar;
import org.moinex.entities.Goal;
import org.moinex.services.GoalService;
import org.moinex.services.WalletTransactionService;
import org.moinex.ui.dialog.AddExpenseController;
import org.moinex.ui.dialog.AddIncomeController;
import org.moinex.ui.dialog.AddTransferController;
import org.moinex.ui.dialog.EditGoalController;
import org.moinex.ui.main.GoalController;
import org.moinex.util.Constants;
import org.moinex.util.UIUtils;
import org.moinex.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Goal Full Pane
 *
 * @note prototype is necessary so that each scene knows to which goal it belongs
 */
@Controller
@Scope("prototype") // Each instance of this controller is unique
public class GoalFullPaneController
{
    @FXML
    private VBox rootVBox;

    @FXML
    private VBox infosVBox;

    @FXML
    private ImageView goalIcon;

    @FXML
    private Label goalName;

    @FXML
    private Label goalMotivation;

    @FXML
    private Label goalTargetAmount;

    @FXML
    private Label goalCurrentAmount;

    @FXML
    private Label currentTitleLabel;

    @FXML
    private Label dateTitleLabel;

    @FXML
    private Label idealPerMonthTitleLabel;

    @FXML
    private Label daysTitleLabel;

    @FXML
    private Label goalTargetDate;

    @FXML
    private Label goalIdealAMountPerMonth;

    @FXML
    private Label missingDays;

    @FXML
    private StackPane progressBarPane;

    @FXML
    private MenuItem toggleArchiveGoal;

    @FXML
    private HBox currentHBox;

    @FXML
    private HBox idealPerMonthHBox;

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private GoalController goalController;

    private GoalService goalService;

    private WalletTransactionService walletTransactionService;

    private Goal goal;

    public GoalFullPaneController() { }

    /**
     * Constructor
     * @param GoalService Goal service
     * @param WalletTransactionService Wallet transaction service
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public GoalFullPaneController(GoalService              goalService,
                                  WalletTransactionService walletTransactionService)
    {
        this.goalService              = goalService;
        this.walletTransactionService = walletTransactionService;
    }

    /**
     * Load goal information from the database
     */
    public void LoadGoalInfo()
    {
        if (goal == null)
        {
            return;
        }

        // Reload goal from the database
        goal = goalService.GetGoalById(goal.GetId());
    }

    /**
     * Update the goal pane with the given goal
     * @param gl Goal to update the pane with
     * @return The updated VBox
     */
    public VBox UpdateGoalPane(Goal gl)
    {
        // If the goal is null, do not update the pane
        if (gl == null)
        {
            SetDefaultValues();
            return rootVBox;
        }

        goal = gl;
        LoadGoalInfo();

        goalName.setText(goal.GetName());
        goalMotivation.setText(goal.GetMotivation());

        // If the goal is archived, then is finished, so show the trophy icon
        // Otherwise, show the goal icon
        goalIcon.setImage(new Image(goal.IsArchived()
                                        ? Constants.TROPHY_ICON
                                        : Constants.WALLET_TYPE_ICONS_PATH +
                                              goal.GetType().GetIcon()));

        goalTargetAmount.setText(UIUtils.FormatCurrency(goal.GetTargetBalance()));
        goalTargetDate.setText(
            goal.GetTargetDate().format(Constants.DATE_FORMATTER_NO_TIME));

        // Create a tooltip for name and motivation
        UIUtils.AddTooltipToNode(goalName, goal.GetName());

        if (!goal.GetMotivation().isEmpty())
        {
            UIUtils.AddTooltipToNode(goalMotivation, goal.GetMotivation());
        }

        // Add the progress bar to the pane
        CircularProgressBar progressBar =
            new CircularProgressBar(Constants.GOAL_PANE_PROGRESS_BAR_RADIUS,
                                    Constants.GOAL_PANE_PROGRESS_BAR_WIDTH);

        Double percentage;

        if (goal.IsArchived())
        {
            dateTitleLabel.setText("Achieved");
            goalTargetDate.setText(
                goal.GetCompletionDate().format(Constants.DATE_FORMATTER_NO_TIME));

            daysTitleLabel.setText("Days Ahead of Target");
            missingDays.setText(String.valueOf(Constants.CalculateDaysUntilTarget(
                goal.GetCompletionDate().toLocalDate(),
                goal.GetTargetDate().toLocalDate())));

            // Remove the fields that are not necessary
            infosVBox.getChildren().remove(currentHBox);
            infosVBox.getChildren().remove(idealPerMonthHBox);

            percentage = 100.0;

            // Set the button text according to the goal status
            toggleArchiveGoal.setText("Reopen Goal");
        }
        else
        {
            // Show the current amount
            goalCurrentAmount.setText(UIUtils.FormatCurrency(goal.GetBalance()));

            // Calculate the ideal amount per month
            BigDecimal idealAmountPerMonth =
                goal.GetTargetBalance()
                    .subtract(goal.GetBalance())
                    .divide(BigDecimal.valueOf(Constants.CalculateMonthsUntilTarget(
                                LocalDate.now(),
                                goal.GetTargetDate().toLocalDate())),
                            2,
                            RoundingMode.HALF_UP);

            goalIdealAMountPerMonth.setText(
                UIUtils.FormatCurrency(idealAmountPerMonth));

            // Calculate the missing days
            Long missingDaysValue =
                Constants.CalculateDaysUntilTarget(LocalDate.now(),
                                                   goal.GetTargetDate().toLocalDate());

            missingDays.setText(missingDaysValue.toString());

            if (goal.GetTargetBalance().compareTo(BigDecimal.ZERO) == 0)
            {
                percentage = 0.0;
            }
            else
            {
                percentage = goal.GetBalance().doubleValue() /
                             goal.GetTargetBalance().doubleValue() * 100.0;
            }
        }

        progressBar.Draw(percentage);

        progressBarPane.getChildren().clear();
        progressBarPane.getChildren().add(progressBar);

        return rootVBox;
    }

    @FXML
    private void initialize()
    { }

    @FXML
    private void handleAddIncome()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_INCOME_FXML,
                                    "Add new income",
                                    springContext,
                                    (AddIncomeController controller)
                                        -> { controller.SetWalletComboBox(goal); },
                                    List.of(() -> goalController.UpdateDisplay()));
    }

    @FXML
    private void handleAddExpense()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_EXPENSE_FXML,
                                    "Add new expense",
                                    springContext,
                                    (AddExpenseController controller)
                                        -> { controller.SetWalletComboBox(goal); },
                                    List.of(() -> goalController.UpdateDisplay()));
    }

    @FXML
    private void handleAddTransfer()
    {
        WindowUtils.OpenModalWindow(
            Constants.ADD_TRANSFER_FXML,
            "Add new transfer",
            springContext,
            (AddTransferController controller)
                -> { controller.SetReceiverWalletComboBox(goal); },
            List.of(() -> goalController.UpdateDisplay()));
    }

    @FXML
    private void handleEditGoal()
    {
        WindowUtils.OpenModalWindow(Constants.EDIT_GOAL_FXML,
                                    "Edit goal",
                                    springContext,
                                    (EditGoalController controller)
                                        -> { controller.SetGoal(goal); },
                                    List.of(() -> goalController.UpdateDisplay()));
    }

    @FXML
    private void handleArchiveGoal()
    {
        if (goal.IsArchived())
        {
            if (WindowUtils.ShowConfirmationDialog(
                    "Confirmation",
                    "Reopen goal " + goal.GetName(),
                    "Are you sure you want to reopen this goal?"))
            {
                goalService.UnarchiveGoal(goal.GetId());

                // Update goal display in the main window
                goalController.UpdateDisplay();
            }
        }
        else
        {
            if (WindowUtils.ShowConfirmationDialog(
                    "Confirmation",
                    "Archive goal " + goal.GetName(),
                    "Are you sure you want to archive this goal?"))
            {
                goalService.ArchiveGoal(goal.GetId());

                // Update goal display in the main window
                goalController.UpdateDisplay();
            }
        }
    }

    @FXML
    private void handleDeleteGoal()
    {
        // Prevent the removal of a wallet with associated transactions
        if (walletTransactionService.GetTransactionCountByWallet(goal.GetId()) > 0)
        {
            WindowUtils.ShowErrorDialog(
                "Error",
                "Goal wallet has transactions",
                "Cannot delete a goal wallet with associated transactions. "
                    + "Remove the transactions first or archive the goal");
            return;
        }

        // Create a message to show to the user
        StringBuilder message = new StringBuilder();

        message.append("Name: ").append(goal.GetName()).append("\n");
        message.append("Initial Amount: ")
            .append(UIUtils.FormatCurrency(goal.GetInitialBalance()))
            .append("\n");
        message.append("Current Amount: ")
            .append(UIUtils.FormatCurrency(goal.GetBalance()))
            .append("\n");
        message.append("Target Amount: ")
            .append(UIUtils.FormatCurrency(goal.GetTargetBalance()))
            .append("\n");
        message.append("Target Date: ")
            .append(goal.GetTargetDate().format(Constants.DATE_FORMATTER_NO_TIME))
            .append("\n");

        try
        {
            // Confirm the deletion
            if (WindowUtils.ShowConfirmationDialog(
                    "Delete Goal",
                    "Are you sure you want to delete this goal?",
                    message.toString()))
            {
                goalService.DeleteGoal(goal.GetId());

                // Update goal display in the main window
                goalController.UpdateDisplay();
            }
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error", "Error deleting goal", e.getMessage());
        }
    }

    private void SetDefaultValues()
    {
        goalName.setText("");
        goalMotivation.setText("");
        goalIcon.setImage(null);

        goalTargetDate.setText("YY-MM-DD");
        missingDays.setText("0");

        SetLabelValue(goalTargetAmount, BigDecimal.ZERO);
        SetLabelValue(goalIdealAMountPerMonth, BigDecimal.ZERO);
    }

    /**
     * Set the value of a label
     * @param signLabel Label to set the sign
     * @param valueLabel Label to set the value
     * @param value Value to set
     */
    private void SetLabelValue(Label valueLabel, BigDecimal value)
    {
        valueLabel.setText(UIUtils.FormatCurrency(value));
        UIUtils.SetLabelStyle(valueLabel, Constants.NEUTRAL_BALANCE_STYLE);
    }
}
