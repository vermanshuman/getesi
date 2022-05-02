package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "citiesListBean")
@ViewScoped
public class CitiesListBean extends EntityLazyInListEditPageBean<City> implements Serializable {

    private static final long serialVersionUID = -397464426868428763L;

    private boolean canCreate;

    private List<SelectItem> cities;

    private List<SelectItem> provinces;

    private Long selectedProvinceId;
    
    private String cfis;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setCanCreate(true);
        this.loadList(City.class, new Criterion[]{
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))
        }, new Order[]{
                Order.asc("code")
        });
        setProvinces(ComboboxHelper.fillList(Province.class));
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        try {
            if (DaoManager.getCount(City.class, "id", new Criterion[]{
                    Restrictions.eq("cfis", getEntity().getCfis()),
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.ne("id", getEntity().isNew() ? 0L : getEntity().getId())
            }) > 0) {
                getEntity().setCfis(getCfis());
                addException("cityWarning");
                getValidationFailed();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    @Override
    public void editEntity() {
        this.cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            try {
                this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                setCfis(getEntity().getCfis());
                DaoManager.getSession().evict(this.getEntity());
                if (!ValidationHelper.isNullOrEmpty(getEntity().getProvince())) {
                    setSelectedProvinceId(getEntity().getProvince().getId());
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException, InstantiationException, IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
            getEntity().setProvince(DaoManager.get(Province.class, getSelectedProvinceId()));
            DaoManager.save(getEntity());

            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper.getValidation("successfullySaved"), "");
        } else {
            addException("warning");
        }
        setSelectedProvinceId(null);
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        try {
            City entity = DaoManager
                    .get(City.class, getEntityDeleteId());
            entity.setIsDeleted(Boolean.TRUE);
            DaoManager.save(entity, true);
        } catch (Exception e1) {
            LogHelper.log(log, e1);
        }
    }

    @Override
    protected void setEditedValues() {
        setEditedEntity(getEntity());
    }

    @Override
    public void cleanValidation() {
        super.cleanValidation();
    }

    public List<SelectItem> getCities() {
        return cities;
    }

    public void setCities(List<SelectItem> cities) {
        this.cities = cities;
    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    public List<SelectItem> getProvinces() {
        return provinces;
    }

    public void setProvinces(List<SelectItem> provinces) {
        this.provinces = provinces;
    }

    public Long getSelectedProvinceId() {
        return selectedProvinceId;
    }

    public void setSelectedProvinceId(Long selectedProvinceId) {
        this.selectedProvinceId = selectedProvinceId;
    }

    public String getCfis() {
        return cfis;
    }

    public void setCfis(String cfis) {
        this.cfis = cfis;
    }
}
