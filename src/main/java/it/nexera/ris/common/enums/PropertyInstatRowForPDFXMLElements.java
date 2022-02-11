package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum PropertyInstatRowForPDFXMLElements {
    PROPERTY_INSTAT_INDEX("Intestato.IndiceIntestato", "%PROPERTY_INSTAT_INDEX%", false),
    PROPERTY_INSTAT_NOMINATIVE("Nominativo", "%PROPERTY_INSTAT_NOMINATIVE%", false),
    PROPERTY_INSTAT_FISCAL_CODE("CF", "%PROPERTY_INSTAT_FISCAL_CODE%", true),
    PROPERTY_INSTAT_LAW_CODE("DirittiReali.CodiceDiritto", "%PROPERTY_INSTAT_LAW_CODE%", false),
    PROPERTY_INSTAT_SPOUSES_REGIME("DirittiReali.RegimeConiugi", "%PROPERTY_INSTAT_SPOUSES_REGIME%", false),
    REAL_RIGHTS_DESCRIPTION("DirittiReali.Descrizione", "%REAL_RIGHTS_DESCRIPTION%", false),
    REAL_RIGHTS_QUOTE("DirittiReali.Quota", "%REAL_RIGHTS_QUOTE%", true);

    private String elementXML;

    private boolean specialFlow;

    private PropertyInstatRowForPDFXMLElements(String elementXML,
                                               String elementHTML,
                                               boolean specialFlow) {
        this.elementXML = elementXML;
        this.elementHTML = elementHTML;
        this.specialFlow = specialFlow;
    }

    private String elementHTML;

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
