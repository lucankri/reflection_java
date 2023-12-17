package edu.school21.models;

import edu.school21.annotations.OrmColumn;
import edu.school21.annotations.OrmEntity;


public class Computer {
    @OrmColumn(name = "name", length = 10)
    private String name;
    @OrmColumn(name = "processor", length = 10)
    private String processor;
    @OrmColumn(name = "ram_gb", length = 10)
    private Integer ramGB;
    @OrmColumn(name = "wi_fi")
    private Boolean wi_fi;

    public Computer() {}

    @Override
    public String toString() {
        return "Computer{" +
                "name='" + name + '\'' +
                ", processor='" + processor + '\'' +
                ", ramGB=" + ramGB +
                ", wi_fi=" + wi_fi +
                '}';
    }

    public Computer(String name, String processor, Integer ramGB, Boolean wi_fi) {
        this.name = name;
        this.processor = processor;
        this.ramGB = ramGB;
        this.wi_fi = wi_fi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessor() {
        return processor;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public Integer getRamGB() {
        return ramGB;
    }

    public void setRamGB(Integer ramGB) {
        this.ramGB = ramGB;
    }

    public Boolean getWi_fi() {
        return wi_fi;
    }

    public void setWi_fi(Boolean wi_fi) {
        this.wi_fi = wi_fi;
    }
}
