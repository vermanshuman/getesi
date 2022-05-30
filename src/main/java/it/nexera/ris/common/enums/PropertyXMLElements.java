package it.nexera.ris.common.enums;

/**
 * @author Vlad Strunenko
 *
 * <pre>
 *         Some note about structure of "element"
 *
 *         1) Simple tag: "tag"
 *         2) Concatenate several tags: "tag tag"
 *         3) Get value from attribute: "tag.attribute"
 *                 </pre>
 */
public enum PropertyXMLElements implements XMLElements {

    ADDITIONAL_DATA("Annotazione Notifica", false),
    SCALA("DatiIndirizzo.Scala", false),
    INTERNO("Interno", false),
    ADDRESS("IndirizzoImm.IndirizzoImm NumeroCivico", true),
    DATA_FROM("DatiDerivantiDa", false),
    AGRICULTURAL_INCOME("DatiClassamentoT.RedditoAgrarioEuro", false, true),
    AREA("DatiClassamentoF.ZonaCensuaria", false),
    ARES("SuperficieT.Are", false, true),
    CADASTRAL_AREA("SuperficieF.Totale", false),
    EXCLUSED_AREA("SuperficieF.TotaleE", false),
    CADASTRAL_INCOME("DatiClassamentoT.RedditoDominicaleEuro", false, true),
    CENTIARES("SuperficieT.Ca", false, true),
    CLASS_REAL_ESTATE("", true),
    CONSISTENCY("Consistenza.Valore Consistenza.Unita", false),
    DEDUCTION("", false),
    ESTIMATE_OMI("", false),
    HECTARES("SuperficieT.Ha", false, true),
    MICRO_ZONE("DatiClassamentoF.MicroZona", false),
    NUMBER_OF_ROOMS("", true),
    PORTION("DatiClassamentoT.Porzione", false),
    PROPERTY_ASSESSMENT_DATE("", false),
    QUALITY("DatiClassamentoT.Qualita", false),
    REVENUE("DatiClassamentoF.RenditaEuro", false),
    TYPE_ID("", true),
    BUILD_EVALUATION_METHOD_ID("", false),
    BUILD_EVALUATION_TYPE_ID("", false),
    CATEGORY_CODE("DatiClassamentoF.Categoria", true),
    CITY_CODE("GruppoUnitaImmobiliari.CodiceComune", false),
    CITY_CODE_POSTFIX("GruppoUnitaImmobiliari.Sezione", false),
    PROVINCE_CODE("DatiRichiesta.Provincia", true),
    ARISING_FROM_DATA("DatiDerivantiDa", false),
    QUOTE("DirittiReali.Quota", false),
    PROPERTYTYPE("DirittiReali.Descrizione", true),
    FLOOR("Piano", true),
    /* Tags for new fromat */
    DATA_FROM_ALT("DatiDerivantiDa.Descrizione", false),
    CONSISTENCY_ALT("ClassamentoT.SuperficieMQ", false),
    AGRICULTURAL_INCOME_ALT("ClassamentoT.RedditoAgrarioEuro", false, true),
    CADASTRAL_INCOME_ALT("ClassamentoT.RedditoDominicaleEuro", false, true),
    QUALITY_ALT("ClassamentoT.Qualita", false),;

    private String element;

    private boolean specialFlow;

    private boolean haveSeveralTags;

    private PropertyXMLElements(String element, boolean specialFlow) {
        this(element, specialFlow, false);
    }

    private PropertyXMLElements(String element, boolean specialFlow, boolean haveSeveralTags) {
        this.element = element;
        this.specialFlow = specialFlow;
        this.haveSeveralTags = haveSeveralTags;
    }

    public String getElement() {
        return element;
    }

    public boolean isSpecialFlow() {
        return specialFlow;
    }

    public boolean isHaveSeveralTags() {
        return haveSeveralTags;
    }
}
