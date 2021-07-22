package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

import java.util.Arrays;

public enum ReferentType {

    RESPONSIBLE_FOR_LITIGATION,
    MANAGER;

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public static ReferentType getByName(String name) {
        return Arrays.stream(ReferentType.values()).filter(t -> t.name().equals(name)).findAny().orElse(null);
    }
}
