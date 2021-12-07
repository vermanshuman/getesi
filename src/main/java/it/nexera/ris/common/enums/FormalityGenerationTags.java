package it.nexera.ris.common.enums;

import it.nexera.ris.persistence.beans.entities.domain.Relationship;
import it.nexera.ris.persistence.beans.entities.domain.Subject;

public enum FormalityGenerationTags {

    ROW_NUMBER("%ROW_NUMBER%", null, null),
    PROVEN("%PROVEN%", null, null),
    CITY("%CITY%", null, null),
    SUBJECT_FULL_NAME("%SUBJECT_FULL_NAME%", "getFullName", Subject.class),
    QUOTA("%QUOTA%", "getQuote", Relationship.class),
    FORMALITY_BLOCK("%FORMALITY_BLOCK%", null, null),
    PROPERTY_BLOCK("%PROPERTY_BLOCK%", null, null);

    private String tag;

    private String getMethod;

    private Class<?> clazz;

    private FormalityGenerationTags(String tag, String getMethod, Class<?> clazz) {
        this.tag = tag;
        this.getMethod = getMethod;
        this.clazz = clazz;
    }

    public String getTag() {
        return tag;
    }

    public String getGetMethod() {
        return getMethod;
    }

    public Class<?> getClazz() {
        return clazz;
    }

}
