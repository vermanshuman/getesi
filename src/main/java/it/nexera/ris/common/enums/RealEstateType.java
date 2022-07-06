package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Arrays;

public enum RealEstateType {
    BUILDING(1l, "F"),
    LAND(2l, "T");

    private Long id;

    private String shortValue;

    private RealEstateType(Long id, String shortValue) {
        this.id = id;
        this.shortValue = shortValue;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public static RealEstateType getTypeById(Long id) {
        return Arrays.stream(RealEstateType.values()).filter(i -> i.getId().equals(id)).findAny().orElse(null);
    }

    public Long getId() {
        return id;
    }

    public String getShortValue() {
        return shortValue;
    }

}
