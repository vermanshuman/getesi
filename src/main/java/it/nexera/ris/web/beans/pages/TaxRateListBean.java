package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
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

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;
import org.hibernate.criterion.Restrictions;
import org.primefaces.context.RequestContext;

@ManagedBean(name = "taxRateListBean")
@ViewScoped
@Getter
@Setter
public class TaxRateListBean extends EntityLazyListPageBean<TaxRate>
        implements Serializable  {

    private BigDecimal percentage;

    private String description;

    private String codeSDI;

    private Integer selectedUse;

    private TaxRate entity;

    private static final long serialVersionUID = -2398683768894736150L;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        setEntity(new TaxRate());
        filterTableFromPanel();
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getPercentage()) && !getPercentage().equals(BigDecimal.ZERO)) {
            restrictions.add(Restrictions.eq("percentage", getPercentage()));
        }
        if (!ValidationHelper.isNullOrEmpty(getDescription())) {
            restrictions.add(Restrictions.ilike("description", getDescription(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getCodeSDI())) {
            restrictions.add(Restrictions.ilike("codeSDI", getCodeSDI(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedUse()) && getSelectedUse() > -1) {
            restrictions.add(Restrictions.eq("use", getSelectedUse().equals(1) ? true : false));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        this.loadList(TaxRate.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("description")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setPercentage(null);
        setDescription(null);
        setCodeSDI(null);
        setSelectedUse(null);
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
            RequestContext.getCurrentInstance().update("addTaxRateDialog");
            executeJS("PF('addTaxRateDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            this.cleanValidation();
            setEntity(new TaxRate());
            RequestContext.getCurrentInstance().update("addTaxRateDialog");
            executeJS("PF('addTaxRateDialogWV').show();");
        }
    }

    protected void validate() throws PersistenceBeanException {
        if(ValidationHelper.isNullOrEmpty(getEntity().getPercentage())) {
            addRequiredFieldException("form:percentage");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getDescription())) {
            addRequiredFieldException("form:description");
        }
        if(ValidationHelper.isNullOrEmpty(getEntity().getCodeSDI())) {
            addRequiredFieldException("form:codeSDI");
        }
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getUse())) {
            addRequiredFieldException("form:use");
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
        executeJS("PF('addTaxRateDialogWV').hide()");
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
       setEntity(new TaxRate());
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