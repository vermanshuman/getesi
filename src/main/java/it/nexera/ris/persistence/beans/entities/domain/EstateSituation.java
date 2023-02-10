package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;
import it.nexera.ris.persistence.interfaces.AfterSave;
import it.nexera.ris.persistence.interfaces.BeforeSave;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "estate_situation")
public class EstateSituation extends IndexedEntity implements BeforeSave, AfterSave {

    private static final long serialVersionUID = 863893897426501718L;

    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;

    @ManyToMany
    @JoinTable(name = "situation_formality", joinColumns = {
            @JoinColumn(name = "situation_id", table = "estate_situation")
    }, inverseJoinColumns = {
            @JoinColumn(name = "formality_id", table = "estate_formality")
    })
    private List<EstateFormality> estateFormalityList;

    @Transient
    private List<Property> propertyList;

    @OneToMany(mappedBy = "situation", cascade = CascadeType.ALL)
    private List<SituationProperty> situationProperties;

    @ManyToMany
    @JoinTable(name = "situation_real_formality", joinColumns = {
            @JoinColumn(name = "situation_id", table = "estate_situation")
    }, inverseJoinColumns = {
            @JoinColumn(name = "formality_id", table = "formality")
    })
    private List<Formality> formalityList;

    @ManyToOne
    @JoinColumn(name = "request_id")
    private Request request;

    @OneToMany(mappedBy = "estateSituations", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<EstateSituationFormalityProperty> estateSituationFormalityPropertyList;

    @Column(name = "comment_init", columnDefinition = "LONGTEXT")
    private String commentInit;

    @Column(name = "other_type")
    private Boolean otherType;
    
    @Column(name = "report_relationship")
    private Boolean reportRelationship;

    @Column(name = "sales_development")
    private Boolean salesDevelopment;

    @Column(name = "regime")
    private Boolean regime;

    @Override
    public void beforeSave() {
        if (ValidationHelper.isNullOrEmpty(getPropertyListWithoutInit())
                || ValidationHelper.isNullOrEmpty(getSituationProperties())) {
            return;
        }

        List<SituationProperty> listToRemove = new ArrayList<>();

        getSituationProperties().stream()
                .filter(sp -> getPropertyListWithoutInit().stream().noneMatch(p -> p.getId().equals(sp.getProperty().getId())))
                .forEach(sp -> {
                            sp.removeAllDatafromPropertiesAssociations();
                            sp.getProperty().getSituationProperties().remove(sp);
                            DaoManager.removeWeak(sp, false);
                            listToRemove.add(sp);
                        }
                );
        getSituationProperties().removeAll(listToRemove);

    }

    @Override
    public void afterSave() {
        if (ValidationHelper.isNullOrEmpty(getPropertyListWithoutInit())) {
            return;
        }
        if (getSituationProperties() == null) {
            setSituationProperties(new ArrayList<>());
        }
        List<SituationProperty> listToAdd = new ArrayList<>();

        getPropertyListWithoutInit().stream()
                .filter(p -> getSituationProperties().stream().noneMatch(sp -> sp.getProperty().getId().equals(p.getId())))
                .forEach(p -> {
                            SituationProperty situationProperty = new SituationProperty(this, p);
                            p.getSituationProperties().add(situationProperty);
                            DaoManager.saveWeak(situationProperty, false);
                            listToAdd.add(situationProperty);
                        }
                );
        getSituationProperties().addAll(listToAdd);
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public String getComment() {
        if (comment == null) {
            comment = ResourcesHelper.getString("estateSituationCommentDefaultValue");
        }
        return comment;
    }

    public String getCommentWithoutInitialize(){
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Property> getPropertyList() {
        if (propertyList == null) {
            if (!ValidationHelper.isNullOrEmpty(getSituationProperties())) {
                propertyList = getSituationProperties().stream()
                        .map(SituationProperty::getProperty).collect(Collectors.toList());
            } else {
                propertyList = new ArrayList<>();
            }
        }
        return propertyList.stream().distinct().collect(Collectors.toList());
    }

    public List<Property> getPropertyListWithoutInit() {
        return propertyList;
    }

    public void setPropertyList(List<Property> propertyList) {
        this.propertyList = propertyList;
    }

    public List<SituationProperty> getSituationProperties() {
        return situationProperties;
    }

    public void setSituationProperties(List<SituationProperty> situationProperties) {
        this.situationProperties = situationProperties;
    }

    public List<EstateFormality> getEstateFormalityList() {
        return estateFormalityList;
    }

    public void setEstateFormalityList(List<EstateFormality> estateFormalityList) {
        this.estateFormalityList = estateFormalityList;
    }

    public List<Formality> getFormalityList() {
        return formalityList;
    }

    public void setFormalityList(List<Formality> formalityList) {
        this.formalityList = formalityList;
    }

    public String getCommentInit() {

        return commentInit;
    }

    public void setCommentInit(String commentInit) {
        this.commentInit = commentInit;
    }

    public List<EstateSituationFormalityProperty> getEstateSituationFormalityPropertyList() {
        return estateSituationFormalityPropertyList;
    }

    public void setEstateSituationFormalityPropertyList(List<EstateSituationFormalityProperty> estateSituationFormalityPropertyList) {
        this.estateSituationFormalityPropertyList = estateSituationFormalityPropertyList;
    }

    public Boolean getOtherType() {
        return otherType;
    }

    public void setOtherType(Boolean otherType) {
        this.otherType = otherType;
    }

	public Boolean getReportRelationship() {
		return reportRelationship;
	}

	public void setReportRelationship(Boolean reportRelationship) {
		this.reportRelationship = reportRelationship;
	}

    public Boolean getSalesDevelopment() {
        return salesDevelopment;
    }

    public void setSalesDevelopment(Boolean salesDevelopment) {
        this.salesDevelopment = salesDevelopment;
    }

    public Boolean getRegime() {
        return regime;
    }

    public void setRegime(Boolean regime) {
        this.regime = regime;
    }
}
