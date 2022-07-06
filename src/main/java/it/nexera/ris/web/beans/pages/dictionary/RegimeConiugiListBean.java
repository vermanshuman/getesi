package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.Regime;
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

@ManagedBean(name = "regimeConiugiListBean")
@ViewScoped
@Getter
@Setter
public class RegimeConiugiListBean extends
        EntityLazyListPageBean<Regime> implements Serializable {
    private static final long serialVersionUID = 3787302455471012439L;

    private String code;

    private String text;

    private Regime entity;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException,
            InstantiationException, IllegalAccessException, IOException {
        filterTableFromPanel();
        setEntity(new Regime());
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getCode())) {
            restrictions.add(Restrictions.ilike("text", getCode(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getText())) {
            restrictions.add(Restrictions.ilike("text", getText(), MatchMode.ANYWHERE));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));

        this.loadList(Regime.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("code")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setCode(null);
        setText(null);
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
            RequestContext.getCurrentInstance().update("addRegimeDialog");
            executeJS("PF('addRegimeDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new Regime());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addRegimeDialog");
            executeJS("PF('addRegimeDialogWV').show();");
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
        executeJS("PF('addRegimeDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getCode())) {
            addRequiredFieldException("form:code");
        } else if (!ValidationHelper.isUnique(Regime.class, "code",
                getEntity().getCode(), this.getEntity().getId())) {
            addFieldException("form:code", "codeAlreadyInUse");
        } else if (ValidationHelper.isNullOrEmpty(this.getEntity().getText())) {
            addRequiredFieldException("form:text");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new Regime());
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
