package it.nexera.ris.common.enums;

import it.nexera.ris.common.helpers.ValidationHelper;

public enum MailEditType {
    FORWARD, REPLY, REPLY_TO_ALL, NEW, EDIT, REQUEST, REQUEST_REPLY_ALL,SEND_TO_MANAGER;

    public static MailEditType findByName(String name) {
        if (!ValidationHelper.isNullOrEmpty(name)) {
            for (MailEditType type : MailEditType.values()) {
                if (type.name().equals(name)) {
                    return type;
                }
            }
        }
        return null;
    }
}
