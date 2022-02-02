package it.nexera.ris.web.beans.pages.dictionary;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.RequestType;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;

@ManagedBean(name = "courtListBean")
@ViewScoped
public class CourtListBean extends
        EntityLazyInListEditPageBean<Court> implements Serializable {

    private static final long serialVersionUID = 3952135359518006317L;
    private List<SelectItem> cities;

    private Long selectedCityId;

    
    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setName(this.getEntity().getName());
        this.getEditedEntity()
                .setFiscalCode(this.getEntity().getFiscalCode());
        this.getEditedEntity().setCity(this.getEntity().getCity());
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(RequestType.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        if (!ValidationHelper.isNullOrEmpty(getSelectedCityId())) {
            getEntity().setCity(DaoManager.get(City.class, getSelectedCityId()));
        }
        setSelectedCityId(null);
        DaoManager.save(this.getEntity());
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(Court.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))}, new Order[]
                {Order.asc("name")});
        setCities(ComboboxHelper.fillList(City.class, Order.asc("description"), new Criterion[]{
                Restrictions.eq("external", Boolean.TRUE)
        }));
        
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            super.deleteEntityInternal(id);
        } catch (Exception e) {
            try {
                this.getEntity().setIsDeleted(Boolean.TRUE);

                DaoManager.save(getEntity());
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
    }

    public void editEntity() {
        this.cleanValidation();
        if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
            try {
                this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                DaoManager.getSession().evict(this.getEntity());
                if (!ValidationHelper.isNullOrEmpty(getEntity().getCity())) {
                    setSelectedCityId(getEntity().getCity().getId());
                }
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    @Override
    public void resetFields() {
        setSelectedCityId(null);
        super.resetFields();
    }
    public List<SelectItem> getCities() {
        return cities;
    }

    public Long getSelectedCityId() {
        return selectedCityId;
    }

    public void setCities(List<SelectItem> cities) {
        this.cities = cities;
    }

    public void setSelectedCityId(Long selectedCityId) {
        this.selectedCityId = selectedCityId;
    }
}
