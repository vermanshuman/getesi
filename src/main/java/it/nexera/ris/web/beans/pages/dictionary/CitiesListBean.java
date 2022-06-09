package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.*;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.City;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Province;
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

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "citiesListBean")
@ViewScoped
@Getter
@Setter
public class CitiesListBean extends EntityLazyListPageBean<City> implements Serializable {

    private static final long serialVersionUID = -397464426868428763L;

    private boolean canCreate;

    private List<SelectItem> provinces;

    private Long selectedFilterProvinceId;

    private String name;

    private String cap;

    private String cfis;

    private City entity;

    private Long selectedProvinceId;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        setCanCreate(true);
//        this.loadList(City.class, new Criterion[]{
//                Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
//                        Restrictions.isNull("isDeleted"))
//        }, new Order[]{
//                Order.asc("code")
//        });
        setProvinces(ComboboxHelper.fillList(Province.class));
        setEntity(new City());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.ilike("description", getName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCap())) {
            restrictions.add(Restrictions.ilike("cap", getCap(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCfis())) {
            restrictions.add(Restrictions.ilike("cfis", getCap(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedFilterProvinceId())) {
            restrictions.add(Restrictions.eq("province",
                    DaoManager.get(Province.class, getSelectedFilterProvinceId())));
        }

        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        this.loadList(City.class, restrictions.toArray(new Criterion[0]), new Order[]{
                Order.asc("code")
        });
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setCap(null);
        setCfis(null);
        setSelectedFilterProvinceId(null);
        filterTableFromPanel();
    }

    @Override
    public void editEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanEdit()) {
            this.cleanValidation();
            if (!ValidationHelper.isNullOrEmpty(this.getEntityEditId())) {
                try {
                    try {
                        this.setEntity(DaoManager.get(getType(), this.getEntityEditId()));
                        setCfis(getEntity().getCfis());
                        DaoManager.getSession().evict(this.getEntity());
                        if (!ValidationHelper.isNullOrEmpty(getEntity().getProvince())) {
                            setSelectedProvinceId(getEntity().getProvince().getId());
                        }else
                            setSelectedProvinceId(null);
                    } catch (Exception e) {
                        LogHelper.log(log, e);
                    }
                    DaoManager.getSession().evict(this.getEntity());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addCityDialog");
            executeJS("PF('addCityDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new City());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addCityDialog");
            executeJS("PF('addCityDialogWV').show();");
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
        if (!ValidationHelper.isNullOrEmpty(getSelectedProvinceId())) {
            saveEntity();
            MessageHelper.addGlobalMessage(FacesMessage.SEVERITY_INFO,
                    ResourcesHelper.getValidation("successfullySaved"), "");
            this.resetFields();
            executeJS("PF('addCityDialogWV').hide()");
            executeJS("refreshTable()");
        } else {
            addException("warning");
        }
        setSelectedProvinceId(null);
    }

    protected void validate() throws PersistenceBeanException {
        try {
            if (DaoManager.getCount(City.class, "id", new Criterion[]{
                    Restrictions.eq("cfis", getEntity().getCfis()),
                    Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                            Restrictions.isNull("isDeleted")),
                    Restrictions.ne("id", getEntity().isNew() ? 0L : getEntity().getId())
            }) > 0) {
                addException("cityWarning");
                getValidationFailed();
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new City());
        setSelectedProvinceId(null);
        setCfis(null);
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
            getEntity().setProvince(DaoManager.get(Province.class, getSelectedProvinceId()));
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
