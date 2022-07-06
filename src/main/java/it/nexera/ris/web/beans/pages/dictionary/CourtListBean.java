package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Court;
import it.nexera.ris.web.beans.EntityLazyListPageBean;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "courtListBean")
@ViewScoped
@Getter
@Setter
public class CourtListBean extends
        EntityLazyListPageBean<Court> implements Serializable {

    private static final long serialVersionUID = 3952135359518006317L;

    private String name;

    private String taxCode;

    private Long selectedCityId;

    private List<SelectItem> courtCities;

    private Court entity;

    private List<SelectItem> cities;

    private Long selectedFilterCityId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        setEntity(new Court());
        setCities(ComboboxHelper.fillList(City.class, Order.asc("description"), new Criterion[]{
                Restrictions.eq("external", Boolean.TRUE),
                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))
        }));
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.ilike("name", getName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getTaxCode())) {
            restrictions.add(Restrictions.ilike("fiscalCode", getTaxCode(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedFilterCityId())) {
            restrictions.add(Restrictions.eq("city", DaoManager.get(City.class, getSelectedFilterCityId())));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        List<Court> courts = DaoManager.load(Court.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))});
        List<Long> cityIds = new ArrayList<>();

        for (Court court : courts) {
            if (!ValidationHelper.isNullOrEmpty(court.getCity()) && !cityIds.contains(court.getCity().getId())) {
                cityIds.add(court.getCity().getId());
            }
        }
        if (!ValidationHelper.isNullOrEmpty(cityIds)) {
            setCourtCities(ComboboxHelper.fillList(City.class,
                    Order.asc("description"),
                    new Criterion[]{Restrictions.isNotNull("province.id")
                            , Restrictions.eq("external", Boolean.TRUE), Restrictions.in("id", cityIds)}, Boolean.FALSE));
        }
        this.loadList(Court.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});

    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setTaxCode(null);
        setSelectedFilterCityId(null);
        filterTableFromPanel();
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            this.cleanValidation();
            if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
                try {
                    this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                    DaoManager.getSession().evict(this.getEntity());
                    if(!ValidationHelper.isNullOrEmpty(this.getEntity().getCity()))
                        setSelectedCityId(this.getEntity().getCity().getId());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addCourtDialog");
            executeJS("PF('addCourtDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setSelectedCityId(null);
            setEntity(new Court());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addCourtDialog");
            executeJS("PF('addCourtDialogWV').show();");
        }
    }

    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        this.cleanValidation();
        this.setValidationFailed(false);

        try {
            this.validate();
        } catch (PersistenceBeanException e) {
            LogHelper.log(log, e);
        }
        if (this.getValidationFailed()) {
            return;
        }

        saveEntity();
        this.resetFields();
        executeJS("PF('addCourtDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        } else if (!ValidationHelper.isUnique(Court.class, "name",
                getEntity().getName(), this.getEntity().getId())) {
            addFieldException("form:name", "nameAlreadyInUse");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new Court());
        setSelectedCityId(null);
        this.cleanValidation();
        this.filterTableFromPanel();
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, NumberFormatException, IOException {
        this.setEntity(DaoManager.get(getType(), this.getEntityDeleteId()));
        getEntity().setIsDeleted(Boolean.TRUE);
        saveEntity();
        filterTableFromPanel();
    }

    private void saveEntity() {
        Transaction tr = null;
        try {
            tr = PersistenceSessionManager.getBean().getSession()
                    .beginTransaction();
            if (!ValidationHelper.isNullOrEmpty(getSelectedCityId())) {
                getEntity().setCity(DaoManager.get(City.class, getSelectedCityId()));
            }
            DaoManager.save(this.getEntity());
        } catch (Exception e) {
            if (tr != null) {
                tr.rollback();
            }
            LogHelper.log(log, e);
        } finally {
            if (tr != null && !tr.wasRolledBack()
                    && tr.isActive()) {
                tr.commit();
            }
        }
    }
}
