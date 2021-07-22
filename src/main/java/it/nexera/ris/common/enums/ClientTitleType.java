package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum ClientTitleType {

    SIG(1l),
    SIG_RA(2l),
    DOTT(3l),
    DOTT_SSA(4l),
    AVV(5l),
    ING(6l),
    PROF(7l),
    RAG(8l),
    GEOM(9l);

    private Long id;

    private ClientTitleType(Long id) {
        this.id = id;
    }

    public static ClientTitleType findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (ClientTitleType clientTitleType : ClientTitleType.values()) {
                if (clientTitleType.getId().equals(id)) {
                    return clientTitleType;
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
