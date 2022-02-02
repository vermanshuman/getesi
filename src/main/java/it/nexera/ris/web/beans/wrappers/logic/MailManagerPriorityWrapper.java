package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.MailManagerPriority;
import it.nexera.ris.common.helpers.EnumHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;

/**
 * Created by Admin on 11.07.2017.
 */
public class MailManagerPriorityWrapper extends BaseEnumWrapper {

    private static final String POSTFIX_MESSAGE = "_MESSAGE";

    private MailManagerPriority priority;

    private String icon;
    private String message;

    public MailManagerPriorityWrapper(MailManagerPriority priority) {
        this.priority = priority;
        setId((long) priority.getId());
        this.icon = priority.getIcon();
        this.message = ResourcesHelper.getEnum(EnumHelper.toStringFormatter(priority).concat(POSTFIX_MESSAGE));
    }

    public MailManagerPriority getPriority() {
        return priority;
    }

    public void setPriority(MailManagerPriority priority) {
        this.priority = priority;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return priority.toString();
    }
}
