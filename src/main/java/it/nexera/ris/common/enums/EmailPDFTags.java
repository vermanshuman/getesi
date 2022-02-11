package it.nexera.ris.common.enums;

public enum EmailPDFTags {

    MAIL_DATE,
    MAIL_FROM,
    MAIL_TO,
    MAIL_COPY,
    MAIL_BLIND_COPY,
    MAIL_SUBJECT,
    MAIL_BODY,
    MAIL_ATTACHED,
    MAIL_REQUESTS;

    public String getTag() {
        return String.format("%%%s%%", name());
    }

}
