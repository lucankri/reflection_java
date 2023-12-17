package edu.school21.classes;

import java.util.StringJoiner;

public class Car {
    private String name;
    private String color;
    private boolean automatic;
    private double volume;


    public Car() {
        name = "Default name";
        color = "Default color";
        automatic = false;
        volume = 0;
    }

    public Car(String name, String color, boolean automatic, double volume) {
        this.name = name;
        this.color = color;
        this.automatic = automatic;
        this.volume = volume;
    }

    public double growVolume(double volume) {
        this.volume += volume;
        return this.volume;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Car.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("color='" + color + "'")
                .add("automatic='" + automatic + "'")
                .add("volume=" + volume)
                .toString();
    }
}
