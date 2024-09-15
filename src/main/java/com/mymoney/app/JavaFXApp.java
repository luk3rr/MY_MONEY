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
        SpringApplicationBuilder builder =
            new SpringApplicationBuilder(MainApplication.class);
        springContext = builder.run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(Constants.FXML_PATH));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();

        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("My Money Manager");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception
    {
        springContext.close();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
