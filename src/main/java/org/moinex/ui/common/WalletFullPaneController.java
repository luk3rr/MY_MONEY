/*
 * Filename: WalletFullPaneController.java
 * Created on: October  5, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import org.moinex.entities.CreditCardPayment;
import org.moinex.entities.Transfer;
import org.moinex.entities.Wallet;
import org.moinex.entities.WalletTransaction;
import org.moinex.services.CreditCardService;
import org.moinex.services.WalletService;
import org.moinex.services.WalletTransactionService;
import org.moinex.ui.dialog.AddExpenseController;
import org.moinex.ui.dialog.AddIncomeController;
import org.moinex.ui.dialog.AddTransferController;
import org.moinex.ui.dialog.ChangeWalletBalanceController;
import org.moinex.ui.dialog.ChangeWalletTypeController;
import org.moinex.ui.dialog.RenameWalletController;
import org.moinex.ui.main.WalletController;
import org.moinex.util.Constants;
import org.moinex.util.TransactionStatus;
import org.moinex.util.TransactionType;
import org.moinex.util.UIUtils;
import org.moinex.util.WindowUtils;
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

    private CreditCardService creditCardService;

    private WalletTransactionService walletTransactionService;

    private Wallet wallet;

    private BigDecimal crcPaidAmount;

    private BigDecimal crcPendingAmount;

    private List<WalletTransaction> transactions;

    private List<Transfer> transfers;

    public WalletFullPaneController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @param creditCardService CreditCardService
     * @param walletTransactionService WalletTransactionService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public WalletFullPaneController(WalletService            walletService,
                                    CreditCardService        creditCardService,
                                    WalletTransactionService walletTransactionService)
    {
        this.walletService            = walletService;
        this.creditCardService        = creditCardService;
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

        crcPaidAmount = creditCardService.GetPaidPaymentsByMonth(wallet.GetId(),
                                                                 now.getMonthValue(),
                                                                 now.getYear());

        List<CreditCardPayment> payments =
            creditCardService.GetCreditCardPayments(now.getMonthValue(), now.getYear());

        // Filter payments that are related to the wallet and are not paid
        crcPendingAmount = payments.stream()
                               .filter(p
                                       -> p.GetCreditCardDebt()
                                                  .GetCreditCard()
                                                  .GetDefaultBillingWallet()
                                                  .GetId() == wallet.GetId())
                               .filter(p -> p.GetWallet() == null)
                               .map(CreditCardPayment::GetAmount)
                               .reduce(BigDecimal.ZERO, BigDecimal::add);
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

        BigDecimal confirmedIncomesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.INCOME))
                .filter(t -> t.GetStatus().equals(TransactionStatus.CONFIRMED))
                .map(WalletTransaction::GetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingIncomesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.INCOME))
                .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                .map(WalletTransaction::GetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal confirmedExpensesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                .filter(t -> t.GetStatus().equals(TransactionStatus.CONFIRMED))
                .map(WalletTransaction::GetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Consider the paid amount of the credit card
        confirmedExpensesSum = confirmedExpensesSum.add(crcPaidAmount);

        BigDecimal pendingExpensesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                .filter(t -> t.GetStatus().equals(TransactionStatus.PENDING))
                .map(WalletTransaction::GetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Consider the pending amount of the credit card
        pendingExpensesSum = pendingExpensesSum.add(crcPendingAmount);

        BigDecimal creditedTransfersSum =
            transfers.stream()
                .filter(t -> t.GetReceiverWallet().GetId() == wallet.GetId())
                .map(Transfer::GetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal debitedTransfersSum =
            transfers.stream()
                .filter(t -> t.GetSenderWallet().GetId() == wallet.GetId())
                .map(Transfer::GetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal openingBalance = wallet.GetBalance()
                                        .subtract(confirmedIncomesSum)
                                        .add(confirmedExpensesSum)
                                        .subtract(creditedTransfersSum)
                                        .add(debitedTransfersSum);

        BigDecimal foreseenBalance =
            wallet.GetBalance().add(pendingIncomesSum).subtract(pendingExpensesSum);

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
    private void handleChangeWalletBalance()
    {
        WindowUtils.OpenModalWindow(Constants.CHANGE_WALLET_BALANCE_FXML,
                                    "Change wallet balance",
                                    springContext,
                                    (ChangeWalletBalanceController controller)
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

        SetLabelValue(openingBalanceSign, openingBalanceValue, BigDecimal.ZERO);
        SetLabelValue(incomesSign, incomesValue, BigDecimal.ZERO);
        SetLabelValue(expensesSign, expensesValue, BigDecimal.ZERO);
        SetLabelValue(creditedTransfersSign, creditedTransfersValue, BigDecimal.ZERO);
        SetLabelValue(debitedTransfersSign, debitedTransfersValue, BigDecimal.ZERO);
        SetLabelValue(currentBalanceSign, currentBalanceValue, BigDecimal.ZERO);
        SetLabelValue(foreseenBalanceSign, foreseenBalanceValue, BigDecimal.ZERO);
    }

    /**
     * Set the value of a label
     * @param signLabel Label to set the sign
     * @param valueLabel Label to set the value
     * @param value Value to set
     */
    private void SetLabelValue(Label signLabel, Label valueLabel, BigDecimal value)
    {
        if (value.compareTo(BigDecimal.ZERO) < 0)
        {
            signLabel.setText("-");
            valueLabel.setText(UIUtils.FormatCurrency(value.abs()));
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
