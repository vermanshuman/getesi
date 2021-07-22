package it.nexera.ris.persistence.beans.entities;

import it.nexera.ris.common.helpers.ValidationHelper;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import java.util.Date;

@MappedSuperclass
public abstract class MultivalueEntity extends IndexedEntity {

    @Column
    private String value;

    @Column(name = "number_value")
    private Long numberValue;

    @Column(name = "date_value")
    private Date dateValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(Long numberValue) {
        this.numberValue = numberValue;
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    @Transient
    public Object getRealValue() {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            return value;
        }
        if (!ValidationHelper.isNullOrEmpty(numberValue)) {
            return numberValue;
        }
        if (!ValidationHelper.isNullOrEmpty(dateValue)) {
            return dateValue;
        }
        return booleanValue;
    }
}
