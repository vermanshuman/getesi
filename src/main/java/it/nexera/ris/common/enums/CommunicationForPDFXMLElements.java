package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum CommunicationForPDFXMLElements {

    AC_NUMBER("", "%AC_NUMBER%", true),
    AC_TYPE("", "%AC_TYPE%", true),
    AC_PARTICULAR_REGISTER("Comunicazioni.NumeroRegistro", "%AC_PARTICULAR_REGISTER%", false),
    AC_RECORD_DATE("Comunicazioni.DataRicezione", "%AC_RECORD_DATE%", true),
    AC_STATUS_BY_COMMUNICATION_CODE("Comunicazioni.CodComunicazione", "%AC_STATUS_BY_COMMUNICATION_CODE%", true),
    AC_REMARKS("Comunicazioni.Osservazioni", "%AC_REMARKS%", true),
    AC_ACT_TYPE("", "%AC_ACT_TYPE%", true);

    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private CommunicationForPDFXMLElements(String elementXML,
                                           String elementHTML, boolean specialFlow) {
        this.elementXML = elementXML;
        this.elementHTML = elementHTML;
        this.specialFlow = specialFlow;
    }

    public String getElementXML() {
        return elementXML;
    }

    public String getElementHTML() {
        return elementHTML;
    }

    public boolean isSpecialFlow() {
        return specialFlow;
    }

}
