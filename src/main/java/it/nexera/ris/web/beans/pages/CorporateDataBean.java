package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.model.SelectItem;

import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.SelectItemWrapperConverter;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.xml.wrappers.SelectItemWrapper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
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
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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

	 private String postalCode;

	private List<SelectItem> provinces;

	private Long selectedProvinceId;

	private Boolean foreignCountry;

	private List<SelectItemWrapper<City>> selectedCity;

	private List<SelectItemWrapper<City>> cities;

	private SelectItemWrapperConverter<City> citySelectItemWrapperConverter;

	@Override
	public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
			InstantiationException, IllegalAccessException {
		setCitySelectItemWrapperConverter(new SelectItemWrapperConverter<>(City.class));
		setCompanyName(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_NAME).getValue());
		setFiscalCode(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.FISCAL_CODE).getValue());
		setAddress(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.ADDRESS).getValue());
		setPostalCode(ApplicationSettingsHolder.getInstance().getByKey(ApplicationSettingsKeys.COMPANY_POSTAL_CODE).getValue());
		setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
		getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
		String provinceDescription = ApplicationSettingsHolder.getInstance().getByKey(
				ApplicationSettingsKeys.COMPANY_PROVINCE).getValue();
		if(!ValidationHelper.isNullOrEmpty(provinceDescription)){
			List<Province> provinces = DaoManager.load(Province.class,
					new Criterion[] {
							Restrictions.eq("description", provinceDescription).ignoreCase()
					});
			if(!ValidationHelper.isNullOrEmpty(provinces)){
				setSelectedProvinceId(provinces.get(0).getId());
				handleProvinceChange();
			}else {
				setSelectedProvinceId(null);
			}
			String cityString = ApplicationSettingsHolder.getInstance().getByKey(
					ApplicationSettingsKeys.COMPANY_CITY).getValue();
			setSelectedCity(new LinkedList<>());
			if(!ValidationHelper.isNullOrEmpty(cityString)){
				List<String> cityDescriptions = Stream.of(cityString.split(",", -1))
						.collect(Collectors.toList());

				List<City> cities = DaoManager.load(City.class, new Criterion[]{
						Restrictions.eq("province.id",getSelectedProvinceId()),
						Restrictions.eq("external", Boolean.TRUE),
						Restrictions.in("description", cityDescriptions)
				},Order.asc("description"));
				if(!ValidationHelper.isNullOrEmpty(cities)){
					setSelectedCity(ComboboxHelper.fillWrapperList(cities));
				}
			}
			getCitySelectItemWrapperConverter().getWrapperList().addAll(getCities());
		}
	}

	@Override
	public void onValidate() throws PersistenceBeanException, HibernateException, IllegalAccessException {
	}

	@Override
	public void onSave() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
			InstantiationException, IllegalAccessException {
		ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_NAME, getCompanyName());
		ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.FISCAL_CODE, getFiscalCode());
		ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.ADDRESS, getAddress());
		ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_POSTAL_CODE, getPostalCode());
		if (!ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
			Province selectedProvince = DaoManager.get(Province.class, getSelectedProvinceId());
			if (!ValidationHelper.isNullOrEmpty(selectedProvince)) {
				ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_PROVINCE,
						selectedProvince.getDescription());
			}
		}else {
			ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_PROVINCE, null);
		}
		if (!ValidationHelper.isNullOrEmpty(getSelectedCity())) {
			List<Long> cityIds = getSelectedCity().stream()
					.map(SelectItemWrapper::getId).collect(Collectors.toList());
			List<City> cities = DaoManager.load(City.class, new Criterion[]{
					Restrictions.in("id",cityIds)},Order.asc("description"));
			if (!ValidationHelper.isNullOrEmpty(cities)) {
				String cityString = cities
						.stream()
						.map(c -> c.getDescription())
						.collect(Collectors.joining(","));
				ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_CITY,
						cityString);
			}else {
				ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_CITY, null);
			}
		}else {
			ApplicationSettingsHolder.getInstance().applyNewValue(ApplicationSettingsKeys.COMPANY_CITY, null);
		}
		OMIHelper.initCategoryCodes();
	}
	
	@Override
	public void goBack() {
		RedirectHelper.goTo(PageTypes.HOME);
	}

	public void handleProvinceChange() throws HibernateException, PersistenceBeanException, IllegalAccessException {
		setSelectedCity(new LinkedList<>());
		setCities(null);
		if (!ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
			if (!Province.FOREIGN_COUNTRY_ID.equals(getSelectedProvinceId())) {
				this.setForeignCountry(Boolean.FALSE);
				List<City> cities = DaoManager.load(City.class, new Criterion[]{
						Restrictions.eq("province.id",getSelectedProvinceId()),
						Restrictions.eq("external", Boolean.TRUE)
				},Order.asc("description"));
				setCities(ComboboxHelper.fillWrapperList(cities));
			}else {
				this.setForeignCountry(Boolean.TRUE);
			}
		}
	}
}
