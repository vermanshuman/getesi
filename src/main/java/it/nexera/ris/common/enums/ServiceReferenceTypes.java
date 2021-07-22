package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum ServiceReferenceTypes {
    REGISTER(1l),
    TAVOLARI(2l),
    COMMON(3l),
    LAW_COURTS(4l);

    private Long id;

    ServiceReferenceTypes(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static ServiceReferenceTypes getById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (ServiceReferenceTypes requestType : ServiceReferenceTypes.values()) {
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
