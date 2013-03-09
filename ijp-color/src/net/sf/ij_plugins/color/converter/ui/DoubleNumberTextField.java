/*
 * Copyright unknown.
 */
package net.sf.ij_plugins.color.converter.ui;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * Based on article at [[http://java.dzone.com/articles/javafx-numbertextfield-and]].
 */
final class DoubleNumberTextField extends TextField {
    private final NumberFormat nf;
    private SimpleDoubleProperty number = new SimpleDoubleProperty();

    public DoubleNumberTextField() {
        this(0);
    }

    public DoubleNumberTextField(final double value) {
        this(value, NumberFormat.getInstance());
        initHandlers();
    }

    public DoubleNumberTextField(final double value, NumberFormat nf) {
        super();
        this.nf = nf;
        initHandlers();
        setNumber(value);
    }

    public final double getNumber() {
        return number.get();
    }

    public final void setNumber(double value) {
        number.set(value);
    }

    public DoubleProperty numberProperty() {
        return number;
    }

    private void initHandlers() {

        // try to parse when focus is lost or RETURN is hit
        setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                parseAndFormatInput();
            }
        });

        focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                if (!newValue) {
                parseAndFormatInput();
            }
//            }
        });

        // Set text in field if BigDecimal property is changed from outside.
        numberProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setText(nf.format(newValue));
            }
        });
    }

    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
        try {
            String input = getText();
            if (input == null || input.length() == 0) {
                return;
            }
            Number parsedNumber = nf.parse(input);
            double newValue = new Double(parsedNumber.toString());
            setNumber(newValue);
            selectAll();
        } catch (ParseException ex) {
            // If parsing fails keep old number
            setText(nf.format(number.get()));
        }
    }
}
