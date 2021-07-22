package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.enums.ApplicationSettingsKeys;
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

@ManagedBean(name = "documentConversionSettingsBean")
@ViewScoped
public class DocumentConversionSettingsBean extends EntityEditPageBean<ApplicationSettingsValue> implements Serializable {

	private static final long serialVersionUID = -590140471148186091L;

    private String sofficeCommand;
    
    private String sofficeTempDirPrefix;
    
    private String headerImage;
    
    private String footerImage;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        setSofficeCommand(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SOFFICE_COMMAND).getValue());
        setSofficeTempDirPrefix(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.SOFFICE_TEMP_DIR_PREFIX).getValue());
        setHeaderImage(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.DOCUMENT_CONVERSION_HEADER_IMAGE).getValue());
        setFooterImage(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.DOCUMENT_CONVERSION_FOOTER_IMAGE).getValue());        
    }

    @Override
    public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
        if (ValidationHelper.isNullOrEmpty(getSofficeCommand())) {
            addRequiredFieldException("command");
        }
        if (ValidationHelper.isNullOrEmpty(getSofficeTempDirPrefix())) {
            addRequiredFieldException("tempDirPrefix");
        }

    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.SOFFICE_COMMAND, getSofficeCommand());
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.SOFFICE_TEMP_DIR_PREFIX, getSofficeTempDirPrefix());
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.DOCUMENT_CONVERSION_HEADER_IMAGE, getHeaderImage());
        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.DOCUMENT_CONVERSION_FOOTER_IMAGE, getFooterImage());
    }

    @Override
    public void goBack() {
        RedirectHelper.goTo(PageTypes.HOME);
    }

	public String getSofficeCommand() {
		return sofficeCommand;
	}

	public void setSofficeCommand(String sofficeCommand) {
		this.sofficeCommand = sofficeCommand;
	}

	public String getSofficeTempDirPrefix() {
		return sofficeTempDirPrefix;
	}

	public void setSofficeTempDirPrefix(String sofficeTempDirPrefix) {
		this.sofficeTempDirPrefix = sofficeTempDirPrefix;
	}

	public String getHeaderImage() {
		return headerImage;
	}

	public void setHeaderImage(String headerImage) {
		this.headerImage = headerImage;
	}

	public String getFooterImage() {
		return footerImage;
	}

	public void setFooterImage(String footerImage) {
		this.footerImage = footerImage;
	}


}
