package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum AnnotationForPDFXMLElements {

    AC_NUMBER("", "%AC_NUMBER%", true),
    AC_TYPE("FormalitaSuccessive.TipoNota", "%AC_TYPE%", true),
    AC_PARTICULAR_REGISTER("FormalitaSuccessive.NumRP", "%AC_PARTICULAR_REGISTER%", false),
    AC_RECORD_DATE("FormalitaSuccessive.Data", "%AC_RECORD_DATE%", true),
    AC_ACT_TYPE("FormalitaSuccessive.Descrizione", "%AC_ACT_TYPE%", true);

    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private AnnotationForPDFXMLElements(String elementXML, String elementHTML,
                                        boolean specialFlow) {
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
