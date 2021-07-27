package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.ResourcesHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.common.xml.wrappers.CitySelectItem;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.LandCulture;
import it.nexera.ris.persistence.beans.entities.domain.LandOmi;
import it.nexera.ris.persistence.beans.entities.domain.LandOmiValue;
import it.nexera.ris.persistence.beans.entities.domain.Property;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityEditPageBean;
import lombok.Data;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean(name = "landOmiEditBean")
@ViewScoped
@Data
public class LandOmiEditBean extends EntityEditPageBean<LandOmi>
        implements Serializable {

    private static final long serialVersionUID = 2090025385750315108L;

    private List<SelectItem> provinces;

    private Long selectedProvinceId;

    private Boolean foreignCountry;

    private LandOmiValue newLandOmiValue;

    private List<LandOmiValue> landOmiValueList;

    private LandOmiValue deletedLandOmiValue;

    private List<CitySelectItem> selectedCity;

    private List<CitySelectItem> cities;

    private List<SelectItem> landCultures;

    private Long selectedLandCultureId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException {

        setProvinces(ComboboxHelper.fillList(Province.class, Order.asc("description")));
        getProvinces().add(new SelectItem(Province.FOREIGN_COUNTRY_ID, Province.FOREIGN_COUNTRY));
        fillFields();
        setNewLandOmiValue(new LandOmiValue());
        if(ValidationHelper.isNullOrEmpty(getLandOmiValueList())){
            setLandOmiValueList(new ArrayList<>());
        }
        setLandCultures(ComboboxHelper.fillList(LandCulture.class, Order.asc("name"), true));
    }

    @Override
    public void onValidate() throws PersistenceBeanException,
            HibernateException, IllegalAccessException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getYear())) {
            addRequiredFieldException("form:year");
        }
        if(ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
            addRequiredFieldException("form:province");
        }
        if(ValidationHelper.isNullOrEmpty(getSelectedCity())) {
            addRequiredFieldException("form:city");
        }
    }

    @Override
    public void onSave() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getSelectedProvinceId()))
            getEntity().setProvince(DaoManager.get(Province.class, getSelectedProvinceId()));
        if(!ValidationHelper.isNullOrEmpty(getSelectedCity())){
            List<City> cities = DaoManager.load(City.class,
                    new Criterion[] {
                            Restrictions.in("id", getSelectedCity().stream()
                            .map(CitySelectItem::getId).collect(Collectors.toList()))
                    });
            getEntity().setCities(cities);
        }
        removeDeletedLandOmiValueFromDB();
        DaoManager.save(getEntity());
        for (LandOmiValue landOmiValue : getLandOmiValueList()) {
            landOmiValue.setLandOmi(getEntity());
            DaoManager.save(landOmiValue);
        }
    }

    private void fillFields() throws PersistenceBeanException, IllegalAccessException {
        if(!ValidationHelper.isNullOrEmpty(getEntity().getProvince())){
            this.setSelectedProvinceId(getEntity().getProvince().getId());
            handleProvinceChange();
        }else
            this.setSelectedProvinceId(null);

        setSelectedCity(new LinkedList<>());
        if(!ValidationHelper.isNullOrEmpty(getEntity().getCities())){
            for(City city : getEntity().getCities()){
                CitySelectItem item = new CitySelectItem(city);
                if(!getSelectedCity().contains(item)) {
                    getSelectedCity().add(item);
                }
            }
        }
        setLandOmiValueList(DaoManager.load(LandOmiValue.class, new Criterion[]{
                Restrictions.eq("landOmi.id", getEntity().getId())
        }));
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
                setCities(cities.stream().map(CitySelectItem::new).collect(Collectors.toList()));
            }else {
                this.setForeignCountry(Boolean.TRUE);
            }
        }
    }

    public void deleteLandOmiValue() {
        getLandOmiValueList().remove(getDeletedLandOmiValue());
    }

    public void addLandOmiValue() throws PersistenceBeanException, InstantiationException, IllegalAccessException {
        if (!getNewLandOmiValue().isEmpty() && !ValidationHelper.isNullOrEmpty(getSelectedLandCultureId())
        && getSelectedLandCultureId() > 0) {
            getNewLandOmiValue().setLandCulture(DaoManager.get(LandCulture.class, getSelectedLandCultureId()));
            getLandOmiValueList().add(getNewLandOmiValue());
            setNewLandOmiValue(new LandOmiValue());
        }
        setSelectedLandCultureId(null);
    }

    private void removeDeletedLandOmiValueFromDB() throws PersistenceBeanException {
        if (!ValidationHelper.isNullOrEmpty(getEntity().getOmiValues())) {
            List<LandOmiValue> listToRemove = new ArrayList<>();
            for (LandOmiValue landOmiValue : getEntity().getOmiValues()) {
                if (!getLandOmiValueList().contains(landOmiValue)) {
                    listToRemove.add(landOmiValue);
                }
            }
            getEntity().getOmiValues().removeAll(listToRemove);
            for (LandOmiValue landOmiValue : listToRemove) {
                landOmiValue.setLandOmi(null);
                DaoManager.remove(landOmiValue);
            }
        }
    }
}
