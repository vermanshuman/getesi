package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum PropertyAnnotationForPDFXMLElements {
    PROPERTY_BUILDING_INDEX("ImmobileFabbricati.IndiceImmobile", "%PROPERTY_BUILDING_INDEX%"),
    /* Added for handling new format */
    PROPERTY_BUILDING_INDEX_ALT("ImmobileFabbricatiS.IndiceImmobile", "%PROPERTY_BUILDING_INDEX_ALT%"),
    PROPERTY_LAND_INDEX("ImmobileTerreni.IndiceImmobile", "%PROPERTY_LAND_INDEX%"),
    /* Added for handling new format */
    PROPERTY_LAND_INDEX_ALT("ImmobileTerreniS.IndiceImmobile", "%PROPERTY_LAND_INDEX_ALT%"),
    PROPERTY_ANNOTATION("Annotazione", "%PROPERTY_ANNOTATION%"),
    /* Added for handling new format */
    PROPERTY_ANNOTATION_ALT("Annotazione.Descrizione", "%PROPERTY_ANNOTATION_ALT%"),
    ;


    private String elementXML;

    private String elementHTML;

    private PropertyAnnotationForPDFXMLElements(String elementXML,
                                                String elementHTML) {
        this.elementXML = elementXML;
        this.elementHTML = elementHTML;
    }

    public String getElementXML() {
        return elementXML;
    }

    public String getElementHTML() {
        return elementHTML;
    }
}
