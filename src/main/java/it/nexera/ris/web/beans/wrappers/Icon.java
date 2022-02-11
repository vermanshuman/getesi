package it.nexera.ris.web.beans.wrappers;

public class Icon {

    private String name;

    public Icon(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}