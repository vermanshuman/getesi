package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "application_settings")
public class ApplicationSettingsValue extends IndexedEntity {
    private static final long serialVersionUID = -5721332310846891726L;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_key")
    private ApplicationSettingsKeys key;

    @Column
    private String value;

    public ApplicationSettingsKeys getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(ApplicationSettingsKeys key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
