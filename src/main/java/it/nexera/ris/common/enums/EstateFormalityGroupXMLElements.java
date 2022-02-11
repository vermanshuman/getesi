package it.nexera.ris.common.enums;

public enum EstateFormalityGroupXMLElements implements XMLElements {

    CONSERVATION_DATE("DataAl", true);

    private String element;

    private boolean specialFlow;

    EstateFormalityGroupXMLElements(String element, boolean specialFlow) {
        this.element = element;
        this.specialFlow = specialFlow;
    }

    public String getElement() {
        return element;
    }

    public boolean isSpecialFlow() {
        return specialFlow;
    }
}
