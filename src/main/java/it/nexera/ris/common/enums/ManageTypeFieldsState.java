package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum ManageTypeFieldsState {

    ENABLE_AND_MANDATORY,
    ENABLE,
    HIDDEN;

    @Override
    public String toString() {
        return getLocaleString();
    }

    public String getLocaleString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }
}
