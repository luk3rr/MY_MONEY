/*
 * Filename: EditCreditCardDebtController.java
 * Created on: October 28, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.dialog;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.mymoney.entities.Category;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.CreditCardDebt;
import org.mymoney.entities.CreditCardPayment;
import org.mymoney.services.CategoryService;
import org.mymoney.services.CreditCardService;
import org.mymoney.util.Constants;
import org.mymoney.util.UIUtils;
import org.mymoney.util.WindowUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class EditCreditCardDebtController
{
    @FXML
    private ComboBox<String> crcComboBox;

    @FXML
    private ComboBox<String> categoryComboBox;

    @FXML
    private ComboBox<YearMonth> invoiceComboBox;

    @FXML
    private Label crcLimitLabel;

    @FXML
    private Label crcAvailableLimitLabel;

    @FXML
    private Label crcLimitAvailableAfterDebtLabel;

    @FXML
    private Label msgLabel;

    @FXML
    private TextField descriptionField;

    @FXML
    private TextField valueField;

    @FXML
    private TextField installmentsField;

    private List<Category> categories;

    private List<CreditCard> creditCards;

    private CategoryService categoryService;

    private CreditCardService creditCardService;

    private CreditCardDebt debtToUpdate;

    @Autowired
    public EditCreditCardDebtController(CategoryService   categoryService,
                                        CreditCardService creditCardService)
    {
        this.categoryService   = categoryService;
        this.creditCardService = creditCardService;
    }

    public void SetCreditCardDebt(CreditCardDebt crcDebt)
    {
        debtToUpdate = crcDebt;

        // Set the values of the expense to the fields
        crcComboBox.setValue(crcDebt.GetCreditCard().GetName());
        crcLimitLabel.setText(
            UIUtils.FormatCurrency(crcDebt.GetCreditCard().GetMaxDebt()));

        Double availableLimit =
            creditCardService.GetAvailableCredit(crcDebt.GetCreditCard().GetId());

        crcAvailableLimitLabel.setText(UIUtils.FormatCurrency(availableLimit));

        // The debt value has already been subtracted from the available limit, so
        // unless the user changes the debt value, the available limit after the debt
        // will be the same
        crcLimitAvailableAfterDebtLabel.setText(UIUtils.FormatCurrency(availableLimit));

        descriptionField.setText(crcDebt.GetDescription());
        valueField.setText(crcDebt.GetTotalAmount().toString());
        installmentsField.setText(crcDebt.GetInstallments().toString());

        categoryComboBox.setValue(crcDebt.GetCategory().GetName());

        CreditCardPayment firstPayment =
            creditCardService.GetPaymentsByDebtId(crcDebt.GetId()).getFirst();

        invoiceComboBox.setValue(YearMonth.of(firstPayment.GetDate().getYear(),
                                              firstPayment.GetDate().getMonth()));
    }

    @FXML
    private void initialize()
    {
        ConfigureInvoiceComboBox();
        PopulateInvoiceComboBox();

        LoadCategories();
        LoadCreditCards();

        PopulateCategoryComboBox();
        PopulateCreditCardComboBox();

        // Reset all labels
        UIUtils.ResetLabel(crcLimitLabel);
        UIUtils.ResetLabel(crcAvailableLimitLabel);
        UIUtils.ResetLabel(crcLimitAvailableAfterDebtLabel);

        // Add listeners
        crcComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            UpdateCreditCardLimitLabels();
            UpdateAvailableLimitAfterDebtLabel();
        });

        valueField.textProperty().addListener((observable, oldValue, newValue) -> {
            UpdateAvailableLimitAfterDebtLabel();
            UpdateMsgLabel();
        });

        installmentsField.textProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateMsgLabel(); });
    }

    @FXML
    private void handleSave()
    {
        String    crcName         = crcComboBox.getValue();
        String    categoryName    = categoryComboBox.getValue();
        YearMonth invoiceMonth    = invoiceComboBox.getValue();
        String    description     = descriptionField.getText().strip();
        String    valueStr        = valueField.getText();
        String    installmentsStr = installmentsField.getText();

        if (crcName == null || crcName.isEmpty() || categoryName == null ||
            categoryName.isEmpty() || description.isEmpty() || valueStr.isEmpty() ||
            invoiceMonth == null)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Empty fields",
                                        "Please fill all the fields");
            return;
        }

        try
        {
            Double debtValue = Double.parseDouble(valueStr);

            Integer installments =
                installmentsStr.isEmpty() ? 1 : Integer.parseInt(installmentsStr);

            CreditCard crc = creditCards.stream()
                                 .filter(c -> c.GetName().equals(crcName))
                                 .findFirst()
                                 .get();

            Category category = categories.stream()
                                    .filter(c -> c.GetName().equals(categoryName))
                                    .findFirst()
                                    .get();

            // Get the date of the first payment to check if the invoice month is the
            // same
            CreditCardPayment firstPayment =
                creditCardService.GetPaymentsByDebtId(debtToUpdate.GetId()).getFirst();

            YearMonth invoice = YearMonth.of(firstPayment.GetDate().getYear(),
                                             firstPayment.GetDate().getMonth());

            // Check if has any modification
            if (debtToUpdate.GetCreditCard().GetId() == crc.GetId() &&
                debtToUpdate.GetCategory().GetId() == category.GetId() &&
                Math.abs(debtValue - debtToUpdate.GetTotalAmount()) <
                    Constants.EPSILON &&
                debtToUpdate.GetInstallments() == installments &&
                debtToUpdate.GetDescription().equals(description) &&
                invoice.equals(invoiceMonth))
            {
                WindowUtils.ShowInformationDialog("Info",
                                                  "No changes",
                                                  "No changes were made.");
            }
            else // If there is any modification, update the debt
            {
                debtToUpdate.SetCreditCard(crc);
                debtToUpdate.SetCategory(category);
                debtToUpdate.SetDescription(description);
                debtToUpdate.SetTotalAmount(debtValue);
                debtToUpdate.SetInstallments(installments);

                creditCardService.UpdateCreditCardDebt(debtToUpdate, invoiceMonth);

                WindowUtils.ShowSuccessDialog("Success",
                                              "Transaction updated",
                                              "Transaction updated successfully.");
            }

            Stage stage = (Stage)crcComboBox.getScene().getWindow();
            stage.close();
        }
        catch (NumberFormatException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Invalid expense value",
                                        "Debt value must be a number");
        }
        catch (RuntimeException e)
        {
            WindowUtils.ShowErrorDialog("Error", "Error creating debt", e.getMessage());
        }
    }

    @FXML
    private void handleCancel()
    {
        Stage stage = (Stage)crcComboBox.getScene().getWindow();
        stage.close();
    }

    private void LoadCategories()
    {
        categories = categoryService.GetCategories();
    }

    private void LoadCreditCards()
    {
        creditCards = creditCardService.GetAllCreditCardsOrderedByName();
    }

    private void UpdateCreditCardLimitLabels()
    {
        CreditCard crc = creditCards.stream()
                             .filter(c -> c.GetName().equals(crcComboBox.getValue()))
                             .findFirst()
                             .orElse(null);

        if (crc == null)
        {
            return;
        }

        crcLimitLabel.setText(UIUtils.FormatCurrency(crc.GetMaxDebt()));

        Double availableLimit = creditCardService.GetAvailableCredit(crc.GetId());

        crcAvailableLimitLabel.setText(UIUtils.FormatCurrency(availableLimit));
    }

    private void UpdateAvailableLimitAfterDebtLabel()
    {
        CreditCard crc = creditCards.stream()
                             .filter(c -> c.GetName().equals(crcComboBox.getValue()))
                             .findFirst()
                             .orElse(null);

        if (crc == null)
        {
            return;
        }

        String value = valueField.getText();

        if (value.isEmpty())
        {
            UIUtils.ResetLabel(crcLimitAvailableAfterDebtLabel);
            return;
        }

        try
        {
            Double debtValue = Double.parseDouble(valueField.getText());

            if (debtValue <= 0)
            {
                UIUtils.ResetLabel(msgLabel);
                return;
            }

            Double diff = debtValue - debtToUpdate.GetTotalAmount();

            Double availableLimitAfterDebt =
                creditCardService.GetAvailableCredit(crc.GetId()) - diff;

            // Set the style according to the balance value after the expense
            if (availableLimitAfterDebt < 0)
            {
                UIUtils.SetLabelStyle(crcLimitAvailableAfterDebtLabel,
                                      Constants.NEGATIVE_BALANCE_STYLE);
            }
            else
            {
                UIUtils.SetLabelStyle(crcLimitAvailableAfterDebtLabel,
                                      Constants.NEUTRAL_BALANCE_STYLE);
            }

            crcLimitAvailableAfterDebtLabel.setText(
                UIUtils.FormatCurrency(availableLimitAfterDebt));
        }
        catch (NumberFormatException e)
        {
            UIUtils.ResetLabel(crcLimitAvailableAfterDebtLabel);
        }
    }

    private void UpdateMsgLabel()
    {
        Integer installments = installmentsField.getText().isEmpty()
                                   ? 1
                                   : Integer.parseInt(installmentsField.getText());

        if (installments < 1)
        {
            msgLabel.setText("Invalid number of installments");
            return;
        }

        String value = valueField.getText();

        if (value.isEmpty())
        {
            UIUtils.ResetLabel(msgLabel);
            return;
        }

        try
        {
            Double debtValue = Double.parseDouble(valueField.getText());

            if (debtValue <= 0)
            {
                UIUtils.ResetLabel(msgLabel);
                return;
            }

            String msgBase = "Repeat for %d months of %s";

            msgLabel.setText(
                String.format(msgBase,
                              installments,
                              UIUtils.FormatCurrency(debtValue / installments)));
        }
        catch (NumberFormatException e)
        {
            msgLabel.setText("Valor da dívida inválido");
        }
    }

    private void PopulateInvoiceComboBox()
    {
        YearMonth currentYearMonth = YearMonth.now();
        YearMonth startYearMonth   = currentYearMonth.minusMonths(12);
        YearMonth endYearMonth     = currentYearMonth.plusMonths(13);

        // Show the last 12 months and the next 12 months as options to invoice date
        for (YearMonth yearMonth = startYearMonth; yearMonth.isBefore(endYearMonth);
             yearMonth           = yearMonth.plusMonths(1))
        {
            invoiceComboBox.getItems().add(yearMonth);
        }

        // Set default as next month
        invoiceComboBox.setValue(currentYearMonth.plusMonths(1));
    }

    private void PopulateCategoryComboBox()
    {
        categoryComboBox.getItems().addAll(
            categories.stream().map(Category::GetName).toList());
    }

    private void PopulateCreditCardComboBox()
    {
        crcComboBox.getItems().addAll(
            creditCards.stream().map(CreditCard::GetName).toList());
    }

    private void ConfigureInvoiceComboBox()
    {
        // Set the format to display the month and year
        invoiceComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(YearMonth yearMonth)
            {
                return yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            }

            @Override
            public YearMonth fromString(String string)
            {
                return YearMonth.parse(string,
                                       DateTimeFormatter.ofPattern("MMMM yyyy"));
            }
        });
    }
}
