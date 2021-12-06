package it.nexera.ris.common.enums;

public enum FormalityBlockGenerationTags {
    REG_GEN("%REG_GEN%", "getGeneralRegister"),
    REG_PAR("%REG_PAR%", "getParticularRegister"),
    PRES_DATE("%PRES_DATE%", "getPresentationDate"),
    FORMALITY_TYPE("%FORMALITY_TYPE%", "getMortgageSpeciesDerivedFrom"),
    NOTAIO("%NOTAIO%", "getSectionAPublicOfficialNotary"),
    NOTAIO_SEDE("%NOTAIO_SEDE%", "getSectionASeat"),
    DATA_ATTO("%DATA_ATTO%", "getSectionATitleDate"),
    REPERTORIO("%REPERTORIO%", "getSectionANumberDirectory"),
    SUBJECT_LIST_IN_FAVOR("%SUBJECT_LIST_IN_FAVOR%", "getSecrionCFavorSubjects"),
    SUBJECT_LIST_AGAINST("%SUBJECT_LIST_AGAINST%", "getSecrionCAgainstSubjects"),
    TOTALE("%TOTALE%", "getSectionATotal"),
    CAPITALE("%CAPITALE%", "getSectionACapital"),
    YEARS("%YEARS%", "getSectionADurationYear");

    private String tag;

    private String getMethod;

    private FormalityBlockGenerationTags(String tag, String getMethod) {
        this.tag = tag;
        this.getMethod = getMethod;
    }

    public String getTag() {
        return tag;
    }

    public String getGetMethod() {
        return getMethod;
    }
}
