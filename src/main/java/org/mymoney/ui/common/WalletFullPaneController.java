/*
 * Filename: WalletFullPaneController.java
 * Created on: October  5, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.common;

import org.mymoney.entities.Transfer;
import org.mymoney.entities.Wallet;
import org.mymoney.entities.WalletTransaction;
import org.mymoney.services.WalletService;
import org.mymoney.ui.dialog.AddExpenseController;
import org.mymoney.ui.dialog.AddIncomeController;
import org.mymoney.ui.dialog.AddTransferController;
import org.mymoney.ui.dialog.ChangeWalletTypeController;
import org.mymoney.ui.dialog.RenameWalletController;
import org.mymoney.util.Constants;
// import org.mymoney.util.LoggerConfig;
import org.mymoney.util.TransactionStatus;
import org.mymoney.util.TransactionType;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
// import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Wallet Full Pane
 *
 * @note prototype is necessary so that each scene knows to which wallet it belongs
 */
@Controller
@Scope("prototype") // Each instance of this controller is unique
public class WalletFullPaneController
{
    @FXML
    private VBox rootVBox;

    @FXML
    private HBox header;

    @FXML
    private ImageView walletIcon;

    @FXML
    private Label walletName;

    @FXML
    private Label walletType;

    @FXML
    private Label openingBalanceDescription;

    @FXML
    private Label openingBalanceSign;

    @FXML
    private Label openingBalanceValue;

    @FXML
    private Label incomesDescription;

    @FXML
    private Label incomesValue;

    @FXML
    private Label incomesSign;

    @FXML
    private Label expensesDescription;

    @FXML
    private Label expensesSign;

    @FXML
    private Label expensesValue;

    @FXML
    private Label creditedTransfersDescription;

    @FXML
    private Label creditedTransfersSign;

    @FXML
    private Label creditedTransfersValue;

    @FXML
    private Label debitedTransfersDescription;

    @FXML
    private Label debitedTransfersSign;

    @FXML
    private Label debitedTransfersValue;

    @FXML
    private Label currentBalanceDescription;

    @FXML
    private Label currentBalanceSign;

    @FXML
    private Label currentBalanceValue;

    @FXML
    private Label foreseenBalanceDescription;

    @FXML
    private Label foreseenBalanceSign;

    @FXML
    private Label foreseenBalanceValue;

    @FXML
    private MenuItem addIncomeMenuItem;

    @FXML
    private MenuItem addExpenseMenuItem;

    @FXML
    private MenuItem addTransferMenuItem;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private WalletService walletService;

    private Wallet wallet;

    private List<WalletTransaction> transactions;

    private List<Transfer> transfers;

    // private static final Logger logger = LoggerConfig.GetLogger();

    public WalletFullPaneController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public WalletFullPaneController(WalletService walletService)
    {
        this.walletService = walletService;
    }

    @FXML
    private void initialize()
    { }

    @FXML
    private void handleAddIncome()
    {
        OpenPopupWindow(Constants.ADD_INCOME_FXML,
                        "Add new income",
                        (AddIncomeController controller) -> {
                            controller.SetWalletComboBox(wallet);
                        });
    }

    @FXML
    private void handleAddExpense()
    {
        OpenPopupWindow(Constants.ADD_EXPENSE_FXML,
                        "Add new expense",
                        (AddExpenseController controller) -> {
                            controller.SetWalletComboBox(wallet);
                        });
    }

    @FXML
    private void handleAddTransfer()
    {
        OpenPopupWindow(Constants.ADD_TRANSFER_FXML,
                        "Add new transfer",
                        (AddTransferController controller) -> {
                            controller.SetSenderWalletComboBox(wallet);
                        });
    }

    @FXML
    private void handleRenameWallet()
    {
        OpenPopupWindow(Constants.RENAME_WALLET_FXML,
                        "Rename wallet",
                        (RenameWalletController controller) -> {
                            controller.SetWalletComboBox(wallet);
                        });
    }

    @FXML
    private void handleChangeWalletType()
    {
        OpenPopupWindow(Constants.CHANGE_WALLET_TYPE_FXML,
                        "Change wallet type",
                        (ChangeWalletTypeController controller) -> {
                            controller.SetWalletComboBox(wallet);
                        });
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

            popupStage.setOnHidden(e -> {
                LoadWalletInfo();
                UpdateWalletPane(wallet);
            });

            popupStage.showAndWait();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Load wallet information from the database
     * @param wtName Wallet name to find in the database
     */
    public void LoadWalletInfo()
    {
        if (wallet == null)
        {
            transactions.clear();
            transfers.clear();
            return;
        }

        // Reload wallet from the database
        wallet = walletService.GetWalletById(wallet.GetId());

        LocalDate now = LocalDate.now();

        transactions =
            walletService.GetTransactionsByWalletAndMonth(wallet.GetId(),
                                                          now.getMonthValue(),
                                                          now.getYear());

        transfers = walletService.GetTransfersByWalletAndMonth(wallet.GetId(),
                                                               now.getMonthValue(),
                                                               now.getYear());
    }

    /**
     * Load wallet information
     * @param wtName Wallet name to find in the database
     * @return The updated VBox
     */
    public VBox UpdateWalletPane(Wallet wt)
    {
        // If the wallet is null, do not update the pane
        if (wt == null)
        {
            SetDefaultValues();
            return rootVBox;
        }

        wallet = wt;
        LoadWalletInfo();

        walletName.setText(wallet.GetName());
        walletType.setText(wallet.GetType().GetName());
        walletIcon.setImage(
            new Image(Constants.WALLET_TYPE_ICONS_PATH + wallet.GetType().GetIcon()));

        Double confirmedIncomesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.INCOME))
                .filter(t -> t.GetStatus().equals(TransactionStatus.CONFIRMED))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double pendingIncomesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.INCOME))
                .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double confirmedExpensesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                .filter(t -> t.GetStatus().equals(TransactionStatus.CONFIRMED))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double pendingExpensesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double creditedTransfersSum =
            transfers.stream()
                .filter(t -> t.GetReceiverWallet().GetId() == wallet.GetId())
                .mapToDouble(Transfer::GetAmount)
                .sum();

        Double debitedTransfersSum =
            transfers.stream()
                .filter(t -> t.GetSenderWallet().GetId() == wallet.GetId())
                .mapToDouble(Transfer::GetAmount)
                .sum();

        Double openingBalance = wallet.GetBalance() - confirmedIncomesSum +
                                confirmedExpensesSum - creditedTransfersSum +
                                debitedTransfersSum;

        Double foreseenBalance =
            wallet.GetBalance() + pendingIncomesSum - pendingExpensesSum;

        SetLabelValue(openingBalanceSign, openingBalanceValue, openingBalance);
        SetLabelValue(incomesSign, incomesValue, confirmedIncomesSum);
        SetLabelValue(expensesSign, expensesValue, confirmedExpensesSum);
        SetLabelValue(creditedTransfersSign,
                      creditedTransfersValue,
                      creditedTransfersSum);
        SetLabelValue(debitedTransfersSign, debitedTransfersValue, debitedTransfersSum);
        SetLabelValue(currentBalanceSign, currentBalanceValue, wallet.GetBalance());
        SetLabelValue(foreseenBalanceSign, foreseenBalanceValue, foreseenBalance);

        return rootVBox;
    }

    private void SetDefaultValues()
    {
        walletName.setText("");
        walletType.setText("");
        walletIcon.setImage(null);

        SetLabelValue(openingBalanceSign, openingBalanceValue, 0.0);
        SetLabelValue(incomesSign, incomesValue, 0.0);
        SetLabelValue(expensesSign, expensesValue, 0.0);
        SetLabelValue(creditedTransfersSign, creditedTransfersValue, 0.0);
        SetLabelValue(debitedTransfersSign, debitedTransfersValue, 0.0);
        SetLabelValue(currentBalanceSign, currentBalanceValue, 0.0);
        SetLabelValue(foreseenBalanceSign, foreseenBalanceValue, 0.0);
    }

    /**
     * Set the value of a label
     * @param signLabel Label to set the sign
     * @param valueLabel Label to set the value
     * @param value Value to set
     */
    private void SetLabelValue(Label signLabel, Label valueLabel, Double value)
    {
        if (value + Constants.EPSILON < 0)
        {
            signLabel.setText("-");
            valueLabel.setText(String.format("$ %.2f", -value));
            SetLabelStyle(signLabel, Constants.NEGATIVE_BALANCE_STYLE);
            SetLabelStyle(valueLabel, Constants.NEGATIVE_BALANCE_STYLE);
        }
        else
        {
            signLabel.setText("");
            valueLabel.setText(String.format("$ %.2f", value));
            SetLabelStyle(signLabel, Constants.NEUTRAL_BALANCE_STYLE);
            SetLabelStyle(valueLabel, Constants.NEUTRAL_BALANCE_STYLE);
        }
    }

    /**
     * Set the style of a label
     * @param label Label to set the style
     * @param style Style to set
     */
    private void SetLabelStyle(Label label, String style)
    {
        label.getStyleClass().removeAll(Constants.NEGATIVE_BALANCE_STYLE,
                                        Constants.POSITIVE_BALANCE_STYLE,
                                        Constants.NEUTRAL_BALANCE_STYLE);

        label.getStyleClass().add(style);
    }
}
