package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.ValidationHelper;

public enum EmailType {

    ADDITIONAL(1l),
    PERSONAL(2l);

    private Long id;

    private EmailType(Long id) {
        this.id = id;
    }

    public static EmailType findById(Long id) {
        if (!ValidationHelper.isNullOrEmpty(id)) {
            for (EmailType emailType : EmailType.values()) {
                if (emailType.getId().equals(id)) {
                    return emailType;
                }
            }
        }

        return null;
    }

    public Long getId() {
        return id;
    }

}
