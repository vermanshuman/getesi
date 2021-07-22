package it.nexera.ris.common.enums;


public enum EstateLocationsXMLElements implements XMLElements {
    COMMON_CODE("CodiceComune"),
    DESCRIPTION("Descrizione");

    private String element;

    private EstateLocationsXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
}
