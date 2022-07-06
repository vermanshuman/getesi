package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.ReferentType;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Referent;
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

@ManagedBean(name = "referentListBean")
@ViewScoped
@Getter
@Setter
public class ReferentListBean extends EntityLazyListPageBean<Referent>
        implements Serializable {

    private static final long serialVersionUID = -6955566457370982617L;

    private Referent entity;

    private String surName;

    private String name;

    private String phone;

    private String email;

    private List<SelectItem> referentTypes;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException,
            PersistenceBeanException, InstantiationException,
            IllegalAccessException, IOException {
        filterTableFromPanel();
        setEntity(new Referent());
        setReferentTypes(ComboboxHelper.fillList(ReferentType.class, false, false));
    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();

        if (!ValidationHelper.isNullOrEmpty(getName())) {
            restrictions.add(Restrictions.ilike("name", getName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSurName())) {
            restrictions.add(Restrictions.ilike("surname", getSurName(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getPhone())) {
            restrictions.add(Restrictions.ilike("phone", getPhone(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getEmail())) {
            restrictions.add(Restrictions.ilike("email", getEmail(), MatchMode.ANYWHERE));
        }

        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        this.loadList(Referent.class, restrictions.toArray(new Criterion[0]), new Order[]
                {Order.asc("surname")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setName(null);
        setSurName(null);
        setPhone(null);
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
            RequestContext.getCurrentInstance().update("addReferentDialog");
            executeJS("PF('addReferentDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new Referent());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addReferentDialog");
            executeJS("PF('addReferentDialogWV').show();");
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
        executeJS("PF('addReferentDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        if (ValidationHelper.isNullOrEmpty(this.getEntity().getSurname())) {
            addRequiredFieldException("form:surname");
        }

        if (ValidationHelper.isNullOrEmpty(this.getEntity().getName())) {
            addRequiredFieldException("form:name");
        }

        if (!ValidationHelper.isNullOrEmpty(this.getEntity().getEmail())
                && !ValidationHelper
                .checkMailCorrectFormat(this.getEntity().getEmail())) {
            addFieldException("form:email", "emailWrongFormat");
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new Referent());
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
