package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum PropertyRowsForPDFXMLElements {
    /*Added for handling new format*/
    PROPERTY_BUILDING_INDEX_ALT("ImmobileFabbricatiS.IndiceImmobile", "%PROPERTY_BUILDING_INDEX_ALT%", false),
    PROPERTY_LAND_QUALITY_ALT("ClassamentoT.Qualita", "%PROPERTY_LAND_QUALITY_ALT%", false),
    PROPERTY_LAND_CLASS_ALT("ClassamentoT.Classe", "%PROPERTY_LAND_CLASS_ALT%", false),
    PROPERTY_LAND_DOMINICALE_ALT("ClassamentoT.RedditoDominicaleEuro", "%PROPERTY_LAND_DOMINICALE_ALT%", true),
    PROPERTY_LAND_AGRICULTURAL_ALT("ClassamentoT.RedditoAgrarioEuro", "%PROPERTY_LAND_AGRICULTURAL_ALT%", true),

    PROPERTY_BUILDING_INDEX("ImmobileFabbricati.IndiceImmobile", "%PROPERTY_BUILDING_INDEX%", false),
    PROPERTY_BUILDING_SEC_URBANA("IdentificativoDefinitivo.SezUrbana", "%PROPERTY_BUILDING_SEC_URBANA%", true),
    PROPERTY_BUILDING_SHEET("IdentificativoDefinitivo.Foglio", "%PROPERTY_BUILDING_SHEET%", true),
    PROPERTY_BUILDING_PARTICLE("IdentificativoDefinitivo.ParticellaNum", "%PROPERTY_BUILDING_PARTICLE%", true),
    PROPERTY_BUILDING_SUB("IdentificativoDefinitivo.Subalterno", "%PROPERTY_BUILDING_SUB%", true),
    PROPERTY_BUILDING_AREA("DatiClassamentoF.ZonaCensuaria", "%PROPERTY_BUILDING_AREA%", false),
    PROPERTY_BUILDING_MICRO_ZONE("DatiClassamentoF.MicroZona", "%PROPERTY_BUILDING_MICRO_ZONE%", false),
    PROPERTY_BUILDING_CATEGORY("DatiClassamentoF.Categoria", "%PROPERTY_BUILDING_CATEGORY%", false),
    PROPERTY_BUILDING_CLASS("DatiClassamentoF.Classe", "%PROPERTY_BUILDING_CLASS%", false),
    PROPERTY_BUILDING_CONSISTENCY("Consistenza.Valore Consistenza.Unita", "%PROPERTY_BUILDING_CONSISTENCY%", true),
    PROPERTY_BUILDING_TOTAL("", "%PROPERTY_BUILDING_TOTAL%", true),
    PROPERTY_BUILDING_REVENUE("", "%PROPERTY_BUILDING_REVENUE%", true),
    PROPERTY_BUILDING_ADDRESS("", "%PROPERTY_BUILDING_ADDRESS%", true),
    PROPERTY_BUILDING_ANNOTATION("Annotazione", "%PROPERTY_BUILDING_ANNOTATION%", true),
    PROPERTY_BUILDING_INDRIZZO("", "%PROPERTY_BUILDING_INDRIZZO%", true),
    PROPERTY_BUILDING_REMARKS("", "%PROPERTY_BUILDING_REMARKS%", true),
    PROPERTY_BUILDING_NOTIFICATION("", "%PROPERTY_BUILDING_NOTIFICATION%", true),
    PROPERTY_LAND_INDEX("ImmobileTerreni.IndiceImmobile", "%PROPERTY_LAND_INDEX%", false),
    /* Added for handling new format */
    PROPERTY_LAND_INDEX_ALT("ImmobileTerreniS.IndiceImmobile", "%PROPERTY_LAND_INDEX_ALT%", false),
    PROPERTY_LAND_PORTION("DatiClassamentoT.Porzione", "%PROPERTY_LAND_PORTION%", false),
    PROPERTY_LAND_QUALITY("DatiClassamentoT.Qualita", "%PROPERTY_LAND_QUALITY%", false),
    PROPERTY_LAND_CLASS("DatiClassamentoT.Classe", "%PROPERTY_LAND_CLASS%", false),
    PROPERTY_LAND_HA("SuperficieT.Ha", "%PROPERTY_LAND_HA%", true),
    PROPERTY_LAND_ARE("SuperficieT.Are", "%PROPERTY_LAND_ARE%", true),
    PROPERTY_LAND_CA("SuperficieT.Ca", "%PROPERTY_LAND_CA%", true),
    PROPERTY_LAND_DEDUCTION("", "%PROPERTY_LAND_DEDUCTION%", false),
    PROPERTY_LAND_DOMINICALE("DatiClassamentoT.RedditoDominicaleEuro", "%PROPERTY_LAND_DOMINICALE%", true),
    PROPERTY_LAND_AGRICULTURAL("DatiClassamentoT.RedditoAgrarioEuro", "%PROPERTY_LAND_AGRICULTURAL%", true),
    PROPERTY_LAND_ADDRESS("DatiDerivantiDa", "%PROPERTY_LAND_ADDRESS%", false),
    PROPERTY_HISTORY_INDEX("MutazioneSoggettiva.IndiceMutazione", "%PROPERTY_HISTORY_INDEX%", false),
    PROPERTY_HISTORY_NOMINATIVE("Nominativo", "%PROPERTY_HISTORY_NOMINATIVE%", true),
    PROPERTY_HISTORY_FISCAL_CODE("CF", "%PROPERTY_HISTORY_FISCAL_CODE%", true),
    PROPERTY_HISTORY_LAW_CODE("DirittiReali.CodiceDiritto", "%PROPERTY_HISTORY_LAW_CODE%", true),
    PROPERTY_HISTORY_REAL_RIGHTS_DESCRIPTION("DirittiReali.Descrizione", "%PROPERTY_HISTORY_REAL_RIGHTS_DESCRIPTION%", true),
    PROPERTY_HISTORY_REAL_RIGHTS_QUOTE("DirittiReali.Quota", "%PROPERTY_HISTORY_REAL_RIGHTS_QUOTE%", true),
    PROPERTY_HISTORY_RESULT_DATA("DatiDerivantiDaMutazSogg", "%PROPERTY_HISTORY_RESULT_DATA%", true),
    PROPERTY_LAND_CONSISTENCY("ClassamentoT.SuperficieMQ", "%PROPERTY_LAND_CONSISTENCY%", true),;


    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private PropertyRowsForPDFXMLElements(String elementXML, String elementHTML,
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
