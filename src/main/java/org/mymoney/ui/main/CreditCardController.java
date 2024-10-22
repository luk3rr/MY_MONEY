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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import org.mymoney.entities.Category;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.CreditCardDebt;
import org.mymoney.entities.CreditCardPayment;
import org.mymoney.entities.WalletTransaction;
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
    private AnchorPane crcPane1;

    @FXML
    private AnchorPane crcPane2;

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

    private List<CreditCardDebt> creditCardDebts;

    private Integer crcPaneCurrentPage = 0;

    private Integer itemsPerPage = 2;

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

        PopulateYearFilterComboBox();

        // Select the default values
        LocalDateTime now = LocalDateTime.now();

        totalDebtsYearFilterComboBox.setValue(Year.from(now));

        UpdateTotalDebtsInfo();
        UpdateDisplayCards();
        UpdateMoneyFlow();

        SetButtonsActions();

        // Add a listener to the year filter combo box
        totalDebtsYearFilterComboBox.valueProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateTotalDebtsInfo(); });
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

    /**
     * Load credit card debts from database
     */
    private void LoadCreditCardDebts() { }

    /**
     * Update the display of the total debts information
     */
    private void UpdateTotalDebtsInfo()
    {
        // Get the selected year from the year filter combo box
        Year selectedYear = totalDebtsYearFilterComboBox.getValue();

        Double totalDebts =
            creditCardService.GetTotalDebtAmount(selectedYear.getValue());

        Double totalPendingPayments =
            creditCardService.GetTotalPendingPayments(LocalDate.now().getYear());

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
        crcPane2.getChildren().clear();

        Integer start = crcPaneCurrentPage * itemsPerPage;
        Integer end   = Math.min(start + itemsPerPage, creditCards.size());

        for (Integer i = start; i < end; i++)
        {
            CreditCard crc = creditCards.get(i);

            try
            {
                FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(Constants.CRC_PANE_FXML));
                loader.setControllerFactory(springContext::getBean);
                Parent newContent = loader.load();

                // Add style class to the wallet pane
                newContent.getStylesheets().add(
                    getClass()
                        .getResource(Constants.COMMON_STYLE_SHEET)
                        .toExternalForm());

                CreditCardPaneController crcPaneController = loader.getController();

                crcPaneController.UpdateCreditCardPane(crc);

                AnchorPane.setTopAnchor(newContent, 0.0);
                AnchorPane.setBottomAnchor(newContent, 0.0);
                AnchorPane.setLeftAnchor(newContent, 0.0);
                AnchorPane.setRightAnchor(newContent, 0.0);

                switch (i % itemsPerPage)
                {
                    case 0:
                        crcPane1.getChildren().add(newContent);
                        break;

                    case 1:
                        crcPane2.getChildren().add(newContent);
                        break;
                }
            }
            catch (IOException e)
            {
                logger.severe("Error while loading credit card pane");
                e.printStackTrace();
                continue;
            }
        }

        crcPrevButton.setDisable(crcPaneCurrentPage == 0);
        crcNextButton.setDisable(end >= creditCards.size());
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
            if (crcPaneCurrentPage < creditCards.size() / itemsPerPage)
            {
                crcPaneCurrentPage++;
                UpdateDisplayCards();
            }
        });
    }
}
