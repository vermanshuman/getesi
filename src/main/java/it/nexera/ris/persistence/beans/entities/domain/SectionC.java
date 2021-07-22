    package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.beans.entities.domain.support.PropertyType;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "section_c")
public class SectionC extends IndexedEntity {

    private static final long serialVersionUID = -1333993636454167493L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "formality_id")
    private Formality formality;

    @Column(name = "in_virtue", length = 50)
    private String inVirtue;

    @Column(name = "seat", length = 150)
    private String seat;

    @Column(name = "relatively_bargaining_unit", length = 10)
    private String relativelyBargainingUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_type_id")
    private PropertyType propertyType;

    @Column(name = "share_fraction", length = 20)
    private String shareFraction;

    @Column(name = "share_milliseconds")
    private Long shareMilliseconds;

    @Column(name = "estate_property_type", length = 100)
    private String estatePropertyType;

    @Column(name = "section_c_type", length = 100)
    private String sectionCType;

    @ManyToMany
    @JoinTable(name = "subject_section_c", joinColumns =
            {
                    @JoinColumn(name = "section_c_id", table = "section_c")
            }, inverseJoinColumns =
            {
                    @JoinColumn(name = "subject_id", table = "subject")
            })
    private List<Subject> subject;

    public String getAllSubjects() {
        return getSubject().stream().map(Subject::getNameBirthDateCity)
                .collect(Collectors.joining(", "));
    }

    public Formality getFormality() {
        return formality;
    }

    public void setFormality(Formality formality) {
        this.formality = formality;
    }

    public String getInVirtue() {
        return inVirtue;
    }

    public void setInVirtue(String inVirtue) {
        this.inVirtue = inVirtue;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getRelativelyBargainingUnit() {
        return relativelyBargainingUnit;
    }

    public void setRelativelyBargainingUnit(String relativelyBargainingUnit) {
        this.relativelyBargainingUnit = relativelyBargainingUnit;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public String getShareFraction() {
        return shareFraction;
    }

    public void setShareFraction(String shareFraction) {
        this.shareFraction = shareFraction;
    }

    public Long getShareMilliseconds() {
        return shareMilliseconds;
    }

    public void setShareMilliseconds(Long shareMilliseconds) {
        this.shareMilliseconds = shareMilliseconds;
    }

    public String getEstatePropertyType() {
        return estatePropertyType;
    }

    public void setEstatePropertyType(String estatePropertyType) {
        this.estatePropertyType = estatePropertyType;
    }

    public String getSectionCType() {
        return sectionCType;
    }

    public void setSectionCType(String sectionCType) {
        this.sectionCType = sectionCType;
    }

    public List<Subject> getSubject() {
        return subject;
    }

    public void setSubject(List<Subject> subject) {
        this.subject = subject;
    }

}
