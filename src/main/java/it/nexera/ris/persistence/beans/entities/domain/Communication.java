package it.nexera.ris.persistence.beans.entities.domain;

import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.persistence.beans.entities.IndexedEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "communication")
public class Communication extends IndexedEntity {

    private static final long serialVersionUID = 926278759745161319L;

    @Column(name = "formality_type", length = 150)
    private String formalityType;

    @Column(name = "extinction_date")
    private Date extinctionDate;

    @Column(name = "received_date")
    private Date receiveDate;

    @Column(name = "communication_date")
    private Date communicationDate;

    @Column(name = "particular_register", length = 30)
    private String particularRegister;

    @Column(name = "communication_code", length = 150)
    private String communicationCode;

    @Column(name = "remark", length = 300)
    private String remark;

    @ManyToOne
    @JoinColumn(name = "estate_formality_id")
    private EstateFormality estateFormality;

    public String getFormalityType() {
        return formalityType;
    }

    public void setFormalityType(String formalityType) {
        this.formalityType = formalityType;
    }

    public Date getCommunicationDate() {
        return communicationDate;
    }

    public void setCommunicationDate(Date communicationDate) {
        this.communicationDate = communicationDate;
    }

    public String getParticularRegister() {
        return particularRegister;
    }

    public void setParticularRegister(String particularRegister) {
        this.particularRegister = particularRegister;
    }

    public String getCommunicationCode() {
        return communicationCode;
    }

    public void setCommunicationCode(String communicationCode) {
        this.communicationCode = communicationCode;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Date getExtinctionDate() {
        return extinctionDate;
    }

    public void setExtinctionDate(Date extinctionDate) {
        this.extinctionDate = extinctionDate;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate) {
        this.receiveDate = receiveDate;
    }

    public EstateFormality getEstateFormality() {
        return estateFormality;
    }

    public void setEstateFormality(EstateFormality estateFormality) {
        this.estateFormality = estateFormality;
    }

    public String getTypeStr() {
        return "Comunicazione";
    }

    public String getDate() {
        return DateTimeHelper.toString(getCommunicationDate());
    }

    public String getActType() {
        return null;
    }

}
