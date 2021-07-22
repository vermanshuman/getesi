package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum SubjectXMLElements implements XMLElements {

    BIRTH_DATE("SoggettoPF.DataNascita"),
    BORN_IN_FOREIGN_STATE(""),
    FISCAL_CODE("SoggettoPF.CodiceFiscale"),
    FOREIGN_STATE(""),
    FIRST_NAME("SoggettoPF.Nome"),
    SEX("SoggettoPF.Sesso"),
    LAST_NAME("SoggettoPF.Cognome"),
    BIRTH_CITY_DESCRIPTION("SoggettoPF.ComuneNascita"),
    CITY_DESCRIPTION("SoggettoPNF.Sede"),
    BUSINESS_NAME("SoggettoPNF.Denominazione"),
    ELECTED_MORTGAGE_HOME(""),
    NUMBER_VAT("SoggettoPNF.CodiceFiscale"),
    TYPE_ID(""),
    // Added to handle new format
    CITY_CODE("SoggettoPF.CodiceComune");

    private String element;

    private SubjectXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }

}
