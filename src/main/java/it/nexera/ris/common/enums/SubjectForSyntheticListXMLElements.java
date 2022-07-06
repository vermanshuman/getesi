package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum SubjectForSyntheticListXMLElements implements XMLElements {
    BIRTH_DATE("SoggettoF.DataNascita"),
    BORN_IN_FOREIGN_STATE(""),
    FISCAL_CODE("SoggettoF.CodiceFiscale"),
    FOREIGN_STATE(""),
    FIRST_NAME("SoggettoF.Nome"),
    SEX("SoggettoF.Sesso"),
    LAST_NAME("SoggettoF.Cognome"),
    BIRTH_CITY_CODE("SoggettoF.Comune"),
    CITY_DESCRIPTION("SoggettoN.Sede"),
    BUSINESS_NAME("SoggettoN.Denominazione"),
    ELECTED_MORTGAGE_HOME(""),
    NUMBER_VAT("SoggettoN.CodiceFiscale"),
    TYPE_ID("");

    private String element;

    private SubjectForSyntheticListXMLElements(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }
}
