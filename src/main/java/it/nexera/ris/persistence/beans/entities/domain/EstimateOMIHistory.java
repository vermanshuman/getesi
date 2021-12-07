package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "estimate_omi_history")
public class EstimateOMIHistory extends IndexedEntity {

    private static final long serialVersionUID = -3397127889753690503L;

    @Column(name = "estimate_omi", length = 100)
    private String estimateOMI;

    @Column(name = "property_assessment_date")
    private Date propertyAssessmentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public EstimateOMIHistory copy() {
        EstimateOMIHistory history = new EstimateOMIHistory();
        history.setEstimateOMI(getEstimateOMI());
        history.setPropertyAssessmentDate(getPropertyAssessmentDate());
        history.setUser(getUser());
        return history;
    }

    public String getEstimateOMI() {
        return estimateOMI;
    }

    public void setEstimateOMI(String estimateOMI) {
        this.estimateOMI = estimateOMI;
    }

    public Date getPropertyAssessmentDate() {
        return propertyAssessmentDate;
    }

    public void setPropertyAssessmentDate(Date propertyAssessmentDate) {
        this.propertyAssessmentDate = propertyAssessmentDate;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
