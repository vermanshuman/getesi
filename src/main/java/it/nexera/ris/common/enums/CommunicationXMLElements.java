package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum CommunicationXMLElements implements XMLElements {

    FORMALITY_TYPE("", true),
    COMMUNICATION_DATE("DataEsecuzione", false),
    EXTINCTION_DATE("DataEstinzione", false),
    RECEIVE_DATE("DataRicezione", false),
    PARTICULAR_REGISTER("NumeroRegistro", false),
    COMMUNICATION_CODE("CodComunicazione", false),
    REMARK("Osservazioni", false);

    private String element;

    private boolean specialFlow;

    private CommunicationXMLElements(String element, boolean specialFlow) {
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
