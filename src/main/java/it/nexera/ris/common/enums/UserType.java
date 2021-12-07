package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum UserType {

    PHYSICAL, LEGAL;

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }
}
