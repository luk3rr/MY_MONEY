/*
 * Filename: GoalController.java
 * Created on: December  8, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.main;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import org.moinex.entities.Goal;
import org.moinex.services.GoalService;
import org.moinex.services.WalletTransactionService;
import org.moinex.ui.dialog.AddGoalController;
import org.moinex.ui.dialog.AddTransferController;
import org.moinex.ui.dialog.EditGoalController;
import org.moinex.util.Constants;
import org.moinex.util.LoggerConfig;
import org.moinex.util.UIUtils;
import org.moinex.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * Controller class for the goal view
 */
@Controller
public class GoalController
{
    private static final Logger logger = LoggerConfig.GetLogger();

    @FXML
    private AnchorPane goalsResumeView;

    @FXML
    private TableView<Goal> goalTableView;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField goalSearchField;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private GoalService goalService;

    private WalletTransactionService walletTransactionService;

    private List<Goal> goals;

    /**
     * Constructor
     * @param goalService The goal service
     * @param walletTransactionService The wallet transaction service
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public GoalController(GoalService              goalService,
                          WalletTransactionService walletTransactionService)
    {
        this.goalService              = goalService;
        this.walletTransactionService = walletTransactionService;
    }

    @FXML
    private void initialize()
    {
        PopulateStatusComboBox();

        ConfigureTableView();

        LoadGoalsFromDatabase();

        UpdateGoalTableView();

        statusComboBox.setOnAction(event -> UpdateGoalTableView());

        // Add listener to the search field
        goalSearchField.textProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateGoalTableView(); });
    }

    @FXML
    private void handleAddGoal()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_GOAL_FXML,
                                    "Add Goal",
                                    springContext,
                                    (AddGoalController controller)
                                        -> {},
                                    List.of(() -> {
                                        LoadGoalsFromDatabase();
                                        UpdateGoalTableView();
                                    }));
    }

    @FXML
    private void handleAddDeposit()
    {
        // Get the selected goal
        Goal goal = goalTableView.getSelectionModel().getSelectedItem();

        if (goal == null)
        {
            WindowUtils.ShowInformationDialog("Information",
                                              "No goal selected",
                                              "Please select a goal to add a deposit");
            return;
        }

        WindowUtils.OpenModalWindow(
            Constants.ADD_TRANSFER_FXML,
            "Add new transfer",
            springContext,
            (AddTransferController controller)
                -> { controller.SetReceiverWalletComboBox(goal); },
            List.of(() -> {
                LoadGoalsFromDatabase();
                UpdateGoalTableView();
            }));
    }

    @FXML
    private void handleEditGoal()
    {
        // Get the selected goal
        Goal goal = goalTableView.getSelectionModel().getSelectedItem();

        if (goal == null)
        {
            WindowUtils.ShowInformationDialog("Information",
                                              "No goal selected",
                                              "Please select a goal to edit");
            return;
        }

        WindowUtils.OpenModalWindow(Constants.EDIT_GOAL_FXML,
                                    "Edit Goal",
                                    springContext,
                                    (EditGoalController controller)
                                        -> { controller.SetGoal(goal); },
                                    List.of(() -> {
                                        LoadGoalsFromDatabase();
                                        UpdateGoalTableView();
                                    }));
    }

    @FXML
    private void handleDeleteGoal()
    {
        // Get the selected goal
        Goal goal = goalTableView.getSelectionModel().getSelectedItem();

        if (goal == null)
        {
            WindowUtils.ShowInformationDialog("Information",
                                              "No goal selected",
                                              "Please select a goal to delete");
            return;
        }

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
                goals.remove(goal);

                UpdateGoalTableView();
            }
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error", "Error deleting goal", e.getMessage());
        }
    }

    private void LoadGoalsFromDatabase()
    {
        goals = goalService.GetGoals();
    }

    private void UpdateGoalTableView()
    {
        // Get the search text
        String searchText = goalSearchField.getText();

        // Get the selected status
        String selectedGoalStatus =
            statusComboBox.getSelectionModel().getSelectedItem();

        goalTableView.getItems().clear();

        if (searchText.isEmpty())
        {
            goals.stream()
                .filter(g -> {
                    if (selectedGoalStatus.equals("ALL"))
                    {
                        return true;
                    }
                    else if (selectedGoalStatus.equals("ARCHIVED"))
                    {
                        return g.IsArchived();
                    }
                    else if (selectedGoalStatus.equals("ACTIVE"))
                    {
                        return !g.IsArchived();
                    }
                    return false;
                })
                .forEach(goalTableView.getItems()::add);
        }
        else
        {
            goals.stream()
                .filter(g -> {
                    if (selectedGoalStatus.equals("ALL"))
                    {
                        return true;
                    }
                    else if (selectedGoalStatus.equals("ARCHIVED"))
                    {
                        return g.IsArchived();
                    }
                    else if (selectedGoalStatus.equals("ACTIVE"))
                    {
                        return !g.IsArchived();
                    }
                    return false;
                })
                .filter(g -> {
                    String name          = g.GetName().toLowerCase();
                    String initialAmount = g.GetInitialBalance().toString();
                    String currentAmount = g.GetBalance().toString();
                    String targetAmount  = g.GetTargetBalance().toString();
                    String targetDate =
                        g.GetTargetDate().format(Constants.DATE_FORMATTER_NO_TIME);

                    String monthsUntilTarget =
                        CalculateMonthsUntilTarget(LocalDate.now(),
                                                   g.GetTargetDate().toLocalDate())
                            .toString();

                    String recommendedMonthlyDeposit =
                        g.GetTargetBalance()
                            .subtract(g.GetBalance())
                            .divide(BigDecimal.valueOf(CalculateMonthsUntilTarget(
                                        LocalDate.now(),
                                        g.GetTargetDate().toLocalDate())),
                                    2,
                                    RoundingMode.HALF_UP)
                            .toString();

                    return name.contains(searchText.toLowerCase()) ||
                        initialAmount.contains(searchText.toLowerCase()) ||
                        currentAmount.contains(searchText.toLowerCase()) ||
                        targetAmount.contains(searchText.toLowerCase()) ||
                        targetDate.contains(searchText.toLowerCase()) ||
                        monthsUntilTarget.contains(searchText.toLowerCase()) ||
                        recommendedMonthlyDeposit.contains(searchText.toLowerCase());
                })
                .forEach(goalTableView.getItems()::add);
        }

        goalTableView.refresh();
    }

    private void ConfigureTableView()
    {
        TableColumn<Goal, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetId()));

        // Align the ID column to the center
        idColumn.setCellFactory(column -> {
            return new TableCell<Goal, Long>() {
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
                        setStyle("-fx-padding: 0;"); // set padding to zero to
                                                     // ensure the text is centered
                    }
                }
            };
        });

        TableColumn<Goal, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetName()));

        TableColumn<Goal, String> initialAmountColumn =
            new TableColumn<>("Initial Amount");
        initialAmountColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                UIUtils.FormatCurrency(param.getValue().GetInitialBalance())));

        TableColumn<Goal, String> currentAmountColumn =
            new TableColumn<>("Current Amount");
        currentAmountColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                UIUtils.FormatCurrency(param.getValue().GetBalance())));

        TableColumn<Goal, String> targetAmountColumn =
            new TableColumn<>("Target Amount");
        targetAmountColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                UIUtils.FormatCurrency(param.getValue().GetTargetBalance())));

        TableColumn<Goal, String> progressColumn = new TableColumn<>("Progress");

        TableColumn<Goal, String> targetDateColumn = new TableColumn<>("Target Date");
        targetDateColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(param.getValue().GetTargetDate().format(
                Constants.DATE_FORMATTER_NO_TIME)));

        TableColumn<Goal, Integer> monthsUntilTargetColumn =
            new TableColumn<>("Months Until Target");
        monthsUntilTargetColumn.setCellValueFactory(param -> {
            // Calculate the number of months until the target date
            Integer monthsUntilTarget = CalculateMonthsUntilTarget(
                LocalDate.now(),
                param.getValue().GetTargetDate().toLocalDate());

            return new SimpleObjectProperty<>(monthsUntilTarget);
        });

        TableColumn<Goal, String> recommendedMonthlyDepositColumn =
            new TableColumn<>("Recommended Monthly Deposit");
        recommendedMonthlyDepositColumn.setCellValueFactory(param -> {
            // Calculate the number of months until the target date
            Integer monthsUntilTarget = CalculateMonthsUntilTarget(
                LocalDate.now(),
                param.getValue().GetTargetDate().toLocalDate());

            // Calculate the recommended monthly deposit
            BigDecimal recommendedMonthlyDeposit =
                param.getValue()
                    .GetTargetBalance()
                    .subtract(param.getValue().GetBalance())
                    .divide(BigDecimal.valueOf(monthsUntilTarget),
                            2,
                            RoundingMode.HALF_UP);

            return new SimpleObjectProperty<>(
                UIUtils.FormatCurrency(recommendedMonthlyDeposit));
        });

        progressColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(UIUtils.FormatPercentage(
                // Calculate the progress, avoiding division by zero
                param.getValue().GetBalance().compareTo(BigDecimal.ZERO) == 0
                    ? BigDecimal.ZERO
                    : param.getValue().GetBalance().divide(
                          param.getValue().GetTargetBalance(),
                          2,
                          RoundingMode.HALF_UP))));

        goalTableView.getColumns().add(idColumn);
        goalTableView.getColumns().add(nameColumn);
        goalTableView.getColumns().add(initialAmountColumn);
        goalTableView.getColumns().add(currentAmountColumn);
        goalTableView.getColumns().add(targetAmountColumn);
        goalTableView.getColumns().add(progressColumn);
        goalTableView.getColumns().add(targetDateColumn);
        goalTableView.getColumns().add(monthsUntilTargetColumn);
        goalTableView.getColumns().add(recommendedMonthlyDepositColumn);

        // Show motivation as a tooltip
        goalTableView.setRowFactory(tv -> {
            TableRow<Goal> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null)
                {
                    Tooltip tooltip =
                        new Tooltip("Motivation: " + newItem.GetMotivation());
                    Tooltip.install(row, tooltip);
                }
            });
            return row;
        });
    }

    /**
     * Calculate the number of months until the target date
     * @param beginDate The begin date
     * @param targetDate The target date
     * @return The number of months until the target date
     */
    private Integer CalculateMonthsUntilTarget(LocalDate beginDate,
                                               LocalDate targetDate)
    {
        Period period = Period.between(beginDate, targetDate);

        // Add one to the number of months to account for the current month
        return period.getYears() * 12 + period.getMonths() + 1;
    }

    private void PopulateStatusComboBox()
    {
        statusComboBox.getItems().add("ALL");
        statusComboBox.getItems().add("ACTIVE");
        statusComboBox.getItems().add("ARCHIVED");
        statusComboBox.getSelectionModel().selectFirst();
    }
}
