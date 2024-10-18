/*
 * Filename: HomeController.java
 * Created on: September 20, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.main;

import com.jfoenix.controls.JFXButton;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.Wallet;
import org.mymoney.entities.WalletTransaction;
import org.mymoney.services.CreditCardService;
import org.mymoney.services.WalletService;
import org.mymoney.services.WalletTransactionService;
import org.mymoney.ui.common.ResumePaneController;
import org.mymoney.util.Animation;
import org.mymoney.util.Constants;
import org.mymoney.util.LoggerConfig;
import org.mymoney.util.TransactionType;
import org.mymoney.util.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

/**
 * Controller for the home view
 */
@Controller
public class HomeController
{
    private static final Logger logger = LoggerConfig.GetLogger();

    @FXML
    private JFXButton walletPrevButton;

    @FXML
    private JFXButton walletNextButton;

    @FXML
    private AnchorPane walletView1;

    @FXML
    private AnchorPane walletView2;

    @FXML
    private AnchorPane creditCardView1;

    @FXML
    private AnchorPane creditCardView2;

    @FXML
    private AnchorPane monthResumeView;

    @FXML
    private AnchorPane moneyFlowBarChartAnchorPane;

    @FXML
    private JFXButton creditCardPrevButton;

    @FXML
    private JFXButton creditCardNextButton;

    @FXML
    private BarChart<String, Number> moneyFlowBarChart;

    @FXML
    private Label monthResumePaneTitle;

    @FXML
    private TableView<WalletTransaction> transactionsTableView;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private List<Wallet> wallets;

    private List<CreditCard> creditCards;

    private List<WalletTransaction> transactions;

    private WalletService walletService;

    private WalletTransactionService walletTransactionService;

    private CreditCardService creditCardService;

    private Integer walletPaneCurrentPage = 0;

    private Integer creditCardPaneCurrentPage = 0;

    public HomeController() { }

    /**
     * Constructor for injecting the wallet and credit card services
     * @param walletService The wallet service
     * @param walletTransactionService The wallet transaction service
     * @param creditCardService The credit card service
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public HomeController(WalletService            walletService,
                          WalletTransactionService walletTransactionService,
                          CreditCardService        creditCardService)
    {
        this.walletService            = walletService;
        this.walletTransactionService = walletTransactionService;
        this.creditCardService        = creditCardService;
    }

    @FXML
    public void initialize()
    {
        LoadWalletsFromDatabase();
        LoadCreditCardsFromDatabase();
        LoadLastTransactionsFromDatabase(Constants.HOME_LAST_TRANSACTIONS_SIZE);

        logger.info("Loaded " + wallets.size() + " wallets from the database");

        logger.info("Loaded " + creditCards.size() + " credit cards from the database");

        // Update the display with the loaded data
        UpdateDisplayWallets();
        UpdateDisplayCreditCards();
        UpdateDisplayLastTransactions();
        UpdateMonthResume();
        UpdateMoneyFlowBarChart();

        SetButtonsActions();
    }

    /**
     * Set the actions for the buttons
     */
    private void SetButtonsActions()
    {
        walletPrevButton.setOnAction(event -> {
            if (walletPaneCurrentPage > 0)
            {
                walletPaneCurrentPage--;
                UpdateDisplayWallets();
            }
        });

        walletNextButton.setOnAction(event -> {
            if (walletPaneCurrentPage <
                wallets.size() / Constants.HOME_PANES_ITEMS_PER_PAGE)
            {
                walletPaneCurrentPage++;
                UpdateDisplayWallets();
            }
        });

        creditCardPrevButton.setOnAction(event -> {
            if (creditCardPaneCurrentPage > 0)
            {
                creditCardPaneCurrentPage--;
                UpdateDisplayCreditCards();
            }
        });

        creditCardNextButton.setOnAction(event -> {
            if (creditCardPaneCurrentPage <
                creditCards.size() / Constants.HOME_PANES_ITEMS_PER_PAGE)
            {
                creditCardPaneCurrentPage++;
                UpdateDisplayCreditCards();
            }
        });
    }

    /**
     * Load wallets from the database
     */
    private void LoadWalletsFromDatabase()
    {
        wallets = walletService.GetAllNonArchivedWalletsOrderedByName();
    }

    /**
     * Load credit cards from the database
     */
    private void LoadCreditCardsFromDatabase()
    {
        creditCards = creditCardService.GetAllCreditCardsOrderedByName();
    }

    /**
     * Load the last transactions from the database
     * @param n The number of transactions to be loaded
     */
    private void LoadLastTransactionsFromDatabase(Integer n)
    {
        transactions = walletTransactionService.GetNonArchivedLastTransactions(n);
    }

    /**
     * Update the display of wallets
     */
    private void UpdateDisplayWallets()
    {
        walletView1.getChildren().clear();
        walletView2.getChildren().clear();

        Integer start = walletPaneCurrentPage * Constants.HOME_PANES_ITEMS_PER_PAGE;
        Integer end =
            Math.min(start + Constants.HOME_PANES_ITEMS_PER_PAGE, wallets.size());

        for (Integer i = start; i < end; i++)
        {
            Wallet wallet     = wallets.get(i);
            HBox   walletHBox = CreateWalletItemNode(wallet);

            AnchorPane.setTopAnchor(walletHBox, 0.0);
            AnchorPane.setBottomAnchor(walletHBox, 0.0);

            if (i % 2 == 0)
            {
                walletView1.getChildren().add(walletHBox);
                AnchorPane.setLeftAnchor(walletHBox, 0.0);
                AnchorPane.setRightAnchor(walletHBox, 10.0);
            }
            else
            {
                walletView2.getChildren().add(walletHBox);
                AnchorPane.setLeftAnchor(walletHBox, 10.0);
                AnchorPane.setRightAnchor(walletHBox, 0.0);
            }
        }

        walletPrevButton.setDisable(walletPaneCurrentPage == 0);
        walletNextButton.setDisable(end >= wallets.size());
    }

    /**
     * Update the display of credit cards
     */
    private void UpdateDisplayCreditCards()
    {
        creditCardView1.getChildren().clear();
        creditCardView2.getChildren().clear();

        Integer start = creditCardPaneCurrentPage * Constants.HOME_PANES_ITEMS_PER_PAGE;
        Integer end =
            Math.min(start + Constants.HOME_PANES_ITEMS_PER_PAGE, creditCards.size());

        for (Integer i = start; i < end; i++)
        {
            CreditCard creditCard = creditCards.get(i);
            HBox       crcHbox    = CreateCreditCardItemNode(creditCard);

            AnchorPane.setTopAnchor(crcHbox, 0.0);
            AnchorPane.setBottomAnchor(crcHbox, 0.0);

            if (i % 2 == 0)
            {
                creditCardView1.getChildren().add(crcHbox);
                AnchorPane.setLeftAnchor(crcHbox, 0.0);
                AnchorPane.setRightAnchor(crcHbox, 10.0);
            }
            else
            {
                creditCardView2.getChildren().add(crcHbox);
                AnchorPane.setLeftAnchor(crcHbox, 10.0);
                AnchorPane.setRightAnchor(crcHbox, 0.0);
            }
        }

        creditCardPrevButton.setDisable(creditCardPaneCurrentPage == 0);
        creditCardNextButton.setDisable(end >= creditCards.size());
    }

    /**
     * Update the display of the last transactions using VBox
     */
    private void UpdateDisplayLastTransactions()
    {
        transactionsTableView.getColumns().clear();

        TableColumn<WalletTransaction, WalletTransaction> transactionColumn =
            new TableColumn<>("Last " + Constants.HOME_LAST_TRANSACTIONS_SIZE +
                              " Transactions");

        transactionColumn.setCellValueFactory(
            param -> new SimpleObjectProperty<>(param.getValue()));

        // Set the cell factory to display the transaction information
        transactionColumn.setCellFactory(
            column -> new TableCell<WalletTransaction, WalletTransaction>() {
                @Override
                protected void updateItem(WalletTransaction transaction, boolean empty)
                {
                    super.updateItem(transaction, empty);
                    if (empty || transaction == null)
                    {
                        setGraphic(null);
                    }
                    else
                    {
                        ImageView icon =
                            transaction.GetType() == TransactionType.INCOME
                                ? new ImageView(Constants.HOME_INCOME_ICON)
                                : new ImageView(Constants.HOME_EXPENSE_ICON);

                        icon.setFitHeight(Constants.HOME_LAST_TRANSACTIONS_ICON_SIZE);
                        icon.setFitWidth(Constants.HOME_LAST_TRANSACTIONS_ICON_SIZE);

                        Label descriptionLabel =
                            new Label(transaction.GetDescription());
                        descriptionLabel.setMinWidth(
                            Constants.HOME_LAST_TRANSACTIONS_DESCRIPTION_LABEL_WIDTH);

                        Label valueLabel =
                            new Label(UIUtils.FormatCurrency(transaction.GetAmount()));
                        valueLabel.setMinWidth(
                            Constants.HOME_LAST_TRANSACTIONS_VALUE_LABEL_WIDTH);

                        Label walletLabel =
                            new Label(transaction.GetWallet().GetName());
                        walletLabel.setMinWidth(
                            Constants.HOME_LAST_TRANSACTIONS_WALLET_LABEL_WIDTH);

                        Label dateLabel = new Label(
                            transaction.GetDate().format(DateTimeFormatter.ofPattern(
                                Constants.DATE_FORMAT_NO_TIME)));
                        dateLabel.setMinWidth(
                            Constants.HOME_LAST_TRANSACTIONS_DATE_LABEL_WIDTH);

                        Label transactionStatusLabel = new Label(StringUtils.capitalize(
                            transaction.GetStatus().toString().toLowerCase()));
                        transactionStatusLabel.setMinWidth(
                            Constants.HOME_LAST_TRANSACTIONS_STATUS_LABEL_WIDTH);

                        Label transactionCategoryLabel =
                            new Label(transaction.GetCategory().GetName());
                        transactionCategoryLabel.setMinWidth(
                            Constants.HOME_LAST_TRANSACTIONS_CATEGORY_LABEL_WIDTH);

                        HBox descriptionValueBox =
                            new HBox(descriptionLabel, valueLabel);
                        descriptionValueBox.setAlignment(Pos.CENTER_LEFT);

                        HBox walletCategoryStatusDateBox =
                            new HBox(walletLabel,
                                     transactionCategoryLabel,
                                     transactionStatusLabel,
                                     dateLabel);
                        walletCategoryStatusDateBox.setAlignment(Pos.CENTER_LEFT);

                        VBox vbox = new VBox(5,
                                             descriptionValueBox,
                                             walletCategoryStatusDateBox);
                        HBox hbox = new HBox(10, icon, vbox);
                        hbox.setAlignment(Pos.CENTER_LEFT);

                        // Set style class based on the transaction type
                        if (transaction.GetType() == TransactionType.INCOME)
                        {
                            hbox.getStyleClass().add(
                                Constants.HOME_LAST_TRANSACTIONS_INCOME_ITEM_STYLE);
                        }
                        else
                        {
                            hbox.getStyleClass().add(
                                Constants.HOME_LAST_TRANSACTIONS_EXPENSE_ITEM_STYLE);
                        }

                        setGraphic(hbox);
                    }
                }
            });

        transactionsTableView.getColumns().add(transactionColumn);

        transactionsTableView.setItems(FXCollections.observableArrayList(transactions));
    }

    /**
     * Update the chart with incomes and expenses for the last months
     */
    private void UpdateMoneyFlowBarChart()
    {
        CreateMoneyFlowBarChart();

        // LinkedHashMap to keep the order of the months
        Map<String, Double> monthlyExpenses = new LinkedHashMap<>();
        Map<String, Double> monthlyIncomes  = new LinkedHashMap<>();

        LocalDateTime     currentDate = LocalDateTime.now();
        DateTimeFormatter formatter   = DateTimeFormatter.ofPattern("MMM/yy");

        // Collect data for the last months
        for (Integer i = 0; i < Constants.XYBAR_CHART_MONTHS; i++)
        {
            // Get the data from the oldest month to the most recent, to keep the order
            LocalDateTime date =
                currentDate.minusMonths(Constants.XYBAR_CHART_MONTHS - i - 1);
            Integer month = date.getMonthValue();
            Integer year  = date.getYear();

            // Get transactions
            List<WalletTransaction> transactions =
                walletTransactionService.GetNonArchivedTransactionsByMonth(month, year);
            logger.info("Found " + transactions.size() + " transactions for " + month +
                        "/" + year);

            // Calculate total expenses for the month
            Double totalExpenses =
                transactions.stream()
                    .filter(t -> t.GetType() == TransactionType.EXPENSE)
                    .mapToDouble(WalletTransaction::GetAmount)
                    .sum();

            // Calculate total incomes for the month
            Double totalIncomes =
                transactions.stream()
                    .filter(t -> t.GetType() == TransactionType.INCOME)
                    .mapToDouble(WalletTransaction::GetAmount)
                    .sum();

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
                new XYChart.Data<>(month, 0.0)); // start at 0 for animation
            incomesSeries.getData().add(
                new XYChart.Data<>(month, 0.0)); // start at 0 for animation

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
            // The tick unit must be a multiple of 10
            Integer tickUnit =
                (int)Math.round(((maxValue / Constants.XYBAR_CHART_TICKS) / 10) * 10);
            numberAxis.setTickUnit(tickUnit);
        }

        moneyFlowBarChart.setVerticalGridLinesVisible(false);

        // Clear previous data and add the new series (expenses and incomes)
        moneyFlowBarChart.getData().add(expensesSeries);
        moneyFlowBarChart.getData().add(incomesSeries);

        // Add tooltips and animations to the bars
        // expensesSeries and incomesSeries have the same size
        for (Integer i = 0; i < expensesSeries.getData().size(); i++)
        {
            XYChart.Data<String, Number> expenseData = expensesSeries.getData().get(i);
            XYChart.Data<String, Number> incomeData  = incomesSeries.getData().get(i);

            Double targetExpenseValue = monthlyExpenses.get(expenseData.getXValue());

            // Add tooltip to the bars
            UIUtils.AddTooltipToXYChartNode(expenseData.getNode(),
                                            UIUtils.FormatCurrency(targetExpenseValue));

            Double targetIncomeValue =
                monthlyIncomes.getOrDefault(expenseData.getXValue(), 0.0);

            UIUtils.AddTooltipToXYChartNode(incomeData.getNode(),
                                            UIUtils.FormatCurrency(targetIncomeValue));

            // Animation for the bars
            Animation.XYChartAnimation(expenseData, targetExpenseValue);
            Animation.XYChartAnimation(incomeData, targetIncomeValue);
        }
    }

    /**
     * Update the display of the month resume
     */
    private void UpdateMonthResume()
    {
        try
        {
            FXMLLoader loader =
                new FXMLLoader(getClass().getResource(Constants.RESUME_PANE_FXML));
            loader.setControllerFactory(springContext::getBean);
            Parent newContent = loader.load();

            // Add style class to the wallet pane
            newContent.getStylesheets().add(
                getClass().getResource(Constants.COMMON_STYLE_SHEET).toExternalForm());

            ResumePaneController resumePaneController = loader.getController();

            LocalDateTime currentDate = LocalDateTime.now();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy");
            monthResumePaneTitle.setText(currentDate.format(formatter) + " Resume");

            resumePaneController.UpdateResumePane(currentDate.getMonthValue(),
                                                  currentDate.getYear());

            AnchorPane.setTopAnchor(newContent, 0.0);
            AnchorPane.setBottomAnchor(newContent, 0.0);
            AnchorPane.setLeftAnchor(newContent, 0.0);
            AnchorPane.setRightAnchor(newContent, 0.0);

            monthResumeView.getChildren().clear();
            monthResumeView.getChildren().add(newContent);
        }
        catch (Exception e)
        {
            logger.severe("Error updating month resume: " + e.getMessage());
        }
    }

    /**
     * Create a node for a credit card
     * @param creditCard The credit card to be displayed
     * @return The HBox containing the credit card information
     */
    private HBox CreateCreditCardItemNode(CreditCard creditCard)
    {
        HBox rootHbox = new HBox(10);
        rootHbox.getStyleClass().add(Constants.HOME_CREDIT_CARD_ITEM_STYLE);

        VBox infoVbox = new VBox(10);
        infoVbox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(creditCard.GetName());
        nameLabel.getStyleClass().add(Constants.HOME_CREDIT_CARD_ITEM_NAME_STYLE);
        UIUtils.AddTooltipToNode(nameLabel, "Credit card name");

        Label crcOperatorLabel = new Label(creditCard.GetOperator().GetName());
        crcOperatorLabel.getStyleClass().add(
            Constants.HOME_CREDIT_CARD_ITEM_OPERATOR_STYLE);
        crcOperatorLabel.setAlignment(Pos.TOP_LEFT);
        UIUtils.AddTooltipToNode(crcOperatorLabel, "Credit card operator");

        Label availableCredit = new Label(UIUtils.FormatCurrency(
            creditCardService.GetAvailableCredit(creditCard.GetId())));

        availableCredit.getStyleClass().add(
            Constants.HOME_CREDIT_CARD_ITEM_BALANCE_STYLE);

        UIUtils.AddTooltipToNode(availableCredit, "Available credit");

        Label digitsLabel =
            new Label(UIUtils.FormatCreditCardNumber(creditCard.GetLastFourDigits()));
        digitsLabel.getStyleClass().add(Constants.HOME_CREDIT_CARD_ITEM_DIGITS_STYLE);
        UIUtils.AddTooltipToNode(digitsLabel, "Credit card number");

        infoVbox.getChildren().addAll(nameLabel,
                                      crcOperatorLabel,
                                      availableCredit,
                                      digitsLabel);

        ImageView icon = new ImageView(Constants.CRC_OPERATOR_ICONS_PATH +
                                       creditCard.GetOperator().GetIcon());

        icon.setFitHeight(Constants.CRC_OPERATOR_ICONS_SIZE);
        icon.setFitWidth(Constants.CRC_OPERATOR_ICONS_SIZE);

        VBox iconVBox = new VBox();
        iconVBox.setAlignment(Pos.CENTER_RIGHT);
        iconVBox.getChildren().add(icon);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        rootHbox.getChildren().addAll(infoVbox, spacer, iconVBox);

        return rootHbox;
    }

    /**
     * Create a node for a wallet
     * @param wallet The wallet to be displayed
     * @return The HBox containing the wallet information
     */
    private HBox CreateWalletItemNode(Wallet wallet)
    {
        HBox rootHbox = new HBox(10);
        rootHbox.getStyleClass().add(Constants.HOME_WALLET_ITEM_STYLE);

        VBox infoVbox = new VBox(10);
        infoVbox.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(wallet.GetName());
        nameLabel.getStyleClass().add(Constants.HOME_WALLET_ITEM_NAME_STYLE);
        UIUtils.AddTooltipToNode(nameLabel, "Wallet name");

        Label walletTypeLabel = new Label(wallet.GetType().GetName());
        walletTypeLabel.getStyleClass().add(Constants.HOME_WALLET_TYPE_STYLE);
        walletTypeLabel.setAlignment(Pos.TOP_LEFT);
        UIUtils.AddTooltipToNode(walletTypeLabel, "Wallet type");

        Label balanceLabel = new Label(UIUtils.FormatCurrency(wallet.GetBalance()));
        balanceLabel.getStyleClass().add(Constants.HOME_WALLET_ITEM_BALANCE_STYLE);
        UIUtils.AddTooltipToNode(balanceLabel, "Wallet balance");

        infoVbox.getChildren().addAll(nameLabel, walletTypeLabel, balanceLabel);

        ImageView icon = new ImageView(Constants.WALLET_TYPE_ICONS_PATH +
                                       wallet.GetType().GetIcon());

        icon.setFitHeight(Constants.WALLET_TYPE_ICONS_SIZE);
        icon.setFitWidth(Constants.WALLET_TYPE_ICONS_SIZE);

        VBox iconVBox = new VBox();
        iconVBox.setAlignment(Pos.CENTER_RIGHT);
        iconVBox.getChildren().add(icon);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        rootHbox.getChildren().addAll(infoVbox, spacer, iconVBox);

        return rootHbox;
    }

    /**
     * Create a new bar chart
     */
    private void CreateMoneyFlowBarChart()
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
    }
}
