/*
 * Filename: WalletController.java
 * Created on: September 29, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.jfoenix.controls.JFXButton;
import com.mymoney.charts.DoughnutChart;
import com.mymoney.entities.Wallet;
import com.mymoney.entities.WalletTransaction;
import com.mymoney.entities.WalletType;
import com.mymoney.services.WalletService;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import com.mymoney.util.TransactionStatus;
import com.mymoney.util.TransactionType;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Controller for the wallet view
 */
@Component
public class WalletController
{
    @FXML
    private AnchorPane totalBalanceView;

    @FXML
    private AnchorPane walletPane1;

    @FXML
    private AnchorPane walletPane2;

    @FXML
    private AnchorPane walletPane3;

    @FXML
    private AnchorPane moneyFlowBarChartAnchorPane;

    @FXML
    private AnchorPane balanceByWalletTypePieChartAnchorPane;

    @FXML
    private VBox totalBalanceByWalletTypeVBox;

    @FXML
    private VBox totalBalancePaneInfoVBox;

    @FXML
    private JFXButton totalBalancePaneTransferButton;

    @FXML
    private JFXButton totalBalancePaneAddWalletButton;

    @FXML
    private JFXButton walletPrevButton;

    @FXML
    private JFXButton walletNextButton;

    @FXML
    private ComboBox<String> totalBalancePaneWalletTypeComboBox;

    @FXML
    private ComboBox<String> moneyFlowPaneWalletTypeComboBox;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private BarChart<String, Number> moneyFlowBarChart;

    private WalletService walletService;

    private List<WalletTransaction> transactions;

    private List<WalletType> walletTypes;

    private List<Wallet> wallets;

    private Integer totalBalanceSelectedMonth;

    private Integer totalBalanceSelectedYear;

    private static final Logger logger = LoggerConfig.GetLogger();

    private Integer walletPaneCurrentPage = 0;

    private Integer itemsPerPage = 3;

    public WalletController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public WalletController(WalletService walletService)
    {
        this.walletService = walletService;
    }

    @FXML
    public void initialize()
    {
        totalBalanceSelectedMonth = LocalDate.now().getMonthValue();
        totalBalanceSelectedYear  = LocalDate.now().getYear();

        LoadWalletTransactions();
        LoadWalletTypes();
        LoadWallets();

        totalBalancePaneWalletTypeComboBox.getItems().addAll(
            walletTypes.stream().map(WalletType::GetName).toList());

        moneyFlowPaneWalletTypeComboBox.getItems().addAll(
            walletTypes.stream().map(WalletType::GetName).toList());

        // Add default wallet type and select it
        totalBalancePaneWalletTypeComboBox.getItems().add(0, "All Wallets");
        totalBalancePaneWalletTypeComboBox.getSelectionModel().selectFirst();

        moneyFlowPaneWalletTypeComboBox.getItems().add(0, "All Wallets");
        moneyFlowPaneWalletTypeComboBox.getSelectionModel().selectFirst();

        UpdateTotalBalanceView();
        UpdateDisplayWallets();
        CreateNewBarChart();
        UpdateBalanceByWalletTypeChartWithFilter();

        SetButtonsActions();
    }

    /**
     * Set the actions for the buttons
     */
    private void SetButtonsActions()
    {
        totalBalancePaneWalletTypeComboBox.setOnAction(e -> UpdateTotalBalanceView());
        moneyFlowPaneWalletTypeComboBox.setOnAction(e -> { CreateNewBarChart(); });

        totalBalancePaneTransferButton.setOnAction(e -> AddTransfer());
        totalBalancePaneAddWalletButton.setOnAction(e -> AddWallet());

        walletPrevButton.setOnAction(event -> {
            if (walletPaneCurrentPage > 0)
            {
                walletPaneCurrentPage--;
                UpdateDisplayWallets();
            }
        });

        walletNextButton.setOnAction(event -> {
            if (walletPaneCurrentPage < wallets.size() / itemsPerPage)
            {
                walletPaneCurrentPage++;
                UpdateDisplayWallets();
            }
        });
    }

    /**
     * Load the wallet transactions
     */
    private void LoadWalletTransactions()
    {
        transactions =
            walletService.GetAllTransactionsByMonth(totalBalanceSelectedMonth,
                                                    totalBalanceSelectedYear);
    }

    /**
     * Load the wallets
     */
    private void LoadWallets()
    {
        wallets = walletService.GetAllWallets();
    }

    /**
     * Load the wallet types
     */
    private void LoadWalletTypes()
    {
        walletTypes = walletService.GetAllWalletTypes();

        String nameToMove = "Others";

        // Move the "Others" wallet type to the end of the list
        if (walletTypes.stream()
                .filter(n -> n.GetName().equals(nameToMove))
                .findFirst()
                .isPresent())
        {
            WalletType wt = walletTypes.stream()
                                .filter(n -> n.GetName().equals(nameToMove))
                                .findFirst()
                                .get();

            walletTypes.remove(wt);
            walletTypes.add(wt);
        }
    }

    /**
     * Update the display of the total balance pane
     */
    private void UpdateTotalBalanceView()
    {
        LoadWallets();
        LoadWalletTransactions();
        LoadWalletTypes();

        Double pendingExpenses       = 0.0;
        Double pendingIncomes        = 0.0;
        Double walletsCurrentBalance = 0.0;
        Long   totalWallets          = 0L;

        // Filter wallet type according to the selected item
        // If "All Wallets" is selected, show all transactions
        Integer selectedIndex =
            totalBalancePaneWalletTypeComboBox.getSelectionModel().getSelectedIndex();

        if (selectedIndex == 0)
        {
            logger.info("Selected: " +
                        totalBalancePaneWalletTypeComboBox.getSelectionModel()
                            .getSelectedIndex());

            walletsCurrentBalance =
                wallets.stream().mapToDouble(Wallet::GetBalance).sum();

            pendingExpenses =
                transactions.stream()
                    .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                    .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                    .mapToDouble(WalletTransaction::GetAmount)
                    .sum();

            pendingIncomes =
                transactions.stream()
                    .filter(t -> t.GetType().equals(TransactionType.INCOME))
                    .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                    .mapToDouble(WalletTransaction::GetAmount)
                    .sum();

            totalWallets = transactions.stream()
                               .map(t -> t.GetWallet().GetId())
                               .distinct()
                               .count();
        }
        else if (selectedIndex > 0 && selectedIndex - 1 < walletTypes.size())
        {
            WalletType selectedWalletType = walletTypes.get(selectedIndex - 1);

            logger.info("Selected: " + selectedWalletType.GetName());

            walletsCurrentBalance =
                wallets.stream()
                    .filter(w -> w.GetType().GetId() == selectedWalletType.GetId())
                    .mapToDouble(Wallet::GetBalance)
                    .sum();

            pendingExpenses =
                transactions.stream()
                    .filter(t
                            -> t.GetWallet().GetType().GetId() ==
                                   selectedWalletType.GetId())
                    .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                    .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                    .mapToDouble(WalletTransaction::GetAmount)
                    .sum();

            pendingIncomes =
                transactions.stream()
                    .filter(t
                            -> t.GetWallet().GetType().GetId() ==
                                   selectedWalletType.GetId())
                    .filter(t -> t.GetType().equals(TransactionType.INCOME))
                    .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                    .mapToDouble(WalletTransaction::GetAmount)
                    .sum();

            totalWallets = transactions.stream()
                               .filter(t
                                       -> t.GetWallet().GetType().GetId() ==
                                              selectedWalletType.GetId())
                               .map(t -> t.GetWallet().GetId())
                               .distinct()
                               .count();
        }
        else
        {
            logger.warning("Invalid index: " + selectedIndex);
        }

        Double foreseenBalance =
            walletsCurrentBalance - pendingExpenses + pendingIncomes;

        String totalBalanceText;

        if (walletsCurrentBalance < 0)
        {
            totalBalanceText = String.format("- $ %.2f", -walletsCurrentBalance);
        }
        else
        {
            totalBalanceText = String.format("$ %.2f", walletsCurrentBalance);
        }

        String foreseenBalanceText = "Foreseen: ";

        if (foreseenBalance < 0)
        {
            foreseenBalanceText += String.format("- $ %.2f", -foreseenBalance);
        }
        else
        {
            foreseenBalanceText += String.format("$ %.2f", foreseenBalance);
        }

        Label totalBalanceValueLabel = new Label(totalBalanceText);
        totalBalanceValueLabel.getStyleClass().add(
            Constants.WALLET_TOTAL_BALANCE_VALUE_LABEL_STYLE);

        Label balanceForeseenLabel = new Label(foreseenBalanceText);
        balanceForeseenLabel.getStyleClass().add(
            Constants.WALLET_TOTAL_BALANCE_FORESEEN_LABEL_STYLE);

        Label totalWalletsLabel =
            new Label("Balance corresponds to " + totalWallets + " wallets");
        totalWalletsLabel.getStyleClass().add(
            Constants.WALLET_TOTAL_BALANCE_WALLETS_LABEL_STYLE);

        totalBalancePaneInfoVBox.getChildren().clear();
        totalBalancePaneInfoVBox.getChildren().add(totalBalanceValueLabel);
        totalBalancePaneInfoVBox.getChildren().add(balanceForeseenLabel);
        totalBalancePaneInfoVBox.getChildren().add(totalWalletsLabel);
    }

    /**
     * Update the display of wallets
     */
    private void UpdateDisplayWallets()
    {
        walletPane1.getChildren().clear();
        walletPane2.getChildren().clear();
        walletPane3.getChildren().clear();

        Integer start = walletPaneCurrentPage * itemsPerPage;
        Integer end   = Math.min(start + itemsPerPage, wallets.size());

        for (Integer i = start; i < end; i++)
        {
            Wallet wallet = wallets.get(i);

            try
            {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(Constants.WALLET_FULL_PANE_FXML));
                loader.setControllerFactory(springContext::getBean);
                Parent newContent = loader.load();

                // Add style class to the wallet pane
                newContent.getStylesheets().add(
                    getClass()
                        .getResource(Constants.COMMON_STYLE_SHEET)
                        .toExternalForm());

                WalletFullPaneController walletFullPaneController =
                    loader.getController();

                walletFullPaneController.UpdateWalletPane(wallet);

                AnchorPane.setTopAnchor(newContent, 0.0);
                AnchorPane.setBottomAnchor(newContent, 0.0);
                AnchorPane.setLeftAnchor(newContent, 0.0);
                AnchorPane.setRightAnchor(newContent, 0.0);

                switch (i % itemsPerPage)
                {
                    case 0:
                        walletPane1.getChildren().add(newContent);
                        break;

                    case 1:
                        walletPane2.getChildren().add(newContent);
                        break;

                    case 2:
                        walletPane3.getChildren().add(newContent);
                        break;
                }
            }
            catch (IOException e)
            {
                logger.severe("Error while loading wallet full pane");
                e.printStackTrace();
                continue;
            }
        }

        walletPrevButton.setDisable(walletPaneCurrentPage == 0);
        walletNextButton.setDisable(end >= wallets.size());
    }

    /**
     * Update the balance by wallet type chart with filter
     */
    private void UpdateBalanceByWalletTypeChartWithFilter()
    {
        LoadWallets();
        LoadWalletTypes();

        balanceByWalletTypePieChartAnchorPane.getChildren().clear();

        List<CheckBox> checkBoxes = new ArrayList<>();

        for (WalletType wt : walletTypes)
        {
            CheckBox checkBox = new CheckBox(wt.GetName());
            checkBox.getStyleClass().add(Constants.WALLET_CHECK_BOX_STYLE);
            checkBox.setSelected(true);
            checkBoxes.add(checkBox);
            totalBalanceByWalletTypeVBox.getChildren().add(checkBox);
        }

        UpdateChart(checkBoxes);

        for (CheckBox checkBox : checkBoxes)
        {
            checkBox.selectedProperty().addListener(
                (obs, wasSelected, isNowSelected) -> { UpdateChart(checkBoxes); });
        }
    }

    /**
     * Update the chart with the selected wallet types
     * @param checkBoxes The list of checkboxes
     */
    private void UpdateChart(List<CheckBox> checkBoxes)
    {
        ObservableList<PieChart.Data> pieChartData =
            FXCollections.observableArrayList();

        for (CheckBox checkBox : checkBoxes)
        {
            checkBox.getStyleClass().add(Constants.WALLET_CHECK_BOX_STYLE);

            if (checkBox.isSelected())
            {
                String     walletTypeName = checkBox.getText();
                WalletType wt =
                    walletTypes.stream()
                        .filter(type -> type.GetName().equals(walletTypeName))
                        .findFirst()
                        .orElse(null);

                // If the wallet type is not found, skip
                if (wt != null)
                {
                    Double totalBalance =
                        wallets.stream()
                            .filter(w -> w.GetType().GetId() == wt.GetId())
                            .mapToDouble(Wallet::GetBalance)
                            .sum();
                    pieChartData.add(new PieChart.Data(wt.GetName(), totalBalance));
                }
            }
        }

        DoughnutChart doughnutChart = new DoughnutChart(pieChartData);
        doughnutChart.setLabelsVisible(false);

        // Remove the previous chart and add the new one
        balanceByWalletTypePieChartAnchorPane.getChildren().removeIf(
            node -> node instanceof DoughnutChart);

        balanceByWalletTypePieChartAnchorPane.getChildren().add(doughnutChart);

        AnchorPane.setTopAnchor(doughnutChart, 0.0);
        AnchorPane.setBottomAnchor(doughnutChart, 0.0);
        AnchorPane.setLeftAnchor(doughnutChart, 0.0);
        AnchorPane.setRightAnchor(doughnutChart, 0.0);
    }

    /**
     * Create a new bar chart
     */
    private void CreateNewBarChart()
    {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();

        moneyFlowBarChart = new BarChart<>(xAxis, yAxis);

        moneyFlowBarChartAnchorPane.getChildren().clear();

        moneyFlowBarChartAnchorPane.getChildren().add(moneyFlowBarChart);

        AnchorPane.setTopAnchor(moneyFlowBarChart, 0.0);
        AnchorPane.setBottomAnchor(moneyFlowBarChart, 0.0);
        AnchorPane.setLeftAnchor(moneyFlowBarChart, 0.0);
        AnchorPane.setRightAnchor(moneyFlowBarChart, 0.0);

        // Populate the chart with data
        UpdateMoneyFlowBarChart();
    }

    /**
     * Update the chart with incomes and expenses for the last months
     */
    private void UpdateMoneyFlowBarChart()
    {
        // LinkedHashMap to keep the order of the months
        Map<String, Double> monthlyExpenses = new LinkedHashMap<>();
        Map<String, Double> monthlyIncomes  = new LinkedHashMap<>();

        LocalDateTime     currentDate = LocalDateTime.now();
        DateTimeFormatter formatter   = DateTimeFormatter.ofPattern("MMM/yy");

        // Filter wallet type according to the selected item
        // If "All Wallets" is selected, show all transactions
        Integer selectedIndex =
            moneyFlowPaneWalletTypeComboBox.getSelectionModel().getSelectedIndex();

        // Collect data for the last months
        for (Integer i = 0; i < Constants.HOME_BAR_CHART_MONTHS; i++)
        {
            // Get the data from the oldest month to the most recent, to keep the order
            LocalDateTime date =
                currentDate.minusMonths(Constants.HOME_BAR_CHART_MONTHS - i - 1);
            Integer month = date.getMonthValue();
            Integer year  = date.getYear();

            // Get transactions
            List<WalletTransaction> transactions =
                walletService.GetAllTransactionsByMonth(month, year);
            logger.info("Found " + transactions.size() + " transactions for " + month +
                        "/" + year);

            Double totalExpenses = 0.0;
            Double totalIncomes  = 0.0;

            if (selectedIndex == 0)
            {
                // Calculate total expenses for the month
                totalExpenses = transactions.stream()
                                    .filter(t -> t.GetType() == TransactionType.EXPENSE)
                                    .mapToDouble(WalletTransaction::GetAmount)
                                    .sum();

                // Calculate total incomes for the month
                totalIncomes = transactions.stream()
                                   .filter(t -> t.GetType() == TransactionType.INCOME)
                                   .mapToDouble(WalletTransaction::GetAmount)
                                   .sum();
            }
            else if (selectedIndex > 0 && selectedIndex - 1 < walletTypes.size())
            {
                WalletType selectedWalletType = walletTypes.get(selectedIndex - 1);

                // Calculate total expenses for the month
                totalExpenses = transactions.stream()
                                    .filter(t
                                            -> t.GetWallet().GetType().GetId() ==
                                                   selectedWalletType.GetId())
                                    .filter(t -> t.GetType() == TransactionType.EXPENSE)
                                    .mapToDouble(WalletTransaction::GetAmount)
                                    .sum();

                // Calculate total incomes for the month
                totalIncomes = transactions.stream()
                                   .filter(t
                                           -> t.GetWallet().GetType().GetId() ==
                                                  selectedWalletType.GetId())
                                   .filter(t -> t.GetType() == TransactionType.INCOME)
                                   .mapToDouble(WalletTransaction::GetAmount)
                                   .sum();
            }
            else
            {
                logger.warning("Invalid index: " + selectedIndex);
            }

            monthlyExpenses.put(date.format(formatter), totalExpenses);
            monthlyIncomes.put(date.format(formatter), totalIncomes);
        }

        // Create two series: one for incomes and one for expenses
        XYChart.Series<String, Number> expensesSeries = new XYChart.Series<>();
        expensesSeries.setName("Expenses");

        XYChart.Series<String, Number> incomesSeries = new XYChart.Series<>();
        incomesSeries.setName("Incomes");

        Double maxValue = 0.0;

        // Add data to each series
        for (Map.Entry<String, Double> entry : monthlyExpenses.entrySet())
        {
            String month        = entry.getKey();
            Double expenseValue = entry.getValue();
            Double incomeValue  = monthlyIncomes.getOrDefault(month, 0.0);

            expensesSeries.getData().add(
                new XYChart.Data<>(month, 0.0)); // Start at 0 for animation
            incomesSeries.getData().add(
                new XYChart.Data<>(month, 0.0)); // Start at 0 for animation

            maxValue = Math.max(maxValue, Math.max(expenseValue, incomeValue));
        }

        // Set Y-axis limits based on the maximum value found
        Axis<?> yAxis = moneyFlowBarChart.getYAxis();
        if (yAxis instanceof NumberAxis)
        {
            NumberAxis numberAxis = (NumberAxis)yAxis;
            numberAxis.setAutoRanging(false);
            numberAxis.setLowerBound(0);
            numberAxis.setUpperBound(maxValue);

            // Set the tick unit based on the maximum value
            Integer tickUnit = (int)Math.round(
                ((maxValue / Constants.HOME_BAR_CHART_TICKS) / 10) * 10);
            numberAxis.setTickUnit(tickUnit);
        }

        moneyFlowBarChart.setVerticalGridLinesVisible(false);

        moneyFlowBarChart.getData().add(expensesSeries);
        moneyFlowBarChart.getData().add(incomesSeries);

        for (int i = 0; i < expensesSeries.getData().size(); i++)
        {
            XYChart.Data<String, Number> expenseData = expensesSeries.getData().get(i);
            XYChart.Data<String, Number> incomeData  = incomesSeries.getData().get(i);

            Double targetExpenseValue = monthlyExpenses.get(expenseData.getXValue());

            // Add tooltip to the bars
            AddTooltipToNode(expenseData.getNode(),
                             String.format("%.2f", targetExpenseValue));

            Double targetIncomeValue =
                monthlyIncomes.getOrDefault(expenseData.getXValue(), 0.0);

            AddTooltipToNode(incomeData.getNode(),
                             String.format("%.2f", targetIncomeValue));

            // Animation for Expenses
            CreateAnimation(expenseData, targetExpenseValue);

            // Animation for Incomes
            CreateAnimation(incomeData, targetIncomeValue);
        }
    }

    /**
     * Create an animation for a bar
     * @param data The data to animate
     * @param targetValue The target value
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
     * Add a new wallet
     */
    private void AddWallet()
    {
        try
        {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource(Constants.ADD_WALLET_FXML));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Create new wallet");
            popupStage.setScene(new Scene(root));

            popupStage.setOnHidden(e -> {
                UpdateTotalBalanceView();
                UpdateDisplayWallets();
            });

            popupStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Add a new transfer
     */
    private void AddTransfer()
    {
        try
        {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource(Constants.ADD_TRANSFER_FXML));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage popupStage = new Stage();

            Scene scene = new Scene(root);

            scene.getStylesheets().add(
                getClass().getResource(Constants.COMMON_STYLE_SHEET).toExternalForm());

            popupStage.setTitle("Add new transfer");
            popupStage.setScene(scene);

            popupStage.setOnHidden(e -> {
                UpdateTotalBalanceView();
                UpdateDisplayWallets();
            });

            popupStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
}
