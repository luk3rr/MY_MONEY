/*
 * Filename: UIUtils.java
 * Created on: October 12, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.util;

import java.text.DecimalFormat;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Utility class for UI-related functionalities
 */
public final class UIUtils
{
    private static final DecimalFormat currencyFormat =
        new DecimalFormat(Constants.CURRENCY_FORMAT);

    private static final DecimalFormat percentageFormat =
        new DecimalFormat(Constants.PERCENTAGE_FORMAT);

    /**
     * Prevent instantiation
     */
    private UIUtils() { }

    /**
     * Add a tooltip to a XYChart node
     * @param node The node to add the tooltip
     * @param text The text of the tooltip
     */
    static public void AddTooltipToXYChartNode(Node node, String text)
    {
        node.setOnMouseEntered(event -> { node.setStyle("-fx-opacity: 0.7;"); });
        node.setOnMouseExited(event -> { node.setStyle("-fx-opacity: 1;"); });

        AddTooltipToNode(node, text);
    }

    /**
     * Add a tooltip to a node
     * @param node The node to add the tooltip
     * @param text The text of the tooltip
     */
    static public void AddTooltipToNode(Node node, String text)
    {
        Tooltip tooltip = new Tooltip(text);
        tooltip.getStyleClass().add(Constants.TOOLTIP_STYLE);
        tooltip.setShowDelay(Duration.seconds(Constants.TOOLTIP_ANIMATION_DELAY));
        tooltip.setHideDelay(Duration.seconds(Constants.TOOLTIP_ANIMATION_DURATION));

        Tooltip.install(node, tooltip);
    }

    /**
     * Format a number to a currency string
     * @param value The value to be formatted
     */
    static public String FormatCurrency(Number value)
    {
        return currencyFormat.format(value);
    }

    /**
     * Format a number to percentage string
     * @param value The value to be formatted
     */
    static public String FormatPercentage(Number value)
    {
        return percentageFormat.format(value) + " %";
    }
}
