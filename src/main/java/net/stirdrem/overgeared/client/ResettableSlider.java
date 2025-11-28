package net.stirdrem.overgeared.client;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public class ResettableSlider extends AbstractSliderButton {
    private final double min;
    private final double max;
    private final Consumer<Double> setter;

    // Constructor
    public ResettableSlider(int x, int y, int width, int height, Component text, double defaultValue, double min, double max, Consumer<Double> setter) {
        super(x, y, width, height, text, (defaultValue - min) / (max - min)); // Normalize value
        this.min = min;
        this.max = max;
        this.setter = setter;
        this.updateMessage(); // Initialize the display text
    }

    @Override
    protected void updateMessage() {
        double currentValue = min + this.value * (max - min);
        setMessage(Component.literal(String.format("%.2f", currentValue)));
    }

    @Override
    protected void applyValue() {
        double actualValue = min + this.value * (max - min);
        setter.accept(actualValue);
    }

    // Public method to allow external reset
    public void setValueFromNumber(double newValue) {
        this.value = (newValue - min) / (max - min); // Convert to normalized value
        this.updateMessage();
    }
}