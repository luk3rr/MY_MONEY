/*
 * Filename: CreditCardController.java
 * Created on: October 19, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.main;

import com.jfoenix.controls.JFXButton;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.CreditCardDebt;
import org.mymoney.services.CreditCardService;
import org.mymoney.ui.common.CreditCardPaneController;
import org.mymoney.util.Constants;
import org.mymoney.util.LoggerConfig;
import org.mymoney.util.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

@Controller
public class CreditCardController
{
    private static final Logger logger = LoggerConfig.GetLogger();

    @FXML
    private VBox totalDebtsInfoVBox;

    @FXML
    private ComboBox<Year> totalDebtsYearFilterComboBox;

    @FXML
    private AnchorPane crcPane1;

    @FXML
    private AnchorPane crcPane2;

    //@FXML
    // private AnchorPane crcPane3;

    @FXML
    private JFXButton crcNextButton;

    @FXML
    private JFXButton crcPrevButton;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private StackedBarChart<String, Number> debtsFlowStackedBarChart;

    private CreditCardService creditCardService;

    private List<CreditCard> creditCards;

    private List<CreditCardDebt> creditCardDebts;

    private Integer crcPaneCurrentPage = 0;

    private Integer itemsPerPage = 2;

    /**
     * Constructor
     * @param creditCardService CreditCardService
     */
    public CreditCardController(CreditCardService creditCardService)
    {
        this.creditCardService = creditCardService;
    }

    @FXML
    private void initialize()
    {
        LoadCreditCards();

        PopulateYearFilterComboBox();

        // Select the default values
        LocalDateTime now = LocalDateTime.now();

        totalDebtsYearFilterComboBox.setValue(Year.from(now));

        UpdateTotalDebtsInfo();
        UpdateDisplayCards();

        SetButtonsActions();

        // Add a listener to the year filter combo box
        totalDebtsYearFilterComboBox.valueProperty().addListener(
            (observable, oldValue, newValue) -> { UpdateTotalDebtsInfo(); });
    }

    @FXML
    private void handleAddDebt()
    { }

    @FXML
    private void handleAddCreditCard()
    { }

    @FXML
    private void handleEditDebt()
    { }

    @FXML
    private void handleDeleteDebt()
    { }

    /**
     * Load credit cards from database
     */
    private void LoadCreditCards()
    {
        creditCards = creditCardService.GetAllCreditCardsOrderedByName();
    }

    /**
     * Load credit card debts from database
     */
    private void LoadCreditCardDebts() { }

    /**
     * Update the display of the total debts information
     */
    private void UpdateTotalDebtsInfo()
    {
        // Get the selected year from the year filter combo box
        Year selectedYear = totalDebtsYearFilterComboBox.getValue();

        Double totalDebts =
            creditCardService.GetTotalDebtAmount(selectedYear.getValue());

        Double totalPendingPayments =
            creditCardService.GetTotalPendingPayments(LocalDate.now().getYear());

        Label totalTotalDebtsLabel = new Label(UIUtils.FormatCurrency(totalDebts));

        Label totalPendingPaymentsLabel = new Label(
            "Pending payments: " + UIUtils.FormatCurrency(totalPendingPayments));

        totalTotalDebtsLabel.getStyleClass().add(
            Constants.TOTAL_BALANCE_VALUE_LABEL_STYLE);

        totalPendingPaymentsLabel.getStyleClass().add(
            Constants.TOTAL_BALANCE_FORESEEN_LABEL_STYLE);

        totalDebtsInfoVBox.getChildren().clear();
        totalDebtsInfoVBox.getChildren().add(totalTotalDebtsLabel);
        totalDebtsInfoVBox.getChildren().add(totalPendingPaymentsLabel);
    }

    /**
     * Update the display of the credit cards
     */
    private void UpdateDisplayCards()
    {
        crcPane1.getChildren().clear();
        crcPane2.getChildren().clear();
        // crcPane3.getChildren().clear();

        Integer start = crcPaneCurrentPage * itemsPerPage;
        Integer end   = Math.min(start + itemsPerPage, creditCards.size());

        for (Integer i = start; i < end; i++)
        {
            CreditCard crc = creditCards.get(i);

            try
            {
                FXMLLoader loader =
                    new FXMLLoader(getClass().getResource(Constants.CRC_PANE_FXML));
                loader.setControllerFactory(springContext::getBean);
                Parent newContent = loader.load();

                // Add style class to the wallet pane
                newContent.getStylesheets().add(
                    getClass()
                        .getResource(Constants.COMMON_STYLE_SHEET)
                        .toExternalForm());

                CreditCardPaneController crcPaneController = loader.getController();

                crcPaneController.UpdateCreditCardPane(crc);

                AnchorPane.setTopAnchor(newContent, 0.0);
                AnchorPane.setBottomAnchor(newContent, 0.0);
                AnchorPane.setLeftAnchor(newContent, 0.0);
                AnchorPane.setRightAnchor(newContent, 0.0);

                switch (i % itemsPerPage)
                {
                    case 0:
                        crcPane1.getChildren().add(newContent);
                        break;

                    case 1:
                        crcPane2.getChildren().add(newContent);
                        break;
                }
            }
            catch (IOException e)
            {
                logger.severe("Error while loading credit card pane");
                e.printStackTrace();
                continue;
            }
        }

        crcPrevButton.setDisable(crcPaneCurrentPage == 0);
        crcNextButton.setDisable(end >= creditCards.size());
    }

    private void PopulateYearFilterComboBox()
    {
        LocalDateTime oldestDebtDate = creditCardService.GetOldestDebtDate();

        LocalDate now = LocalDate.now();

        // Generate a list of Year objects from the oldest transaction date to the
        // current date
        Year startYear   = Year.from(oldestDebtDate);
        Year currentYear = Year.from(now);

        // Generate the list of years between the oldest and the current date
        List<Year> years = new ArrayList<>();
        while (!startYear.isAfter(currentYear))
        {
            years.add(currentYear);
            currentYear = currentYear.minusYears(1);
        }

        ObservableList<Year> yearList = FXCollections.observableArrayList(years);

        totalDebtsYearFilterComboBox.setItems(yearList);

        // Custom string converter to format the Year as "Year"
        totalDebtsYearFilterComboBox.setConverter(new StringConverter<Year>() {
            private final DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy");

            @Override
            public String toString(Year year)
            {
                return year != null ? year.format(formatter) : "";
            }

            @Override
            public Year fromString(String string)
            {
                return Year.parse(string, formatter);
            }
        });
    }

    /**
     * Set the actions for the buttons
     */
    private void SetButtonsActions()
    {
        crcPrevButton.setOnAction(event -> {
            if (crcPaneCurrentPage > 0)
            {
                crcPaneCurrentPage--;
                UpdateDisplayCards();
            }
        });

        crcNextButton.setOnAction(event -> {
            if (crcPaneCurrentPage < creditCards.size() / itemsPerPage)
            {
                crcPaneCurrentPage++;
                UpdateDisplayCards();
            }
        });
    }
}
