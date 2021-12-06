package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "commercial_value_history")
public class CommercialValueHistory extends IndexedEntity {

    private static final long serialVersionUID = 4484983510024804915L;

    @Column(name = "commercial_value", length = 100)
    private String commercialValue;

    @Column(name = "commercial_value_date")
    private Date commercialValueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private Property property;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public CommercialValueHistory copy() {
        CommercialValueHistory history = new CommercialValueHistory();
        history.setCommercialValue(getCommercialValue());
        history.setCommercialValueDate(getCommercialValueDate());
        history.setUser(getUser());
        return history;
    }

    public String getCommercialValue() {
        return commercialValue;
    }

    public void setCommercialValue(String commercialValue) {
        this.commercialValue = commercialValue;
    }

    public Date getCommercialValueDate() {
        return commercialValueDate;
    }

    public void setCommercialValueDate(Date commercialValueDate) {
        this.commercialValueDate = commercialValueDate;
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
