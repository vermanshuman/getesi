package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum PropertyInstatTableForPDFXMLElements {
    PROPERTY_BUILDING_NUMBER("GruppoUnitaImmobiliari.IndiceGruppo", "%PROPERTY_BUILDING_NUMBER%", false),
    PROPERTY_RESULT_DATA("DatiDerivantiDaMutazSogg", "%PROPERTY_RESULT_DATA%", true),
    PROPERTY_INSTAT_ROWS("", "%PROPERTY_INSTAT_ROWS%", true);

    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private PropertyInstatTableForPDFXMLElements(String elementXML,
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
