package it.nexera.ris.web.beans.wrappers.logic;

import it.nexera.ris.common.enums.MailManagerTypes;

public class MailManagerTypeWrapper extends BaseEnumWrapper {

    private static final long serialVersionUID = 7145814452783878491L;

    private MailManagerTypes type;

    private boolean showPriority;

    public MailManagerTypeWrapper(MailManagerTypes type) {
        this.setType(type);
        this.setId(type.getId());
        this.setValue(type.toString());
        this.setSelected(Boolean.TRUE);
        this.setShowPriority(type == MailManagerTypes.SENT || type == MailManagerTypes.RECEIVED);
    }

    public MailManagerTypes getType() {
        return type;
    }

    public void setType(MailManagerTypes type) {
        this.type = type;
    }

    public String getFacesTitle(String value) {
        StringBuilder builder = new StringBuilder();
        builder.append(getValue());
        if (value != null && type.isAmount()) {
            if (type == MailManagerTypes.RECEIVED) {
                builder.append(" (<b>").append(value).append("</b>)");
            } else {
                builder.append(" (").append(value).append(")");
            }
        }
        return builder.toString();
    }

    public boolean isShowPriority() {
        return showPriority;
    }

    public void setShowPriority(boolean showPriority) {
        this.showPriority = showPriority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MailManagerTypeWrapper wrapper = (MailManagerTypeWrapper) o;

        return type == wrapper.type;

    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }
}
