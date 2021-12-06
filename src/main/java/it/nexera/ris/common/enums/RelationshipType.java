package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum RelationshipType {
    CADASTRAL_DOCUMENT(1l),
    FORMALITY(2l),
    MANUAL_ENTRY(3l);

    private Long id;

    private RelationshipType(Long id) {
        this.id = id;
    }

    public static RelationshipType getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (RelationshipType relationshipType : RelationshipType.values()) {
                if (relationshipType.getId().equals(id)) {
                    return relationshipType;
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
