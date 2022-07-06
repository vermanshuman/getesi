package it.nexera.ris.common.enums;

public enum SubjectXMLTag {
    SUBJECT("Nominativo"),
    QUOTE("DirittiReali.Quota"),
    REGIME_CONIUGI("DirittiReali.RegimeConiugi"),
    TYPE("DirittiReali.Descrizione"),
    FISCAL_CODE("CF");


    private String element;

    private SubjectXMLTag(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }

}
