/*
 * Filename: CreditCardController.java
 * Created on: October 19, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.main;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.mymoney.entities.Category;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.CreditCardPayment;
import org.mymoney.services.CategoryService;
import org.mymoney.services.CreditCardService;
import org.mymoney.ui.common.CreditCardPaneController;
import org.mymoney.util.Animation;
import org.mymoney.util.Constants;
import org.mymoney.util.LoggerConfig;
import org.mymoney.util.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class CreditCardController
{
    private static final Logger logger = LoggerConfig.GetLogger();

    @FXML
    private VBox totalDebtsInfoVBox;

    @FXML
    private ComboBox<Year> totalDebtsYearFilterComboBox;

    @FXML
    private ComboBox<YearMonth> debtsListMonthFilterComboBox;

    @FXML
    private TableView<CreditCardPayment> debtsTableView;

    @FXML
    private TextField debtSearchField;

    @FXML
    private AnchorPane crcPane1;

    @FXML
    private AnchorPane debtsFlowPane;

    @FXML
    private JFXButton crcNextButton;

    @FXML
    private JFXButton crcPrevButton;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private StackedBarChart<String, Number> debtsFlowStackedBarChart;

    private CreditCardService creditCardService;

    private CategoryService categoryService;

    private List<CreditCard> creditCards;

    private Integer crcPaneCurrentPage = 0;

    /**
     * Constructor
     * @param creditCardService CreditCardService
     */
    public CreditCardController(CreditCardService creditCardService,
                                CategoryService   categoryService)
    {
        this.creditCardService = creditCardService;
        this.categoryService   = categoryService;
    }

    @FXML
    private void initialize()
    {
        LoadCreditCards();

        PopulateDebtsListMonthFilterComboBox();
        PopulateYearFilterComboBox();
        ConfigureTableView();

        // Select the default values
        LocalDateTime now = LocalDateTime.now();

        totalDebtsYearFilterComboBox.setValue(Year.from(now));

        LocalDateTime currentDate = LocalDateTime.now();

        // Select the default values
        debtsListMonthFilterComboBox.setValue(
            YearMonth.of(currentDate.getYear(), currentDate.getMonthValue()));

        debtsListMonthFilterComboBox.setOnAction(event -> { UpdateDebtsTableView(); });

        UpdateTotalDebtsInfo();
        UpdateDisplayCards();
        UpdateMoneyFlow();
        UpdateDebtsTableView();

        SetButtonsActions();

        // Add listeners
        totalDebtsYearFilterComboBox.valueProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateTotalDebtsInfo(); });

        debtsListMonthFilterComboBox.valueProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateDebtsTableView(); });

        debtSearchField.textProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateDebtsTableView(); });
    }

    @FXML
    private void handleAddDebt()
    { }

    @FXML
    private void handleAddCreditCard()
    { }

    @FXML
    private void handleEditDebt()
    { }

    @FXML
    private void handleDeleteDebt()
    { }

    /**
     * Load credit cards from database
     */
    private void LoadCreditCards()
    {
        creditCards = creditCardService.GetAllCreditCardsOrderedByName();
    }

    private void UpdateDebtsTableView()
    {
        YearMonth selectedMonth = debtsListMonthFilterComboBox.getValue();

        // Get the search text
        String similarTextOrId = debtSearchField.getText().toLowerCase();

        // Clear the transaction list view
        debtsTableView.getItems().clear();

        // Fetch all transactions within the selected range and filter by transaction
        // type. If transaction type is null, all transactions are fetched
        if (similarTextOrId.isEmpty())
        {
            creditCardService
                .GetCreditCardPayments(selectedMonth.getMonthValue(),
                                       selectedMonth.getYear())
                .stream()
                .forEach(debtsTableView.getItems()::add);
        }
        else
        {
            creditCardService
                .GetCreditCardPayments(selectedMonth.getMonthValue(),
                                       selectedMonth.getYear())
                .stream()
                .filter(
                    p
                    -> p.GetCreditCardDebt().GetDescription().toLowerCase().contains(
                           similarTextOrId) ||
                           String.valueOf(p.GetCreditCardDebt().GetId())
                               .contains(similarTextOrId))
                .forEach(debtsTableView.getItems()::add);
        }

        debtsTableView.refresh();
    }

    /**
     * Update the display of the total debts information
     */
    private void UpdateTotalDebtsInfo()
    {
        // Get the selected year from the year filter combo box
        Year selectedYear = totalDebtsYearFilterComboBox.getValue();

        Double totalDebts =
            creditCardService.GetTotalDebtAmount(selectedYear.getValue());

        Double totalPendingPayments = creditCardService.GetTotalPendingPayments();

        Label totalTotalDebtsLabel = new Label(UIUtils.FormatCurrency(totalDebts));

        Label totalPendingPaymentsLabel = new Label(
            "Pending payments: " + UIUtils.FormatCurrency(totalPendingPayments));

        totalTotalDebtsLabel.getStyleClass().add(
            Constants.TOTAL_BALANCE_VALUE_LABEL_STYLE);

        totalPendingPaymentsLabel.getStyleClass().add(
            Constants.TOTAL_BALANCE_FORESEEN_LABEL_STYLE);

        totalDebtsInfoVBox.getChildren().clear();
        totalDebtsInfoVBox.getChildren().add(totalTotalDebtsLabel);
        totalDebtsInfoVBox.getChildren().add(totalPendingPaymentsLabel);
    }

    /**
     * Update the display of the credit cards
     */
    private void UpdateDisplayCards()
    {
        crcPane1.getChildren().clear();

        CreditCard crc = creditCards.get(crcPaneCurrentPage);

        try
        {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource(Constants.CRC_PANE_FXML));
            loader.setControllerFactory(springContext::getBean);
            Parent newContent = loader.load();

            // Add style class to the wallet pane
            newContent.getStylesheets().add(
                getClass().getResource(Constants.COMMON_STYLE_SHEET).toExternalForm());

            CreditCardPaneController crcPaneController = loader.getController();

            crcPaneController.UpdateCreditCardPane(crc);

            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

            crcPane1.getChildren().add(newContent);
        }
        catch (IOException e)
        {
            logger.severe("Error while loading credit card pane");
            e.printStackTrace();
        }

        crcPrevButton.setDisable(crcPaneCurrentPage == 0);
        crcNextButton.setDisable(crcPaneCurrentPage == creditCards.size() - 1);
    }

    /**
     * Update money flow chart
     */
    private void UpdateMoneyFlow()
    {
        CategoryAxis categoryAxis = new CategoryAxis();
        NumberAxis   numberAxis   = new NumberAxis();
        debtsFlowStackedBarChart  = new StackedBarChart<>(categoryAxis, numberAxis);

        debtsFlowStackedBarChart.setVerticalGridLinesVisible(false);
        debtsFlowPane.getChildren().clear();
        debtsFlowPane.getChildren().add(debtsFlowStackedBarChart);

        AnchorPane.setTopAnchor(debtsFlowStackedBarChart, 0.0);
        AnchorPane.setBottomAnchor(debtsFlowStackedBarChart, 0.0);
        AnchorPane.setLeftAnchor(debtsFlowStackedBarChart, 0.0);
        AnchorPane.setRightAnchor(debtsFlowStackedBarChart, 0.0);

        debtsFlowStackedBarChart.getData().clear();

        LocalDateTime     currentDate = LocalDateTime.now();
        DateTimeFormatter formatter   = DateTimeFormatter.ofPattern("MMM/yy");

        List<Category> categories = categoryService.GetCategories();
        Map<YearMonth, Map<Category, Double>> monthlyTotals = new LinkedHashMap<>();

        // Loop through the months
        Integer halfMonths = Constants.CRC_XYBAR_CHART_MAX_MONTHS / 2;

        // Positive to negative to keep the order of the months
        for (Integer i = halfMonths; i >= -halfMonths; i--)
        {
            // Get the date for the current month
            LocalDateTime date = currentDate.minusMonths(i);

            YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());

            // Get confirmed transactions for the month
            List<CreditCardPayment> payments =
                creditCardService.GetCreditCardPayments(date.getMonthValue(),
                                                        date.getYear());

            // Calculate total for each category
            for (Category category : categories)
            {
                Double total =
                    payments.stream()
                        .filter(t
                                -> t.GetCreditCardDebt().GetCategory().GetId() ==
                                       category.GetId())
                        .mapToDouble(CreditCardPayment::GetAmount)
                        .sum();

                monthlyTotals.putIfAbsent(yearMonth, new LinkedHashMap<>());
                monthlyTotals.get(yearMonth).put(category, total);
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

                series.getData().add(
                    new XYChart.Data<>(yearMonth.format(formatter), total));
            }

            // Only add series to the chart if it has data greater than zero
            if (series.getData().stream().anyMatch(
                    data -> (Double)data.getYValue() > 0))
            {
                debtsFlowStackedBarChart.getData().add(series);
            }
        }

        // Calculate the maximum total for each month
        Double maxTotal = monthlyTotals.values()
                              .stream()
                              .map(monthData
                                   -> monthData.values()
                                          .stream()
                                          .mapToDouble(Double::doubleValue)
                                          .sum())
                              .max(Double::compare)
                              .orElse(0.0);

        // Set the maximum total as the upper bound of the y-axis
        Animation.SetDynamicYAxisBounds(numberAxis, maxTotal);

        for (XYChart.Series<String, Number> series : debtsFlowStackedBarChart.getData())
        {
            for (XYChart.Data<String, Number> data : series.getData())
            {
                // Calculate total for the month to find the percentage
                YearMonth yearMonth = YearMonth.parse(data.getXValue(), formatter);
                Double    monthTotal =
                    monthlyTotals.getOrDefault(yearMonth, new LinkedHashMap<>())
                        .values()
                        .stream()
                        .mapToDouble(Double::doubleValue)
                        .sum();
                //
                // Calculate the percentage
                Double value      = (Double)data.getYValue();
                Double percentage = (monthTotal > 0) ? (value / monthTotal) * 100 : 0;

                // Add tooltip with value and percentage
                UIUtils.AddTooltipToXYChartNode(
                    data.getNode(),
                    series.getName() + ": " + UIUtils.FormatCurrency(value) + " (" +
                        UIUtils.FormatPercentage(percentage) +
                        ")\nTotal: " + UIUtils.FormatCurrency(monthTotal));

                // Animate the data after setting up the tooltip
                Animation.StackedXYChartAnimation(Collections.singletonList(data),
                                                  Collections.singletonList(value));
            }
        }
    }

    private void PopulateDebtsListMonthFilterComboBox()
    {
        debtsListMonthFilterComboBox.getItems().clear();

        // Get the oldest and newest debt date
        LocalDateTime oldestDebtDate = creditCardService.GetOldestDebtDate();

        LocalDateTime newestDebtDate = creditCardService.GetNewestDebtDate();

        // Generate a list of YearMonth objects from the oldest transaction date to the
        // newest transaction date
        YearMonth startYearMonth = YearMonth.from(oldestDebtDate);
        YearMonth endYearMonth   = YearMonth.from(newestDebtDate);

        // Generate the list of years between the oldest and the current date
        List<YearMonth> yearMonths = new ArrayList<>();

        while (endYearMonth.isAfter(startYearMonth) ||
               endYearMonth.equals(startYearMonth))
        {
            yearMonths.add(endYearMonth);
            endYearMonth = endYearMonth.minusMonths(1);
        }

        ObservableList<YearMonth> yearMonthList =
            FXCollections.observableArrayList(yearMonths);

        debtsListMonthFilterComboBox.setItems(yearMonthList);

        // Custom string converter to format the YearMonth as "MMM/yy"
        debtsListMonthFilterComboBox.setConverter(new StringConverter<YearMonth>() {
            private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM/yy");

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

    private void PopulateYearFilterComboBox()
    {
        LocalDateTime oldestDebtDate = creditCardService.GetOldestDebtDate();

        LocalDate now = LocalDate.now();

        // Generate a list of Year objects from the oldest transaction date to the
        // current date
        Year startYear   = Year.from(oldestDebtDate);
        Year currentYear = Year.from(now);

        // Generate the list of years between the oldest and the current date
        List<Year> years = new ArrayList<>();
        while (!startYear.isAfter(currentYear))
        {
            years.add(currentYear);
            currentYear = currentYear.minusYears(1);
        }

        ObservableList<Year> yearList = FXCollections.observableArrayList(years);

        totalDebtsYearFilterComboBox.setItems(yearList);

        // Custom string converter to format the Year as "Year"
        totalDebtsYearFilterComboBox.setConverter(new StringConverter<Year>() {
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
     * Set the actions for the buttons
     */
    private void SetButtonsActions()
    {
        crcPrevButton.setOnAction(event -> {
            if (crcPaneCurrentPage > 0)
            {
                crcPaneCurrentPage--;
                UpdateDisplayCards();
            }
        });

        crcNextButton.setOnAction(event -> {
            if (crcPaneCurrentPage < creditCards.size() - 1)
            {
                crcPaneCurrentPage++;
                UpdateDisplayCards();
            }
        });
    }

    /**
     * Configure the table view columns
     */
    private void ConfigureTableView()
    {
        TableColumn<CreditCardPayment, Long> idColumn = new TableColumn<>("Debt ID");
        idColumn.setCellValueFactory(param
                                     -> new SimpleObjectProperty<>(
                                         param.getValue().GetCreditCardDebt().GetId()));

        // Align the ID column to the center
        idColumn.setCellFactory(column -> {
            return new TableCell<CreditCardPayment, Long>() {
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

        TableColumn<CreditCardPayment, String> descriptionColumn =
            new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(
                param.getValue().GetCreditCardDebt().GetDescription()));

        TableColumn<CreditCardPayment, String> amountColumn =
            new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(
                UIUtils.FormatCurrency(param.getValue().GetAmount())));

        TableColumn<CreditCardPayment, String> installmentColumn =
            new TableColumn<>("Installment");
        installmentColumn.setCellValueFactory(
            param
            -> new SimpleObjectProperty<>(param.getValue().GetInstallment().toString() +
                                          "/" +
                                          param.getValue().GetTotalInstallments()));

        // Align the installment column to the center
        installmentColumn.setCellFactory(column -> {
            return new TableCell<CreditCardPayment, String>() {
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
                        setStyle("-fx-padding: 0;"); // set padding to zero to
                                                     // ensure the text is centered
                    }
                }
            };
        });

        TableColumn<CreditCardPayment, String> crcColumn =
            new TableColumn<>("Credit Card");
        crcColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(
                param.getValue().GetCreditCardDebt().GetCreditCard().GetName()));

        TableColumn<CreditCardPayment, String> categoryColumn =
            new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(
                param.getValue().GetCreditCardDebt().GetCategory().GetName()));

        TableColumn<CreditCardPayment, String> dateColumn =
            new TableColumn<>("Invoice date");
        dateColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(
                param.getValue().GetDate().format(Constants.DATE_FORMATTER_NO_TIME)));

        TableColumn<CreditCardPayment, String> statusColumn =
            new TableColumn<>("Status");
        statusColumn.setCellValueFactory(
            param
            -> new SimpleStringProperty(param.getValue().GetWallet() == null ? "Pending"
                                                                             : "Paid"));

        debtsTableView.getColumns().add(idColumn);
        debtsTableView.getColumns().add(descriptionColumn);
        debtsTableView.getColumns().add(amountColumn);
        debtsTableView.getColumns().add(installmentColumn);
        debtsTableView.getColumns().add(crcColumn);
        debtsTableView.getColumns().add(categoryColumn);
        debtsTableView.getColumns().add(dateColumn);
        debtsTableView.getColumns().add(statusColumn);
    }
}
