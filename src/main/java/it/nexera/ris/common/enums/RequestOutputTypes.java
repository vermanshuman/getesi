package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum RequestOutputTypes {
    ONLY_EDITOR(1l),
    ONLY_FILE(2l),
    ALL(3l),
    XML(4l);

    private Long id;

    RequestOutputTypes(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static RequestOutputTypes getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (RequestOutputTypes requestType : RequestOutputTypes.values()) {
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
}
