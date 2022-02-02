package it.nexera.ris.common.enums;


public enum NextProcedureFormXMLElements implements XMLElements {
    DATE("Data"),
    NUM_RPB("NumRPBis"),
    NUM_RP("NumRP"),
    ACT_CODE("CodiceAtto"),
    NOTE_TYPE("TipoNota"),
    YEAR("Anno"),
    DESCRIPTION("Descrizione");

    private String element;

    private NextProcedureFormXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
}
