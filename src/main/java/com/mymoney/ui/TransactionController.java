/*
 * Filename: TransactionController.java
 * Created on: October 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.mymoney.entities.Category;
import com.mymoney.entities.WalletTransaction;
import com.mymoney.services.CategoryService;
import com.mymoney.services.CreditCardService;
import com.mymoney.services.WalletService;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import com.mymoney.util.TransactionType;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Controller class for the transaction view
 * TODO: Load information from the database only when necessary
 */
@Component
public class TransactionController
{
    @FXML
    private AnchorPane monthResumeView;

    @FXML
    private AnchorPane yearResumeView;

    @FXML
    private ComboBox<YearMonth> monthResumeComboBox;

    @FXML
    private ComboBox<Year> yearResumeComboBox;

    @FXML
    private ComboBox<TransactionType> moneyFlowComboBox;

    @FXML
    private ComboBox<TransactionType> transactionsListTransactionTypeComboBox;

    @FXML
    private DatePicker transactionsListEndDatePicker;

    @FXML
    private DatePicker transactionsListStartDatePicker;

    @FXML
    private TableView<WalletTransaction> transactionsListTableView;

    @FXML
    private AnchorPane moneyFlowView;

    @FXML
    private MenuItem addIncomeMenuItem;

    @FXML
    private MenuItem addExpenseMenuItem;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private StackedBarChart<String, Number> moneyFlowBarChart;

    private WalletService walletService;

    private CreditCardService creditCardService;

    private CategoryService categoryService;

    private static final Logger logger = LoggerConfig.GetLogger();

    @Autowired
    public TransactionController(WalletService     walletService,
                                 CreditCardService creditCardService,
                                 CategoryService   categoryService)
    {
        this.walletService     = walletService;
        this.creditCardService = creditCardService;
        this.categoryService   = categoryService;
    }

    @FXML
    private void initialize()
    {
        ConfigureTableView();
        ConfigureDatePicker();

        PopulateMonthResumeComboBox();
        PopulateYearComboBox();
        PopulateTransactionTypeComboBox();

        LocalDateTime currentDate = LocalDateTime.now();

        // Select the default values
        monthResumeComboBox.setValue(
            YearMonth.of(currentDate.getYear(), currentDate.getMonthValue()));

        yearResumeComboBox.setValue(Year.of(currentDate.getYear()));

        moneyFlowComboBox.setValue(TransactionType.EXPENSE);

        transactionsListTransactionTypeComboBox.setValue(null); // All transactions

        // Set the start and end date pickers to the first and last day of the current
        // month
        LocalDateTime firstDayOfMonth = currentDate.withDayOfMonth(1);
        LocalDateTime lastDayOfMonth  = currentDate.withDayOfMonth(
            currentDate.getMonth().length(currentDate.toLocalDate().isLeapYear()));

        transactionsListStartDatePicker.setValue(firstDayOfMonth.toLocalDate());
        transactionsListEndDatePicker.setValue(lastDayOfMonth.toLocalDate());

        // Update the resumes
        UpdateMonthResume(currentDate.getMonthValue(), currentDate.getYear());
        UpdateYearResume(currentDate.getYear());
        UpdateMoneyFlow();
        UpdateTransactionTableView();

        // Add a listener to handle user selection
        monthResumeComboBox.setOnAction(event -> {
            YearMonth selectedYearMonth = monthResumeComboBox.getValue();
            if (selectedYearMonth != null)
            {
                UpdateMonthResume(selectedYearMonth.getMonthValue(),
                                  selectedYearMonth.getYear());
            }
        });

        yearResumeComboBox.setOnAction(event -> {
            Year selectedYear = yearResumeComboBox.getValue();
            if (selectedYear != null)
            {
                UpdateYearResume(selectedYear.getValue());
            }
        });

        moneyFlowComboBox.setOnAction(event -> { UpdateMoneyFlow(); });

        transactionsListTransactionTypeComboBox.setOnAction(
            event -> { UpdateTransactionTableView(); });

        transactionsListStartDatePicker.setOnAction(
            event -> { UpdateTransactionTableView(); });

        transactionsListEndDatePicker.setOnAction(
            event -> { UpdateTransactionTableView(); });
    }

    @FXML
    private void handleAddIncome()
    {
        OpenPopupWindow(Constants.ADD_INCOME_FXML,
                        "Add new income",
                        (AddIncomeController controller) -> {});
    }

    @FXML
    private void handleAddExpense()
    {
        OpenPopupWindow(Constants.ADD_EXPENSE_FXML,
                        "Add new expense",
                        (AddExpenseController controller) -> {});
    }

    @FXML
    private void handleRemoveIncome()
    {
        OpenPopupWindow(Constants.REMOVE_INCOME_FXML,
                        "Remove income",
                        (RemoveIncomeController controller) -> {});
    }

    @FXML
    private void handleRemoveExpense()
    {
        OpenPopupWindow(Constants.REMOVE_EXPENSE_FXML,
                        "Remove expense",
                        (RemoveExpenseController controller) -> {});
    }

    /**
     * Update the transaction table view
     */
    private void UpdateTransactionTableView()
    {
        // Get selected values from the comboboxes
        TransactionType selectedTransactionType =
            transactionsListTransactionTypeComboBox.getValue();

        LocalDateTime startDate =
            transactionsListStartDatePicker.getValue().atStartOfDay();
        LocalDateTime endDate =
            transactionsListEndDatePicker.getValue().atTime(23, 59, 59);

        // Clear the transaction list view
        transactionsListTableView.getItems().clear();

        // Fetch all transactions within the selected range and filter by transaction
        // type
        // If transaction type is null, all transactions are fetched
        List<WalletTransaction> filteredTransactions =
            walletService.GetTransactionsBetweenDates(startDate, endDate)
                .stream()
                .filter(t
                        -> selectedTransactionType == null ||
                               t.GetType().equals(selectedTransactionType))
                .toList();
        // Preenche o TableView com uma lista de transações filtradas
        ObservableList<WalletTransaction> transactions =
            FXCollections.observableArrayList(filteredTransactions);
        transactionsListTableView.setItems(transactions);
    }

    /**
     * Update the money flow bar chart
     */
    private void UpdateMoneyFlow()
    {
        // Get the selected transaction type
        TransactionType selectedTransactionType = moneyFlowComboBox.getValue();

        CategoryAxis categoryAxis = new CategoryAxis();
        NumberAxis   numberAxis   = new NumberAxis();
        moneyFlowBarChart         = new StackedBarChart<>(categoryAxis, numberAxis);

        moneyFlowBarChart.setVerticalGridLinesVisible(false);
        moneyFlowView.getChildren().clear();
        moneyFlowView.getChildren().add(moneyFlowBarChart);

        AnchorPane.setTopAnchor(moneyFlowBarChart, 0.0);
        AnchorPane.setBottomAnchor(moneyFlowBarChart, 0.0);
        AnchorPane.setLeftAnchor(moneyFlowBarChart, 0.0);
        AnchorPane.setRightAnchor(moneyFlowBarChart, 0.0);

        moneyFlowBarChart.getData().clear();

        LocalDateTime     currentDate = LocalDateTime.now();
        DateTimeFormatter formatter   = DateTimeFormatter.ofPattern("MMM/yy");

        List<Category> categories = categoryService.GetAllCategories();
        Map<YearMonth, Map<Category, Double>> monthlyTotals = new LinkedHashMap<>();

        // Loop through the last few months
        for (int i = 0; i < Constants.HOME_BAR_CHART_MONTHS; i++)
        {
            // Get the date for the current month
            LocalDateTime date =
                currentDate.minusMonths(Constants.HOME_BAR_CHART_MONTHS - i - 1);
            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());

            // Get confirmed transactions for the month
            List<WalletTransaction> transactions =
                walletService.GetConfirmedTransactionsByMonth(date.getMonthValue(),
                                                              date.getYear());

            // Calculate total for each category
            for (Category category : categories)
            {
                double total =
                    transactions.stream()
                        .filter(t -> t.GetType().equals(selectedTransactionType))
                        .filter(t -> t.GetCategory().GetId() == category.GetId())
                        .mapToDouble(WalletTransaction::GetAmount)
                        .sum();

                // Store total if it's greater than zero
                if (total > 0)
                {
                    monthlyTotals.putIfAbsent(yearMonth, new LinkedHashMap<>());
                    monthlyTotals.get(yearMonth).put(category, total);
                }
            }
        }

        // Add series to the chart
        for (Category category : categories)
        {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(category.GetName());

            // Loop through the months in the order they were added
            for (YearMonth yearMonth : monthlyTotals.keySet())
            {
                Double total =
                    monthlyTotals.getOrDefault(yearMonth, new LinkedHashMap<>())
                        .getOrDefault(category, 0.0);

                // Add total to the series if it's greater than zero
                if (total > 0)
                {
                    series.getData().add(
                        new XYChart.Data<>(yearMonth.format(formatter), total));
                }
                else
                {
                    // Add zero value to keep the structure and order of the series
                    series.getData().add(
                        new XYChart.Data<>(yearMonth.format(formatter), 0.0));
                }
            }

            // Only add series to the chart if it has data greater than zero
            if (series.getData().stream().anyMatch(
                    data -> (Double)data.getYValue() > 0))
            {
                moneyFlowBarChart.getData().add(series);
            }
        }

        // Calculate the maximum total for each month
        double maxTotal = monthlyTotals.values()
                              .stream()
                              .map(monthData
                                   -> monthData.values()
                                          .stream()
                                          .mapToDouble(Double::doubleValue)
                                          .sum())
                              .max(Double::compare)
                              .orElse(0.0);

        // Set the Y-axis properties only if maxTotal is greater than 0
        if (maxTotal > 0)
        {
            numberAxis.setAutoRanging(false);
            numberAxis.setLowerBound(0);
            numberAxis.setUpperBound(maxTotal);

            // Calculate the tick unit based on the maximum total
            int tickUnit = (int)Math.ceil(maxTotal / Constants.HOME_BAR_CHART_TICKS);
            numberAxis.setTickUnit(tickUnit);
        }

        // Add tooltips to the bars
        for (XYChart.Series<String, Number> series : moneyFlowBarChart.getData())
        {
            for (XYChart.Data<String, Number> data : series.getData())
            {
                // Calculate total for the month to find the percentage
                YearMonth yearMonth = YearMonth.parse(data.getXValue(), formatter);
                double    monthTotal =
                    monthlyTotals.getOrDefault(yearMonth, new LinkedHashMap<>())
                        .values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();

                // Calculate the percentage
                double value      = (Double)data.getYValue();
                double percentage = (monthTotal > 0) ? (value / monthTotal) * 100 : 0;

                // Add tooltip with value and percentage
                AddTooltipToNode(data.getNode(),
                                 series.getName() + ": $ " + value + " (" +
                                     String.format("%.2f", percentage) + "%)");
                CreateAnimation(data, value);
            }
        }
    }

    /**
     * Update the year resume view
     * @param year The year to update
     */
    private void UpdateYearResume(Integer year)
    {
        try
        {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource(Constants.RESUME_PANE_FXML));
            loader.setControllerFactory(springContext::getBean);
            Parent newContent = loader.load();

            newContent.getStylesheets().add(
                getClass().getResource(Constants.COMMON_STYLE_SHEET).toExternalForm());

            ResumePaneController resumePaneController = loader.getController();
            resumePaneController.UpdateResumePane(year);

            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 10.0);
            AnchorPane.setRightAnchor(newContent, 10.0);

            yearResumeView.getChildren().clear();
            yearResumeView.getChildren().add(newContent);
        }
        catch (Exception e)
        {
            logger.severe("Error updating year resume: " + e.getMessage());
        }
    }

    /**
     * Update the month resume view
     * @param month The month to update
     * @param year The year to update
     */
    private void UpdateMonthResume(Integer month, Integer year)
    {
        try
        {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource(Constants.RESUME_PANE_FXML));
            loader.setControllerFactory(springContext::getBean);
            Parent newContent = loader.load();

            newContent.getStylesheets().add(
                getClass().getResource(Constants.COMMON_STYLE_SHEET).toExternalForm());

            ResumePaneController resumePaneController = loader.getController();
            resumePaneController.UpdateResumePane(month, year);

            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 10.0);
            AnchorPane.setRightAnchor(newContent, 10.0);

            monthResumeView.getChildren().clear();
            monthResumeView.getChildren().add(newContent);
        }
        catch (Exception e)
        {
            logger.severe("Error updating month resume: " + e.getMessage());
        }
    }

    /**
     * Populate the year combo box with the years between the oldest transaction date
     * and the current date
     */
    private void PopulateYearComboBox()
    {
        LocalDateTime oldestWalletTransaction =
            walletService.GetOldestTransactionDate();
        LocalDateTime oldestCreditCard = creditCardService.GetOldestDebtDate();

        LocalDateTime oldest = oldestCreditCard.isBefore(oldestWalletTransaction)
                                   ? oldestCreditCard
                                   : oldestWalletTransaction;

        LocalDate now = LocalDate.now();

        // Generate a list of Year objects from the oldest transaction date to the
        // current date
        Year startYear   = Year.from(oldest);
        Year currentYear = Year.from(now);

        // Generate the list of years between the oldest and the current date
        List<Year> years = new ArrayList<>();
        while (!startYear.isAfter(currentYear))
        {
            years.add(currentYear);
            currentYear = currentYear.minusYears(1);
        }

        ObservableList<Year> yearList = FXCollections.observableArrayList(years);

        yearResumeComboBox.setItems(yearList);

        // Custom string converter to format the Year as "Year"
        yearResumeComboBox.setConverter(new StringConverter<Year>() {
            private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy");

            @Override
            public String toString(Year year)
            {
                return year != null ? year.format(formatter) : "";
            }

            @Override
            public Year fromString(String string)
            {
                return Year.parse(string, formatter);
            }
        });
    }

    /**
     * Populate the transaction type combo box with the available transaction types
     */
    private void PopulateTransactionTypeComboBox()
    {
        ObservableList<TransactionType> transactionTypes =
            FXCollections.observableArrayList(TransactionType.values());

        moneyFlowComboBox.setItems(transactionTypes);

        // Make a copy of the list to add the 'All' option
        // Add 'All' option to the transaction type combo box
        // All is the first element in the list and is represented by a null value
        ObservableList<TransactionType> transactionTypesWithNull =
            FXCollections.observableArrayList(TransactionType.values());
        transactionTypesWithNull.add(0, null);

        transactionsListTransactionTypeComboBox.setItems(transactionTypesWithNull);

        // Custom string converter to format the TransactionType as "TransactionType"
        moneyFlowComboBox.setConverter(new StringConverter<TransactionType>() {
            @Override
            public String toString(TransactionType transactionType)
            {
                return transactionType != null ? transactionType.toString() : "";
            }

            @Override
            public TransactionType fromString(String string)
            {
                return TransactionType.valueOf(string);
            }
        });

        transactionsListTransactionTypeComboBox.setConverter(
            new StringConverter<TransactionType>() {
                @Override
                public String toString(TransactionType transactionType)
                {
                    return transactionType != null
                        ? transactionType.toString()
                        : "ALL"; // Show "All" instead of null
                }

                @Override
                public TransactionType fromString(String string)
                {
                    return string.equals("ALL")
                        ? null
                        : TransactionType.valueOf(
                              string); // Return null if "All" is selected
                }
            });
    }

    /**
     * Populate the month resume combo box with the months between the oldest
     * transaction date and the current date
     */
    private void PopulateMonthResumeComboBox()
    {
        LocalDateTime oldestWalletTransaction =
            walletService.GetOldestTransactionDate();
        LocalDateTime oldestCreditCard = creditCardService.GetOldestDebtDate();

        LocalDateTime oldest = oldestCreditCard.isBefore(oldestWalletTransaction)
                                   ? oldestCreditCard
                                   : oldestWalletTransaction;

        LocalDate now = LocalDate.now();

        // Generate a list of YearMonth objects from the oldest transaction date to the
        // current date
        YearMonth startMonth   = YearMonth.from(oldest);
        YearMonth currentMonth = YearMonth.from(now);

        // Generate the list of months between the oldest and the current date
        List<YearMonth> months = new ArrayList<>();
        while (!startMonth.isAfter(currentMonth))
        {
            months.add(currentMonth);
            currentMonth = currentMonth.minusMonths(1);
        }

        ObservableList<YearMonth> monthYearList =
            FXCollections.observableArrayList(months);
        monthResumeComboBox.setItems(monthYearList);

        // Custom string converter to format the YearMonth as "Month/Year"
        monthResumeComboBox.setConverter(new StringConverter<YearMonth>() {
            private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM yyyy");

            @Override
            public String toString(YearMonth yearMonth)
            {
                return yearMonth != null ? yearMonth.format(formatter) : "";
            }

            @Override
            public YearMonth fromString(String string)
            {
                return YearMonth.parse(string, formatter);
            }
        });
    }

    /**
     * Configure date picker
     */
    private void ConfigureDatePicker()
    {
        // Set how the date is displayed in the date picker
        transactionsListStartDatePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date)
            {
                return date != null ? date.format(Constants.DATE_FORMATTER_NO_TIME)
                                    : "";
            }

            @Override
            public LocalDate fromString(String string)
            {
                return LocalDate.parse(string, Constants.DATE_FORMATTER_NO_TIME);
            }
        });

        transactionsListEndDatePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date)
            {
                return date != null ? date.format(Constants.DATE_FORMATTER_NO_TIME)
                                    : "";
            }

            @Override
            public LocalDate fromString(String string)
            {
                return LocalDate.parse(string, Constants.DATE_FORMATTER_NO_TIME);
            }
        });
    }

    /**
     * Configure the table view columns
     */
    private void ConfigureTableView()
    {
        TableColumn<WalletTransaction, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetId()));

        TableColumn<WalletTransaction, String> categoryColumn =
            new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(param.getValue().GetCategory().GetName()));

        TableColumn<WalletTransaction, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetType().name()));

        TableColumn<WalletTransaction, String> statusColumn =
            new TableColumn<>("Status");
        statusColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetStatus().name()));

        TableColumn<WalletTransaction, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetDate().toString()));

        TableColumn<WalletTransaction, Double> amountColumn =
            new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue().GetAmount()));

        TableColumn<WalletTransaction, String> descriptionColumn =
            new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetDescription()));

        TableColumn<WalletTransaction, String> walletNameColumn =
            new TableColumn<>("Wallet");
        walletNameColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetWallet().GetName()));

        transactionsListTableView.getColumns().addAll(idColumn,
                                                      descriptionColumn,
                                                      amountColumn,
                                                      walletNameColumn,
                                                      dateColumn,
                                                      typeColumn,
                                                      categoryColumn,
                                                      statusColumn);
    }

    /**
     * Create an animation for a bar in the bar chart
     * @param data The data to animate
     * @param targetValue The target value for the animation
     */
    private void CreateAnimation(XYChart.Data<String, Number> data, Double targetValue)
    {
        data.setYValue(0.0); // Start at zero

        AnimationTimer timer = new AnimationTimer() {
            private Long         lastUpdate   = 0L;
            private Double       currentValue = 0.0;
            private final Double increment =
                targetValue / Constants.HOME_BAR_CHART_ANIMATION_FRAMES;
            Double elapsed = 0.0;

            @Override
            public void handle(long now)
            {
                if (lastUpdate == 0)
                {
                    lastUpdate = now;
                }

                elapsed += (now - lastUpdate) / Constants.ONE_SECOND_IN_NS;

                if (elapsed >= Constants.HOME_BAR_CHART_ANIMATION_DURATION)
                {
                    currentValue = targetValue;
                    data.setYValue(currentValue);
                    stop();
                }
                else
                {
                    currentValue += increment;
                    if (currentValue > targetValue)
                    {
                        currentValue = targetValue;
                    }
                    data.setYValue(currentValue);
                }

                lastUpdate = now;
            }
        };
        timer.start();
    }

    /**
     * Add a tooltip to a node
     * @param node The node to add the tooltip
     * @param text The text of the tooltip
     */
    private void AddTooltipToNode(Node node, String text)
    {
        node.setOnMouseEntered(event -> { node.setStyle("-fx-opacity: 0.7;"); });
        node.setOnMouseExited(event -> { node.setStyle("-fx-opacity: 1;"); });

        Tooltip tooltip = new Tooltip(text);
        tooltip.getStyleClass().add(Constants.HOME_TOOLTIP_STYLE);
        tooltip.setShowDelay(Duration.seconds(Constants.HOME_TOOLTIP_ANIMATION_DELAY));
        tooltip.setHideDelay(
            Duration.seconds(Constants.HOME_TOOLTIP_ANIMATION_DURATION));

        Tooltip.install(node, tooltip);
    }

    /**
     * Opens a popup window for adding expenses or transfers
     * @param fxmlFileName The FXML file to load
     * @param title The title of the popup window
     * @param controllerSetup A consumer that accepts the controller for additional
     *     setup
     * @param <T> The type of the controller
     */
    private <T> void
    OpenPopupWindow(String fxmlFileName, String title, Consumer<T> controllerSetup)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage popupStage = new Stage();
            Scene scene      = new Scene(root);
            scene.getStylesheets().add(
                getClass().getResource(Constants.COMMON_STYLE_SHEET).toExternalForm());

            T controller = loader.getController();
            controllerSetup.accept(controller);

            popupStage.setTitle(title);
            popupStage.setScene(scene);

            popupStage.setOnHidden(e -> {});

            popupStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
