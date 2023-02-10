package it.nexera.ris.web.beans.pages;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Supplier;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(name = "supplierListBean")
@ViewScoped
@Getter
@Setter
public class SupplierListBean extends EntityLazyListPageBean<Supplier>
        implements Serializable  {

    private String name;

    private String address;

    private String email;

    private Supplier entity;

    private static final long serialVersionUID = -5502215777292073481L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        setEntity(new Supplier());
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.eq("name", getName()));
        }
        if (!ValidationHelper.isNullOrEmpty(getAddress())) {
            restrictions.add(Restrictions.ilike("address", getAddress(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getEmail())) {
            restrictions.add(Restrictions.ilike("email", getEmail(), MatchMode.ANYWHERE));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        restrictions.add(Restrictions.eq("getesi", Boolean.TRUE));
        this.loadList(Supplier.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("name")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setAddress(null);
        setEmail(null);
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
            RequestContext.getCurrentInstance().update("addSupplierDialog");
            executeJS("PF('addSupplierDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            this.cleanValidation();
            setEntity(new Supplier());
            RequestContext.getCurrentInstance().update("addSupplierDialog");
            executeJS("PF('addSupplierDialogWV').show();");
        }
    }

    protected void validate() throws PersistenceBeanException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getName())) {
            addRequiredFieldException("form:name");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getAddress())) {
            addRequiredFieldException("form:address");
        }
        if(ValidationHelper.isNullOrEmpty(getEntity().getEmail())) {
            addRequiredFieldException("form:email");
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

        getEntity().setGetesi(Boolean.TRUE);
        getEntity().setBrexa(null);
        saveEntity();
        this.resetFields();
        executeJS("PF('addSupplierDialogWV').hide()");
        executeJS("refreshTable()");
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

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
       setEntity(new Supplier());
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
}