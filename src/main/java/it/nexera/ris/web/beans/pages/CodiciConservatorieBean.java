package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.hibernate.HibernateException;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.persistence.beans.entities.domain.ApplicationSettingsValue;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ManagedBean
@RequestScoped
public class CodiciConservatorieBean extends EntityEditPageBean<ApplicationSettingsValue> implements Serializable {

    private static final long serialVersionUID = 8159112000550209859L;

    private String codiciConservatorieClientFilesPath;
    
    private String codiciConservatorieTableFilesPath;
    
    private String dtdFilePath;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException, IllegalAccessException {
        setCodiciConservatorieClientFilesPath(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES).getValue());
        setCodiciConservatorieTableFilesPath(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.TABLE_CONSERVATIVE_CODE_FILES).getValue());
        setDtdFilePath(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.DTD).getValue());
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.CLIENT_CONSERVATIVE_CODE_FILES, getCodiciConservatorieClientFilesPath());
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.TABLE_CONSERVATIVE_CODE_FILES, getCodiciConservatorieTableFilesPath());
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.DTD, getDtdFilePath());
    }
    
    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.HOME);
    }

    public String getCodiciConservatorieClientFilesPath() {
        return codiciConservatorieClientFilesPath;
    }

    public void setCodiciConservatorieClientFilesPath(String codiciConservatorieClientFilesPath) {
        this.codiciConservatorieClientFilesPath = codiciConservatorieClientFilesPath;
    }

    public String getCodiciConservatorieTableFilesPath() {
        return codiciConservatorieTableFilesPath;
    }

    public void setCodiciConservatorieTableFilesPath(String codiciConservatorieTableFilesPath) {
        this.codiciConservatorieTableFilesPath = codiciConservatorieTableFilesPath;
    }

    public String getDtdFilePath() {
        return dtdFilePath;
    }

    public void setDtdFilePath(String dtdFilePath) {
        this.dtdFilePath = dtdFilePath;
    }

}
