package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Notary;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "notaryListBean")
@ViewScoped
@Getter
@Setter
public class NotaryListBean extends EntityLazyListPageBean<Notary>
        implements Serializable {

    private static final long serialVersionUID = -5997984513392754412L;

    private Notary entity;

    private String name;

    private String city;

    private String address;

    private String inscriptions;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();
        setEntity(new Notary());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.ilike("name", getName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCity())) {
            restrictions.add(Restrictions.ilike("city", getCity(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getAddress())) {
            restrictions.add(Restrictions.ilike("officeAddress", getAddress(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getInscriptions())) {
            restrictions.add(Restrictions.ilike("inscriptions", getInscriptions(), MatchMode.ANYWHERE));
        }

        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));

        this.loadList(Notary.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setCity(null);
        setAddress(null);
        setInscriptions(null);
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
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addNotaryDialog");
            executeJS("PF('addNotaryDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new Notary());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addNotaryDialog");
            executeJS("PF('addNotaryDialogWV').show();");
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
        executeJS("PF('addNotaryDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCity())) {
            addRequiredFieldException("form:city");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new Notary());
        this.cleanValidation();
        this.filterTableFromPanel();
    }

    @Override
    public void deleteEntity() throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, NumberFormatException, IOException {
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
