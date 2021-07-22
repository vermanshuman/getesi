package it.nexera.ris.common.enums;

public enum PropertyBlockGenerationTags {

    CATASTO("%CATASTO%", "getCadastralCategory"),
    SHEET("%SHEET%", "getSheets"),
    PARTICLE("%PARTICLE%", "getParticles"),
    SUB("%SUB%", "getSubs"),
    CATEGORY("%CATEGORY%", "getCadastralCategoryCode"),
    RENDITA("%RENDITA%", "getRevenue"),
    OMI("%OMI%", "getLastEstimateOMI"),
    VAL_COM("%VAL_COM%", "getLastCommercialValue"),
    INDIRIZZO("%INDIRIZZO%", "getAddress"),
    FLOOR("%FLOOR%", "getFloor"),
    CONSISTENCY("%CONSISTENCY%", "getConsistency");

    private PropertyBlockGenerationTags(String tag, String getMethod) {
        this.tag = tag;
        this.getMethod = getMethod;
    }

    private String tag;

    private String getMethod;

    public String getTag() {
        return tag;
    }

    public String getGetMethod() {
        return getMethod;
    }

}
