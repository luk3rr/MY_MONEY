/*
 * Filename: WalletController.java
 * Created on: September 29, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.jfoenix.controls.JFXButton;
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
import java.util.List;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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

    @Autowired
    private ConfigurableApplicationContext springContext;

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

        // Add default wallet type and select it
        totalBalancePaneWalletTypeComboBox.getItems().add(0, "All Wallets");
        totalBalancePaneWalletTypeComboBox.getSelectionModel().selectFirst();

        UpdateTotalBalanceView();
        UpdateDisplayWallets();

        SetButtonsActions();
    }

    /**
     * Set the actions for the buttons
     */
    private void SetButtonsActions()
    {
        totalBalancePaneTransferButton.setOnAction(e -> AddTransfer());
        totalBalancePaneWalletTypeComboBox.setOnAction(e -> UpdateTotalBalanceView());
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
}
