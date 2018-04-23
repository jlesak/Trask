

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


public class MyTextField extends JTextField {

    private String placeholder;
    private boolean required;


    public MyTextField(String placeholder) {
        this();
        setPlaceholder(placeholder);

    }

    public MyTextField() {
        super();
        this.required = false;
        addFocusListeners();
    }

    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * Sets TextField placeholder and foreground color to gray
     *
     * @param placeholder String placeholder
     */
    public void setPlaceholder(final String placeholder) {
        this.placeholder = placeholder;
        setForeground(placeholder.isEmpty() ? Color.BLACK : Color.GRAY);
        if (getText().isEmpty()) setText(this.placeholder);
    }

    private void addFocusListeners() {
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (getText().equals(placeholder)) {
                    setText("");
                    setForeground(Color.BLACK);
                    setDefaultBorder();
                }
            }

            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    clear();
                }
            }
        });
    }

    /**
     * Sets TextField border to red
     */
    public void markRequired() {
        Border redBorder = BorderFactory.createLineBorder(Color.RED, 1);
        setBorder(redBorder);
    }

    private void setDefaultBorder() {
        Border redBorder = BorderFactory.createLineBorder(Color.GRAY, 1);
        setBorder(redBorder);
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Clears text value in TextField and shows placeholder
     */
    public void clear() {
        setText(placeholder);
        setForeground(Color.GRAY);
    }
}