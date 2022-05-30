package it.nexera.ris.web.beans.pages.dictionary;

import it.nexera.ris.common.enums.TypeActEnum;
import it.nexera.ris.common.exceptions.PersistenceBeanException;
import it.nexera.ris.common.helpers.ComboboxHelper;
import it.nexera.ris.common.helpers.LogHelper;
import it.nexera.ris.common.helpers.ValidationHelper;
import it.nexera.ris.persistence.PersistenceSessionManager;
import it.nexera.ris.persistence.beans.dao.DaoManager;
import it.nexera.ris.persistence.beans.entities.domain.Iban;
import it.nexera.ris.persistence.beans.entities.domain.dictionary.TypeAct;
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

@ManagedBean(name = "typeActBean")
@ViewScoped
@Getter
@Setter
public class TypeActBean extends EntityLazyListPageBean<TypeAct> implements Serializable {

    private static final long serialVersionUID = -1741091000449749081L;

    private List<SelectItem> types;
    private TypeActEnum selectedType;
    private TypeActEnum selectedFilterType;

    private String typeActCode;
    private String typeActDescription;
    private String typeActTextInVisura;
    private String typeActCodeInVisura;
    private TypeAct entity;

    @Override
    public void onLoad() throws NumberFormatException, HibernateException, PersistenceBeanException, InstantiationException, IllegalAccessException, IOException {
        setTypes(ComboboxHelper.fillList(TypeActEnum.class));
        filterTableFromPanel();
        setEntity(new TypeAct());

    }

    public void filterTableFromPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        List<Criterion> restrictions = new ArrayList<>();
        if (!ValidationHelper.isNullOrEmpty(getTypeActCode())) {
            restrictions.add(Restrictions.ilike("code", getTypeActCode(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getTypeActDescription())) {
            restrictions.add(Restrictions.ilike("description", getTypeActDescription(), MatchMode.ANYWHERE));
        }

        if (!ValidationHelper.isNullOrEmpty(getTypeActTextInVisura())) {
            restrictions.add(Restrictions.ilike("textInVisura", getTypeActTextInVisura(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getTypeActCodeInVisura())) {
            restrictions.add(Restrictions.ilike("codeInVisura", getTypeActCodeInVisura(), MatchMode.ANYWHERE));
        }
        if (!ValidationHelper.isNullOrEmpty(getSelectedFilterType())) {
            restrictions.add(Restrictions.eq("type", getSelectedFilterType()));
        }
        restrictions.add(Restrictions.or(Restrictions.eq("isDeleted", Boolean.FALSE),
                Restrictions.isNull("isDeleted")));
        this.loadList(TypeAct.class, restrictions.toArray(new Criterion[0]),
                new Order[]{Order.asc("code")});
    }

    public void clearFilterPanel() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setTypeActCode(null);
        setTypeActDescription(null);
        setTypeActTextInVisura(null);
        setTypeActCodeInVisura(null);
        setSelectedFilterType(null);
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
                    if (!ValidationHelper.isNullOrEmpty(this.getEntity().getType()))
                        setSelectedType(this.getEntity().getType());
                } catch (Exception e) {
                    LogHelper.log(log, e);
                }
            }
            RequestContext.getCurrentInstance().update("addTypeActDialog");
            executeJS("PF('addTypeActDialogWV').show();");
        }
    }

    @Override
    public void addEntity() throws HibernateException, InstantiationException, IllegalAccessException, PersistenceBeanException {
        if (this.getCanCreate()) {
            setEntity(new TypeAct());
            this.cleanValidation();
            RequestContext.getCurrentInstance().update("addTypeActDialog");
            executeJS("PF('addTypeActDialogWV').show();");
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
        executeJS("PF('addTypeActDialogWV').hide()");
        executeJS("refreshTable()");
    }

    protected void validate() throws PersistenceBeanException {
        try {
            if (DaoManager.getCount(TypeAct.class, "id", new Criterion[]{
                    Restrictions.eq("code", getEntity().getCode()),
                    Restrictions.eq("type", getSelectedType()),
                    Restrictions.ne("id", getEntity().isNew() ? 0L : getEntity().getId())
            }) > 0) {
                addException("typeActInUse");
            }
        } catch (Exception e) {
            LogHelper.log(log, e);
        }
    }

    public void resetFields() throws PersistenceBeanException, IllegalAccessException, InstantiationException {
        setEntity(new TypeAct());
        setSelectedType(null);
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
            getEntity().setType(getSelectedType());
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
