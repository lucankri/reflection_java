package edu.school21.models;

import edu.school21.annotations.OrmColumn;
import edu.school21.annotations.OrmColumnId;
import edu.school21.annotations.OrmEntity;

@OrmEntity(table = "car")
public class Car {
    @OrmColumnId
    private Long id;
    @OrmColumn(name = "name", length = 20)
    private String name;
    @OrmColumn(name = "color", length = 20)
    private String color;
    @OrmColumn(name = "automatic")
    private Boolean automatic;
    @OrmColumn(name = "volume")
    private Double volume;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getAutomatic() {
        return automatic;
    }

    public void setAutomatic(Boolean automatic) {
        this.automatic = automatic;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }


    public Car() {}

    public Car(Long id, String name, String color, Boolean automatic, Double volume) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.automatic = automatic;
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", automatic=" + automatic +
                ", volume=" + volume +
                '}';
    }
}
