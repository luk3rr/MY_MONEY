/*
 * Filename: HomeController.java
 * Created on: September 20, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.jfoenix.controls.JFXButton;
import com.mymoney.entities.CreditCard;
import com.mymoney.entities.Wallet;
import com.mymoney.entities.WalletTransaction;
import com.mymoney.services.CreditCardService;
import com.mymoney.services.WalletService;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import com.mymoney.util.TransactionType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.Axis;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HomeController
{
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
    private JFXButton creditCardPrevButton;

    @FXML
    private JFXButton creditCardNextButton;

    @FXML
    private ListView<WalletTransaction> lastTransactions;

    @FXML
    private BarChart<String, Double> expensesLast12Months;

    @FXML
    private Label monthResumePaneTitle;

    private List<Wallet> wallets;

    private List<CreditCard> creditCards;

    private List<WalletTransaction> transactions;

    private WalletService walletService;

    private CreditCardService creditCardService;

    private Integer walletPaneCurrentPage = 0;

    private Integer creditCardPaneCurrentPage = 0;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public HomeController() { }

    /**
     * Constructor for injecting the wallet and credit card services
     * @param walletService The wallet service
     * @param creditCardService The credit card service
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public HomeController(WalletService     walletService,
                          CreditCardService creditCardService)
    {
        this.walletService     = walletService;
        this.creditCardService = creditCardService;
    }

    @FXML
    public void initialize()
    {
        LoadWalletsFromDatabase();
        LoadCreditCardsFromDatabase();
        LoadLastTransactionsFromDatabase(Constants.HOME_LAST_TRANSACTIONS_SIZE);

        m_logger.info("Loaded " + wallets.size() + " wallets from the database");

        m_logger.info("Loaded " + creditCards.size() +
                      " credit cards from the database");

        // Update the display with the loaded data
        UpdateDisplayWallets();
        UpdateDisplayCreditCards();
        UpdateDisplayLastTransactions();
        UpdateChartExpensesLast12Months();
        UpdateMonthResume();

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
        wallets = walletService.GetAllWalletsOrderedByName();
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
        transactions = walletService.GetLastTransactions(n);
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
            VBox   walletVBox = CreateWalletItemNode(wallet);

            AnchorPane.setTopAnchor(walletVBox, 0.0);
            AnchorPane.setBottomAnchor(walletVBox, 0.0);

            if (i % 2 == 0)
            {
                walletView1.getChildren().add(walletVBox);
                AnchorPane.setLeftAnchor(walletVBox, 0.0);
                AnchorPane.setRightAnchor(walletVBox, 10.0);
            }
            else
            {
                walletView2.getChildren().add(walletVBox);
                AnchorPane.setLeftAnchor(walletVBox, 10.0);
                AnchorPane.setRightAnchor(walletVBox, 0.0);
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
            VBox       crcVBox    = CreateCreditCardItemNode(creditCard);

            AnchorPane.setTopAnchor(crcVBox, 0.0);
            AnchorPane.setBottomAnchor(crcVBox, 0.0);

            if (i % 2 == 0)
            {
                creditCardView1.getChildren().add(crcVBox);
                AnchorPane.setLeftAnchor(crcVBox, 0.0);
                AnchorPane.setRightAnchor(crcVBox, 10.0);
            }
            else
            {
                creditCardView2.getChildren().add(crcVBox);
                AnchorPane.setLeftAnchor(crcVBox, 10.0);
                AnchorPane.setRightAnchor(crcVBox, 0.0);
            }
        }

        creditCardPrevButton.setDisable(creditCardPaneCurrentPage == 0);
        creditCardNextButton.setDisable(end >= creditCards.size());
    }

    /**
     * Update the display of the last transactions
     */
    private void UpdateDisplayLastTransactions()
    {
        lastTransactions.getItems().clear();
        lastTransactions.setMouseTransparent(true);
        lastTransactions.getItems().addAll(transactions);

        lastTransactions.setCellFactory(listView -> new ListCell<WalletTransaction>() {
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
                    ImageView icon = transaction.GetType() == TransactionType.INCOME
                                         ? new ImageView(Constants.INCOME_ICON)
                                         : new ImageView(Constants.EXPENSE_ICON);

                    icon.setFitHeight(Constants.HOME_LAST_TRANSACTIONS_ICON_SIZE);
                    icon.setFitWidth(Constants.HOME_LAST_TRANSACTIONS_ICON_SIZE);

                    // Labels
                    Label descriptionLabel = new Label(transaction.GetDescription());
                    descriptionLabel.setPrefWidth(
                        Constants.HOME_LAST_TRANSACTIONS_DESCRIPTION_LABEL_WIDTH);

                    Label valueLabel =
                        new Label(String.format("$ %.2f", transaction.GetAmount()));
                    valueLabel.setPrefWidth(
                        Constants.HOME_LAST_TRANSACTIONS_VALUE_LABEL_WIDTH);

                    Label dateLabel = new Label(transaction.GetDate().toString());
                    dateLabel.setPrefWidth(
                        Constants.HOME_LAST_TRANSACTIONS_DATE_LABEL_WIDTH);

                    Label walletLabel = new Label(transaction.GetWallet().GetName());
                    walletLabel.setPrefWidth(
                        Constants.HOME_LAST_TRANSACTIONS_WALLET_LABEL_WIDTH);

                    HBox descriptionValueBox = new HBox();
                    descriptionValueBox.getStyleClass().add(
                        Constants.HOME_LAST_TRANSACTIONS_DESCRIPTION_VALUE_STYLE);

                    descriptionValueBox.getChildren().addAll(descriptionLabel,
                                                             valueLabel);

                    HBox.setHgrow(descriptionLabel, Priority.ALWAYS);
                    descriptionValueBox.setAlignment(Pos.CENTER_LEFT);

                    HBox walletDateBox = new HBox();

                    walletDateBox.getStyleClass().add(
                        Constants.HOME_LAST_TRANSACTIONS_WALLET_DATE_STYLE);

                    walletDateBox.getChildren().addAll(walletLabel, dateLabel);

                    HBox.setHgrow(walletLabel, Priority.ALWAYS);

                    walletDateBox.setAlignment(Pos.CENTER_LEFT);

                    VBox vbox = new VBox(5, descriptionValueBox, walletDateBox);
                    vbox.getStyleClass().add(
                        Constants.HOME_LAST_TRANSACTIONS_TRANSACTION_DETAILS_STYLE);

                    HBox hbox = new HBox(10, icon, vbox);
                    hbox.setAlignment(Pos.CENTER_LEFT);

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
    }

    /**
     * Update the expenses chart
     */
    private void UpdateChartExpensesLast12Months()
    {
        Map<String, Double> monthlyExpenses =
            new LinkedHashMap<>(); // To keep the order

        LocalDate         currentDate = LocalDate.now();
        DateTimeFormatter formatter   = DateTimeFormatter.ofPattern("MMM/yy");

        for (Integer i = 0; i < Constants.HOME_BAR_CHART_MONTHS; i++)
        {
            // Get the data from the oldest month to the most recent, to keep the order
            // in the chart
            LocalDate date =
                currentDate.minusMonths(Constants.HOME_BAR_CHART_MONTHS - i - 1);

            Integer month = date.getMonthValue();
            Integer year  = date.getYear();

            List<WalletTransaction> transactions =
                walletService.GetTransactionsByMonth(month, year);

            m_logger.info("Found " + transactions.size() + " transactions for " +
                          month + "/" + year);

            Double total = transactions.stream()
                               .filter(t -> t.GetType() == TransactionType.EXPENSE)
                               .mapToDouble(WalletTransaction::GetAmount)
                               .sum();

            monthlyExpenses.put(date.format(formatter), total);
        }

        // Get the maximum value to set the axis
        Double maxExpenseValue = 0.0;

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        series.setName("Total Expenses");

        for (Map.Entry<String, Double> entry : monthlyExpenses.entrySet())
        {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            maxExpenseValue = Math.max(maxExpenseValue, entry.getValue());
        }

        // Set the maximum value for the Y axis as the maximum expense value
        Axis<?> yAxis = expensesLast12Months.getYAxis();
        if (yAxis instanceof NumberAxis)
        {
            NumberAxis numberAxis = (NumberAxis)yAxis;
            numberAxis.setAutoRanging(false);
            numberAxis.setLowerBound(0);
            numberAxis.setUpperBound(maxExpenseValue);
            numberAxis.setTickUnit(Constants.HOME_BAR_CHART_TICK_UNIT);
        }

        expensesLast12Months.setVerticalGridLinesVisible(false);
        expensesLast12Months.setTitle("Expenses in the Last " +
                                      Constants.HOME_BAR_CHART_MONTHS + " Months");

        expensesLast12Months.getData().clear();
        expensesLast12Months.getData().add(series);

        // Animate the chart
        for (XYChart.Data<String, Double> data : series.getData())
        {
            Double targetValue = data.getYValue();
            data.setYValue(0.0);

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
    }

    /**
     * Update the display of the month resume
     */
    private void UpdateMonthResume()
    {
        LocalDate currentDate = LocalDate.now();
        Integer   month       = currentDate.getMonthValue();
        Integer   year        = currentDate.getYear();

        List<WalletTransaction> transactions =
            walletService.GetTransactionsByMonth(month, year);

        Double totalIncome = transactions.stream()
                                 .filter(t -> t.GetType() == TransactionType.INCOME)
                                 .mapToDouble(WalletTransaction::GetAmount)
                                 .sum();

        Double totalExpenses = transactions.stream()
                                   .filter(t -> t.GetType() == TransactionType.EXPENSE)
                                   .mapToDouble(WalletTransaction::GetAmount)
                                   .sum();

        Double balance = totalIncome - totalExpenses;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM/yy");
        monthResumePaneTitle.setText(currentDate.format(formatter) + " Resume");

        // Total Income
        Label incomeTextLabel = new Label("Total Income: ");
        incomeTextLabel.setPrefWidth(Constants.HOME_MONTH_RESUME_TEXT_LABEL_WIDTH);

        Label incomeLabel = new Label(String.format("$ %.2f", totalIncome));
        incomeLabel.getStyleClass().add(
            Constants.HOME_MONTH_RESUME_POSITIVE_LABEL_STYLE);

        Label incomeSignLabel = new Label("");
        incomeSignLabel.setPrefWidth(Constants.HOME_MONTH_RESUME_SIGN_LABEL_WIDTH);

        HBox incomeWithSignalHBox = new HBox(0, incomeSignLabel, incomeLabel);
        incomeWithSignalHBox.setAlignment(Pos.CENTER_LEFT);

        // Total Expenses
        Label expensesTextLabel = new Label("Total Expenses: ");
        expensesTextLabel.setPrefWidth(Constants.HOME_MONTH_RESUME_TEXT_LABEL_WIDTH);

        Label expensesLabel = new Label(String.format("$ %.2f", totalExpenses));
        expensesLabel.getStyleClass().add(
            Constants.HOME_MONTH_RESUME_NEGATIVE_LABEL_STYLE);

        Label expensesSignLabel = new Label("");
        expensesSignLabel.setPrefWidth(Constants.HOME_MONTH_RESUME_SIGN_LABEL_WIDTH);

        HBox expensesWithSignalHBox = new HBox(0, expensesSignLabel, expensesLabel);
        expensesWithSignalHBox.setAlignment(Pos.CENTER_LEFT);

        // Balance
        Label balanceTextLabel = new Label("Balance: ");
        balanceTextLabel.setPrefWidth(Constants.HOME_MONTH_RESUME_TEXT_LABEL_WIDTH);

        Label balanceSignLabel;
        Label balanceLabel;

        // Set the balance label and sign label according to the balance value
        if (balance >= 0)
        {
            balanceLabel     = new Label(String.format("$ %.2f", balance));
            balanceSignLabel = new Label("+");

            balanceSignLabel.setPrefWidth(Constants.HOME_MONTH_RESUME_SIGN_LABEL_WIDTH);

            balanceLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_POSITIVE_LABEL_STYLE);

            balanceSignLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_POSITIVE_LABEL_STYLE);
        }
        else
        {
            balanceLabel     = new Label(String.format("$ %.2f", -balance));
            balanceSignLabel = new Label("-");

            balanceSignLabel.setPrefWidth(Constants.HOME_MONTH_RESUME_SIGN_LABEL_WIDTH);

            balanceLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_NEGATIVE_LABEL_STYLE);

            balanceSignLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_NEGATIVE_LABEL_STYLE);
        }

        HBox balanceWithSignalHBox = new HBox(0, balanceSignLabel, balanceLabel);
        balanceWithSignalHBox.setAlignment(Pos.CENTER_LEFT);

        // Mensal Economies
        Double economyPercentage = (totalIncome - totalExpenses) / totalIncome * 100;
        Label  mensalEconomiesTextLabel;
        Label  mensalEconomiesSignLabel;
        Label  mensalEconomiesPercentLabel;

        // Set the economy label and sign label according to the economy value
        if (economyPercentage >= 0)
        {
            mensalEconomiesTextLabel = new Label("Economy: ");
            mensalEconomiesSignLabel = new Label("");

            mensalEconomiesSignLabel.setPrefWidth(
                Constants.HOME_MONTH_RESUME_SIGN_LABEL_WIDTH);

            mensalEconomiesPercentLabel =
                new Label(String.format("%.2f%%", economyPercentage));

            mensalEconomiesPercentLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_POSITIVE_LABEL_STYLE);

            mensalEconomiesSignLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_POSITIVE_LABEL_STYLE);
        }
        else
        {
            mensalEconomiesTextLabel = new Label("No savings: ");
            mensalEconomiesSignLabel = new Label("-");

            mensalEconomiesSignLabel.setPrefWidth(
                Constants.HOME_MONTH_RESUME_SIGN_LABEL_WIDTH);

            mensalEconomiesPercentLabel =
                new Label(String.format("%.2f%%", -economyPercentage));

            mensalEconomiesPercentLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_NEGATIVE_LABEL_STYLE);

            mensalEconomiesSignLabel.getStyleClass().add(
                Constants.HOME_MONTH_RESUME_NEGATIVE_LABEL_STYLE);
        }

        mensalEconomiesTextLabel.setPrefWidth(
            Constants.HOME_MONTH_RESUME_TEXT_LABEL_WIDTH);

        HBox mensalEconomiesWithSignalHBox =
            new HBox(0, mensalEconomiesSignLabel, mensalEconomiesPercentLabel);

        mensalEconomiesWithSignalHBox.setAlignment(Pos.CENTER_LEFT);

        // HBox for each row (explanation + VBox with signal and value)
        HBox incomeHBox   = new HBox(10, incomeTextLabel, incomeWithSignalHBox);
        HBox expensesHBox = new HBox(10, expensesTextLabel, expensesWithSignalHBox);
        HBox balanceHBox  = new HBox(10, balanceTextLabel, balanceWithSignalHBox);
        HBox mensalEconomiesHBox =
            new HBox(10, mensalEconomiesTextLabel, mensalEconomiesWithSignalHBox);

        // Alignment for the HBoxes
        incomeHBox.setAlignment(Pos.CENTER_LEFT);
        expensesHBox.setAlignment(Pos.CENTER_LEFT);
        balanceHBox.setAlignment(Pos.CENTER_LEFT);
        mensalEconomiesHBox.setAlignment(Pos.CENTER_LEFT);

        // VBox to hold the HBoxes
        VBox vbox =
            new VBox(10, incomeHBox, expensesHBox, balanceHBox, mensalEconomiesHBox);
        vbox.setAlignment(Pos.CENTER_LEFT);

        // Clear previous content and add the new layout
        monthResumeView.getChildren().clear();
        monthResumeView.getChildren().add(vbox);
    }

    /**
     * Create a node for a credit card
     * @param creditCard The credit card to be displayed
     * @return The VBox containing the credit card information
     */
    private VBox CreateCreditCardItemNode(CreditCard creditCard)
    {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.getStyleClass().add(Constants.HOME_CREDIT_CARD_ITEM_STYLE);

        Label nameLabel = new Label(creditCard.GetName());

        Label availableCredit = new Label(
            String.format("$ %.2f",
                          creditCardService.GetAvailableCredit(creditCard.GetId())));
        Label digitsLabel =
            new Label("**** **** **** " + creditCard.GetLastFourDigits());

        vbox.getChildren().addAll(nameLabel, availableCredit, digitsLabel);

        return vbox;
    }

    /**
     * Create a node for a wallet
     * @param wallet The wallet to be displayed
     * @return The VBox containing the wallet information
     */
    private VBox CreateWalletItemNode(Wallet wallet)
    {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.getStyleClass().add(Constants.HOME_WALLET_ITEM_STYLE);

        Label nameLabel    = new Label(wallet.GetName());
        Label balanceLabel = new Label(String.format("$ %.2f", wallet.GetBalance()));

        vbox.getChildren().addAll(nameLabel, balanceLabel);

        return vbox;
    }
}
