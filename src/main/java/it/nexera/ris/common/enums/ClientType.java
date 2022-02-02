package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum ClientType {

    PROFESSIONAL(1l),
    COMPANY(2l),
    BANK(3l),
    TRUST(4l),
    PRIVATE(5l);

    private Long id;

    private ClientType(Long id) {
        this.id = id;
    }

    public static ClientType findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (ClientType clientType : ClientType.values()) {
                if (clientType.getId().equals(id)) {
                    return clientType;
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
