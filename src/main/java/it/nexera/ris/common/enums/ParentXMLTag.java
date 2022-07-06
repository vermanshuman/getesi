package it.nexera.ris.common.enums;

public enum ParentXMLTag {
    SUBJECT("SoggettoIndividuato"),
    PROPERTY_LAND("ImmobileTerreni"),
    PROPERTY_SUBJECT("IntestazioneGruppo"),
    PROPERTY_BUILDING("ImmobileFabbricati"),
    SYNTHETIC_LIST("ElencoSintetico"),
    STORIC_PROPERTY_SUBJECT("IntestazioneAttuale"),
    /* Handle New Format*/
    PROPERTY_BUILDING_ALT("ImmobileFabbricatiS"),
    PROPERTY_LAND_ALT("ImmobileTerreniS");
    private String element;

    private ParentXMLTag(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }

}
