package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see PropertyXMLElements
 */
public enum PropertyAnnotationBuildingTablePDFXMLElements {
    VANI_PROPERTY_TAB("Consistenza.Valore", "%VANI_PROPERTY_TAB%"),
    TYPE_PROPERTY_TAB("Consistenza.Unita", "%TYPE_PROPERTY_TAB%"),
    MQ_PROPERTY_TAB("", "%MQ_PROPERTY_TAB%"),
    ANNUITY_PROPERTY_TAB("DatiClassamentoF.RenditaEuro", "%ANNUITY_PROPERTY_TAB%"),
    AGRARIO_ANNUITY_PROPERTY_TAB("DatiClassamentoT.RedditoAgrarioEuro", "%AGRARIO_ANNUITY_PROPERTY_TAB%"),
    SUNDAY_ANNUITY_PROPERTY_TAB("DatiClassamentoT.RedditoDominicaleEuro", "%SUNDAY_ANNUITY_PROPERTY_TAB%"),
    SURFACE_ARE_PROPERTY_TAB("SuperficieT.Are", "%SURFACE_PROPERTY_TAB%"),
    SURFACE_CA_PROPERTY_TAB("SuperficieT.Ca", ""),
    SURFACE_HA_PROPERTY_TAB("SuperficieT.Ha", ""),
    /* Added for new format*/
    AGRARIO_ANNUITY_PROPERTY_TAB_ALT("ClassamentoT.RedditoAgrarioEuro", "%AGRARIO_ANNUITY_PROPERTY_TAB_ALT%"),
    SUNDAY_ANNUITY_PROPERTY_TAB_ALT("ClassamentoT.RedditoDominicaleEuro", "%SUNDAY_ANNUITY_PROPERTY_TAB_ALT%");


    private String elementXML;

    private String elementHTML;

    private PropertyAnnotationBuildingTablePDFXMLElements(String elementXML,
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
