package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum SubjectType {
    PHYSICAL_PERSON(1l), LEGAL_PERSON(2l);

    private Long id;

    private SubjectType(Long id) {
        this.id = id;
    }

    public static SubjectType getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (SubjectType type : SubjectType.values()) {
                if (type.getId().equals(id)) {
                    return type;
                }
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return ResourcesHelper.getEnum(EnumHelper.toStringFormatter(this));
    }

    public Long getId() {
        return id;
    }

}
