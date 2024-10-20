/*
 * Filename: WalletFullPaneController.java
 * Created on: October  5, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.common;

import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.mymoney.entities.Transfer;
import org.mymoney.entities.Wallet;
import org.mymoney.entities.WalletTransaction;
import org.mymoney.services.WalletService;
import org.mymoney.services.WalletTransactionService;
import org.mymoney.ui.dialog.AddExpenseController;
import org.mymoney.ui.dialog.AddIncomeController;
import org.mymoney.ui.dialog.AddTransferController;
import org.mymoney.ui.dialog.ChangeWalletTypeController;
import org.mymoney.ui.dialog.RenameWalletController;
import org.mymoney.ui.main.WalletController;
import org.mymoney.util.Constants;
import org.mymoney.util.TransactionStatus;
import org.mymoney.util.TransactionType;
import org.mymoney.util.UIUtils;
import org.mymoney.util.WindowUtils;
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
    private ImageView walletIcon;

    @FXML
    private Label walletName;

    @FXML
    private Label walletType;

    @FXML
    private Label openingBalanceSign;

    @FXML
    private Label openingBalanceValue;

    @FXML
    private Label incomesValue;

    @FXML
    private Label incomesSign;

    @FXML
    private Label expensesSign;

    @FXML
    private Label expensesValue;

    @FXML
    private Label creditedTransfersSign;

    @FXML
    private Label creditedTransfersValue;

    @FXML
    private Label debitedTransfersSign;

    @FXML
    private Label debitedTransfersValue;

    @FXML
    private Label currentBalanceSign;

    @FXML
    private Label currentBalanceValue;

    @FXML
    private Label foreseenBalanceSign;

    @FXML
    private Label foreseenBalanceValue;

    @Autowired
    private ConfigurableApplicationContext springContext;

    @Autowired
    private WalletController walletController;

    private WalletService walletService;

    private WalletTransactionService walletTransactionService;

    private Wallet wallet;

    private List<WalletTransaction> transactions;

    private List<Transfer> transfers;

    public WalletFullPaneController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @param walletTransactionService WalletTransactionService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public WalletFullPaneController(WalletService            walletService,
                                    WalletTransactionService walletTransactionService)
    {
        this.walletService            = walletService;
        this.walletTransactionService = walletTransactionService;
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
            walletTransactionService.GetNonArchivedTransactionsByWalletAndMonth(
                wallet.GetId(),
                now.getMonthValue(),
                now.getYear());

        transfers =
            walletTransactionService.GetTransfersByWalletAndMonth(wallet.GetId(),
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

    @FXML
    private void initialize()
    { }

    @FXML
    private void handleAddIncome()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_INCOME_FXML,
                                    "Add new income",
                                    springContext,
                                    (AddIncomeController controller)
                                        -> { controller.SetWalletComboBox(wallet); },
                                    List.of(() -> walletController.UpdateDisplay()));
    }

    @FXML
    private void handleAddExpense()
    {
        WindowUtils.OpenModalWindow(Constants.ADD_EXPENSE_FXML,
                                    "Add new expense",
                                    springContext,
                                    (AddExpenseController controller)
                                        -> { controller.SetWalletComboBox(wallet); },
                                    List.of(() -> walletController.UpdateDisplay()));
    }

    @FXML
    private void handleAddTransfer()
    {
        WindowUtils.OpenModalWindow(
            Constants.ADD_TRANSFER_FXML,
            "Add new transfer",
            springContext,
            (AddTransferController controller)
                -> { controller.SetSenderWalletComboBox(wallet); },
            List.of(() -> walletController.UpdateDisplay()));
    }

    @FXML
    private void handleRenameWallet()
    {
        WindowUtils.OpenModalWindow(Constants.RENAME_WALLET_FXML,
                                    "Rename wallet",
                                    springContext,
                                    (RenameWalletController controller)
                                        -> { controller.SetWalletComboBox(wallet); },
                                    List.of(() -> walletController.UpdateDisplay()));
    }

    @FXML
    private void handleChangeWalletType()
    {
        WindowUtils.OpenModalWindow(Constants.CHANGE_WALLET_TYPE_FXML,
                                    "Change wallet type",
                                    springContext,
                                    (ChangeWalletTypeController controller)
                                        -> { controller.SetWalletComboBox(wallet); },
                                    List.of(() -> walletController.UpdateDisplay()));
    }

    @FXML
    private void handleArchiveWallet()
    {
        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Archive wallet " + wallet.GetName(),
                "Are you sure you want to archive this wallet?"))
        {
            walletService.ArchiveWallet(wallet.GetId());
            UpdateWalletPane(wallet);

            // Update wallet display in the main window
            walletController.UpdateDisplay();
        }
    }

    @FXML
    private void handleDeleteWallet()
    {
        // Prevent the removal of a wallet with associated transactions
        if (walletTransactionService.GetTransactionCountByWallet(wallet.GetId()) > 0)
        {
            WindowUtils.ShowErrorDialog(
                "Error",
                "Wallet has transactions",
                "Cannot delete a wallet with associated transactions");
            return;
        }

        if (WindowUtils.ShowConfirmationDialog(
                "Confirmation",
                "Delete wallet " + wallet.GetName(),
                "Are you sure you want to remove this wallet?"))
        {
            try
            {
                walletService.DeleteWallet(wallet.GetId());

                WindowUtils.ShowSuccessDialog("Success",
                                              "Wallet deleted",
                                              "Wallet " + wallet.GetName() +
                                                  " has been deleted");

                // Update wallet display in the main window
                walletController.UpdateDisplay();
            }
            catch (RuntimeException e)
            {
                WindowUtils.ShowErrorDialog("Error",
                                            "Error removing wallet",
                                            e.getMessage());
                return;
            }
        }
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
            valueLabel.setText(UIUtils.FormatCurrency(-value));
            UIUtils.SetLabelStyle(signLabel, Constants.NEGATIVE_BALANCE_STYLE);
            UIUtils.SetLabelStyle(valueLabel, Constants.NEGATIVE_BALANCE_STYLE);
        }
        else
        {
            signLabel.setText(" ");
            valueLabel.setText(UIUtils.FormatCurrency(value));
            UIUtils.SetLabelStyle(signLabel, Constants.NEUTRAL_BALANCE_STYLE);
            UIUtils.SetLabelStyle(valueLabel, Constants.NEUTRAL_BALANCE_STYLE);
        }
    }
}
