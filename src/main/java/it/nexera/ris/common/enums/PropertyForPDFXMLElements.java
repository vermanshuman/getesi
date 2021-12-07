package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum PropertyForPDFXMLElements {
    PROPERTY_BUILDING_NUMBER("GruppoUnitaImmobiliari.IndiceGruppo", "%PROPERTY_BUILDING_NUMBER%", true),
    PROPERTY_BUILDING_DESCRIPTION("GruppoUnitaImmobiliari.Descrizione", "%PROPERTY_BUILDING_DESCRIPTION%", true),
    PROPERTY_ANNOTATION_ROW("", "%PROPERTY_ANNOTATION_ROW%", true),
    PROPERTY_ANNOTATION_TABLE("", "%PROPERTY_ANNOTATION_TABLE%", true),
    PROPERTY_BUILDING_ROWS("", "%PROPERTY_BUILDING_ROWS%", true),
    PROPERTY_LAND_ROWS("", "%PROPERTY_LAND_ROWS%", true),
    PROPERTY_INSTAT_TABLE("", "%PROPERTY_INSTAT_TABLE%", true),
    EXTRA_COLUMN("", "%EXTRA_COLUMN%", true),
    EXTRA_COLUMNS("", "%EXTRA_COLUMNS%", true),
    PROPERTY_BUILDING_HEADING("Situazione", "%PROPERTY_BUILDING_HEADING%", false),
    PROPERTY_BUILDING_MAPS("", "%PROPERTY_BUILDING_MAPS%", true),
    PROPERTY_HISTORY_DESCRIPTION("MutazioneSoggettiva.Descrizione", "%PROPERTY_HISTORY_DESCRIPTION%", true),
    PROPERTY_HISTORY_ROWS("", "%PROPERTY_HISTORY_ROWS%", true);

    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private PropertyForPDFXMLElements(String elementXML, String elementHTML,
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
