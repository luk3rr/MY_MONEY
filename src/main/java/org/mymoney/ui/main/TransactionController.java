/*
 * Filename: TransactionController.java
 * Created on: October 10, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.main;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;
import org.mymoney.entities.Category;
import org.mymoney.entities.WalletTransaction;
import org.mymoney.services.CategoryService;
import org.mymoney.services.CreditCardService;
import org.mymoney.services.WalletService;
import org.mymoney.ui.common.ResumePaneController;
import org.mymoney.ui.dialog.AddExpenseController;
import org.mymoney.ui.dialog.AddIncomeController;
import org.mymoney.ui.dialog.RemoveTransactionController;
import org.mymoney.util.Animation;
import org.mymoney.util.Constants;
import org.mymoney.util.LoggerConfig;
import org.mymoney.util.TransactionType;
import org.mymoney.util.UIUtils;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * Controller class for the transaction view
 * TODO: Load information from the database only when necessary
 */
@Controller
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
    private TextField transactionsSearchField;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private StackedBarChart<String, Number> moneyFlowStackedBarChart;

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

        PopulateMonthResumeComboBox();
        PopulateYearComboBox();
        PopulateTransactionTypeComboBox();

        // Format the date pickers
        UIUtils.SetDatePickerFormat(transactionsListStartDatePicker);
        UIUtils.SetDatePickerFormat(transactionsListEndDatePicker);

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

        // Add listener to the search field
        transactionsSearchField.textProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateTransactionTableView(); });
    }

    @FXML
    private void handleAddIncome()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_INCOME_FXML,
                                    "Add new income",
                                    springContext,
                                    (AddIncomeController controller) -> {});
    }

    @FXML
    private void handleAddExpense()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_EXPENSE_FXML,
                                    "Add new expense",
                                    springContext,
                                    (AddExpenseController controller) -> {});
    }

    @FXML
    private void handleRemoveIncome()
    {
        WindowUtils.OpenModalWindow(Constants.REMOVE_TRANSACTION_FXML,
                                    "Remove income",
                                    springContext,
                                    (RemoveTransactionController controller) -> {
                                        controller.InitializeWithTransactionType(
                                            TransactionType.INCOME);
                                    });
    }

    @FXML
    private void handleRemoveExpense()
    {
        WindowUtils.OpenModalWindow(Constants.REMOVE_TRANSACTION_FXML,
                                    "Remove expense",
                                    springContext,
                                    (RemoveTransactionController controller) -> {
                                        controller.InitializeWithTransactionType(
                                            TransactionType.EXPENSE);
                                    });
    }

    /**
     * Update the transaction table view
     */
    private void UpdateTransactionTableView()
    {
        // Get the search text
        String similarTextOrId = transactionsSearchField.getText().toLowerCase();

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
        // type. If transaction type is null, all transactions are fetched
        if (similarTextOrId.isEmpty())
        {
            walletService.GetTransactionsBetweenDates(startDate, endDate)
                .stream()
                .filter(t
                        -> selectedTransactionType == null ||
                               t.GetType().equals(selectedTransactionType))
                .forEach(transactionsListTableView.getItems()::add);
        }
        else
        {
            walletService.GetTransactionsBetweenDates(startDate, endDate)
                .stream()
                .filter(t
                        -> selectedTransactionType == null ||
                               t.GetType().equals(selectedTransactionType))
                .filter(t
                        -> t.GetDescription().toLowerCase().contains(similarTextOrId) ||
                               String.valueOf(t.GetId()).contains(similarTextOrId))
                .forEach(transactionsListTableView.getItems()::add);
        }

        transactionsListTableView.refresh();
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
        moneyFlowStackedBarChart  = new StackedBarChart<>(categoryAxis, numberAxis);

        moneyFlowStackedBarChart.setVerticalGridLinesVisible(false);
        moneyFlowView.getChildren().clear();
        moneyFlowView.getChildren().add(moneyFlowStackedBarChart);

        AnchorPane.setTopAnchor(moneyFlowStackedBarChart, 0.0);
        AnchorPane.setBottomAnchor(moneyFlowStackedBarChart, 0.0);
        AnchorPane.setLeftAnchor(moneyFlowStackedBarChart, 0.0);
        AnchorPane.setRightAnchor(moneyFlowStackedBarChart, 0.0);

        moneyFlowStackedBarChart.getData().clear();

        LocalDateTime     currentDate = LocalDateTime.now();
        DateTimeFormatter formatter   = DateTimeFormatter.ofPattern("MMM/yy");

        List<Category> categories = categoryService.GetAllCategories();
        Map<YearMonth, Map<Category, Double>> monthlyTotals = new LinkedHashMap<>();

        // Loop through the last few months
        for (Integer i = 0; i < Constants.XYBAR_CHART_MONTHS; i++)
        {
            // Get the date for the current month
            LocalDateTime date =
                currentDate.minusMonths(Constants.XYBAR_CHART_MONTHS - i - 1);
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
                moneyFlowStackedBarChart.getData().add(series);
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
            int tickUnit = (int)Math.ceil(maxTotal / Constants.XYBAR_CHART_TICKS);
            numberAxis.setTickUnit(tickUnit);
        }

        for (XYChart.Series<String, Number> series : moneyFlowStackedBarChart.getData())
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
                UIUtils.AddTooltipToXYChartNode(
                    data.getNode(),
                    series.getName() + ": " + UIUtils.FormatCurrency(value) + " (" +
                        UIUtils.FormatPercentage(percentage) + ")");

                // Animate the data after setting up the tooltip
                Animation.StackedXYChartAnimation(Collections.singletonList(data),
                                                  Collections.singletonList(value));
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
     * Populate the year combo box with the years between the oldest transaction
     * date and the current date
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

        // Custom string converter to format the TransactionType as
        // "TransactionType"
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

        // Generate a list of YearMonth objects from the oldest transaction date to
        // the current date
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
     * Configure the table view columns
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
                        setStyle("-fx-padding: 0;"); // set padding to zero to
                                                     // ensure the text is centered
                    }
                }
            };
        });

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
            param
            -> new SimpleStringProperty(
                param.getValue().GetDate().format(Constants.DATE_FORMATTER_WITH_TIME)));

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
}
