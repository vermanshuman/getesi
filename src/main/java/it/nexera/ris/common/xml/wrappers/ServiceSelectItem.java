package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.domain.dictionary.Service;

public class ServiceSelectItem {

    private Long id;

    private String label;

    public ServiceSelectItem() {
    }

    public ServiceSelectItem(Service a) {
        this.id = a.getId();
        this.label = a.toString();
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceSelectItem item = (ServiceSelectItem) o;

        return id != null ? id.equals(item.id) : item.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
