package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.ImportSettingsType;
import it.nexera.ris.common.enums.PageTypes;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.RedirectHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.entities.domain.ApplicationSettingsValue;
import it.nexera.ris.settings.ApplicationSettingsHolder;
import it.nexera.ris.web.beans.EntityEditPageBean;
import org.hibernate.HibernateException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

@ManagedBean(name = "importSettingsBean")
@ViewScoped
public class ImportSettingsEditBean extends EntityEditPageBean<ApplicationSettingsValue> implements Serializable {

    private static final long serialVersionUID = 1643495309198827561L;

    private ImportSettingsType currentType;

    private String path;

    private String interval;

    private String numFiles;

    private String pathFilesServer;

    @Override
    protected void pageLoadStatic() throws PersistenceBeanException {
        if (getRequestParameter("type") != null) {
            setCurrentType(Arrays.stream(ImportSettingsType.values()).filter(t -> t.name()
                    .equalsIgnoreCase(getRequestParameter("type"))).findAny().orElse(null));
        }
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        setPath(ApplicationSettingsHolder.getInstance().getByKey(getCurrentType().getPathKey()).getValue());
        setInterval(ApplicationSettingsHolder.getInstance().getByKey(getCurrentType().getIntervalKey()).getValue());
        if (isTypeWithNumberFilesKey()) {
            setNumFiles(ApplicationSettingsHolder.getInstance().getByKey(getCurrentType().getNumberFilesKey()).getValue());
        }
        if (isTypeWithPathFilesServer()) {
            setPathFilesServer(ApplicationSettingsHolder.getInstance().getByKey(getCurrentType().getPathFilesServer()).getValue());
        }
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getPath())) {
            addRequiredFieldException("path");
        }
        if (ValidationHelper.isNullOrEmpty(getInterval())) {
            addRequiredFieldException("interval");
        }
        if (isTypeWithNumberFilesKey() && ValidationHelper.isNullOrEmpty(getNumFiles())) {
            addRequiredFieldException("numFiles");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        ApplicationSettingsHolder.getInstance().applyNewValue(getCurrentType().getPathKey(), getPath());
        ApplicationSettingsHolder.getInstance().applyNewValue(getCurrentType().getIntervalKey(), getInterval());
        if (isTypeWithNumberFilesKey()) {
            ApplicationSettingsHolder.getInstance().applyNewValue(getCurrentType().getNumberFilesKey(), getNumFiles());
        }
        if (isTypeWithPathFilesServer()) {
            ApplicationSettingsHolder.getInstance().applyNewValue(getCurrentType().getPathFilesServer(), getPathFilesServer());
        }
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.HOME);
    }

    public boolean isFormality() {
        return ImportSettingsType.FORMALITY == getCurrentType();
    }
    
    public boolean isTypeWithNumberFilesKey() {
	return getCurrentType().hasNumberFilesKey();
    }

    public boolean isTypeWithPathFilesServer() {
        return getCurrentType().hasPathFilesServer();
    }

    public String getHeader() {
        return getCurrentType().toString();
    }

    public ImportSettingsType getCurrentType() {
        return currentType;
    }

    public void setCurrentType(ImportSettingsType currentType) {
        this.currentType = currentType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getNumFiles() {
        return numFiles;
    }

    public void setNumFiles(String numFiles) {
        this.numFiles = numFiles;
    }

    public String getPathFilesServer() {
        return pathFilesServer;
    }

    public void setPathFilesServer(String pathFilesServer) {
        this.pathFilesServer = pathFilesServer;
    }
}
