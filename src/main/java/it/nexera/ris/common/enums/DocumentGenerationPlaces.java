package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum DocumentGenerationPlaces {
    DATABASE_DOCUMENT,
    REQUEST_MANAGEMENT,
    REQUEST_PRINT;

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

}
