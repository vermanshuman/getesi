package it.nexera.ris.web.beans.pages;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.TaxRate;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;

@ManagedBean(name = "taxRateListBean")
@ViewScoped
public class TaxRateListBean extends EntityLazyInListEditPageBean<TaxRate>
        implements Serializable  {

    private static final long serialVersionUID = -2398683768894736150L;

    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setDescription(
                this.getEntity().getDescription());
        this.getEditedEntity().setCodeSDI(
                this.getEntity().getCodeSDI());
        this.getEditedEntity().setPercentage(
                this.getEntity().getPercentage());
        this.getEditedEntity().setUse(
                this.getEntity().getUse());
    }

    @Override
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

    @Override
    public void save() throws HibernateException, PersistenceBeanException, NumberFormatException, IOException,
            InstantiationException, IllegalAccessException {
        DaoManager.save(this.getEntity());
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        this.loadList(TaxRate.class, new Criterion[] { },
                new Order[]
                        {Order.asc("description")} );
    }

    @Override
    protected void deleteEntityInternal(Long id)
            throws HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException {
        try {
            super.deleteEntityInternal(id);
        } catch (Exception e) {
            try {
                DaoManager.remove(getEntity());
            } catch (Exception e1) {
                LogHelper.log(log, e1);
            }
        }
    }
}