package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

public enum PropertyWrapperType {
    CADASTRE(1l),
    FORMALITY(2l),
    MANUAL(3l);

    private Long id;

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    private PropertyWrapperType(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

}
