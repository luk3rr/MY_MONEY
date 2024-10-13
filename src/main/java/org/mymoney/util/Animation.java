/*
 * Filename: Animation.java
 * Created on: October 12, 2024
 * Author: Lucas Araújo <araujolucas@dcc.ufmg.br>
 */

package org.mymoney.util;

import java.util.List;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Static methods to create animations
 */
public final class Animation
{
    /**
     * Animates a bar chart
     * @param data The data to be animated
     * @param targetValue The target value for the bar
     */
    static public void XYChartAnimation(XYChart.Data<String, Number> data,
                                        Double                       targetValue)
    {
        data.setYValue(0.0); // Start at zero

        // Property to store the current value being animated
        DoubleProperty currentValue = new SimpleDoubleProperty(0.0);

        // Calculate the increment per frame based on the duration
        double increment = targetValue / Constants.XYBAR_CHART_ANIMATION_FRAMES;

        // Create a timeline to animate the value from 0 to targetValue
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(Constants.XYBAR_CHART_ANIMATION_DURATION),
                         event -> { data.setYValue(currentValue.get()); }));

        // Set the timeline to update the currentValue incrementally
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setRate(increment);

        // Update the currentValue and stop the timeline when the targetValue is reached
        KeyFrame keyFrame =
            new KeyFrame(Duration.seconds(Constants.XYBAR_CHART_ANIMATION_DURATION),
                         event -> {
                             if (currentValue.get() < targetValue)
                             {
                                 currentValue.set(currentValue.get() + increment);
                                 if (currentValue.get() > targetValue)
                                 {
                                     currentValue.set(targetValue);
                                 }
                             }
                             else
                             {
                                 timeline.stop();
                             }
                         });

        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    /**
     * Animates a stacked bar chart
     * @param data The data to be animated
     * @param targetValues The target values for each part of the stacked bar
     */
    static public void StackedXYChartAnimation(List<XYChart.Data<String, Number>> data,
                                               List<Double> targetValues)
    {
        if (data.size() != targetValues.size())
        {
            throw new IllegalArgumentException(
                "Data and targetValues lists must have the same size.");
        }

        for (XYChart.Data<String, Number> item : data)
        {
            item.setYValue(0.0);
        }

        // Increments for each part of the stacked bar
        double[] increments = new double[data.size()];

        for (Integer i = 0; i < data.size(); i++)
        {
            increments[i] =
                targetValues.get(i) / Constants.XYBAR_CHART_ANIMATION_FRAMES;
        }

        // Animation timeline
        Timeline timeline = new Timeline();

        // For each frame, update the value of each part of the stacked bar
        for (Integer frame = 0; frame < Constants.XYBAR_CHART_ANIMATION_FRAMES; frame++)
        {
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(Constants.XYBAR_CHART_ANIMATION_DURATION /
                                 Constants.XYBAR_CHART_ANIMATION_FRAMES * (frame + 1)),
                event -> {
                    double accumulatedValue = 0.0;

                    // Update the value of each part of the stacked bar
                    for (Integer i = 0; i < data.size(); i++)
                    {
                        XYChart.Data<String, Number> item = data.get(i);

                        // Accumulate the value of the previous parts
                        accumulatedValue += increments[i];

                        double newYValue = accumulatedValue;

                        // Limit the value to the target value
                        if (newYValue > targetValues.get(i))
                        {
                            newYValue = targetValues.get(i);
                        }

                        item.setYValue(newYValue);
                    }
                });

            timeline.getKeyFrames().add(keyFrame);
        }

        // Set the final value of each part of the stacked bar
        timeline.setOnFinished(event -> {
            for (Integer i = 0; i < data.size(); i++)
            {
                XYChart.Data<String, Number> item = data.get(i);
                item.setYValue(targetValues.get(i));
            }
        });

        timeline.play();
    }

    /**
     * Applies a fade-in animation to the window
     * @param stage The stage to apply the animation
     */
    public static void ApplyFadeInAnimation(Stage stage)
    {
        FadeTransition fadeIn =
            new FadeTransition(Duration.seconds(Constants.FADE_IN_ANIMATION_DURATION),
                               stage.getScene().getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Applies a fade-out animation to the window
     * @param stage The stage to apply the animation
     */
    public static void ApplyFadeOutAnimation(Stage stage)
    {
        FadeTransition fadeOut =
            new FadeTransition(Duration.seconds(Constants.FADE_OUT_ANIMATION_DURATION),
                               stage.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.play();
    }

    /**
     * Applies a slide-in animation to the window
     * @param stage The stage to apply the animation
     */
    public static void ApplySlideInAnimation(Stage stage)
    {
        TranslateTransition slideIn = new TranslateTransition(
            Duration.seconds(Constants.SLIDE_ANIMATION_DURANTION),
            stage.getScene().getRoot());
        slideIn.setFromX(-stage.getWidth());
        slideIn.setToX(0);
        slideIn.play();
    }
}
