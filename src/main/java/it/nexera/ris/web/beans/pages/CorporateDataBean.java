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
public class CorporateDataBean extends EntityEditPageBean<ApplicationSettingsValue> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	 private String companyName;

	 private String fiscalCode;

	 private String address;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
			InstantiationException, IllegalAccessException {
		setCompanyName(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_NAME).getValue());
        setFiscalCode(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.FISCAL_CODE).getValue());
        setAddress(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.ADDRESS).getValue());
		
	}

	@Override
	public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
			InstantiationException, IllegalAccessException {
		 	ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_NAME, getCompanyName());
	        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.FISCAL_CODE, getFiscalCode());
	        ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.ADDRESS, getAddress());
	        OMIHelper.initCategoryCodes();
	}
	
	@Override
	public void goBack() {
		RedirectHelper.goTo(PageTypes.HOME);
	}

}
