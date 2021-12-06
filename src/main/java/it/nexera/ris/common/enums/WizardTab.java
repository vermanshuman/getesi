package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;

public enum WizardTab {

    MAIL,
    REPRESENTATIVES,
    CLIENT,
    SERVICE;

    public String toString() {
        return EnumHelper.toStringFormatter(this);
    }

}
