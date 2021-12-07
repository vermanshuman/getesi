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
import it.nexera.ris.common.helpers.omi.OMIHelper;
import it.nexera.ris.persistence.beans.entities.domain.ApplicationSettingsValue;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ManagedBean
@RequestScoped
public class OmiValuationDocumentsBean extends EntityEditPageBean<ApplicationSettingsValue> implements Serializable {

    private static final long serialVersionUID = 8159112000550209859L;

    private String omiKmlFilesPath;

    private String omiSecuritiesFilePath;

    private String omiCategoryFilePath;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException, IllegalAccessException {
        setOmiKmlFilesPath(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.OMI_KML_FILES).getValue());
        setOmiSecuritiesFilePath(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.OMI_SECURITIES_FILE).getValue());
        setOmiCategoryFilePath(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.OMI_CATEGORY_FILE).getValue());
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.OMI_KML_FILES, getOmiKmlFilesPath());
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.OMI_SECURITIES_FILE, getOmiSecuritiesFilePath());
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.OMI_CATEGORY_FILE, getOmiCategoryFilePath());

        OMIHelper.initCategoryCodes();
    }
    
    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.HOME);
    }

}
