package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.persistence.beans.entities.Entity;

public class SelectItemWrapper<T extends Entity> {

    private Long id;

    private String label;

    public SelectItemWrapper(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public SelectItemWrapper(T item) {
        this(item.getId(), item.toString());
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectItemWrapper<?> that = (SelectItemWrapper<?>) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return label != null ? label.equals(that.label) : that.label == null;
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
