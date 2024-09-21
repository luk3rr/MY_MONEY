/*
 * Filename: HomeController.java
 * Created on: September 20, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.jfoenix.controls.JFXButton;
import com.mymoney.entities.CreditCard;
import com.mymoney.entities.Wallet;
import com.mymoney.services.CreditCardService;
import com.mymoney.services.WalletService;
import com.mymoney.util.Constants;
import com.mymoney.util.LoggerConfig;
import java.util.List;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
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
    private JFXButton creditCardPrevButton;

    @FXML
    private JFXButton creditCardNextButton;

    private List<Wallet> wallets;

    private WalletService walletService;

    private List<CreditCard> creditCards;

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

        m_logger.info("Loaded " + wallets.size() + " wallets from the database");

        UpdateDisplayWallets();
        UpdateDisplayCreditCards();

        m_logger.info("Loaded " + creditCards.size() +
                      " credit cards from the database");

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
    public void LoadWalletsFromDatabase()
    {
        wallets = walletService.GetAllWalletsOrderedByName();
    }

    /**
     * Load credit cards from the database
     */
    public void LoadCreditCardsFromDatabase()
    {
        creditCards = creditCardService.GetAllCreditCardsOrderedByName();
    }

    /**
     * Update the display of wallets
     */
    private void UpdateDisplayWallets()
    {
        walletView1.getChildren().clear();
        walletView2.getChildren().clear();

        int start = walletPaneCurrentPage * Constants.HOME_PANES_ITEMS_PER_PAGE;
        int end = Math.min(start + Constants.HOME_PANES_ITEMS_PER_PAGE, wallets.size());

        for (int i = start; i < end; i++)
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

        int start = creditCardPaneCurrentPage * Constants.HOME_PANES_ITEMS_PER_PAGE;
        int end =
            Math.min(start + Constants.HOME_PANES_ITEMS_PER_PAGE, creditCards.size());

        for (int i = start; i < end; i++)
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
     * Create a node for a credit card
     * @param creditCard The credit card to be displayed
     * @return The VBox containing the credit card information
     */
    private VBox CreateCreditCardItemNode(CreditCard creditCard)
    {
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.getStyleClass().add("credit-card-item");

        Label nameLabel       = new Label(creditCard.GetName());
        Label availableCredit = new Label(
            "$ " + creditCardService.GetAvailableCredit(creditCard.GetId()).toString());
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
        vbox.getStyleClass().add("wallet-item");

        Label nameLabel    = new Label(wallet.GetName());
        Label balanceLabel = new Label("$ " + wallet.GetBalance().toString());

        vbox.getChildren().addAll(nameLabel, balanceLabel);

        return vbox;
    }
}
