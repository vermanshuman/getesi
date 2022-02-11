package it.nexera.ris.web.beans.pages.dictionary;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Regime;
import it.nexera.ris.web.beans.EntityLazyInListEditPageBean;

@ManagedBean(name = "regimeConiugiListBean")
@ViewScoped
public class RegimeConiugiListBean extends
        EntityLazyInListEditPageBean<Regime> implements Serializable {
    private static final long serialVersionUID = 3787302455471012439L;

    
    @Override
    protected void setEditedValues() {
        this.getEditedEntity().setCode(this.getEntity().getCode());
        this.getEditedEntity().setText(this.getEntity().getText());
    }

    @Override
    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldException("form:code");
        } else if (!ValidationHelper.isUnique(Regime.class, "code",
                getEntity().getCode(), this.getEntity().getId())) {
            addFieldException("form:code", "codeAlreadyInUse");
        }else if (ValidationHelper.isNullOrEmpty(this.getEntity().getText())) {
            addRequiredFieldException("form:text");
        }
    }

    @Override
    public void save() throws HibernateException, PersistenceBeanException,
            NumberFormatException, IOException, InstantiationException,
            IllegalAccessException {
        DaoManager.save(this.getEntity());
    }

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        this.loadList(Regime.class, new Criterion[]
                {Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                        Restrictions.isNull("isDeleted"))}, new Order[]
                {Order.asc("code")});
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
            } catch (Exception e) {
                LogHelper.log(log, e);
            }
        }
    }

    @Override
    public void resetFields() {
        super.resetFields();
    }
}
