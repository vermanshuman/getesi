package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import it.nexera.ris.common.enums.EstateFormalityType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.EstateFormality;

public class EstateFormalityEditInTableWrapper extends BaseEditInTableWrapper {

    private String numRP;

    private Integer numRG;

    private EstateFormalityType estateFormalityType;

    private String description;

    public EstateFormalityEditInTableWrapper(EstateFormality formality) {
        super(formality.getId(), formality.getComment());
        this.numRP = formality.getNumRP();
        this.numRG = formality.getNumRG();
        this.estateFormalityType = formality.getEstateFormalityType();
        this.description = formality.getDescription();
    }

    public void save() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if (isEdited()) {
            EstateFormality formality = DaoManager.get(EstateFormality.class, getId());
            DaoManager.refresh(formality);
            formality.setNumRG(getNumRG());
            formality.setNumRP(getNumRP());
            formality.setEstateFormalityType(getEstateFormalityType());
            formality.setDescription(getDescription());
            formality.setComment(getComment());
            DaoManager.save(formality, true);
        }
    }

    public void setNumRP(String numRP) {
        this.numRP = numRP;
        setEdited(true);
    }

    public void setNumRG(Integer numRG) {
        this.numRG = numRG;
        setEdited(true);
    }

    public void setEstateFormalityType(EstateFormalityType estateFormalityType) {
        this.estateFormalityType = estateFormalityType;
        setEdited(true);
    }

    public void setDescription(String description) {
        this.description = description;
        setEdited(true);
    }

    public String getNumRP() {
        return numRP;
    }

    public Integer getNumRG() {
        return numRG;
    }

    public EstateFormalityType getEstateFormalityType() {
        return estateFormalityType;
    }

    public String getDescription() {
        return description;
    }
}
