package it.nexera.ris.common.xml.wrappers;

import it.nexera.ris.common.enums.CommunicationXMLElements;
import it.nexera.ris.common.enums.XMLElements;
import it.nexera.ris.common.helpers.DateTimeHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.Communication;

public class CommunicationXMLWrapper extends BaseXMLWrapper<Communication> {

    private String formalityType;

    private String communicationDate;

    private String particularRegister;

    private String extinctionDate;

    private String receiveDate;

    private String communicationCode;

    private String remark;

    @Override
    public Communication toEntity() {
        Communication communication = new Communication();

        communication.setId(getId());
        communication.setCreateUserId(getCreateUserId());
        communication.setUpdateUserId(getUpdateUserId());
        communication.setCreateDate(getCreateDate());
        communication.setUpdateDate(getUpdateDate());
        communication.setVersion(getVersion());
        communication.setCommunicationCode(getCommunicationCode());
        communication.setCommunicationDate(DateTimeHelper.fromXMLStringDate(getCommunicationDate()));
        communication.setFormalityType(getFormalityType());
        communication.setParticularRegister(getParticularRegister());
        communication.setRemark(getRemark());
        communication.setExtinctionDate(DateTimeHelper.fromXMLStringDate(getExtinctionDate()));
        communication.setReceiveDate(DateTimeHelper.fromXMLStringDate(getReceiveDate()));

        return communication;
    }

    @Override
    public void setField(XMLElements element, String value) {
        if (!ValidationHelper.isNullOrEmpty(value)) {
            switch ((CommunicationXMLElements) element) {
                case REMARK:
                    setRemark(value);
                    break;
                case COMMUNICATION_CODE:
                    setCommunicationCode(value);
                    break;
                case RECEIVE_DATE:
                    setReceiveDate(value);
                    break;
                case EXTINCTION_DATE:
                    setExtinctionDate(value);
                    break;
                case PARTICULAR_REGISTER:
                    setParticularRegister(value);
                    break;
                case COMMUNICATION_DATE:
                    setCommunicationDate(value);
                    break;
                case FORMALITY_TYPE:
                    setFormalityType(value);
                    break;
                default:
                    break;
            }
        }
    }

    public String getFormalityType() {
        return formalityType;
    }

    public void setFormalityType(String formalityType) {
        this.formalityType = formalityType;
    }

    public String getCommunicationDate() {
        return communicationDate;
    }

    public void setCommunicationDate(String communicationDate) {
        this.communicationDate = communicationDate;
    }

    public void setExtinctionDate(String extinctionDate) {
        this.extinctionDate = extinctionDate;
    }

    public void setReceiveDate(String receiveDate) {
        this.receiveDate = receiveDate;
    }

    public String getExtinctionDate() {
        return extinctionDate;
    }

    public String getReceiveDate() {
        return receiveDate;
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
}
