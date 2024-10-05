/*
 * Filename: WalletFullPaneController.java
 * Created on: October  5, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.mymoney.entities.Transfer;
import com.mymoney.entities.Wallet;
import com.mymoney.entities.WalletTransaction;
import com.mymoney.services.WalletService;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import com.mymoney.util.TransactionStatus;
import com.mymoney.util.TransactionType;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Wallet Full Pane
 */
@Controller
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

    private WalletService walletService;

    private Wallet wallet;

    private List<WalletTransaction> transactions;

    private List<Transfer> transfers;

    private static final Logger logger = LoggerConfig.GetLogger();

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

        Double allIncomesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.INCOME))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double confirmedExpensesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                .filter(t -> t.GetStatus().equals(TransactionStatus.CONFIRMED))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double allExpensesSum =
            transactions.stream()
                .filter(t -> t.GetType().equals(TransactionType.EXPENSE))
                .mapToDouble(WalletTransaction::GetAmount)
                .sum();

        Double creditedTransfersSum =
            transfers.stream()
                .filter(t -> t.GetReceiverWallet().equals(wallet))
                .mapToDouble(Transfer::GetAmount)
                .sum();

        Double debitedTransfersSum =
            transfers.stream()
                .filter(t -> t.GetSenderWallet().equals(wallet))
                .mapToDouble(Transfer::GetAmount)
                .sum();

        Double openingBalance = wallet.GetBalance() - confirmedIncomesSum +
                                confirmedExpensesSum - creditedTransfersSum +
                                debitedTransfersSum;

        Double foreseenBalance = wallet.GetBalance() - allIncomesSum + allExpensesSum;

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
        if (value < 0)
        {
            signLabel.setText("-");
            valueLabel.setText(String.format("$ %.2f", -value));
            SetLabelStyle(signLabel, Constants.NEGATIVE_BALANCE_STYLE);
            SetLabelStyle(valueLabel, Constants.NEGATIVE_BALANCE_STYLE);
        }
        else if (value < 0)
        {
            signLabel.setText("+");
            valueLabel.setText(String.format("$ %.2f", value));
            SetLabelStyle(signLabel, Constants.POSITIVE_BALANCE_STYLE);
            SetLabelStyle(valueLabel, Constants.POSITIVE_BALANCE_STYLE);
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
