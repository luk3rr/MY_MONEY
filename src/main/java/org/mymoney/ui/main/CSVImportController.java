/*
 * Filename: CSVImportController.java
 * Created on: October 23, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.ui.main;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import jakarta.persistence.Column;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.stage.FileChooser;
import org.mymoney.entities.CreditCard;
import org.mymoney.entities.Wallet;
import org.mymoney.services.CategoryService;
import org.mymoney.services.CreditCardService;
import org.mymoney.services.WalletService;
import org.mymoney.services.WalletTransactionService;
import org.mymoney.util.LoggerConfig;
import org.mymoney.util.MappingRow;
import org.mymoney.util.WindowUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Controller for the CSV Import screen
 * TODO: Implement the logic to input data from a CSV file into the database
 * TODO: Add option to default values
 */
@Controller
public class CSVImportController
{
    @FXML
    private TextField selectedCsvField;

    @FXML
    private TableView<ObservableList<String>> csvPreviewTableView;

    @FXML
    private TableView<MappingRow> mappingTableView;

    @FXML
    private ComboBox<String> tableSelectorComboBox;

    private CategoryService categoryService;

    private CreditCardService creditCardService;

    private WalletService walletService;

    private WalletTransactionService walletTransactionService;

    private ObservableList<String> availableDbColumns;

    private static final Logger m_logger = LoggerConfig.GetLogger();

    public CSVImportController() { }

    @Autowired
    public CSVImportController(CategoryService          categoryService,
                               CreditCardService        creditCardService,
                               WalletService            walletService,
                               WalletTransactionService walletTransactionService)
    {
        this.categoryService          = categoryService;
        this.creditCardService        = creditCardService;
        this.walletService            = walletService;
        this.walletTransactionService = walletTransactionService;
    }

    @FXML
    private void initialize()
    {
        ConfigureMappingTable();
        PopulateSelectTableComboBox();

        // Add a listener to the table selector
        tableSelectorComboBox.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> { PopulateMappingTable(); });
    }

    /**
     * Open file explorer to select a CSV file
     */
    @FXML
    private void handleSelectCsv()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");

        FileChooser.ExtensionFilter filter =
            new FileChooser.ExtensionFilter("CSV Files", "*.csv");

        fileChooser.getExtensionFilters().add(filter);

        // Open file explorer
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null)
        {
            selectedCsvField.setText(selectedFile.getAbsolutePath());
            LoadCsvIntoTableView(selectedFile);
        }
        else
        {
            selectedCsvField.setText("");
            WindowUtils.ShowInformationDialog(
                "Info",
                "No file selected",
                "No file was selected. Please select a file.");
        }
    }

    @FXML
    private void handleTableSelection()
    {
        String selectedTable =
            tableSelectorComboBox.getSelectionModel().getSelectedItem();

        if (selectedTable != null)
        {
            try
            {
                List<String> dbColumns = GetDbColumnsForTable(selectedTable);

                for (MappingRow row : mappingTableView.getItems())
                {
                    row.GetDbColumnOptions().clear();
                    row.GetDbColumnOptions().addAll(dbColumns);
                }

                mappingTableView.refresh();
            }
            catch (ClassNotFoundException e)
            {
                WindowUtils.ShowErrorDialog(
                    "Error",
                    "Error getting columns",
                    "An error occurred while getting the "
                        + "columns for the table. Please try again.");

                m_logger.severe(e.getMessage());
            }
        }
    }

    /**
     * Get a list of column names from a JPA entity
     * @param tableName The name of the table
     * @return A list of column names
     */
    public List<String> GetDbColumnsForTable(String tableName)
        throws ClassNotFoundException
    {
        Class<?> entityClass = Class.forName("org.mymoney.entities." + tableName);

        return GetDbColumnsForTable(entityClass);
    }

    /**
     * Get a list of column names from a JPA entity
     * @param entityClass The entity class
     * @return A list of column names
     */
    public List<String> GetDbColumnsForTable(Class<?> entityClass)
    {
        List<String> columns = new ArrayList<>();

        Field[] fields = entityClass.getDeclaredFields();

        for (Field field : fields)
        {
            Column col = field.getAnnotation(Column.class);
            if (col != null)
            {
                // if the anotation @Column has a name defined, use it
                // else, use the field name
                if (!col.name().isEmpty())
                {
                    columns.add(col.name());
                }
                else
                {
                    columns.add(field.getName());
                }
            }
        }
        return columns;
    }

    private void LoadCsvIntoTableView(File csvFile)
    {
        csvPreviewTableView.getColumns().clear();
        csvPreviewTableView.getItems().clear();

        try
        {
            List<String> lines = Files.readAllLines(csvFile.toPath());

            if (lines.isEmpty())
            {
                WindowUtils.ShowInformationDialog(
                    "Info",
                    "Empty file",
                    "The selected file is empty. Please select another file.");
                return;
            }

            String   headerLine = lines.get(0);
            String[] headers    = headerLine.split(",");

            // Add the columns to the TableView
            for (int i = 0; i < headers.length; i++)
            {
                final int                                   colIndex = i;
                TableColumn<ObservableList<String>, String> column =
                    new TableColumn<>(headers[i]);

                column.setCellValueFactory(cellData -> {
                    // if column not exists for this row
                    // return empty string
                    if (colIndex < cellData.getValue().size())
                    {
                        return new SimpleStringProperty(
                            cellData.getValue().get(colIndex));
                    }
                    else
                    {
                        return new SimpleStringProperty("");
                    }
                });

                csvPreviewTableView.getColumns().add(column);
            }

            // Add the data to the TableView
            for (int i = 1; i < lines.size(); i++)
            {
                String[] row = lines.get(i).split(",");

                ObservableList<String> rowData = FXCollections.observableArrayList(row);

                csvPreviewTableView.getItems().add(rowData);
            }
        }
        catch (Exception e)
        {
            WindowUtils.ShowErrorDialog(
                "Error",
                "Error reading file",
                "An error occurred while reading the file. Please try again.");

            m_logger.severe(e.getMessage());
        }

        PopulateMappingTable();
    }

    /**
     * Maps the CSV columns to the database columns
     */
    public void PopulateMappingTable()
    {
        mappingTableView.getItems().clear();

        String selectedTable =
            tableSelectorComboBox.getSelectionModel().getSelectedItem();

        if (selectedTable == null)
        {
            return;
        }

        try
        {

            List<String> dbColumns = GetDbColumnsForTable(selectedTable);

            List<String> csvColumns = csvPreviewTableView.getColumns()
                                          .stream()
                                          .map(TableColumn::getText)
                                          .collect(Collectors.toList());

            for (String csvColumn : csvColumns)
            {
                MappingRow row = new MappingRow(csvColumn, dbColumns);
                mappingTableView.getItems().add(row);
            }

            availableDbColumns = FXCollections.observableArrayList(dbColumns);
            // Remove ID column and sort the list
            availableDbColumns.remove("id");
            FXCollections.sort(availableDbColumns);
        }
        catch (ClassNotFoundException e)
        {
            WindowUtils.ShowErrorDialog("Error",
                                        "Error getting columns",
                                        "An error occurred while getting the "
                                            +
                                            "columns for the table. Please try again.");

            m_logger.severe(e.getMessage());
        }
    }

    public void PopulateSelectTableComboBox()
    {
        List<String> entityNames = new ArrayList<>();

        // Scan all classes in the package entities
        Reflections reflections = new Reflections("org.mymoney.entities");

        Set<Class<?>> entities =
            reflections.getTypesAnnotatedWith(jakarta.persistence.Entity.class);

        // Get the table names
        for (Class<?> entity : entities)
        {
            entityNames.add(entity.getSimpleName());
        }

        tableSelectorComboBox.getItems().setAll(entityNames);
    }

    /**
     * reads a CSV file
     * @param csvFilePath path to the CSV file
     * @return a list of string arrays, where each array represents a row in the CSV
     *     file
     * @throws IOException if the file is not found or cannot be read
     * @throws CsvException if the CSV file is not well formatted
     */
    public List<String[]> ReadCSV(String csvFilePath) throws IOException, CsvException
    {
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath)))
        {
            return reader.readAll();
        }
    }

    /**
     * Maps and inserts categories into the database
     * @param csvData the data from the CSV file
     * @param columnMapping a map that maps the CSV columns to the database columns
     */
    public void MapAndInsertCategory(List<String[]>      csvData,
                                     Map<String, String> columnMapping)
    {
        String[]       csvHeaders = csvData.get(0);                     // CSV Headers
        List<String[]> dataRows   = csvData.subList(1, csvData.size()); // Data

        for (String[] row : dataRows)
        {
            for (Map.Entry<String, String> entry : columnMapping.entrySet())
            {
                String csvColumn = entry.getKey();
                String dbColumn  = entry.getValue();

                if (dbColumn.equals("name"))
                {
                    try
                    {
                        categoryService.AddCategory(
                            GetValueForColumn(csvHeaders, row, csvColumn));
                    }
                    catch (IllegalArgumentException e)
                    {
                        m_logger.severe(e.getMessage());
                    }
                    catch (RuntimeException e)
                    {
                        m_logger.warning(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Maps and inserts wallet into the database
     * @param csvData the data from the CSV file
     * @param columnMapping a map that maps the CSV columns to the database columns
     */
    public void MapAndInsertWallet(List<String[]>      csvData,
                                   Map<String, String> columnMapping)
    {
        String[]       csvHeaders = csvData.get(0);                     // CSV Headers
        List<String[]> dataRows   = csvData.subList(1, csvData.size()); // Data

        for (String[] row : dataRows)
        {
            Wallet wt = new Wallet();

            for (Map.Entry<String, String> entry : columnMapping.entrySet())
            {
                String csvColumn = entry.getKey();
                String dbColumn  = entry.getValue();

                try
                {
                    if (dbColumn.equals("name"))
                    {
                        wt.SetName(GetValueForColumn(csvHeaders, row, csvColumn));
                    }
                    else if (dbColumn.equals("balance"))
                    {
                        wt.SetBalance(Double.parseDouble(
                            GetValueForColumn(csvHeaders, row, csvColumn)));
                    }
                }
                catch (IllegalArgumentException e)
                {
                    m_logger.severe(e.getMessage());
                }
                catch (RuntimeException e)
                {
                    m_logger.warning(e.getMessage());
                }
            }

            walletService.CreateWallet(wt.GetName(), wt.GetBalance());
        }
    }

    /**
     * Maps and inserts credit card into the database
     * @param csvData the data from the CSV file
     * @param columnMapping a map that maps the CSV columns to the database columns
     */
    public void MapAndInsertCreditCard(List<String[]>      csvData,
                                       Map<String, String> columnMapping)
    {
        String[]       csvHeaders = csvData.get(0);                     // CSV Headers
        List<String[]> dataRows   = csvData.subList(1, csvData.size()); // Data

        for (String[] row : dataRows)
        {
            CreditCard crc = new CreditCard();

            for (Map.Entry<String, String> entry : columnMapping.entrySet())
            {
                String csvColumn = entry.getKey();
                String dbColumn  = entry.getValue();

                try
                {
                    if (dbColumn.equals("name"))
                    {
                        crc.SetName(GetValueForColumn(csvHeaders, row, csvColumn));
                    }
                    else if (dbColumn.equals("max_debt"))
                    {
                        crc.SetMaxDebt(Double.parseDouble(
                            GetValueForColumn(csvHeaders, row, csvColumn)));
                    }
                    else if (dbColumn.equals("closing_day"))
                    {
                        crc.SetClosingDay(Integer.parseInt(
                            GetValueForColumn(csvHeaders, row, csvColumn)));
                    }
                    else if (dbColumn.equals("billing_due_day"))
                    {
                        crc.SetBillingDueDay(Integer.parseInt(
                            GetValueForColumn(csvHeaders, row, csvColumn)));
                    }
                    else if (dbColumn.equals("last_four_digits"))
                    {
                        crc.SetLastFourDigits(
                            GetValueForColumn(csvHeaders, row, csvColumn));
                    }
                }
                catch (NumberFormatException e)
                {
                    m_logger.severe(e.getMessage());
                }
                catch (IllegalArgumentException e)
                {
                    m_logger.severe(e.getMessage());
                }
                catch (RuntimeException e)
                {
                    m_logger.warning(e.getMessage());
                }
            }

            creditCardService.CreateCreditCard(crc.GetName(),
                                               crc.GetBillingDueDay(),
                                               crc.GetClosingDay(),
                                               crc.GetMaxDebt(),
                                               crc.GetLastFourDigits(),
                                               0L); // default operator id
        }
    }

    /**
     * Gets the value for a given column
     * @param csvHeaders the CSV headers
     * @param row the row from the CSV file
     * @param csvColumn the column to get the value from
     * @return the value for the given column
     */
    private String
    GetValueForColumn(String[] csvHeaders, String[] row, String csvColumn)
    {
        for (int i = 0; i < csvHeaders.length; i++)
        {
            if (csvHeaders[i].equals(csvColumn))
            {
                return row[i];
            }
        }

        throw new IllegalArgumentException("Column " + csvColumn + " not found");
    }

    private void ConfigureMappingTable()
    {
        TableColumn<MappingRow, String> csvColumnTableColumn =
            new TableColumn<>("CSV Column");
        TableColumn<MappingRow, String> dbColumnTableColumn =
            new TableColumn<>("DB Column");

        csvColumnTableColumn.setCellValueFactory(
            param -> new SimpleStringProperty(param.getValue().GetCsvColumn()));

        dbColumnTableColumn.setCellValueFactory(param -> {
            return new SimpleStringProperty(param.getValue().GetSelectedDbColumn());
        });

        dbColumnTableColumn.setCellFactory(
            col -> new ComboBoxTableCell<MappingRow, String>() {
                @Override
                public void updateItem(String item, boolean empty)
                {
                    super.updateItem(item, empty);

                    if (empty || getTableRow() == null)
                    {
                        setGraphic(null);
                        setText(null);
                    }
                    else
                    {
                        MappingRow row = getTableRow().getItem();

                        if (row != null)
                        {
                            ComboBox<String> comboBox = new ComboBox<>();

                            // Get all available options, removing the already selected
                            // ones
                            ObservableList<String> availableOptions =
                                FXCollections.observableArrayList(
                                    getAvailableOptions(row));

                            // Configure the ComboBox
                            comboBox.setItems(availableOptions);
                            comboBox.setValue(row.GetSelectedDbColumn());
                            comboBox.setMaxWidth(Double.MAX_VALUE);

                            comboBox.valueProperty().addListener(
                                (obs, oldValue, newValue) -> {
                                    if (oldValue != null && !oldValue.isEmpty())
                                    {
                                        // Add the old option back to the global list
                                        availableDbColumns.add(oldValue);
                                        FXCollections.sort(availableDbColumns);
                                    }

                                    row.SetSelectedDbColumn(newValue);

                                    if (newValue != null && !newValue.isEmpty())
                                    {
                                        // Remove the new option from the global list
                                        availableDbColumns.remove(newValue);
                                    }

                                    // Refresh the table
                                    mappingTableView.refresh();
                                });

                            setGraphic(comboBox);
                            setText(null);
                        }
                    }
                }

                /**
                 * Get the available options for the ComboBox, removing the selections
                 * already made by other rows in the table
                 * @param currentRow The current row
                 * @return The list of available options
                 */
                private List<String> getAvailableOptions(MappingRow currentRow)
                {
                    List<String> availableOptions = new ArrayList<>(availableDbColumns);

                    // Remove selections made by other rows, except the current row
                    for (MappingRow row : mappingTableView.getItems())
                    {
                        if (row != currentRow && row.GetSelectedDbColumn() != null)
                        {
                            availableOptions.remove(row.GetSelectedDbColumn());
                        }
                    }

                    Collections.sort(availableOptions);

                    // Add an empty option
                    if (!availableOptions.contains(""))
                    {
                        availableOptions.add(0, "");
                    }

                    return availableOptions;
                }
            });

        mappingTableView.getColumns().add(csvColumnTableColumn);
        mappingTableView.getColumns().add(dbColumnTableColumn);
    }
}
