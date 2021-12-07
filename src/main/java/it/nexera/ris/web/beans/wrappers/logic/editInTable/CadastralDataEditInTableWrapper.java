package it.nexera.ris.web.beans.wrappers.logic.editInTable;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.CadastralData;

public class CadastralDataEditInTableWrapper {

    private Long id;

    private boolean edited;

    private String sheet;

    private String particle;

    private String sub;

    public CadastralDataEditInTableWrapper(CadastralData data) {
        this.id = data.getId();
        this.sheet = data.getSheet();
        this.particle = data.getParticle();
        this.sub = data.getSub();
    }

    public void save() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        if(edited){
            CadastralData data = DaoManager.get(CadastralData.class, getId());
            data.setSheet(getSheet());
            data.setParticle(getParticle());
            data.setSub(getSub());
            DaoManager.save(data, true);
        }
    }

    public void setSheet(String sheet) {
        this.sheet = sheet;
        this.edited = true;
    }

    public void setParticle(String particle) {
        this.particle = particle;
        this.edited = true;
    }

    public void setSub(String sub) {
        this.sub = sub;
        this.edited = true;
    }

    public Long getId() {
        return id;
    }

    public boolean isEdited() {
        return edited;
    }

    public String getSheet() {
        return sheet;
    }

    public String getParticle() {
        return particle;
    }

    public String getSub() {
        return sub;
    }
}
