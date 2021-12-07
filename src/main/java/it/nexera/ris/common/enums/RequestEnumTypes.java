package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum RequestEnumTypes {
    SUBJECT(1L),
    PROPERTY(2L),
    MADE(3L),
    COMMON(4L);

    private Long id;

    private RequestEnumTypes(Long id) {
        this.id = id;
    }

    public static RequestEnumTypes getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (RequestEnumTypes requestType : RequestEnumTypes.values()) {
                if (requestType.getId().equals(id)) {
                    return requestType;
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
