package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum SexTypes {

    MALE(1l, "M"),
    FEMALE(2l, "F");

    private SexTypes(Long id, String shortValue) {
        this.id = id;
        this.shortValue = shortValue;
    }

    private Long id;

    private String shortValue;

    public static SexTypes getById(Long id) {
        SexTypes sex = null;

        for (SexTypes type : SexTypes.values()) {
            if (type.getId().equals(id)) {
                sex = type;

                break;
            }
        }

        return sex;
    }

    public static SexTypes getByShortValue(String shortValue) {
        SexTypes sex = null;

        for (SexTypes type : SexTypes.values()) {
            if (type.getShortValue().equals(shortValue)) {
                sex = type;

                break;
            }
        }

        return sex;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public String getShortValue() {
        return shortValue;
    }

    public Long getId() {
        return id;
    }
}
