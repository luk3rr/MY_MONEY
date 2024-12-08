/*
 * Filename: GoalController.java
 * Created on: December  8, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.moinex.ui.main;

import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.moinex.entities.WalletTransaction;
import org.moinex.services.CategoryService;
import org.moinex.services.CreditCardService;
import org.moinex.services.WalletTransactionService;
import org.moinex.util.LoggerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

/**
 * Controller class for the goal view
 */
@Controller
public class GoalController
{
    private static final Logger logger = LoggerConfig.GetLogger();

    @FXML
    private AnchorPane goalsResumeView;

    @FXML
    private TableView<WalletTransaction> goalTableView;

    @FXML
    private TextField goalSearchField;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private WalletTransactionService walletTransactionService;

    private CreditCardService creditCardService;

    private CategoryService categoryService;

    /**
     * Constructor
     * @param walletTransactionService WalletTransactionService
     * @param creditCardService CreditCardService
     * @param categoryService CategoryService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public GoalController(WalletTransactionService walletTransactionService,
                          CreditCardService        creditCardService,
                          CategoryService          categoryService)
    {
        this.walletTransactionService = walletTransactionService;
        this.creditCardService        = creditCardService;
        this.categoryService          = categoryService;
    }

    @FXML
    private void initialize()
    { }

    @FXML
    private void handleAddGoal()
    { }

    @FXML
    private void handleAddDeposit()
    { }

    @FXML
    private void handleEditGoal()
    { }

    @FXML
    private void handleDeleteGoal()
    { }
}
