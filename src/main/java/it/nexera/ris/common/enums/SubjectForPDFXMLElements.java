package it.nexera.ris.common.enums;

/**
 * (non-Javadoc)
 *
 * @author Vlad Strunenko
 * @see it.nexera.ris.common.enums.PropertyXMLElements
 */
public enum SubjectForPDFXMLElements {
    SUBJECT_NUMBER("", "%SUBJECT_NUMBER%", true),
    SUBJECT_SURNAME_HOMONYM("SoggettoF.Cognome", "%SUBJECT_SURNAME_HOMONYM%", false),
    SUBJECT_NAME_HOMONYM("SoggettoF.Nome", "%SUBJECT_NAME_HOMONYM%", false),
    BIRTH_CITY_HOMONYM("SoggettoF.Comune", "%BIRTH_CITY_HOMONYM%", false),
    BIRTH_PROVINCE_HOMONYM("SoggettoF.Provincia", "%BIRTH_PROVINCE_HOMONYM%", false),
    BIRTH_DATE_HOMONYM("SoggettoF.DataNascita", "%BIRTH_DATE_HOMONYM%", true),
    SEX("SoggettoF.Sesso", "%SEX%", false),
    FISCAL_CODE_HOMONYM("SoggettoF.CodiceFiscale", "%FISCAL_CODE_HOMONYM%", true),
    SUBJECT_DENOMINATION("SoggettoN.Denominazione", "%SUBJECT_DENOMINATION%", false),
    SUBJECT_SEAT("SoggettoN.Sede", "%SUBJECT_SEAT%", false),
    SUBJECT_PROVINCE("SoggettoN.Provincia", "%SUBJECT_PROVINCE%", false),
    SUBJECT_FISCAL_CODE("SoggettoN.CodiceFiscale", "%SUBJECT_FISCAL_CODE%", false);

    private String elementXML;

    private String elementHTML;

    private boolean specialFlow;

    private SubjectForPDFXMLElements(String elementXML, String elementHTML,
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
