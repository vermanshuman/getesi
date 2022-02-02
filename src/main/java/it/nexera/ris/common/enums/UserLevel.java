package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum UserLevel {

    GENERAL_SUPERVISOR,
    BRANCH_SUPERVISOR,
    BRANCH_USER;

    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }
}
