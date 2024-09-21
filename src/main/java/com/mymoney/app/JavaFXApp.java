/*
 * Filename: JavaFXApp.java
 * Created on: September 15, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package com.mymoney.app;

import com.mymoney.util.Constants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * JavaFX application entry point
 */
public class JavaFXApp extends Application
{
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception
    {
        String[] args = getParameters().getRaw().toArray(new String[0]);

        springContext =
            new SpringApplicationBuilder().sources(MainApplication.class).run(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.MAIN_FXML));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        springContext.close();
        super.stop();
    }
}
