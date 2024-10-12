/*
 * Filename: AddIncomeController.java
 * Created on: October  6, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

import org.mymoney.entities.Category;
import org.mymoney.entities.Wallet;
import org.mymoney.services.CategoryService;
import org.mymoney.services.WalletService;
import org.mymoney.util.Constants;
import org.mymoney.util.LoggerConfig;
import org.mymoney.util.TransactionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the Add Income dialog
 */
@Controller
public class AddIncomeController
{
    @FXML
    private Label walletAfterBalanceValueLabel;

    @FXML
    private Label walletCurrentBalanceValueLabel;

    @FXML
    private ComboBox<String> walletComboBox;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private TextField incomeValueField;

    @FXML
    private TextField descriptionField;

    @FXML
    private DatePicker incomeDatePicker;

    private WalletService walletService;

    private CategoryService categoryService;

    private List<Wallet> wallets;

    private List<Category> categories;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public AddIncomeController() { }

    /**
     * Constructor
     * @param walletService WalletService
     * @note This constructor is used for dependency injection
     */
    @Autowired
    public AddIncomeController(WalletService   walletService,
                               CategoryService categoryService)
    {
        this.walletService   = walletService;
        this.categoryService = categoryService;
    }

    @FXML
    private void initialize()
    {
        ConfigureDatePicker();

        LoadWallets();
        LoadCategories();

        // For each element in enum TransactionStatus, add its name to the
        // statusComboBox
        statusComboBox.getItems().addAll(
            Arrays.stream(TransactionStatus.values()).map(Enum::name).toList());

        // Reset all labels
        ResetLabel(walletAfterBalanceValueLabel);
        ResetLabel(walletCurrentBalanceValueLabel);

        walletComboBox.setOnAction(e -> {
            UpdateWalletBalance();
            walletAfterBalance();
        });

        // Update wallet after balance when the value field changes
        incomeValueField.textProperty().addListener(
            (observable, oldValue, newValue) -> { walletAfterBalance(); });
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)descriptionField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleSave()
    {
        String    walletName        = walletComboBox.getValue();
        String    description       = descriptionField.getText();
        String    incomeValueString = incomeValueField.getText();
        String    statusString      = statusComboBox.getValue();
        String    categoryString    = categoryComboBox.getValue();
        LocalDate incomeDate        = incomeDatePicker.getValue();

        if (walletName == null || description == null || description.trim().isEmpty() ||
            incomeValueString == null || incomeValueString.trim().isEmpty() ||
            statusString == null || categoryString == null || incomeDate == null)
        {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText("Empty fields");
            alert.setContentText("Please fill all the fields.");
            alert.showAndWait();
            return;
        }

        try
        {
            Double incomeValue = Double.parseDouble(incomeValueString);

            Wallet wallet = wallets.stream()
                                .filter(w -> w.GetName().equals(walletName))
                                .findFirst()
                                .get();

            Category category = categories.stream()
                                    .filter(c -> c.GetName().equals(categoryString))
                                    .findFirst()
                                    .get();

            TransactionStatus status = TransactionStatus.valueOf(statusString);

            LocalTime     currentTime             = LocalTime.now();
            LocalDateTime dateTimeWithCurrentHour = incomeDate.atTime(currentTime);

            Long incomeId;

            incomeId = walletService.AddIncome(wallet.GetId(),
                                               category,
                                               dateTimeWithCurrentHour,
                                               incomeValue,
                                               description,
                                               status);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setGraphic(new ImageView(
                new Image(this.getClass()
                              .getResource(Constants.COMMON_ICONS_PATH + "success.png")
                              .toString())));

            alert.setTitle("Success");
            alert.setHeaderText("Income created");
            alert.setContentText("The income was successfully created.");
            alert.showAndWait();

            m_logger.info("Income created: " + incomeId);

            Stage stage = (Stage)descriptionField.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid income value");
            alert.setContentText("Income value must be a number.");
            alert.showAndWait();
        }
        catch (RuntimeException e)
        {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error while creating income");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Configure date picker
     */
    private void ConfigureDatePicker()
    {
        // Set how the date is displayed in the date picker
        incomeDatePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date)
            {
                return date != null ? date.format(Constants.DATE_FORMATTER_NO_TIME)
                                    : "";
            }

            @Override
            public LocalDate fromString(String string)
            {
                return LocalDate.parse(string, Constants.DATE_FORMATTER_NO_TIME);
            }
        });
    }

    private void UpdateWalletBalance()
    {
        String walletName = walletComboBox.getValue();

        if (walletName == null)
        {
            return;
        }

        Wallet wallet = wallets.stream()
                            .filter(w -> w.GetName().equals(walletName))
                            .findFirst()
                            .get();

        walletCurrentBalanceValueLabel.setText(
            String.format("$ %.2f", wallet.GetBalance()));
    }

    private void walletAfterBalance()
    {
        String incomeValueString = incomeValueField.getText();
        String walletName        = walletComboBox.getValue();

        if (incomeValueString == null || incomeValueString.trim().isEmpty() ||
            walletName == null)
        {
            ResetLabel(walletAfterBalanceValueLabel);
            return;
        }

        try
        {
            Double incomeValue = Double.parseDouble(incomeValueString);

            if (incomeValue < 0)
            {
                ResetLabel(walletAfterBalanceValueLabel);
                return;
            }

            Wallet wallet = wallets.stream()
                                .filter(w -> w.GetName().equals(walletName))
                                .findFirst()
                                .get();

            Double walletAfterBalance = wallet.GetBalance() + incomeValue;

            // Episilon is used to avoid floating point arithmetic errors
            if (walletAfterBalance < Constants.EPSILON)
            {
                // Remove old style and add negative style
                SetLabelStyle(walletAfterBalanceValueLabel,
                              Constants.NEGATIVE_BALANCE_STYLE);

                walletAfterBalanceValueLabel.setText(
                    String.format("- $ %.2f", -walletAfterBalance));
            }
            else
            {
                // Remove old style and add neutral style
                SetLabelStyle(walletAfterBalanceValueLabel,
                              Constants.NEUTRAL_BALANCE_STYLE);

                walletAfterBalanceValueLabel.setText(
                    String.format("$ %.2f", walletAfterBalance));
            }
        }
        catch (NumberFormatException e)
        {
            ResetLabel(walletAfterBalanceValueLabel);
        }
    }

    private void LoadWallets()
    {
        wallets = walletService.GetAllWallets();

        walletComboBox.getItems().addAll(
            wallets.stream().map(Wallet::GetName).toList());
    }

    private void LoadCategories()
    {
        categories = categoryService.GetAllCategories();

        categoryComboBox.getItems().addAll(
            categories.stream().map(Category::GetName).toList());
    }

    private void ResetLabel(Label label)
    {
        label.setText("-");
        SetLabelStyle(label, Constants.NEUTRAL_BALANCE_STYLE);
    }

    private void SetLabelStyle(Label label, String style)
    {
        label.getStyleClass().removeAll(Constants.NEGATIVE_BALANCE_STYLE,
                                        Constants.POSITIVE_BALANCE_STYLE,
                                        Constants.NEUTRAL_BALANCE_STYLE);

        label.getStyleClass().add(style);
    }

    public void SetWalletComboBox(Wallet wt)
    {
        if (wallets.stream().noneMatch(w -> w.GetId() == wt.GetId()))
        {
            return;
        }

        walletComboBox.setValue(wt.GetName());

        UpdateWalletBalance();
    }
}
