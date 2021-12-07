package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;

public enum Provenance {

    IMAGE(1L),
    ELECTONIC_FRORMAT(2L);

    private Long id;

    private Provenance(Long id) {
        this.id = id;
    }

    public static Provenance findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (Provenance provenance : Provenance.values()) {
                if (provenance.getId().equals(id)) {
                    return provenance;
                }
            }
        }

        return null;
    }

    public static Provenance getEnumByString(String code) {
        for (Provenance e : Provenance.values()) {
            if (code.equals(e.toString())) return e;
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
