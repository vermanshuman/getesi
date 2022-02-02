package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "rec_print_property")
public class RequestPrintProperty extends IndexedEntity {

    private static final long serialVersionUID = 3594345304501787067L;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    @Column(name = "is_selected")
    private Boolean isSelected;

    @OneToMany(mappedBy = "requestPrintProperty", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    private Set<CadastralData> cadastralData;

    public Long getPropertyId() {
        return getProperty() == null ? null : getProperty().getId();
    }

    public Set<String> getSheetList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().map(CadastralData::getSheet)
                    .collect(Collectors.toSet());
        }

        return new HashSet<String>();
    }

    public String getSheets() {
        return getSheetList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public Set<String> getParticleList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().map(CadastralData::getParticle)
                    .collect(Collectors.toSet());
        }

        return new HashSet<String>();
    }

    public String getParticles() {
        return getParticleList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public Set<String> getSubList() {
        if (!ValidationHelper.isNullOrEmpty(getCadastralData())) {
            return getCadastralData().stream().map(CadastralData::getSub)
                    .collect(Collectors.toSet());
        }

        return new HashSet<String>();
    }

    public String getSubs() {
        return getSubList().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(Boolean isSelected) {
        this.isSelected = isSelected;
    }

    public Set<CadastralData> getCadastralData() {
        return cadastralData;
    }

    public void setCadastralData(Set<CadastralData> cadastralData) {
        this.cadastralData = cadastralData;
    }
}
