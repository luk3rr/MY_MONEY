/*
 * Filename: MainController.java
 * Created on: September 19, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.ui;

import com.mymoney.util.Constants;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
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

    private boolean  isMenuExpanded = false;
    private Button[] buttons;

    private void ToggleMenu()
    {
        double targetWidth = isMenuExpanded ? Constants.MENU_COLLAPSED_WIDTH
                                            : Constants.MENU_EXPANDED_WIDTH;

        Timeline timeline = new Timeline();

        // Aumentar a sidebar à direita e redimensionar a contentArea
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

        // Inverter o estado do menu
        isMenuExpanded = !isMenuExpanded;
    }

    private void SetButtonTextVisibility(boolean visible)
    {
        for (Button button : buttons)
        {
            button.setContentDisplay(visible ? ContentDisplay.LEFT
                                             : ContentDisplay.GRAPHIC_ONLY);
        }
    }

    private void SetButtonWidth(double width)
    {
        for (Button button : buttons)
        {
            button.setPrefWidth(width);
        }
    }

    @FXML
    public void initialize()
    {
        buttons = new Button[] { menuButton,
                                 homeButton,
                                 walletButton,
                                 creditCardButton,
                                 transactionButton };

        menuButton.setOnAction(event -> ToggleMenu());
    }
}
