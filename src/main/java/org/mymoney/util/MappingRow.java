/*
 * Filename: MappingRow.java
 * Created on: October 24, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.util;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MappingRow
{
    private StringProperty csvColumn;
    private StringProperty selectedDbColumn;
    private List<String>   dbColumnOptions;

    /**
     * Constructor
     * @param csvColumn CSV column name
     * @param dbColumns List of DB column names
     */
    public MappingRow(String csvColumn, List<String> dbColumns)
    {
        this.csvColumn        = new SimpleStringProperty(csvColumn);
        this.selectedDbColumn = new SimpleStringProperty("");
        this.dbColumnOptions  = new ArrayList<>(dbColumns);
    }

    /**
     * Get the CSV column name
     * @return CSV column name
     */
    public String GetCsvColumn()
    {
        return csvColumn.get();
    }

    public StringProperty CsvColumnProperty()
    {
        return csvColumn;
    }

    /**
     * Get the selected DB column
     * @return selected DB column
     */
    public String GetSelectedDbColumn()
    {
        return selectedDbColumn.get();
    }

    public void SetSelectedDbColumn(String selectedDbColumn)
    {
        this.selectedDbColumn.set(selectedDbColumn);
    }

    public StringProperty SelectedDbColumnProperty()
    {
        return selectedDbColumn;
    }

    /**
     * Get the DB column options for the ComboBox
     * @return List of DB column options
     */
    public List<String> GetDbColumnOptions()
    {
        return dbColumnOptions;
    }
}
