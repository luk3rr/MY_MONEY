/*
 * Filename: MainController.java
 * Created on: September 19, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.mymoney.util.Constants;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MainController
{
    @FXML
    private VBox sidebar;

    @FXML
    private Button menuButton;

    @FXML
    private Button homeButton;

    @FXML
    private Button walletButton;

    @FXML
    private Button creditCardButton;

    @FXML
    private Button transactionButton;

    @FXML
    private AnchorPane contentArea;

    @Autowired
    private ConfigurableApplicationContext springContext;

    private boolean  isMenuExpanded = false;
    private Button[] sidebarButtons;

    @FXML
    public void initialize()
    {
        sidebarButtons = new Button[] { menuButton,
                                        homeButton,
                                        walletButton,
                                        creditCardButton,
                                        transactionButton };

        menuButton.setOnAction(event -> ToggleMenu());
        homeButton.setOnAction(
            event -> LoadContent(Constants.HOME_FXML, Constants.HOME_STYLE_SHEET));
        walletButton.setOnAction(
            event -> LoadContent(Constants.WALLET_FXML, Constants.WALLET_STYLE_SHEET));
        creditCardButton.setOnAction(event
                                     -> LoadContent(Constants.CREDIT_CARD_FXML,
                                                    Constants.CREDIT_CARD_STYLE_SHEET));
        transactionButton.setOnAction(
            event
            -> LoadContent(Constants.TRANSACTION_FXML,
                           Constants.TRANSACTION_STYLE_SHEET));

        // Load start page
        LoadContent(Constants.HOME_FXML, Constants.HOME_STYLE_SHEET);
    }

    /**
     * Load the content of the main window with the content of the FXML file
     * passed as parameter
     * @param fxmlFile The path to the FXML file to be loaded
     */
    public void LoadContent(String fxmlFile, String styleSheet)
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            loader.setControllerFactory(springContext::getBean);
            Parent newContent = loader.load();

            newContent.getStylesheets().add(
                getClass().getResource(styleSheet).toExternalForm());

            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Toggle the visibility of the sidebar menu
     */
    private void ToggleMenu()
    {
        double targetWidth = isMenuExpanded ? Constants.MENU_COLLAPSED_WIDTH
                                            : Constants.MENU_EXPANDED_WIDTH;

        Timeline timeline = new Timeline();

        KeyValue keyValueSidebarWidth =
            new KeyValue(sidebar.prefWidthProperty(), targetWidth);

        KeyFrame keyFrame =
            new KeyFrame(Duration.millis(Constants.MENU_ANIMATION_DURATION),
                         keyValueSidebarWidth);

        timeline.getKeyFrames().add(keyFrame);

        timeline.setOnFinished(event -> {
            SetButtonTextVisibility(isMenuExpanded);
            SetButtonWidth(targetWidth);
        });

        timeline.play();

        isMenuExpanded = !isMenuExpanded;
    }

    /**
     * Set the visibility of the text of the sidebarButtons
     * @param visible True if the text should be visible, false otherwise
     */
    private void SetButtonTextVisibility(boolean visible)
    {
        for (Button button : sidebarButtons)
        {
            button.setContentDisplay(visible ? ContentDisplay.LEFT
                                             : ContentDisplay.GRAPHIC_ONLY);
        }
    }

    /**
     * Set the width of the sidebarButtons
     * @param width The width to be set
     */
    private void SetButtonWidth(double width)
    {
        for (Button button : sidebarButtons)
        {
            button.setPrefWidth(width);
        }
    }
}
